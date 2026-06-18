package com.selisihkurang.service;

import com.selisihkurang.model.MatchedPair;
import com.selisihkurang.model.ReconciliationFilter;
import com.selisihkurang.model.ReconciliationResult;
import com.selisihkurang.model.SettlementSummary;
import com.selisihkurang.model.Transaction;
import com.selisihkurang.util.ParseUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ReconciliationService {

    public ReconciliationResult reconcile(List<Transaction> rcAll, List<Transaction> ejAll,
                                          ReconciliationFilter filter) {
        List<Transaction> rc = filter(rcAll, filter);
        List<Transaction> ej = filter(ejAll, filter);

        Map<String, List<Transaction>> rcByRecord = indexByRecord(rc);
        Map<String, List<Transaction>> ejByRecord = indexByRecord(ej);
        Map<String, List<Transaction>> rcByLoose = indexByLoose(rc);
        Map<String, List<Transaction>> ejByLoose = indexByLoose(ej);

        List<MatchedPair> matched = new ArrayList<>();
        List<Transaction> ejSuspect = new ArrayList<>();
        List<Transaction> rcSuspect = new ArrayList<>();
        List<Transaction> reversals = new ArrayList<>();
        List<Transaction> iconsTerbukuEjSuspect = new ArrayList<>();
        List<Transaction> nasabahDiuntungkan = new ArrayList<>();
        List<Transaction> ejAdaRcTidakTerbuku = new ArrayList<>();

        Set<Transaction> usedRc = new HashSet<>();
        Set<Transaction> usedEj = new HashSet<>();

        for (Transaction ejTx : ej) {
            if (ejTx.reversal()) {
                reversals.add(ejTx);
            }
        }
        for (Transaction rcTx : rc) {
            if (rcTx.reversal()) {
                reversals.add(rcTx);
            }
        }

        for (Transaction ejTx : ej) {
            Transaction rcMatch = findMatch(ejTx, rcByRecord, rcByLoose, usedRc);
            if (rcMatch != null) {
                usedRc.add(rcMatch);
                usedEj.add(ejTx);
                if (isExactMatch(rcMatch, ejTx)) {
                    matched.add(new MatchedPair(rcMatch, ejTx));
                } else {
                    rcSuspect.add(rcMatch);
                    ejSuspect.add(ejTx);
                }

                if (ejTx.suspect() || !isExactMatch(rcMatch, ejTx)) {
                    iconsTerbukuEjSuspect.add(rcMatch);
                    if (rcMatch.hasNorek()) {
                        nasabahDiuntungkan.add(rcMatch);
                    }
                }
            } else {
                ejAdaRcTidakTerbuku.add(ejTx);
                if (ejTx.suspect()) {
                    ejSuspect.add(ejTx);
                }
            }
        }

        for (Transaction rcTx : rc) {
            if (!usedRc.contains(rcTx)) {
                rcSuspect.add(rcTx);
                Transaction ejMatch = findMatch(rcTx, ejByRecord, ejByLoose, usedEj);
                if (ejMatch != null && (ejMatch.suspect() || !isExactMatch(rcTx, ejMatch))) {
                    iconsTerbukuEjSuspect.add(rcTx);
                    if (rcTx.hasNorek()) {
                        nasabahDiuntungkan.add(rcTx);
                    }
                }
            }
        }

        SettlementSummary rcSummary = summarize(rc, rcSuspect);
        SettlementSummary ejSummary = summarize(ej, ejSuspect);

        return new ReconciliationResult(
                matched,
                distinct(ejSuspect),
                distinct(rcSuspect),
                distinct(reversals),
                distinct(iconsTerbukuEjSuspect),
                distinct(nasabahDiuntungkan),
                distinct(ejAdaRcTidakTerbuku),
                rcSummary,
                ejSummary,
                rcAll.size(),
                ejAll.size(),
                rc.size(),
                ej.size()
        );
    }

    private List<Transaction> filter(List<Transaction> all, ReconciliationFilter filter) {
        return all.stream().filter(filter::matches).toList();
    }

    private Map<String, List<Transaction>> indexByRecord(List<Transaction> list) {
        Map<String, List<Transaction>> map = new HashMap<>();
        for (Transaction tx : list) {
            if (tx.record() > 0) {
                map.computeIfAbsent(String.valueOf(tx.record()), k -> new ArrayList<>()).add(tx);
            }
        }
        return map;
    }

    private Map<String, List<Transaction>> indexByLoose(List<Transaction> list) {
        Map<String, List<Transaction>> map = new HashMap<>();
        for (Transaction tx : list) {
            map.computeIfAbsent(tx.looseKey(), k -> new ArrayList<>()).add(tx);
        }
        return map;
    }

    private Transaction findMatch(Transaction target,
                                Map<String, List<Transaction>> byRecord,
                                Map<String, List<Transaction>> byLoose,
                                Set<Transaction> used) {
        if (target.record() > 0) {
            List<Transaction> candidates = byRecord.get(String.valueOf(target.record()));
            if (candidates != null) {
                for (Transaction candidate : candidates) {
                    if (!used.contains(candidate) && candidate.source() != target.source()) {
                        return candidate;
                    }
                }
            }
        }

        List<Transaction> looseCandidates = byLoose.get(target.looseKey());
        if (looseCandidates != null) {
            for (Transaction candidate : looseCandidates) {
                if (!used.contains(candidate) && candidate.source() != target.source()) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private boolean isExactMatch(Transaction rc, Transaction ej) {
        if (rc.record() > 0 && ej.record() > 0 && rc.record() != ej.record()) {
            return false;
        }
        if (rc.amount() != ej.amount()) {
            return false;
        }
        if (!ParseUtil.typesCompatible(rc.type(), ej.type())) {
            return false;
        }
        if (!ParseUtil.cardsMatch(rc.card(), ej.card())) {
            return false;
        }
        return true;
    }

    private SettlementSummary summarize(List<Transaction> all, List<Transaction> suspects) {
        int qtyPenarikan = 0;
        long penarikan = 0;
        int qtySetoran = 0;
        long setoran = 0;
        long suspectAmount = 0;

        for (Transaction tx : all) {
            if (isWithdrawal(tx)) {
                qtyPenarikan++;
                penarikan += Math.abs(tx.amount());
            } else if (isDeposit(tx)) {
                qtySetoran++;
                setoran += Math.abs(tx.amount());
            }
        }
        for (Transaction tx : suspects) {
            suspectAmount += Math.abs(tx.amount());
        }

        return new SettlementSummary(
                qtyPenarikan, penarikan,
                qtySetoran, setoran,
                suspects.size(), suspectAmount,
                setoran - penarikan
        );
    }

    private boolean isWithdrawal(Transaction tx) {
        String norm = ParseUtil.normalizeType(tx.type());
        return norm.equals("D");
    }

    private boolean isDeposit(Transaction tx) {
        String norm = ParseUtil.normalizeType(tx.type());
        return norm.equals("K");
    }

    private List<Transaction> distinct(List<Transaction> list) {
        return list.stream().distinct().toList();
    }
}

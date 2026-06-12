package com.selisihkurang.model;

import java.util.List;

public record ReconciliationResult(
        List<MatchedPair> matched,
        List<Transaction> ejSuspect,
        List<Transaction> rcSuspect,
        List<Transaction> reversals,
        List<Transaction> iconsTerbukuEjSuspect,
        List<Transaction> nasabahDiuntungkan,
        List<Transaction> ejAdaRcTidakTerbuku,
        SettlementSummary rcSummary,
        SettlementSummary ejSummary,
        int totalRc,
        int totalEj,
        int filteredRc,
        int filteredEj
) {
}

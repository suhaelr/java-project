package com.selisihkurang.model;

public record SettlementSummary(
        int qtyPenarikan,
        long jumlahPenarikan,
        int qtySetoran,
        long jumlahSetoran,
        int qtySuspect,
        long suspectAmount,
        long selisihPeriode
) {
    public static SettlementSummary empty() {
        return new SettlementSummary(0, 0, 0, 0, 0, 0, 0);
    }
}

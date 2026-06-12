package com.selisihkurang.model;

import java.time.LocalDate;

public record ReconciliationFilter(
        LocalDate tanggalAwal,
        LocalDate tanggalAkhir,
        long recordAwal,
        long recordAkhir
) {
    public boolean matches(Transaction tx) {
        if (tx.tanggal() != null) {
            if (tanggalAwal != null && tx.tanggal().isBefore(tanggalAwal)) {
                return false;
            }
            if (tanggalAkhir != null && tx.tanggal().isAfter(tanggalAkhir)) {
                return false;
            }
        }
        if (recordAwal > 0 && tx.record() < recordAwal) {
            return false;
        }
        if (recordAkhir > 0 && tx.record() > recordAkhir) {
            return false;
        }
        return true;
    }
}

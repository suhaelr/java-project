package com.selisihkurang.model;

import java.time.LocalDate;

/**
 * Kolom Hasil Rekonsiliasi Excel (A–R) sesuai PDF.
 */
public class HasilRekonsiliasiData {
    private String idAtm = "";
    private long nominalPengisian;
    private LocalDate tglAwal;
    private LocalDate tglAkhir;
    private long recordAwal;
    private long recordAkhir;
    private long sisaMenurutAdmin;
    private long sisaFisik;
    private long setoranSka;
    private long penarikanSka;
    private long sisaRestIcons;
    private long pembukuanSr;
    private long trxByRecNum;

    public String idAtm() {
        return idAtm;
    }

    public void setIdAtm(String idAtm) {
        this.idAtm = idAtm;
    }

    public long nominalPengisian() {
        return nominalPengisian;
    }

    public void setNominalPengisian(long nominalPengisian) {
        this.nominalPengisian = nominalPengisian;
    }

    public LocalDate tglAwal() {
        return tglAwal;
    }

    public void setTglAwal(LocalDate tglAwal) {
        this.tglAwal = tglAwal;
    }

    public LocalDate tglAkhir() {
        return tglAkhir;
    }

    public void setTglAkhir(LocalDate tglAkhir) {
        this.tglAkhir = tglAkhir;
    }

    public long recordAwal() {
        return recordAwal;
    }

    public void setRecordAwal(long recordAwal) {
        this.recordAwal = recordAwal;
    }

    public long recordAkhir() {
        return recordAkhir;
    }

    public void setRecordAkhir(long recordAkhir) {
        this.recordAkhir = recordAkhir;
    }

    public long sisaMenurutAdmin() {
        return sisaMenurutAdmin;
    }

    public void setSisaMenurutAdmin(long sisaMenurutAdmin) {
        this.sisaMenurutAdmin = sisaMenurutAdmin;
    }

    public long sisaFisik() {
        return sisaFisik;
    }

    public void setSisaFisik(long sisaFisik) {
        this.sisaFisik = sisaFisik;
    }

    public long setoranSka() {
        return setoranSka;
    }

    public void setSetoranSka(long setoranSka) {
        this.setoranSka = setoranSka;
    }

    public long penarikanSka() {
        return penarikanSka;
    }

    public void setPenarikanSka(long penarikanSka) {
        this.penarikanSka = penarikanSka;
    }

    public long sisaRestIcons() {
        return sisaRestIcons;
    }

    public void setSisaRestIcons(long sisaRestIcons) {
        this.sisaRestIcons = sisaRestIcons;
    }

    public long pembukuanSr() {
        return pembukuanSr;
    }

    public void setPembukuanSr(long pembukuanSr) {
        this.pembukuanSr = pembukuanSr;
    }

    public long trxByRecNum() {
        return trxByRecNum;
    }

    public void setTrxByRecNum(long trxByRecNum) {
        this.trxByRecNum = trxByRecNum;
    }

    /** Kolom I: H - G (fisik - admin) */
    public long selisihAdmin() {
        return sisaFisik - sisaMenurutAdmin;
    }

    /** Kolom N: M - L */
    public long selisihPerPeriode() {
        return pembukuanSr - sisaRestIcons;
    }

    /** Kolom O */
    public String keteranganSelisih() {
        long n = selisihPerPeriode();
        if (n == 0) {
            return "KLOP";
        }
        if (n < 0) {
            return "SELISIH KURANG";
        }
        return "SELISIH LEBIH";
    }

    /** Kolom Q1: M - P */
    public long selisihRecAkumulasi() {
        return pembukuanSr - trxByRecNum;
    }

    /** Kolom Q2: L - P */
    public long selisihRecNet() {
        return sisaRestIcons - trxByRecNum;
    }

    /** Kolom R */
    public String keteranganRecNum(boolean suspectPending) {
        return suspectPending ? "SUSPECT PENDING" : "TIDAK SUSPECT PENDING";
    }
}

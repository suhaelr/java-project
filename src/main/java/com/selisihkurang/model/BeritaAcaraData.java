package com.selisihkurang.model;

import java.time.LocalDate;

public class BeritaAcaraData {
    private String crmId = "";
    private String petugasSettlement = "";
    private String penyeliaPenunjang = "";
    private String pengelola = "";
    private String noRekeningKas = "";
    private long fisikRestocking;
    private long fisikCollecting;
    private long saldoPembukuan;
    private long saldoAdmin;
    private long saldoBillcount;
    private LocalDate periodeAwal;
    private LocalDate periodeAkhir;
    private long recordAwal;
    private long recordAkhir;
    private long nominalPengisian;
    private long sisaMenurutAdmin;
    private long sisaFisik;
    private long sisaRestIcons;
    private long pembukuanSr;
    private long trxByRecNum;

    public String crmId() {
        return crmId;
    }

    public void setCrmId(String crmId) {
        this.crmId = crmId;
    }

    public String petugasSettlement() {
        return petugasSettlement;
    }

    public void setPetugasSettlement(String petugasSettlement) {
        this.petugasSettlement = petugasSettlement;
    }

    public String penyeliaPenunjang() {
        return penyeliaPenunjang;
    }

    public void setPenyeliaPenunjang(String penyeliaPenunjang) {
        this.penyeliaPenunjang = penyeliaPenunjang;
    }

    public String pengelola() {
        return pengelola;
    }

    public void setPengelola(String pengelola) {
        this.pengelola = pengelola;
    }

    public String noRekeningKas() {
        return noRekeningKas;
    }

    public void setNoRekeningKas(String noRekeningKas) {
        this.noRekeningKas = noRekeningKas;
    }

    public long fisikRestocking() {
        return fisikRestocking;
    }

    public void setFisikRestocking(long fisikRestocking) {
        this.fisikRestocking = fisikRestocking;
    }

    public long fisikCollecting() {
        return fisikCollecting;
    }

    public void setFisikCollecting(long fisikCollecting) {
        this.fisikCollecting = fisikCollecting;
    }

    public long saldoPembukuan() {
        return saldoPembukuan;
    }

    public void setSaldoPembukuan(long saldoPembukuan) {
        this.saldoPembukuan = saldoPembukuan;
    }

    public long saldoAdmin() {
        return saldoAdmin;
    }

    public void setSaldoAdmin(long saldoAdmin) {
        this.saldoAdmin = saldoAdmin;
    }

    public long saldoBillcount() {
        return saldoBillcount;
    }

    public void setSaldoBillcount(long saldoBillcount) {
        this.saldoBillcount = saldoBillcount;
    }

    public LocalDate periodeAwal() {
        return periodeAwal;
    }

    public void setPeriodeAwal(LocalDate periodeAwal) {
        this.periodeAwal = periodeAwal;
    }

    public LocalDate periodeAkhir() {
        return periodeAkhir;
    }

    public void setPeriodeAkhir(LocalDate periodeAkhir) {
        this.periodeAkhir = periodeAkhir;
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

    /** m. Selisih Lebih (kurang) Akumulatif = Admin - Billcount (negatif = kurang) */
    public long selisihAkumulatif() {
        long fisik = sisaFisik > 0 ? sisaFisik : saldoBillcount;
        long admin = sisaMenurutAdmin > 0 ? sisaMenurutAdmin : saldoAdmin;
        return admin - fisik;
    }

    public long nominalPengisian() {
        return nominalPengisian;
    }

    public void setNominalPengisian(long nominalPengisian) {
        this.nominalPengisian = nominalPengisian;
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
}

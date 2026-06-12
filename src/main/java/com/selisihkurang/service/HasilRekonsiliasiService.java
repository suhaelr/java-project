package com.selisihkurang.service;

import com.selisihkurang.model.BeritaAcaraData;
import com.selisihkurang.model.HasilRekonsiliasiData;
import com.selisihkurang.model.ReconciliationResult;

public final class HasilRekonsiliasiService {

    public HasilRekonsiliasiData build(BeritaAcaraData berita, ReconciliationResult result) {
        HasilRekonsiliasiData data = new HasilRekonsiliasiData();
        data.setIdAtm(berita.crmId());
        data.setNominalPengisian(berita.fisikRestocking() > 0
                ? berita.fisikRestocking() : berita.nominalPengisian());
        data.setTglAwal(berita.periodeAwal());
        data.setTglAkhir(berita.periodeAkhir());
        data.setRecordAwal(berita.recordAwal());
        data.setRecordAkhir(berita.recordAkhir());
        data.setSisaMenurutAdmin(berita.saldoAdmin() > 0
                ? berita.saldoAdmin() : berita.sisaMenurutAdmin());
        data.setSisaFisik(berita.saldoBillcount() > 0
                ? berita.saldoBillcount() : berita.sisaFisik());
        data.setSetoranSka(result.rcSummary().jumlahSetoran());
        data.setPenarikanSka(result.rcSummary().jumlahPenarikan());
        data.setSisaRestIcons(berita.sisaRestIcons() > 0
                ? berita.sisaRestIcons() : result.rcSummary().jumlahSetoran());
        data.setPembukuanSr(berita.pembukuanSr());
        data.setTrxByRecNum(berita.trxByRecNum());
        return data;
    }

    public boolean isSuspectPending(ReconciliationResult result) {
        return !result.iconsTerbukuEjSuspect().isEmpty()
                || !result.ejSuspect().isEmpty()
                || !result.ejAdaRcTidakTerbuku().isEmpty();
    }
}

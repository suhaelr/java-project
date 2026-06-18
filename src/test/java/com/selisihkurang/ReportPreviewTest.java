package com.selisihkurang;

import com.selisihkurang.model.BeritaAcaraData;
import com.selisihkurang.model.ReconciliationFilter;
import com.selisihkurang.model.ReconciliationResult;
import com.selisihkurang.model.Source;
import com.selisihkurang.model.Transaction;
import com.selisihkurang.service.ReconciliationService;
import com.selisihkurang.service.ReportGenerator;
import com.selisihkurang.service.TxtFileParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public final class ReportPreviewTest {
    public static void main(String[] args) throws Exception {
        TxtFileParser parser = new TxtFileParser();
        ReconciliationService service = new ReconciliationService();
        ReportGenerator report = new ReportGenerator();

        List<Transaction> rc = parser.parse(Path.of("samples/sample-rc.txt"), Source.RC);
        List<Transaction> ej = parser.parse(Path.of("samples/sample-ej.txt"), Source.EJ);
        ReconciliationResult result = service.reconcile(rc, ej, new ReconciliationFilter(
                LocalDate.of(2026, 6, 9), LocalDate.of(2026, 12, 6), 4982, 5821));

        BeritaAcaraData data = new BeritaAcaraData();
        data.setCrmId("S1JBMSR012");
        data.setNoRekeningKas("0015360100008013");
        data.setPetugasSettlement("Diah");
        data.setPenyeliaPenunjang("Tof");
        data.setPengelola("Eri");
        data.setPeriodeAwal(LocalDate.of(2026, 5, 3));
        data.setPeriodeAkhir(LocalDate.of(2026, 5, 10));
        data.setRecordAwal(6479);
        data.setRecordAkhir(7444);
        data.setFisikRestocking(400_000_000L);
        data.setFisikCollecting(193_300_000L);
        data.setSaldoPembukuan(193_900_000L);
        data.setSaldoBillcount(193_900_000L);
        data.setSaldoAdmin(193_300_000L);
        data.setPembukuanSr(193_900_000L);
        data.setTrxByRecNum(193_900_000L);

        String html = report.generateHtml(data, result);
        Path out = Path.of("build/report-preview.html");
        Files.createDirectories(out.getParent());
        Files.writeString(out, html);
        System.out.println("Preview: " + out.toAbsolutePath());
    }
}

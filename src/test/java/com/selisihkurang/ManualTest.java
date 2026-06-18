package com.selisihkurang;

import com.selisihkurang.model.ReconciliationFilter;
import com.selisihkurang.model.ReconciliationResult;
import com.selisihkurang.model.Source;
import com.selisihkurang.model.Transaction;
import com.selisihkurang.service.ReconciliationService;
import com.selisihkurang.service.TxtFileParser;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public final class ManualTest {
    public static void main(String[] args) throws Exception {
        TxtFileParser parser = new TxtFileParser();
        ReconciliationService service = new ReconciliationService();

        List<Transaction> rc = parser.parse(Path.of("samples/sample-rc.txt"), Source.RC);
        List<Transaction> ej = parser.parse(Path.of("samples/sample-ej.txt"), Source.EJ);

        ReconciliationResult result = service.reconcile(rc, ej, new ReconciliationFilter(
                LocalDate.of(2026, 6, 9),
                LocalDate.of(2026, 12, 6),
                4982,
                5821
        ));

        System.out.println("RC parsed: " + rc.size());
        System.out.println("EJ parsed: " + ej.size());
        System.out.println("Match: " + result.matched().size());
        System.out.println("ACQ: " + result.ejAdaRcTidakTerbuku().size());
        System.out.println("Nasabah Diuntungkan: " + result.nasabahDiuntungkan().size());
        for (Transaction tx : result.nasabahDiuntungkan()) {
            System.out.println("  - record=" + tx.record() + " norek=" + tx.norek());
        }
        for (Transaction tx : result.ejAdaRcTidakTerbuku()) {
            System.out.println("  ACQ record=" + tx.record() + " amount=" + tx.amount());
        }
    }
}

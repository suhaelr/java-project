package com.selisihkurang.service;

import com.selisihkurang.model.ReconciliationFilter;
import com.selisihkurang.model.ReconciliationResult;
import com.selisihkurang.model.Source;
import com.selisihkurang.model.Transaction;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReconciliationServiceTest {
    private final TxtFileParser parser = new TxtFileParser();
    private final ReconciliationService service = new ReconciliationService();

    @Test
    void detectsMatchedNasabahDiuntungkanAndAcq() throws Exception {
        Path rcPath = Path.of("samples/sample-rc.txt");
        Path ejPath = Path.of("samples/sample-ej.txt");

        List<Transaction> rc = parser.parse(rcPath, Source.RC);
        List<Transaction> ej = parser.parse(ejPath, Source.EJ);

        assertEquals(32, rc.size());
        assertEquals(1, ej.size());

        ReconciliationFilter filter = new ReconciliationFilter(
                LocalDate.of(2026, 6, 9),
                LocalDate.of(2026, 12, 6),
                4982,
                5821
        );

        ReconciliationResult result = service.reconcile(rc, ej, filter);

        assertEquals(0, result.matched().size());
        assertEquals(1, result.ejAdaRcTidakTerbuku().size());
        assertEquals(4982, result.ejAdaRcTidakTerbuku().get(0).record());
    }
}

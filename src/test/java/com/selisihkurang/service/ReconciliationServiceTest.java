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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReconciliationServiceTest {
    private final TxtFileParser parser = new TxtFileParser();
    private final ReconciliationService service = new ReconciliationService();

    @Test
    void detectsMatchedNasabahDiuntungkanAndAcq() throws Exception {
        Path rcPath = Path.of("samples/sample-rc.txt");
        Path ejPath = Path.of("samples/sample-ej.txt");

        List<Transaction> rc = parser.parse(rcPath, Source.RC);
        List<Transaction> ej = parser.parse(ejPath, Source.EJ);

        ReconciliationFilter filter = new ReconciliationFilter(
                LocalDate.of(2026, 5, 3),
                LocalDate.of(2026, 5, 10),
                6480,
                6510
        );

        ReconciliationResult result = service.reconcile(rc, ej, filter);

        assertEquals(4, result.matched().size());
        assertEquals(2, result.ejAdaRcTidakTerbuku().size());
        assertFalse(result.nasabahDiuntungkan().isEmpty());
        assertTrue(result.nasabahDiuntungkan().stream().anyMatch(Transaction::hasNorek));
    }
}

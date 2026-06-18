package com.selisihkurang.service;

import com.selisihkurang.model.Source;
import com.selisihkurang.model.Transaction;
import com.selisihkurang.util.ParseUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser file EJ format journal: EJ.TXT
 * Setiap transaksi diawali TRANSACTION START dan berisi TRAN SEQ NR [xxxx].
 */
public final class EjFileParser {
    private static final Pattern TIMESTAMP = Pattern.compile(
            "^(\\d{2}/\\d{2}/\\d{4})\\s+(\\d{2}:\\d{2}:\\d{2})\\s+(.*)$");
    private static final Pattern CARD_NUMBER = Pattern.compile(
            "CARD\\s+NUMBER\\s+(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TRAN_SEQ = Pattern.compile(
            "TRAN\\s+SEQ\\s+NR\\s*\\[(\\d+)\\]", Pattern.CASE_INSENSITIVE);
    private static final Pattern AMOUNT_LINE = Pattern.compile(
            "Amount\\s*:\\s*([0-9,]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TOTAL_AMOUNT = Pattern.compile(
            "TOTAL\\s+AMOUNT\\s+IDR\\s+([0-9,]+)", Pattern.CASE_INSENSITIVE);
    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final Charset[] CHARSETS = {
            StandardCharsets.UTF_8,
            Charset.forName("windows-1252"),
            Charset.forName("ISO-8859-1")
    };

    public List<Transaction> parse(Path file) throws IOException {
        IOException lastError = null;
        for (Charset charset : CHARSETS) {
            try {
                return parseWithCharset(file, charset);
            } catch (IOException e) {
                lastError = e;
            }
        }
        throw lastError != null ? lastError : new IOException("Gagal membaca EJ: " + file);
    }

    private List<Transaction> parseWithCharset(Path file, Charset charset) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return parseLines(lines);
    }

    List<Transaction> parseLines(List<String> lines) {
        List<Transaction> transactions = new ArrayList<>();
        EjBlock block = null;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }

            Matcher ts = TIMESTAMP.matcher(line);
            String message = line;
            if (ts.matches()) {
                LocalDate date = ParseUtil.parseDate(ts.group(1));
                if (block != null && block.tanggal == null) {
                    block.tanggal = date;
                }
                message = ts.group(3).trim();
            }

            if (message.equalsIgnoreCase("TRANSACTION START")) {
                if (block != null) {
                    Transaction tx = block.build();
                    if (tx != null) {
                        transactions.add(tx);
                    }
                }
                block = new EjBlock();
                if (ts.matches()) {
                    block.tanggal = ParseUtil.parseDate(ts.group(1));
                    block.rawLines.append(rawLine).append('\n');
                }
                continue;
            }

            if (block == null) {
                continue;
            }

            block.rawLines.append(rawLine).append('\n');
            block.fullText.append(message).append('\n');

            Matcher cardMatcher = CARD_NUMBER.matcher(message);
            if (cardMatcher.find()) {
                block.card = cardMatcher.group(1).trim();
            }

            Matcher seqMatcher = TRAN_SEQ.matcher(message);
            if (seqMatcher.find()) {
                block.record = ParseUtil.parseRecord(seqMatcher.group(1));
            }

            Matcher amountMatcher = AMOUNT_LINE.matcher(message);
            if (amountMatcher.find()) {
                block.amount = ParseUtil.parseAmount(amountMatcher.group(1));
            }

            Matcher totalMatcher = TOTAL_AMOUNT.matcher(message);
            if (totalMatcher.find()) {
                block.amount = ParseUtil.parseAmount(totalMatcher.group(1));
            }

            String upper = message.toUpperCase(Locale.ROOT);
            if (upper.contains("CASH DEPOSIT") || upper.contains("DEPOSIT BUTTON")
                    || upper.contains("DEPOSIT CASH")) {
                block.type = "K";
            } else if (upper.contains("WITHDRAWAL") || upper.contains("DISPENSE")
                    || upper.contains("CASH PRESENTED") || upper.contains("CASH TAKEN")) {
                block.type = "D";
            }

            if (upper.contains("SUSPECT") || upper.contains("FAIL")
                    || upper.contains("TIMEOUT") || upper.contains("HOST DECLINE")) {
                block.suspect = true;
            }
        }

        if (block != null) {
            Transaction tx = block.build();
            if (tx != null) {
                transactions.add(tx);
            }
        }

        return transactions;
    }

    static boolean looksLikeEjFormat(List<String> firstLines) {
        int hits = 0;
        for (int i = 0; i < Math.min(30, firstLines.size()); i++) {
            String upper = firstLines.get(i).toUpperCase(Locale.ROOT);
            if (upper.contains("TRANSACTION START")) {
                hits += 2;
            }
            if (upper.contains("TRAN SEQ NR")) {
                hits += 2;
            }
            if (upper.contains("CARD NUMBER")) {
                hits++;
            }
            if (TIMESTAMP.matcher(firstLines.get(i).trim()).matches()) {
                hits++;
            }
        }
        return hits >= 3;
    }

    private static final class EjBlock {
        private LocalDate tanggal;
        private String card = "";
        private long record;
        private long amount;
        private String type = "";
        private boolean suspect;
        private final StringBuilder rawLines = new StringBuilder();
        private final StringBuilder fullText = new StringBuilder();

        Transaction build() {
            if (record == 0 && amount == 0) {
                return null;
            }
            if (type.isEmpty()) {
                type = inferType(fullText.toString());
            }
            return new Transaction(Source.EJ, tanggal, card, "", record, type, amount,
                    suspect, fullText.toString().toUpperCase(Locale.ROOT).contains("REVERSAL"),
                    rawLines.toString().trim());
        }

        private String inferType(String text) {
            String upper = text.toUpperCase(Locale.ROOT);
            if (upper.contains("DEPOSIT")) {
                return "K";
            }
            if (upper.contains("WITHDRAW") || upper.contains("DISPENSE")) {
                return "D";
            }
            return "D";
        }
    }
}

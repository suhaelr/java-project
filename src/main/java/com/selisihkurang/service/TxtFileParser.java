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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class TxtFileParser {
    private static final Charset[] CHARSETS = {
            StandardCharsets.UTF_8,
            Charset.forName("windows-1252"),
            Charset.forName("ISO-8859-1")
    };

    public List<Transaction> parse(Path file, Source source) throws IOException {
        IOException lastError = null;
        for (Charset charset : CHARSETS) {
            try {
                return parseWithCharset(file, source, charset);
            } catch (IOException e) {
                lastError = e;
            }
        }
        throw lastError != null ? lastError : new IOException("Gagal membaca file: " + file);
    }

    private List<Transaction> parseWithCharset(Path file, Source source, Charset charset) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
            String line;
            String[] headers = null;
            char delimiter = '\t';
            int lineNo = 0;

            while ((line = reader.readLine()) != null) {
                lineNo++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) {
                    continue;
                }

                if (headers == null) {
                    delimiter = detectDelimiter(trimmed);
                    headers = split(trimmed, delimiter);
                    if (!looksLikeHeader(headers)) {
                        Transaction tx = parseLine(trimmed, delimiter, null, source);
                        if (tx != null) {
                            transactions.add(tx);
                        }
                        headers = defaultHeaders();
                    }
                    continue;
                }

                Transaction tx = parseLine(trimmed, delimiter, headers, source);
                if (tx != null) {
                    transactions.add(tx);
                }
            }
        }
        return transactions;
    }

    private boolean looksLikeHeader(String[] columns) {
        int known = 0;
        for (String column : columns) {
            String key = ParseUtil.normalizeHeader(column);
            if (key.contains("tanggal") || key.contains("date")
                    || key.contains("record") || key.contains("card")
                    || key.contains("norek") || key.contains("amount")
                    || key.contains("nominal") || key.contains("jumlah")) {
                known++;
            }
        }
        return known >= 2;
    }

    private String[] defaultHeaders() {
        return new String[]{"Tanggal", "Card", "Norek", "Record", "Type", "Amount"};
    }

    private char detectDelimiter(String line) {
        int tabs = count(line, '\t');
        int pipes = count(line, '|');
        int semicolons = count(line, ';');
        if (tabs >= pipes && tabs >= semicolons && tabs > 0) {
            return '\t';
        }
        if (pipes >= semicolons && pipes > 0) {
            return '|';
        }
        if (semicolons > 0) {
            return ';';
        }
        return ',';
    }

    private int count(String line, char ch) {
        int total = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == ch) {
                total++;
            }
        }
        return total;
    }

    private String[] split(String line, char delimiter) {
        return line.split(java.util.regex.Pattern.quote(String.valueOf(delimiter)), -1);
    }

    private Transaction parseLine(String line, char delimiter, String[] headers, Source source) {
        String[] values = split(line, delimiter);
        if (values.length < 3) {
            return null;
        }

        Map<String, String> map = new HashMap<>();
        if (headers != null) {
            for (int i = 0; i < headers.length && i < values.length; i++) {
                map.put(ParseUtil.normalizeHeader(headers[i]), values[i].trim());
            }
        } else {
            map.put("tanggal", values[0].trim());
            map.put("card", values.length > 1 ? values[1].trim() : "");
            map.put("norek", values.length > 2 ? values[2].trim() : "");
            map.put("record", values.length > 3 ? values[3].trim() : "0");
            map.put("type", values.length > 4 ? values[4].trim() : "");
            map.put("amount", values.length > 5 ? values[5].trim() : "0");
        }

        LocalDate tanggal = ParseUtil.parseDate(firstNonBlank(map,
                "tanggal", "date", "tgl", "trxdate", "transactiondate"));
        String card = firstNonBlank(map, "card", "nokartu", "kartu", "cardno", "pan");
        String norek = firstNonBlank(map, "norek", "norekening", "rekening", "account", "accountno");
        long record = ParseUtil.parseRecord(firstNonBlank(map,
                "record", "recnum", "norecord", "recordno", "seq", "sequenceno"));
        String type = firstNonBlank(map, "type", "jenis", "rectype", "trxtype", "kode");
        long amount = ParseUtil.parseAmount(firstNonBlank(map,
                "amount", "nominal", "jumlah", "nilai", "amt"));
        String suspectRaw = firstNonBlank(map, "suspect", "status", "keterangan", "flag");
        boolean suspect = isSuspect(suspectRaw);
        boolean reversal = isReversal(type, suspectRaw, amount);

        if (record == 0 && tanggal == null && amount == 0) {
            return null;
        }

        return new Transaction(source, tanggal, card, norek, record, type, amount,
                suspect, reversal, line);
    }

    private String firstNonBlank(Map<String, String> map, String... keys) {
        for (String key : keys) {
            String value = map.get(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private boolean isSuspect(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String upper = value.toUpperCase(Locale.ROOT);
        return upper.contains("SUSPECT")
                || upper.contains("PENDING")
                || upper.equals("S")
                || upper.contains("CURIGA");
    }

    private boolean isReversal(String type, String status, long amount) {
        String combined = (type + " " + status).toUpperCase(Locale.ROOT);
        return combined.contains("REV")
                || combined.contains("REVERSAL")
                || combined.contains("PEMBALIKAN")
                || type.equalsIgnoreCase("R");
    }
}

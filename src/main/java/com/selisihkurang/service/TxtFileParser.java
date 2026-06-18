package com.selisihkurang.service;

import com.selisihkurang.model.Source;
import com.selisihkurang.model.Transaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Router parser: deteksi format RC (semicolon RK) atau EJ (journal) lalu delegasi.
 * Fallback ke parser tabular generik untuk format lama / sample sederhana.
 */
public final class TxtFileParser {
    private final RcFileParser rcParser = new RcFileParser();
    private final EjFileParser ejParser = new EjFileParser();
    private final LegacyTabularParser legacyParser = new LegacyTabularParser();

    public List<Transaction> parse(Path file, Source source) throws IOException {
        List<String> preview = readPreview(file, 40);
        if (preview.isEmpty()) {
            return List.of();
        }

        if (source == Source.RC && preview.stream().anyMatch(RcFileParser::looksLikeRcFormat)) {
            return rcParser.parse(file);
        }
        if (source == Source.EJ && EjFileParser.looksLikeEjFormat(preview)) {
            return ejParser.parse(file);
        }

        return legacyParser.parse(file, source);
    }

    private List<String> readPreview(Path file, int maxLines) throws IOException {
        Charset[] charsets = {
                StandardCharsets.UTF_8,
                Charset.forName("windows-1252"),
                Charset.forName("ISO-8859-1")
        };
        IOException last = null;
        for (Charset charset : charsets) {
            try {
                List<String> lines = new ArrayList<>();
                try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
                    String line;
                    while ((line = reader.readLine()) != null && lines.size() < maxLines) {
                        lines.add(line);
                    }
                }
                return lines;
            } catch (IOException e) {
                last = e;
            }
        }
        if (last != null) {
            throw last;
        }
        return List.of();
    }

    /** Parser tabular generik (tab/pipe header) untuk backward compatibility. */
    static final class LegacyTabularParser {
        private static final Charset[] CHARSETS = {
                StandardCharsets.UTF_8,
                Charset.forName("windows-1252"),
                Charset.forName("ISO-8859-1")
        };

        List<Transaction> parse(Path file, Source source) throws IOException {
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
                boolean headerMode = true;

                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) {
                        continue;
                    }

                    if (headerMode) {
                        delimiter = detectDelimiter(trimmed);
                        String[] cols = split(trimmed, delimiter);
                        if (looksLikeHeader(cols)) {
                            headers = cols;
                            headerMode = false;
                            continue;
                        }
                        headerMode = false;
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
                String key = com.selisihkurang.util.ParseUtil.normalizeHeader(column);
                if (key.contains("tanggal") || key.contains("date") || key.contains("record")
                        || key.contains("card") || key.contains("norek") || key.contains("amount")) {
                    known++;
                }
            }
            return known >= 2;
        }

        private char detectDelimiter(String line) {
            if (count(line, '\t') > 0) {
                return '\t';
            }
            if (count(line, '|') > 0) {
                return '|';
            }
            if (count(line, ';') > 0) {
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
                    map.put(com.selisihkurang.util.ParseUtil.normalizeHeader(headers[i]), values[i].trim());
                }
            } else {
                map.put("tanggal", values[0].trim());
                map.put("card", values.length > 1 ? values[1].trim() : "");
                map.put("norek", values.length > 2 ? values[2].trim() : "");
                map.put("record", values.length > 3 ? values[3].trim() : "0");
                map.put("type", values.length > 4 ? values[4].trim() : "");
                map.put("amount", values.length > 5 ? values[5].trim() : "0");
            }

            var tanggal = com.selisihkurang.util.ParseUtil.parseDate(first(map, "tanggal", "date", "tgl"));
            String card = first(map, "card", "nokartu", "kartu", "cardno", "pan");
            String norek = first(map, "norek", "norekening", "rekening", "account");
            long record = com.selisihkurang.util.ParseUtil.parseRecord(first(map, "record", "recnum", "norecord", "seq"));
            String type = first(map, "type", "jenis", "rectype", "trxtype", "kode");
            long amount = com.selisihkurang.util.ParseUtil.parseAmount(first(map, "amount", "nominal", "jumlah", "nilai", "amt"));
            String suspectRaw = first(map, "suspect", "status", "keterangan", "flag");
            boolean suspect = suspectRaw.toUpperCase().contains("SUSPECT");
            boolean reversal = type.toUpperCase().contains("REV");

            if (record == 0 && tanggal == null && amount == 0) {
                return null;
            }

            return new Transaction(source, tanggal, card, norek, record, type, amount,
                    suspect, reversal, line);
        }

        private String first(Map<String, String> map, String... keys) {
            for (String key : keys) {
                String value = map.get(key);
                if (value != null && !value.isBlank()) {
                    return value;
                }
            }
            return "";
        }
    }
}

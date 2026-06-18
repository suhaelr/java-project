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
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser file RC format: RK_*.txt
 * Contoh baris:
 * 5264222571513795/BNI;S1BBBIR018/5780/188700;0210/S1BBBIR018/5780/ 5264222571513795/1889058327/VL;4000000;D;751800000;06/12/26
 */
public final class RcFileParser {
    private static final Pattern DATE_IN_FIELD = Pattern.compile("\\d{2}/\\d{2}/\\d{2,4}");
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
        throw lastError != null ? lastError : new IOException("Gagal membaca RC: " + file);
    }

    private List<Transaction> parseWithCharset(Path file, Charset charset) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
            String line;
            while ((line = reader.readLine()) != null) {
                Transaction tx = parseLine(line.trim());
                if (tx != null) {
                    transactions.add(tx);
                }
            }
        }
        return transactions;
    }

    Transaction parseLine(String line) {
        if (line.isEmpty() || !line.contains(";")) {
            return null;
        }

        String[] parts = line.split(";", -1);
        if (parts.length < 7) {
            return null;
        }

        String cardBank = parts[0].trim();
        String card = extractBeforeSlash(cardBank);
        String bank = extractAfterSlash(cardBank);

        String[] termParts = parts[1].trim().split("/");
        long record = termParts.length > 1 ? ParseUtil.parseRecord(termParts[1]) : 0;

        String details = parts[2].trim();
        if (isRestockingLine(bank, details)) {
            return null;
        }

        String norek = extractNorek(details, card);
        long amount = ParseUtil.parseAmount(parts[3].trim());
        String type = parts[4].trim().toUpperCase(Locale.ROOT);
        LocalDate tanggal = ParseUtil.parseDate(parts[6].trim());

        if (amount == 0 && record == 0) {
            return null;
        }

        return new Transaction(Source.RC, tanggal, card, norek, record, type, amount,
                false, isReversal(type, details), line);
    }

    private boolean isRestockingLine(String bank, String details) {
        String upperBank = bank.toUpperCase(Locale.ROOT);
        String upperDetails = details.toUpperCase(Locale.ROOT);
        return upperBank.equals("OTHR")
                || upperDetails.contains("/REST/")
                || upperDetails.endsWith("/REST");
    }

    private String extractNorek(String details, String card) {
        String cardDigits = ParseUtil.digitsOnly(card);
        String best = "";
        for (String part : details.split("/")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty() || trimmed.equals("0210")) {
                continue;
            }
            if (trimmed.startsWith("S1") || trimmed.matches("(?i)VL|VK|REST.*")) {
                continue;
            }
            String digits = ParseUtil.digitsOnly(trimmed);
            if (digits.length() == 10 && !digits.equals(cardDigits)) {
                return digits;
            }
            if (digits.length() > 10 && digits.length() <= 19 && !digits.equals(cardDigits)) {
                best = digits;
            }
        }
        return best;
    }

    private String extractBeforeSlash(String value) {
        int idx = value.indexOf('/');
        return idx >= 0 ? value.substring(0, idx).trim() : value.trim();
    }

    private String extractAfterSlash(String value) {
        int idx = value.indexOf('/');
        return idx >= 0 ? value.substring(idx + 1).trim() : "";
    }

    private boolean isReversal(String type, String details) {
        return type.equals("R")
                || details.toUpperCase(Locale.ROOT).contains("REV");
    }

    static boolean looksLikeRcFormat(String line) {
        if (!line.contains(";")) {
            return false;
        }
        String[] parts = line.split(";", -1);
        if (parts.length < 7) {
            return false;
        }
        return parts[0].contains("/")
                && parts[1].contains("/")
                && DATE_IN_FIELD.matcher(parts[parts.length - 1]).find()
                && parts[4].trim().matches("(?i)[DKR]");
    }
}

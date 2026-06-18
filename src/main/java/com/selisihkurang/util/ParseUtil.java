package com.selisihkurang.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public final class ParseUtil {
    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yy"),
            DateTimeFormatter.ofPattern("dd-MM-yy")
    };

    private ParseUtil() {
    }

    public static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        for (DateTimeFormatter formatter : DATE_FORMATS) {
            try {
                return LocalDate.parse(trimmed, formatter);
            } catch (DateTimeParseException ignored) {
                // try next format
            }
        }
        return null;
    }

    public static long parseAmount(String value) {
        if (value == null || value.isBlank()) {
            return 0L;
        }
        String normalized = value.trim()
                .replace("Rp", "")
                .replace("RP", "")
                .replaceAll("\\s+", "")
                .replace(".", "")
                .replace(",", ".");
        if (normalized.startsWith("(") && normalized.endsWith(")")) {
            normalized = "-" + normalized.substring(1, normalized.length() - 1);
        }
        try {
            if (normalized.contains(".")) {
                return Math.round(Double.parseDouble(normalized));
            }
            return Long.parseLong(normalized);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public static long parseRecord(String value) {
        if (value == null || value.isBlank()) {
            return 0L;
        }
        String digits = value.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public static String normalizeHeader(String header) {
        return header == null ? "" : header.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
    }

    public static String formatAmount(long amount) {
        return formatAmountAccounting(amount);
    }

    /** Format angka laporan: 193.300.000 atau (600.000) untuk negatif */
    public static String formatAmountAccounting(long amount) {
        if (amount < 0) {
            return "(" + String.format(Locale.GERMAN, "%,d", Math.abs(amount)).replace(',', '.') + ")";
        }
        return String.format(Locale.GERMAN, "%,d", amount).replace(',', '.');
    }

    /** Format amount transaksi: RP1000000 */
    public static String formatRp(long amount) {
        return "RP" + Math.abs(amount);
    }

    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    public static String digitsOnly(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[^0-9]", "");
    }

    /** Kunci card untuk matching: 6 digit awal + 4 digit akhir (dukung kartu masked EJ). */
    public static String cardMatchKey(String card) {
        String digits = digitsOnly(card);
        if (digits.length() < 10) {
            return digits;
        }
        return digits.substring(0, 6) + digits.substring(digits.length() - 4);
    }

    public static boolean cardsMatch(String a, String b) {
        if (a == null || b == null || a.isBlank() || b.isBlank()) {
            return true;
        }
        return cardMatchKey(a).equals(cardMatchKey(b));
    }

    public static String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            return "";
        }
        String t = type.trim().toUpperCase();
        if (t.equals("W") || t.equals("D") || t.contains("TARIK") || t.contains("DISPENSE")) {
            return "D";
        }
        if (t.equals("K") || t.equals("S") || t.contains("SETOR") || t.contains("DEPOSIT")) {
            return "K";
        }
        return t;
    }

    public static boolean typesCompatible(String a, String b) {
        if (a == null || b == null || a.isBlank() || b.isBlank()) {
            return true;
        }
        return normalizeType(a).equals(normalizeType(b));
    }
}

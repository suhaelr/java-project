package com.selisihkurang.model;

import com.selisihkurang.util.ParseUtil;

import java.time.LocalDate;
import java.util.Objects;

public final class Transaction {
    private final Source source;
    private final LocalDate tanggal;
    private final String card;
    private final String norek;
    private final long record;
    private final String type;
    private final long amount;
    private final boolean suspect;
    private final boolean reversal;
    private final String rawLine;

    public Transaction(Source source, LocalDate tanggal, String card, String norek,
                       long record, String type, long amount, boolean suspect,
                       boolean reversal, String rawLine) {
        this.source = source;
        this.tanggal = tanggal;
        this.card = card == null ? "" : card.trim();
        this.norek = norek == null ? "" : norek.trim();
        this.record = record;
        this.type = type == null ? "" : type.trim().toUpperCase();
        this.amount = amount;
        this.suspect = suspect;
        this.reversal = reversal;
        this.rawLine = rawLine == null ? "" : rawLine;
    }

    public Source source() {
        return source;
    }

    public LocalDate tanggal() {
        return tanggal;
    }

    public String card() {
        return card;
    }

    public String norek() {
        return norek;
    }

    public long record() {
        return record;
    }

    public String type() {
        return type;
    }

    public long amount() {
        return amount;
    }

    public boolean suspect() {
        return suspect;
    }

    public boolean reversal() {
        return reversal;
    }

    public String rawLine() {
        return rawLine;
    }

    public boolean hasNorek() {
        return !norek.isEmpty()
                && !norek.equalsIgnoreCase("null")
                && !norek.equals("-");
    }

    public String matchKey() {
        return record + "|" + amount + "|" + type;
    }

    public String looseKey() {
        return tanggal + "|" + ParseUtil.cardMatchKey(card) + "|" + amount + "|" + ParseUtil.normalizeType(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Transaction that)) {
            return false;
        }
        return source == that.source
                && record == that.record
                && amount == that.amount
                && suspect == that.suspect
                && reversal == that.reversal
                && Objects.equals(tanggal, that.tanggal)
                && Objects.equals(card, that.card)
                && Objects.equals(norek, that.norek)
                && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, tanggal, card, norek, record, type, amount, suspect, reversal);
    }
}

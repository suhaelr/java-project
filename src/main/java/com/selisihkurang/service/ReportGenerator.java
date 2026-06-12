package com.selisihkurang.service;

import com.selisihkurang.model.BeritaAcaraData;
import com.selisihkurang.model.HasilRekonsiliasiData;
import com.selisihkurang.model.ReconciliationResult;
import com.selisihkurang.model.Transaction;
import com.selisihkurang.util.ParseUtil;

import java.util.ArrayList;
import java.util.List;

public final class ReportGenerator {
    private static final int ROWS_PER_PAGE = 14;
    private final HasilRekonsiliasiService excelService = new HasilRekonsiliasiService();

    public String generateHtml(BeritaAcaraData data, ReconciliationResult result) {
        HasilRekonsiliasiData excel = excelService.build(data, result);
        boolean suspectPending = excelService.isSuspectPending(result);

        List<Transaction> iconsSuspect = result.iconsTerbukuEjSuspect();
        List<Transaction> acqList = result.ejAdaRcTidakTerbuku();
        int totalPages = Math.max(1, Math.max(
                pageCount(iconsSuspect.size()),
                pageCount(acqList.size())));

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><style>");
        html.append(CSS);
        html.append("</style></head><body>");

        for (int page = 0; page < totalPages; page++) {
            html.append("<div class='page'>");
            if (page == 0) {
                appendPageOne(html, data, result, excel, suspectPending);
            }
            appendTransactionPages(html, iconsSuspect, acqList, page, totalPages);
            html.append("<div class='page-footer'>Page ").append(page + 1)
                    .append(" of ").append(totalPages).append("</div>");
            html.append("</div>");
        }

        html.append("</body></html>");
        return html.toString();
    }

    private void appendPageOne(StringBuilder html, BeritaAcaraData data,
                               ReconciliationResult result, HasilRekonsiliasiData excel,
                               boolean suspectPending) {
        String crmId = data.crmId().isBlank() ? "-" : escape(data.crmId());

        html.append("<table class='header-table'><tr>");
        html.append("<td class='title-cell'><div class='title-box'>BERITA ACARA SETTLEMENT CRM ID ")
                .append(crmId).append("</div></td>");
        html.append("<td class='logo-cell'><div class='logo-box'>&nbsp;</div></td>");
        html.append("</tr></table>");

        html.append("<table class='info-table'><tr>");
        html.append("<td class='info-left'>");
        html.append(labelValue("a. NAMA CRM ID", crmId));
        html.append(labelValue("b. NO. REKENING KAS", data.noRekeningKas()));
        html.append("</td><td class='info-right'>");
        html.append(labelValue("c. Petugas Settlement", data.petugasSettlement()));
        html.append(labelValue("d. Penyelia Penunjang", data.penyeliaPenunjang()));
        html.append(labelValue("e. Pengelola", data.pengelola()));
        html.append("</td></tr></table>");

        html.append("<div class='section-head'>RESTOCKING &amp; COLLECTING</div>");
        html.append("<table class='split-table'><tr>");
        html.append("<td class='split-left'>");
        html.append(labelValue("f. Periode CRM",
                ParseUtil.formatDate(data.periodeAwal()) + " s/d " + ParseUtil.formatDate(data.periodeAkhir())));
        html.append(labelValue("g. Record Awal", String.valueOf(data.recordAwal())));
        html.append(labelValue("h. Record Akhir", String.valueOf(data.recordAkhir())));
        html.append("</td><td class='split-right'>");
        html.append(labelValue("i. Fisik Collecting", fmt(data.fisikCollecting())));
        html.append(labelValue("j. Saldo Pembukuan Record Akhir", fmt(data.saldoPembukuan())));
        html.append(labelValue("k. Saldo Billcount", fmt(data.saldoBillcount())));
        html.append(labelValue("l. Saldo Admin", fmt(data.saldoAdmin())));
        html.append(labelValue("m. Selisih Lebih (kurang) Akumulatif", fmt(data.selisihAkumulatif())));
        html.append("</td></tr></table>");

        html.append("<div class='section-head'>SETTLEMENT</div>");
        html.append("<table class='settlement-wrap'><tr><td>");
        appendSettlementTable(html, "RC", data, result, true);
        html.append("</td><td>");
        appendSettlementTable(html, "EJ", data, result, false);
        html.append("</td></tr></table>");

        html.append("<table class='excel-summary'><tr>");
        html.append("<td><b>Selisih Admin (I):</b> ").append(fmt(excel.selisihAdmin())).append("</td>");
        html.append("<td><b>Selisih Per Periode (N):</b> ").append(fmt(excel.selisihPerPeriode()))
                .append(" (").append(excel.keteranganSelisih()).append(")</td>");
        html.append("<td><b>Q2 Net:</b> ").append(fmt(excel.selisihRecNet()))
                .append(" | ").append(excel.keteranganRecNum(suspectPending)).append("</td>");
        html.append("</tr></table>");
    }

    private void appendSettlementTable(StringBuilder html, String label, BeritaAcaraData data,
                                       ReconciliationResult result, boolean rcSide) {
        var summary = rcSide ? result.rcSummary() : result.ejSummary();
        long restocking = data.fisikRestocking();
        long collecting = data.fisikCollecting();
        int ejSuspectQty = rcSide ? result.iconsTerbukuEjSuspect().size() : 0;
        long ejSuspectAmt = rcSide ? -sumAmount(result.iconsTerbukuEjSuspect()) : 0;
        int rcSuspectQty = rcSide ? 0 : result.ejAdaRcTidakTerbuku().size();
        long rcSuspectAmt = rcSide ? 0 : -sumAmount(result.ejAdaRcTidakTerbuku());

        html.append("<table class='settlement-table'>");
        html.append("<tr><th colspan='3' class='settlement-title'>").append(label).append("</th></tr>");
        html.append("<tr><th>HASIL TRASIR</th><th>QTY</th><th>AMOUNT (RP)</th></tr>");
        appendSetRow(html, "Jumlah Fisik Restocking", 1, restocking);
        appendSetRow(html, "Jumlah Fisik Collecting", 1, collecting);
        appendSetRow(html, "Jumlah Penarikan", summary.qtyPenarikan(), -summary.jumlahPenarikan());
        appendSetRow(html, "Jumlah Setoran", summary.qtySetoran(), summary.jumlahSetoran());
        appendSetRow(html, "Selisih Periode CRM", 1, summary.selisihPeriode());
        if (rcSide) {
            appendSetRow(html, "EJ Suspect", ejSuspectQty, ejSuspectAmt);
        } else {
            appendSetRow(html, "RC Suspect", rcSuspectQty, rcSuspectAmt);
        }
        html.append("</table>");
    }

    private void appendTransactionPages(StringBuilder html, List<Transaction> left, List<Transaction> right,
                                        int page, int totalPages) {
        int start = page * ROWS_PER_PAGE;
        List<Transaction> leftSlice = slice(left, start, ROWS_PER_PAGE);
        List<Transaction> rightSlice = slice(right, start, ROWS_PER_PAGE);

        if (page > 0 && leftSlice.isEmpty() && rightSlice.isEmpty()) {
            return;
        }

        html.append("<table class='trx-wrap'><tr>");
        html.append("<td>");
        appendTrxTable(html, "TRX DI ICONS TERBUKU, DI EJ SUSPECT", leftSlice, true);
        html.append("</td><td>");
        appendTrxTable(html, "TRX DI EJ ADA, RC TIDAK TERBUKU", rightSlice, false);
        html.append("</td></tr></table>");

        if (page == 0) {
            html.append("<table class='notes-table'><tr>");
            html.append("<td class='note'>Jika terdapat <b>Nasabah Diuntungkan</b>, transaksi muncul di tabel kiri. ")
                    .append("Kolom <b>Norek</b> harus berisi nomor rekening nasabah (bukan null).</td>");
            html.append("<td class='note'>Jika terdapat <b>ACQ</b>, transaksi muncul di tabel kanan ")
                    .append("(TRX DI EJ ADA, RC TIDAK TERBUKU).</td>");
            html.append("</tr></table>");
        }
    }

    private void appendTrxTable(StringBuilder html, String title, List<Transaction> rows, boolean highlightNorek) {
        html.append("<div class='trx-title'>").append(escape(title)).append("</div>");
        html.append("<table class='trx-table'>");
        html.append("<tr><th>Tanggal</th><th>Card</th><th>Norek</th><th>Record</th><th>Type</th><th>Amount</th></tr>");
        if (rows.isEmpty()) {
            html.append("<tr><td colspan='6' class='empty'>&nbsp;</td></tr>");
        } else {
            for (Transaction tx : rows) {
                String norek = tx.hasNorek() ? escape(tx.norek()) : "null";
                String cls = highlightNorek && tx.hasNorek() ? " class='highlight'" : "";
                html.append("<tr").append(cls).append(">");
                html.append("<td>").append(ParseUtil.formatDate(tx.tanggal())).append("</td>");
                html.append("<td>").append(escape(tx.card())).append("</td>");
                html.append("<td>").append(norek).append("</td>");
                html.append("<td>").append(tx.record()).append("</td>");
                html.append("<td>").append(escape(tx.type())).append("</td>");
                html.append("<td class='amt'>").append(ParseUtil.formatRp(tx.amount())).append("</td>");
                html.append("</tr>");
            }
        }
        html.append("</table>");
    }

    private void appendSetRow(StringBuilder html, String label, int qty, long amount) {
        html.append("<tr><td>").append(escape(label)).append("</td>");
        html.append("<td class='center'>").append(qty).append("</td>");
        html.append("<td class='right'>").append(fmt(amount)).append("</td></tr>");
    }

    private String labelValue(String label, String value) {
        return "<div class='lv'><span class='lbl'>" + escape(label) + ":</span> "
                + "<span class='val'>" + escape(value) + "</span></div>";
    }

    private String fmt(long amount) {
        return ParseUtil.formatAmountAccounting(amount);
    }

    private long sumAmount(List<Transaction> list) {
        long total = 0;
        for (Transaction tx : list) {
            total += Math.abs(tx.amount());
        }
        return total;
    }

    private int pageCount(int rows) {
        if (rows == 0) {
            return 1;
        }
        return (int) Math.ceil((double) rows / ROWS_PER_PAGE);
    }

    private List<Transaction> slice(List<Transaction> list, int start, int size) {
        if (start >= list.size()) {
            return List.of();
        }
        int end = Math.min(start + size, list.size());
        return new ArrayList<>(list.subList(start, end));
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static final String CSS = """
            @page { size: A4 landscape; margin: 12mm; }
            body { font-family: Arial, Helvetica, sans-serif; font-size: 9px; color: #000; margin: 0; }
            .page { page-break-after: always; padding: 8px; position: relative; min-height: 190mm; }
            .page:last-child { page-break-after: auto; }
            .header-table { width: 100%; border-collapse: collapse; margin-bottom: 6px; }
            .title-cell { width: 75%; vertical-align: top; }
            .logo-cell { width: 25%; vertical-align: top; }
            .title-box { border: 2px solid #000; padding: 8px 12px; font-weight: bold; font-size: 11px;
                         text-align: center; }
            .logo-box { border: 1px solid #000; height: 42px; min-width: 80px; }
            .info-table { width: 100%; border-collapse: collapse; margin-bottom: 8px; }
            .info-left, .info-right { width: 50%; vertical-align: top; padding: 4px 8px; }
            .lv { margin: 2px 0; }
            .lbl { font-weight: bold; }
            .section-head { font-weight: bold; font-size: 10px; border-bottom: 1px solid #000;
                            margin: 8px 0 4px; padding-bottom: 2px; }
            .split-table { width: 100%; border-collapse: collapse; margin-bottom: 8px; }
            .split-left, .split-right { width: 50%; vertical-align: top; padding: 4px 8px; }
            .settlement-wrap { width: 100%; border-collapse: collapse; margin-bottom: 6px; }
            .settlement-wrap > tbody > tr > td { width: 50%; vertical-align: top; padding: 0 4px; }
            .settlement-table { width: 100%; border-collapse: collapse; font-size: 8px; }
            .settlement-table th, .settlement-table td { border: 1px solid #000; padding: 2px 4px; }
            .settlement-title { background: #d9e8f7; text-align: center; font-weight: bold; }
            .settlement-table th { background: #eef4fb; font-weight: bold; }
            .center { text-align: center; }
            .right { text-align: right; }
            .excel-summary { width: 100%; margin: 6px 0; font-size: 8px; }
            .excel-summary td { padding: 2px 6px; }
            .trx-wrap { width: 100%; border-collapse: collapse; margin-top: 6px; }
            .trx-wrap > tbody > tr > td { width: 50%; vertical-align: top; padding: 0 3px; }
            .trx-title { font-weight: bold; font-size: 8px; text-align: center; margin-bottom: 2px; }
            .trx-table { width: 100%; border-collapse: collapse; font-size: 7px; }
            .trx-table th { background: #b8d4f0; border: 1px solid #000; padding: 2px 3px; }
            .trx-table td { border: 1px solid #000; padding: 1px 3px; }
            .trx-table .amt { text-align: right; white-space: nowrap; }
            .trx-table .empty { height: 18px; }
            .highlight { background: #fff3cd; }
            .notes-table { width: 100%; margin-top: 4px; font-size: 7px; }
            .note { border: 1px solid #999; padding: 4px; vertical-align: top; width: 50%; }
            .page-footer { position: absolute; bottom: 4px; right: 12px; font-size: 8px; }
            @media print {
              body { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
            }
            """;
}

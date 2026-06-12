package com.selisihkurang.ui;

import com.selisihkurang.model.HasilRekonsiliasiData;
import com.selisihkurang.util.ParseUtil;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class HasilRekonsiliasiPanel extends JPanel {
    private final JTextField idAtmField = ro();
    private final JTextField nominalField = ro();
    private final JTextField tglAwalField = ro();
    private final JTextField tglAkhirField = ro();
    private final JTextField recordAwalField = ro();
    private final JTextField recordAkhirField = ro();
    private final JTextField sisaAdminField = ro();
    private final JTextField sisaFisikField = ro();
    private final JTextField selisihAdminField = ro();
    private final JTextField setoranField = ro();
    private final JTextField penarikanField = ro();
    private final JTextField sisaRestField = ro();
    private final JTextField pembukuanField = edit();
    private final JTextField trxRecField = edit();
    private final JTextField selisihPeriodeField = ro();
    private final JTextField keteranganField = ro();
    private final JTextField q1Field = ro();
    private final JTextField q2Field = ro();
    private final JTextField statusField = ro();

    public HasilRekonsiliasiPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 6, 3, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBorder(BorderFactory.createTitledBorder("Hasil Rekonsiliasi (Excel A–R)"));

        int row = 0;
        row = addRow(grid, gbc, row, "A - ID ATM:", idAtmField);
        row = addRow(grid, gbc, row, "B - Nominal Pengisian:", nominalField);
        row = addRow(grid, gbc, row, "C - Tgl Awal:", tglAwalField);
        row = addRow(grid, gbc, row, "D - Tgl Akhir:", tglAkhirField);
        row = addRow(grid, gbc, row, "E - Record Awal:", recordAwalField);
        row = addRow(grid, gbc, row, "F - Record Akhir:", recordAkhirField);
        row = addRow(grid, gbc, row, "G - Sisa Menurut Admin:", sisaAdminField);
        row = addRow(grid, gbc, row, "H - Sisa Fisik:", sisaFisikField);
        row = addRow(grid, gbc, row, "I - Selisih Admin (H-G):", selisihAdminField);
        row = addRow(grid, gbc, row, "J - Setoran SKA:", setoranField);
        row = addRow(grid, gbc, row, "K - Penarikan SKA:", penarikanField);
        row = addRow(grid, gbc, row, "L - Sisa Rest ICONS:", sisaRestField);
        row = addRow(grid, gbc, row, "M - Pembukuan SR (manual):", pembukuanField);
        row = addRow(grid, gbc, row, "N - Selisih Per Periode (M-L):", selisihPeriodeField);
        row = addRow(grid, gbc, row, "O - Keterangan:", keteranganField);
        row = addRow(grid, gbc, row, "P - TRX by Rec Num (manual):", trxRecField);
        row = addRow(grid, gbc, row, "Q1 - Akumulasi (M-P):", q1Field);
        row = addRow(grid, gbc, row, "Q2 - Net (L-P):", q2Field);
        row = addRow(grid, gbc, row, "R - Status:", statusField);

        setLayout(new BorderLayout());
        add(new JScrollPane(grid), BorderLayout.CENTER);
    }

    private int addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
        return row + 1;
    }

    private JTextField ro() {
        JTextField f = new JTextField(20);
        f.setEditable(false);
        f.setBackground(new java.awt.Color(245, 245, 245));
        return f;
    }

    private JTextField edit() {
        return new JTextField(20);
    }

    public void apply(HasilRekonsiliasiData data, boolean suspectPending) {
        idAtmField.setText(data.idAtm());
        nominalField.setText(ParseUtil.formatAmount(data.nominalPengisian()));
        tglAwalField.setText(ParseUtil.formatDate(data.tglAwal()));
        tglAkhirField.setText(ParseUtil.formatDate(data.tglAkhir()));
        recordAwalField.setText(String.valueOf(data.recordAwal()));
        recordAkhirField.setText(String.valueOf(data.recordAkhir()));
        sisaAdminField.setText(ParseUtil.formatAmount(data.sisaMenurutAdmin()));
        sisaFisikField.setText(ParseUtil.formatAmount(data.sisaFisik()));
        selisihAdminField.setText(ParseUtil.formatAmount(data.selisihAdmin()));
        setoranField.setText(ParseUtil.formatAmount(data.setoranSka()));
        penarikanField.setText(ParseUtil.formatAmount(data.penarikanSka()));
        sisaRestField.setText(ParseUtil.formatAmount(data.sisaRestIcons()));
        if (data.pembukuanSr() != 0) {
            pembukuanField.setText(String.valueOf(data.pembukuanSr()));
        }
        if (data.trxByRecNum() != 0) {
            trxRecField.setText(String.valueOf(data.trxByRecNum()));
        }
        selisihPeriodeField.setText(ParseUtil.formatAmount(data.selisihPerPeriode()));
        keteranganField.setText(data.keteranganSelisih());
        q1Field.setText(ParseUtil.formatAmount(data.selisihRecAkumulasi()));
        q2Field.setText(ParseUtil.formatAmount(data.selisihRecNet()));
        statusField.setText(data.keteranganRecNum(suspectPending));
    }

    public long pembukuanSr() {
        return ParseUtil.parseAmount(pembukuanField.getText());
    }

    public long trxByRecNum() {
        return ParseUtil.parseAmount(trxRecField.getText());
    }
}

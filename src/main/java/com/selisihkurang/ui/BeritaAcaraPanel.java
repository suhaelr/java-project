package com.selisihkurang.ui;

import com.selisihkurang.model.BeritaAcaraData;
import com.selisihkurang.util.ParseUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class BeritaAcaraPanel extends JPanel {
    private final JTextField crmIdField = new JTextField(20);
    private final JTextField petugasField = new JTextField(20);
    private final JTextField penyeliaField = new JTextField(20);
    private final JTextField pengelolaField = new JTextField(20);
    private final JTextField rekeningField = new JTextField(20);
    private final JTextField nominalField = new JTextField(15);
    private final JTextField fisikRestockingField = new JTextField(15);
    private final JTextField fisikCollectingField = new JTextField(15);
    private final JTextField saldoPembukuanField = new JTextField(15);
    private final JTextField saldoAdminField = new JTextField(15);
    private final JTextField saldoBillcountField = new JTextField(15);

    private Runnable printAction = () -> {};

    public BeritaAcaraPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        row = addField(form, gbc, row, "a. CRM ID:", crmIdField);
        row = addField(form, gbc, row, "b. No Rekening Kas:", rekeningField);
        row = addField(form, gbc, row, "c. Petugas Settlement:", petugasField);
        row = addField(form, gbc, row, "d. Penyelia Penunjang:", penyeliaField);
        row = addField(form, gbc, row, "e. Pengelola:", pengelolaField);
        row = addField(form, gbc, row, "Nominal Pengisian (B):", nominalField);
        row = addField(form, gbc, row, "Fisik Restocking:", fisikRestockingField);
        row = addField(form, gbc, row, "i. Fisik Collecting:", fisikCollectingField);
        row = addField(form, gbc, row, "j. Saldo Pembukuan:", saldoPembukuanField);
        row = addField(form, gbc, row, "l. Saldo Admin:", saldoAdminField);
        row = addField(form, gbc, row, "k. Saldo Billcount:", saldoBillcountField);

        JButton printButton = new JButton("Print Berita Acara");
        printButton.addActionListener(e -> printAction.run());
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        form.add(printButton, gbc);

        setLayout(new BorderLayout());
        add(new JScrollPane(form), BorderLayout.CENTER);
    }

    private int addField(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
        return row + 1;
    }

    public void setPrintAction(Runnable action) {
        this.printAction = action;
    }

    public BeritaAcaraData collectData() {
        BeritaAcaraData data = new BeritaAcaraData();
        data.setCrmId(crmIdField.getText().trim());
        data.setPetugasSettlement(petugasField.getText().trim());
        data.setPenyeliaPenunjang(penyeliaField.getText().trim());
        data.setPengelola(pengelolaField.getText().trim());
        data.setNoRekeningKas(rekeningField.getText().trim());
        data.setNominalPengisian(ParseUtil.parseAmount(nominalField.getText()));
        data.setFisikRestocking(ParseUtil.parseAmount(fisikRestockingField.getText()));
        data.setFisikCollecting(ParseUtil.parseAmount(fisikCollectingField.getText()));
        data.setSaldoPembukuan(ParseUtil.parseAmount(saldoPembukuanField.getText()));
        data.setSaldoAdmin(ParseUtil.parseAmount(saldoAdminField.getText()));
        data.setSaldoBillcount(ParseUtil.parseAmount(saldoBillcountField.getText()));
        data.setSisaMenurutAdmin(ParseUtil.parseAmount(saldoAdminField.getText()));
        data.setSisaFisik(ParseUtil.parseAmount(saldoBillcountField.getText()));
        return data;
    }

    public void applyData(BeritaAcaraData data) {
        crmIdField.setText(data.crmId());
        petugasField.setText(data.petugasSettlement());
        penyeliaField.setText(data.penyeliaPenunjang());
        pengelolaField.setText(data.pengelola());
        rekeningField.setText(data.noRekeningKas());
        setIfPositive(nominalField, data.nominalPengisian() > 0 ? data.nominalPengisian() : data.fisikRestocking());
        setIfPositive(fisikRestockingField, data.fisikRestocking());
        setIfPositive(fisikCollectingField, data.fisikCollecting());
        setIfPositive(saldoPembukuanField, data.saldoPembukuan());
        setIfPositive(saldoAdminField, data.saldoAdmin());
        setIfPositive(saldoBillcountField, data.saldoBillcount());
    }

    private void setIfPositive(JTextField field, long value) {
        if (value != 0) {
            field.setText(String.valueOf(value));
        }
    }
}

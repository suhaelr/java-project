package com.selisihkurang.ui;

import com.selisihkurang.model.BeritaAcaraData;
import com.selisihkurang.model.HasilRekonsiliasiData;
import com.selisihkurang.model.ReconciliationFilter;
import com.selisihkurang.model.ReconciliationResult;
import com.selisihkurang.model.Transaction;
import com.selisihkurang.service.HasilRekonsiliasiService;
import com.selisihkurang.service.ReconciliationService;
import com.selisihkurang.service.ReportGenerator;
import com.selisihkurang.service.TxtFileParser;
import com.selisihkurang.util.ParseUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class MainFrame extends JFrame {
    private final TxtFileParser parser = new TxtFileParser();
    private final ReconciliationService reconciliationService = new ReconciliationService();
    private final HasilRekonsiliasiService hasilRekonsiliasiService = new HasilRekonsiliasiService();
    private final ReportGenerator reportGenerator = new ReportGenerator();

    private final JTextField rcFileField = new JTextField(30);
    private final JTextField ejFileField = new JTextField(30);
    private final JTextField tanggalAwalField = new JTextField(12);
    private final JTextField tanggalAkhirField = new JTextField(12);
    private final JTextField recordAwalField = new JTextField(8);
    private final JTextField recordAkhirField = new JTextField(8);
    private final JLabel statusLabel = new JLabel("Siap. Pilih file RC dan EJ, lalu klik Rekon.");

    private final TransactionTableModel matchModel = new TransactionTableModel(
            "Tanggal", "Card", "Norek", "Record", "Type RC", "Amount RC", "Amount EJ", "Status");
    private final TransactionTableModel ejSuspectModel = new TransactionTableModel(
            "Tanggal", "Card", "Norek", "Record", "Type", "Amount", "Status");
    private final TransactionTableModel rcSuspectModel = new TransactionTableModel(
            "Tanggal", "Card", "Norek", "Record", "Type", "Amount", "Status");
    private final TransactionTableModel reversalModel = new TransactionTableModel(
            "Tanggal", "Card", "Norek", "Record", "Type", "Amount", "Status");
    private final TransactionTableModel nasabahModel = new TransactionTableModel(
            "Tanggal", "Card", "Norek", "Record", "Type", "Amount", "Status");
    private final TransactionTableModel acqModel = new TransactionTableModel(
            "Tanggal", "Card", "Norek", "Record", "Type", "Amount", "Status");

    private final HasilRekonsiliasiPanel hasilRekonsiliasiPanel = new HasilRekonsiliasiPanel();
    private final BeritaAcaraPanel beritaAcaraPanel = new BeritaAcaraPanel();
    private final ReportPanel reportPanel = new ReportPanel();

    private List<Transaction> rcData = List.of();
    private List<Transaction> ejData = List.of();
    private ReconciliationResult lastResult;

    public MainFrame() {
        super("Selisih Kurang - Rekonsiliasi RC vs EJ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.add(buildInputPanel(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Match", tablePanel(matchModel));
        tabs.addTab("EJ Suspect", tablePanel(ejSuspectModel));
        tabs.addTab("RC Suspect", tablePanel(rcSuspectModel));
        tabs.addTab("Reversal", tablePanel(reversalModel));
        tabs.addTab("Nasabah Diuntungkan", tablePanel(nasabahModel));
        tabs.addTab("ACQ (EJ ada, RC tidak)", tablePanel(acqModel));
        tabs.addTab("Hasil Rekonsiliasi", hasilRekonsiliasiPanel);
        tabs.addTab("Berita Acara", beritaAcaraPanel);
        tabs.addTab("Report Berita Acara", reportPanel);
        root.add(tabs, BorderLayout.CENTER);

        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));
        root.add(statusLabel, BorderLayout.SOUTH);

        beritaAcaraPanel.setPrintAction(() -> {
            if (lastResult == null) {
                JOptionPane.showMessageDialog(this,
                        "Lakukan rekonsiliasi terlebih dahulu.", "Perhatian", JOptionPane.WARNING_MESSAGE);
                return;
            }
            BeritaAcaraData data = beritaAcaraPanel.collectData();
            fillBeritaAcaraDefaults(data);
            data.setPembukuanSr(hasilRekonsiliasiPanel.pembukuanSr());
            data.setTrxByRecNum(hasilRekonsiliasiPanel.trxByRecNum());
            if (data.sisaRestIcons() == 0) {
                data.setSisaRestIcons(lastResult.rcSummary().jumlahSetoran());
            }
            String html = reportGenerator.generateHtml(data, lastResult);
            reportPanel.setReportHtml(html);
            JOptionPane.showMessageDialog(this, "Berita Acara berhasil dibuat. Lihat tab Report Berita Acara.",
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
        });

        setContentPane(root);
    }

    private JPanel buildInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel files = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addFileRow(files, gbc, 0, "File RC:", rcFileField, com.selisihkurang.model.Source.RC);
        addFileRow(files, gbc, 1, "File EJ:", ejFileField, com.selisihkurang.model.Source.EJ);

        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Rekonsiliasi"));
        GridBagConstraints fgbc = new GridBagConstraints();
        fgbc.insets = new Insets(4, 6, 4, 6);
        fgbc.anchor = GridBagConstraints.WEST;

        fgbc.gridx = 0;
        fgbc.gridy = 0;
        filterPanel.add(new JLabel("Tanggal Awal:"), fgbc);
        fgbc.gridx = 1;
        filterPanel.add(tanggalAwalField, fgbc);
        fgbc.gridx = 2;
        filterPanel.add(new JLabel("Tanggal Akhir:"), fgbc);
        fgbc.gridx = 3;
        filterPanel.add(tanggalAkhirField, fgbc);

        fgbc.gridx = 0;
        fgbc.gridy = 1;
        filterPanel.add(new JLabel("Record Awal:"), fgbc);
        fgbc.gridx = 1;
        filterPanel.add(recordAwalField, fgbc);
        fgbc.gridx = 2;
        filterPanel.add(new JLabel("Record Akhir:"), fgbc);
        fgbc.gridx = 3;
        filterPanel.add(recordAkhirField, fgbc);

        JButton rekonButton = new JButton("Rekon");
        rekonButton.addActionListener(e -> runReconciliation());
        fgbc.gridx = 0;
        fgbc.gridy = 2;
        fgbc.gridwidth = 4;
        filterPanel.add(rekonButton, fgbc);

        panel.add(files, BorderLayout.NORTH);
        panel.add(filterPanel, BorderLayout.CENTER);
        return panel;
    }

    private void addFileRow(JPanel panel, GridBagConstraints gbc, int row, String label,
                            JTextField field, com.selisihkurang.model.Source source) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton browse = new JButton("Browse");
        browse.addActionListener(e -> chooseFile(field, source));
        panel.add(browse, gbc);
    }

    private JScrollPane tablePanel(TransactionTableModel model) {
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(22);
        return new JScrollPane(table);
    }

    private void chooseFile(JTextField target, com.selisihkurang.model.Source source) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt", "csv", "dat"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            target.setText(file.getAbsolutePath());
            refreshFiltersFromFiles();
        }
    }

    private void refreshFiltersFromFiles() {
        RangeAccumulator acc = new RangeAccumulator();
        acc.addFile(rcFileField.getText().trim(), com.selisihkurang.model.Source.RC);
        acc.addFile(ejFileField.getText().trim(), com.selisihkurang.model.Source.EJ);

        if (acc.minDate != null) {
            tanggalAwalField.setText(ParseUtil.formatDate(acc.minDate));
            tanggalAkhirField.setText(ParseUtil.formatDate(acc.maxDate));
        }
        if (acc.minRec != Long.MAX_VALUE) {
            recordAwalField.setText(String.valueOf(acc.minRec));
            recordAkhirField.setText(String.valueOf(acc.maxRec));
        }
    }

    private final class RangeAccumulator {
        private LocalDate minDate;
        private LocalDate maxDate;
        private long minRec = Long.MAX_VALUE;
        private long maxRec;

        void addFile(String path, com.selisihkurang.model.Source source) {
            if (path.isEmpty()) {
                return;
            }
            try {
                for (Transaction tx : parser.parse(Path.of(path), source)) {
                    if (tx.tanggal() != null) {
                        if (minDate == null || tx.tanggal().isBefore(minDate)) {
                            minDate = tx.tanggal();
                        }
                        if (maxDate == null || tx.tanggal().isAfter(maxDate)) {
                            maxDate = tx.tanggal();
                        }
                    }
                    if (tx.record() > 0) {
                        minRec = Math.min(minRec, tx.record());
                        maxRec = Math.max(maxRec, tx.record());
                    }
                }
            } catch (Exception ignored) {
                // auto-detect is best-effort
            }
        }
    }

    private void runReconciliation() {
        String rcPath = rcFileField.getText().trim();
        String ejPath = ejFileField.getText().trim();
        if (rcPath.isEmpty() || ejPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "File RC dan File EJ wajib diisi.",
                    "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ReconciliationFilter filter = new ReconciliationFilter(
                ParseUtil.parseDate(tanggalAwalField.getText()),
                ParseUtil.parseDate(tanggalAkhirField.getText()),
                ParseUtil.parseRecord(recordAwalField.getText()),
                ParseUtil.parseRecord(recordAkhirField.getText())
        );

        statusLabel.setText("Memproses rekonsiliasi...");
        new SwingWorker<ReconciliationResult, Void>() {
            @Override
            protected ReconciliationResult doInBackground() throws Exception {
                rcData = parser.parse(Path.of(rcPath), com.selisihkurang.model.Source.RC);
                ejData = parser.parse(Path.of(ejPath), com.selisihkurang.model.Source.EJ);
                return reconciliationService.reconcile(rcData, ejData, filter);
            }

            @Override
            protected void done() {
                try {
                    lastResult = get();
                    updateTables(lastResult);
                    statusLabel.setText(String.format(
                            "Selesai. RC: %d/%d | EJ: %d/%d | Match: %d | Nasabah Diuntungkan: %d | ACQ: %d",
                            lastResult.filteredRc(), lastResult.totalRc(),
                            lastResult.filteredEj(), lastResult.totalEj(),
                            lastResult.matched().size(),
                            lastResult.nasabahDiuntungkan().size(),
                            lastResult.ejAdaRcTidakTerbuku().size()));
                    prefillBeritaAcara();
                } catch (Exception ex) {
                    statusLabel.setText("Gagal: " + ex.getMessage());
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Error: " + ex.getMessage(), "Gagal Rekon", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void updateTables(ReconciliationResult result) {
        matchModel.setMatched(result.matched());
        ejSuspectModel.setTransactions(result.ejSuspect());
        rcSuspectModel.setTransactions(result.rcSuspect());
        reversalModel.setTransactions(result.reversals());
        nasabahModel.setTransactions(result.nasabahDiuntungkan());
        acqModel.setTransactions(result.ejAdaRcTidakTerbuku());
    }

    private void prefillBeritaAcara() {
        BeritaAcaraData data = beritaAcaraPanel.collectData();
        fillBeritaAcaraDefaults(data);
        beritaAcaraPanel.applyData(data);
        updateHasilRekonsiliasi(data);
    }

    private void updateHasilRekonsiliasi(BeritaAcaraData data) {
        if (lastResult == null) {
            return;
        }
        data.setPembukuanSr(hasilRekonsiliasiPanel.pembukuanSr());
        data.setTrxByRecNum(hasilRekonsiliasiPanel.trxByRecNum());
        HasilRekonsiliasiData excel = hasilRekonsiliasiService.build(data, lastResult);
        hasilRekonsiliasiPanel.apply(excel, hasilRekonsiliasiService.isSuspectPending(lastResult));
    }

    private void fillBeritaAcaraDefaults(BeritaAcaraData data) {
        data.setPeriodeAwal(ParseUtil.parseDate(tanggalAwalField.getText()));
        data.setPeriodeAkhir(ParseUtil.parseDate(tanggalAkhirField.getText()));
        data.setRecordAwal(ParseUtil.parseRecord(recordAwalField.getText()));
        data.setRecordAkhir(ParseUtil.parseRecord(recordAkhirField.getText()));

        if (lastResult == null) {
            return;
        }
        if (data.fisikCollecting() == 0) {
            data.setFisikCollecting(lastResult.rcSummary().jumlahSetoran());
        }
        if (data.saldoPembukuan() == 0) {
            data.setSaldoPembukuan(data.fisikCollecting());
        }
        if (data.saldoAdmin() == 0) {
            data.setSaldoAdmin(lastResult.ejSummary().jumlahSetoran());
        }
        if (data.saldoBillcount() == 0) {
            data.setSaldoBillcount(data.saldoPembukuan());
        }
        if (data.sisaRestIcons() == 0) {
            data.setSisaRestIcons(lastResult.rcSummary().jumlahSetoran());
        }
        if (data.nominalPengisian() == 0 && data.fisikRestocking() > 0) {
            data.setNominalPengisian(data.fisikRestocking());
        }
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            try {
                javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}

package com.selisihkurang.ui;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ReportPanel extends JPanel {
    private final JEditorPane editor = new JEditorPane("text/html", "<html><body><p>Belum ada report.</p></body></html>");
    private String currentHtml = "";

    public ReportPanel() {
        setLayout(new BorderLayout());
        editor.setEditable(false);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton saveButton = new JButton("Simpan HTML");
        JButton printButton = new JButton("Cetak (Landscape)");
        saveButton.addActionListener(e -> saveHtml());
        printButton.addActionListener(e -> {
            try {
                java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
                java.awt.print.PageFormat format = job.defaultPage();
                format.setOrientation(java.awt.print.PageFormat.LANDSCAPE);
                job.setPrintable(editor.getPrintable(null, null), format);
                if (job.printDialog()) {
                    job.print();
                }
            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Cetak Gagal",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });
        toolbar.add(saveButton);
        toolbar.add(printButton);

        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(editor), BorderLayout.CENTER);
    }

    public void setReportHtml(String html) {
        currentHtml = html;
        editor.setText(html);
        editor.setCaretPosition(0);
    }

    private void saveHtml() {
        if (currentHtml.isBlank()) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("berita-acara.html"));
        chooser.setFileFilter(new FileNameExtensionFilter("HTML", "html"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path path = chooser.getSelectedFile().toPath();
            try {
                Files.writeString(path, currentHtml);
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(path.toFile());
                }
            } catch (IOException ex) {
                javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Simpan Gagal",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

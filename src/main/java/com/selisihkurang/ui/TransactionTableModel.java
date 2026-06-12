package com.selisihkurang.ui;

import com.selisihkurang.model.MatchedPair;
import com.selisihkurang.model.Transaction;
import com.selisihkurang.util.ParseUtil;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class TransactionTableModel extends AbstractTableModel {
    private final String[] columns;
    private final List<String[]> rows = new ArrayList<>();

    public TransactionTableModel(String... columns) {
        this.columns = columns;
    }

    public void setTransactions(List<Transaction> transactions) {
        rows.clear();
        for (Transaction tx : transactions) {
            rows.add(new String[]{
                    ParseUtil.formatDate(tx.tanggal()),
                    tx.card(),
                    tx.norek(),
                    String.valueOf(tx.record()),
                    tx.type(),
                    ParseUtil.formatAmount(tx.amount()),
                    tx.suspect() ? "SUSPECT" : ""
            });
        }
        fireTableDataChanged();
    }

    public void setMatched(List<MatchedPair> pairs) {
        rows.clear();
        for (MatchedPair pair : pairs) {
            rows.add(new String[]{
                    ParseUtil.formatDate(pair.rc().tanggal()),
                    pair.rc().card(),
                    pair.rc().norek(),
                    String.valueOf(pair.rc().record()),
                    pair.rc().type(),
                    ParseUtil.formatAmount(pair.rc().amount()),
                    ParseUtil.formatAmount(pair.ej().amount()),
                    pair.rc().suspect() || pair.ej().suspect() ? "SUSPECT" : "OK"
            });
        }
        fireTableDataChanged();
    }

    public void clear() {
        rows.clear();
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return rows.get(rowIndex)[columnIndex];
    }
}

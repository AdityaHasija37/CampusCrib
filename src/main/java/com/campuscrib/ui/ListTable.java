package com.campuscrib.ui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

/** A read-only table over a list of records. Cells that are JLabels (badges) are drawn as-is. */
public class ListTable<T> {
    public record Column<T>(String name, int width, Function<T, Object> value) {}

    private final List<Column<T>> columns;
    private final List<T> rows = new ArrayList<>();
    private final JTable table;
    private final Model model = new Model();

    @SafeVarargs
    public ListTable(Column<T>... columns) {
        this.columns = List.of(columns);
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(Theme.sans(Font.BOLD, 12));
        table.setDefaultRenderer(Object.class, new Renderer());
        for (int i = 0; i < columns.length; i++) {
            TableColumn c = table.getColumnModel().getColumn(i);
            if (columns[i].width() > 0) {
                c.setPreferredWidth(columns[i].width());
                c.setMaxWidth(columns[i].width() * 2);
            }
        }
    }

    public void setRows(List<T> items) {
        Optional<T> selected = selected();
        rows.clear();
        rows.addAll(items);
        model.fireTableDataChanged();
        selected.ifPresent(s -> {
            int i = rows.indexOf(s);
            if (i >= 0) table.setRowSelectionInterval(i, i);
        });
    }

    public Optional<T> selected() {
        int i = table.getSelectedRow();
        return i < 0 || i >= rows.size() ? Optional.empty() : Optional.of(rows.get(table.convertRowIndexToModel(i)));
    }

    public void select(int index) {
        if (index >= 0 && index < rows.size()) table.setRowSelectionInterval(index, index);
    }

    public void selectWhere(java.util.function.Predicate<T> match) {
        for (int i = 0; i < rows.size(); i++) {
            if (match.test(rows.get(i))) {
                select(i);
                return;
            }
        }
    }

    public void onSelect(Consumer<Optional<T>> listener) {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) listener.accept(selected());
        });
    }

    public int size() {
        return rows.size();
    }

    public JTable table() {
        return table;
    }

    /** The table in a rounded white card. */
    public JComponent view() {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Theme.SURFACE);
        RoundedPanel card = new RoundedPanel(new java.awt.BorderLayout(), Theme.SURFACE, Theme.LINE, 18);
        card.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        card.add(scroll);
        return card;
    }

    private final class Model extends AbstractTableModel {
        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.size();
        }

        @Override
        public String getColumnName(int column) {
            return columns.get(column).name();
        }

        @Override
        public Object getValueAt(int row, int column) {
            return columns.get(column).value().apply(rows.get(row));
        }
    }

    private static final class Renderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
            if (value instanceof JLabel badge) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 9));
                p.setBackground(selected ? table.getSelectionBackground() : table.getBackground());
                p.add(badge);
                return p;
            }
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, selected, false, row, column);
            l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            l.setForeground(Theme.INK);
            return l;
        }
    }
}

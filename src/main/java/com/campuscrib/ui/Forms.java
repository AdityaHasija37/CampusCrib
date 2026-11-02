package com.campuscrib.ui;

import java.awt.Font;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

/** Form fields Swing doesn't have out of the box: date and date-time pickers. */
public final class Forms {
    private Forms() {}

    public static JSpinner datePicker(LocalDate initial) {
        JSpinner s = new JSpinner(new SpinnerDateModel(toDate(initial.atStartOfDay()), null, null, Calendar.DAY_OF_MONTH));
        s.setEditor(new JSpinner.DateEditor(s, "EEE d MMM yyyy"));
        return s;
    }

    public static JSpinner dateTimePicker(LocalDateTime initial) {
        JSpinner s = new JSpinner(new SpinnerDateModel(toDate(initial), null, null, Calendar.HOUR_OF_DAY));
        s.setEditor(new JSpinner.DateEditor(s, "EEE d MMM yyyy, HH:mm"));
        return s;
    }

    public static LocalDate date(JSpinner s) {
        return ((Date) s.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime dateTime(JSpinner s) {
        return ((Date) s.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().withSecond(0).withNano(0);
    }

    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.sans(Font.BOLD, 13));
        l.setForeground(Theme.INK);
        return l;
    }

    /** A label above its field, as one block. */
    public static JComponent field(String label, JComponent input) {
        return Ui.column(label(label), Ui.gap(6), fixedHeight(input), Ui.gap(14));
    }

    /** Stops vertical layouts from stretching an input taller than it needs to be. */
    public static <T extends JComponent> T fixedHeight(T input) {
        input.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, input.getPreferredSize().height));
        return input;
    }

    private static Date toDate(LocalDateTime t) {
        return Date.from(t.atZone(ZoneId.systemDefault()).toInstant());
    }
}

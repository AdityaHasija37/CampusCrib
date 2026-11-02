package com.campuscrib.ui;

import com.campuscrib.domain.Amenity;
import com.campuscrib.domain.NewRequest;
import com.campuscrib.domain.Priority;
import com.campuscrib.domain.RequestCategory;
import com.campuscrib.domain.Trade;
import com.campuscrib.domain.User;
import com.campuscrib.domain.WorkRequest;
import com.campuscrib.service.Services;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;

/** A resident raises a request. The form changes with the kind of request. */
public class NewRequestDialog extends JDialog {
    private WorkRequest created;
    private RequestCategory category = RequestCategory.MAINTENANCE;

    public NewRequestDialog(Window owner, Services services, User resident) {
        super(owner, ModalityType.APPLICATION_MODAL);
        setTitle("New request");

        JComboBox<Trade> trade = new JComboBox<>(Trade.values());
        JComboBox<Amenity> amenity = new JComboBox<>(Amenity.values());
        JSpinner slot = Forms.dateTimePicker(LocalDate.now().plusDays(1).atTime(18, 0));
        JSpinner hours = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1));
        JTextField title = new JTextField();
        JTextArea description = new JTextArea(5, 20);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        JComboBox<Priority> priority = new JComboBox<>(Priority.values());
        priority.setSelectedItem(Priority.NORMAL);

        JPanel extra = new JPanel(new BorderLayout());
        extra.setOpaque(false);
        java.util.Map<RequestCategory, javax.swing.JComponent> extras = new java.util.EnumMap<>(RequestCategory.class);
        extras.put(RequestCategory.MAINTENANCE, Forms.field("What needs fixing?", trade));
        JPanel amenityFields = new JPanel(new GridLayout(1, 3, 12, 0));
        amenityFields.setOpaque(false);
        amenityFields.add(Forms.field("Space", amenity));
        amenityFields.add(Forms.field("Starting", slot));
        amenityFields.add(Forms.field("Hours", hours));
        extras.put(RequestCategory.AMENITY, amenityFields);
        extras.put(RequestCategory.INQUIRY, Ui.column(Ui.muted("Building management will reply here."), Ui.gap(14)));
        extra.add(extras.get(RequestCategory.MAINTENANCE));

        JPanel kinds = new JPanel(new GridLayout(1, 3, 10, 0));
        kinds.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        String[][] options = {
                {"MAINTENANCE", "Something's broken", "Goes to the right crew"},
                {"AMENITY", "Book a space", "Gym, rooftop and more"},
                {"INQUIRY", "Ask a question", "Management replies"}};
        for (String[] o : options) {
            JToggleButton b = new JToggleButton("<html><b>" + o[1] + "</b><br><span style='color:#6B625B'>" + o[2] + "</span></html>");
            b.setHorizontalAlignment(JLabel.LEFT);
            b.setFocusPainted(false);
            b.putClientProperty("JButton.buttonType", "roundRect");
            b.addActionListener(e -> {
                category = RequestCategory.valueOf(o[0]);
                extra.removeAll();
                extra.add(extras.get(category));
                extra.revalidate();
                extra.repaint();
                priority.setEnabled(category != RequestCategory.AMENITY);
                if (category == RequestCategory.AMENITY) priority.setSelectedItem(Priority.NORMAL);
            });
            group.add(b);
            kinds.add(b);
            if (o[0].equals("MAINTENANCE")) b.setSelected(true);
        }

        JPanel form = Ui.column(
                Forms.label("What kind of request?"), Ui.gap(8), kinds, Ui.gap(18),
                extra,
                Forms.field("Short title", title),
                Forms.field("Details", new JScrollPane(description)),
                Forms.field("Priority", priority),
                Ui.muted("Unit " + resident.unit() + " · " + resident.fullName()));

        JButton submit = Ui.primary("Submit request");
        submit.addActionListener(e -> {
            NewRequest r = new NewRequest(category,
                    category == RequestCategory.MAINTENANCE ? (Trade) trade.getSelectedItem() : null,
                    category == RequestCategory.AMENITY ? (Amenity) amenity.getSelectedItem() : null,
                    category == RequestCategory.AMENITY ? Forms.dateTime(slot) : null,
                    category == RequestCategory.AMENITY ? (Integer) hours.getValue() : null,
                    title.getText(), description.getText(), (Priority) priority.getSelectedItem());
            created = Ui.fetch(this, () -> services.requests.submit(resident, r));
            if (created != null) {
                JOptionPane.showMessageDialog(this, "Sent to " + created.routedTo().toLowerCase() + ". You'll see every update here.", "Request sent", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        });
        JButton cancel = Ui.secondary("Cancel");
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(cancel);
        buttons.add(submit);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(Theme.CREAM);
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));
        root.add(Ui.title("New request"), BorderLayout.NORTH);
        JPanel pinned = new JPanel(new BorderLayout());
        pinned.setOpaque(false);
        pinned.add(form, BorderLayout.NORTH);
        root.add(pinned, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
        setSize(780, 660);
        setLocationRelativeTo(owner);
    }

    public WorkRequest created() {
        return created;
    }
}

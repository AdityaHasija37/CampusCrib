package com.campuscrib.ui;

import com.campuscrib.domain.Apartment;
import com.campuscrib.domain.ApartmentStatus;
import com.campuscrib.domain.Photo;
import com.campuscrib.domain.Role;
import com.campuscrib.domain.User;
import com.campuscrib.service.Services;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

/** Photos and details of one apartment, with the booking request form for students. */
public class ApartmentDialog extends JDialog {
    private final JLabel bigPhoto = new JLabel();

    public ApartmentDialog(Window owner, Services services, User viewer, long apartmentId, Runnable onBooked) {
        super(owner, ModalityType.APPLICATION_MODAL);
        Apartment a = services.listings.get(apartmentId);
        List<Photo> photos = services.listings.photos(apartmentId);
        setTitle(a.title());

        // Gallery: one big photo, thumbnails underneath swap it.
        bigPhoto.setIcon(Ui.image(photos.isEmpty() ? null : photos.get(0).image(), 540, 330));
        JPanel thumbs = new JPanel(new GridLayout(1, 0, 8, 0));
        thumbs.setOpaque(false);
        for (Photo p : photos) {
            JLabel t = new JLabel(Ui.image(p.image(), 126, 80));
            t.setToolTipText(p.caption());
            t.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            t.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    bigPhoto.setIcon(Ui.image(p.image(), 540, 330));
                }
            });
            thumbs.add(t);
        }
        JPanel gallery = Ui.column(bigPhoto, Ui.gap(8), thumbs);

        JLabel price = new JLabel("<html><span style='font-size:20px'><b>" + Ui.money(a.monthlyRent()) + "</b></span><span style='color:#6B625B'> / month</span></html>");
        JPanel facts = new JPanel(new GridLayout(2, 3, 12, 10));
        facts.setOpaque(false);
        facts.add(fact("Bedrooms", SearchScreen.beds(a)));
        facts.add(fact("Bathrooms", String.valueOf(a.bathrooms())));
        facts.add(fact("To campus", a.distanceKm() + " km"));
        facts.add(fact("Neighborhood", a.neighborhood()));
        facts.add(fact("Available", Ui.date(a.availableFrom())));
        facts.add(fact("Listed by", a.agentName()));
        JLabel description = Ui.paragraph(a.description(), 450);
        JLabel amenities = new JLabel(a.amenities().isEmpty() ? "No amenities listed" : "✓ " + String.join("    ✓ ", a.amenities()));
        amenities.setForeground(Theme.SAGE);
        amenities.setFont(Theme.sans(Font.BOLD, 13));

        JPanel details = Ui.column(
                Ui.eyebrow(a.code() + " · " + a.address()), Ui.gap(6), Ui.title(a.title()), Ui.gap(8), price, Ui.gap(16),
                facts, Ui.gap(16), description, Ui.gap(12), amenities);

        JPanel side = new JPanel(new BorderLayout());
        side.setOpaque(false);
        side.setPreferredSize(new Dimension(320, 0));
        side.add(viewer.role() == Role.STUDENT && a.status() == ApartmentStatus.LISTED ? bookingForm(services, viewer, a, onBooked) : statusNote(a), BorderLayout.NORTH);

        WidthTrackingPanel left = new WidthTrackingPanel(new BorderLayout());
        left.add(Ui.column(gallery, Ui.gap(20), details), BorderLayout.NORTH);
        JScrollPane scroll = Ui.scroll(left);
        scroll.setBorder(null);

        JPanel root = new JPanel(new BorderLayout(24, 0));
        root.setBackground(Theme.CREAM);
        root.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        root.add(scroll, BorderLayout.CENTER);
        root.add(side, BorderLayout.EAST);
        setContentPane(root);
        setSize(980, 760);
        setLocationRelativeTo(owner);
    }

    private JComponent bookingForm(Services services, User student, Apartment a, Runnable onBooked) {
        LocalDate earliest = a.availableFrom().isAfter(LocalDate.now()) ? a.availableFrom() : LocalDate.now().plusDays(7);
        JSpinner moveIn = Forms.datePicker(earliest);
        JSpinner months = new JSpinner(new SpinnerNumberModel(12, 1, 24, 1));
        JTextArea message = new JTextArea(5, 20);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        message.putClientProperty("JTextArea.placeholderText", "Introduce yourself: course, move-in plans, questions for the agent");
        JButton send = Ui.primary("Request to book");
        send.addActionListener(e -> {
            boolean ok = Ui.attempt(this, () -> services.bookings.request(student, a.id(), Forms.date(moveIn), (Integer) months.getValue(), message.getText()));
            if (ok) {
                JOptionPane.showMessageDialog(this, "Request sent to " + a.agentName() + ". You'll see their answer under My bookings.", "Request sent", JOptionPane.INFORMATION_MESSAGE);
                onBooked.run();
                dispose();
            }
        });
        JScrollPane messageScroll = new JScrollPane(message);
        JPanel form = Ui.column(
                Ui.heading("Request this apartment"), Ui.gap(4), mutedParagraph("Free to ask. The agent approves or declines.", 200), Ui.gap(16),
                Forms.field("Move-in date", moveIn),
                Forms.field("Lease length (months)", months),
                Forms.field("Message", messageScroll),
                send);
        for (JComponent c : new JComponent[] {moveIn, months, send}) c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return Ui.card(form);
    }

    private static JLabel mutedParagraph(String text, int width) {
        JLabel l = Ui.paragraph(text, width);
        l.setForeground(Theme.MUTED);
        return l;
    }

    private static JComponent statusNote(Apartment a) {
        String text = switch (a.status()) {
            case LEASED -> "This apartment has been leased.";
            case UNLISTED -> "This listing is off the market for now.";
            case LISTED -> "Students request this apartment from their search.";
        };
        return Ui.card(Ui.column(Ui.heading(a.status().toString()), Ui.gap(6), Ui.muted(text)));
    }

    private static JComponent fact(String label, String value) {
        JLabel l = new JLabel(label.toUpperCase());
        l.setFont(Theme.sans(Font.BOLD, 10));
        l.setForeground(Theme.MUTED);
        JLabel v = new JLabel(value);
        v.setFont(Theme.sans(Font.BOLD, 14));
        v.setForeground(Theme.INK);
        return Ui.column(l, Ui.gap(2), v);
    }
}

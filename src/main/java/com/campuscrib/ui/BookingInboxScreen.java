package com.campuscrib.ui;

import com.campuscrib.domain.BookingRequest;
import com.campuscrib.domain.BookingStatus;
import com.campuscrib.domain.User;
import com.campuscrib.service.Services;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** Agents: requests for their listings, pending first, with approve and decline. */
public class BookingInboxScreen implements Screen {
    private final Services services;
    private final User agent;
    private final ListTable<BookingRequest> table = new ListTable<>(
            new ListTable.Column<>("Student", 160, BookingRequest::studentName),
            new ListTable.Column<>("Apartment", 0, b -> b.apartmentCode() + " · " + b.apartmentTitle()),
            new ListTable.Column<>("Move-in", 110, b -> Ui.date(b.moveIn())),
            new ListTable.Column<>("Lease", 90, b -> b.leaseMonths() + " months"),
            new ListTable.Column<>("Status", 120, b -> Ui.badge(b.status())));
    private final JLabel who = Ui.heading("");
    private final JLabel when = Ui.muted("");
    private final JLabel message = Ui.paragraph("", 290);
    private final JTextArea note = new JTextArea(3, 20);
    private final JButton approve = Ui.primary("Approve and lease");
    private final JButton decline = Ui.secondary("Decline");
    private final JPanel actions;
    private final JPanel body = new JPanel(new BorderLayout(20, 0));
    private final JComponent view;

    public BookingInboxScreen(Services services, User agent) {
        this.services = services;
        this.agent = agent;
        body.setOpaque(false);
        note.setLineWrap(true);
        note.setWrapStyleWord(true);
        note.putClientProperty("JTextArea.placeholderText", "Note to the student (required to decline)");
        approve.addActionListener(e -> decide(true));
        decline.addActionListener(e -> decide(false));
        actions = Ui.column(Forms.label("Your reply"), Ui.gap(6), Forms.fixedHeight(new JScrollPane(note)), Ui.gap(12), Ui.row(8, approve, decline),
                Ui.gap(8), Ui.muted("<html>Approving leases the apartment and declines<br>any other pending requests for it.</html>"));
        table.onSelect(this::show);
        view = Ui.screen("Booking requests", "Students asking for your listings. Pending requests are at the top.", null, body);
    }

    @Override
    public String label() {
        return "Booking requests";
    }

    @Override
    public JComponent view() {
        return view;
    }

    @Override
    public void refresh() {
        List<BookingRequest> inbox = Ui.fetch(view, () -> services.bookings.inbox(agent));
        if (inbox == null) return;
        table.setRows(inbox);
        body.removeAll();
        if (inbox.isEmpty()) {
            body.add(Ui.empty("No requests yet.", "When students request one of your listings, it shows up here."), BorderLayout.NORTH);
        } else {
            body.add(table.view(), BorderLayout.CENTER);
            RoundedPanel detail = Ui.card(Ui.column(who, Ui.gap(4), when, Ui.gap(14), message, Ui.gap(18), actions));
            detail.setPreferredSize(new Dimension(360, 0));
            body.add(detail, BorderLayout.EAST);
            if (table.selected().isEmpty()) table.select(0);
            show(table.selected());
        }
        body.revalidate();
        body.repaint();
    }

    private void show(Optional<BookingRequest> selected) {
        selected.ifPresentOrElse(b -> {
            who.setText(b.studentName());
            when.setText("Asked " + Ui.ago(b.createdAt()) + " for " + b.apartmentCode() + ", moving in " + Ui.date(b.moveIn()));
            String text = b.message().isBlank() ? "No message." : "\"" + b.message() + "\"";
            boolean pending = b.status() == BookingStatus.PENDING;
            actions.setVisible(pending);
            if (!pending) text += "\n\nYou " + b.status().toString().toLowerCase() + " this request" + (b.agentNote().isBlank() ? "." : ": \"" + b.agentNote() + "\"");
            Ui.setParagraph(message, text, 290);
            note.setText("");
        }, () -> who.setText("Select a request"));
    }

    private void decide(boolean yes) {
        table.selected().ifPresent(b -> {
            if (Ui.attempt(view, () -> services.bookings.decide(agent, b.id(), yes, note.getText()))) refresh();
        });
    }
}

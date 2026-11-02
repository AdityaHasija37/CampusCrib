package com.campuscrib.ui;

import com.campuscrib.domain.BookingRequest;
import com.campuscrib.domain.BookingStatus;
import com.campuscrib.domain.User;
import com.campuscrib.service.Services;
import java.awt.BorderLayout;
import java.util.List;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;

/** A student's booking requests and what each agent said. */
public class MyBookingsScreen implements Screen {
    private final Services services;
    private final User student;
    private final ListTable<BookingRequest> table = new ListTable<>(
            new ListTable.Column<>("Apartment", 0, b -> b.apartmentCode() + " · " + b.apartmentTitle()),
            new ListTable.Column<>("Move-in", 120, b -> Ui.date(b.moveIn())),
            new ListTable.Column<>("Lease", 90, b -> b.leaseMonths() + " months"),
            new ListTable.Column<>("Sent", 120, b -> Ui.ago(b.createdAt())),
            new ListTable.Column<>("Status", 130, b -> Ui.badge(b.status())));
    private final JLabel detail = Ui.paragraph("", 700);
    private final JButton withdraw = Ui.danger("Withdraw request");
    private final JPanel body = new JPanel(new BorderLayout(0, 16));
    private final JComponent view;

    public MyBookingsScreen(Services services, User student) {
        this.services = services;
        this.student = student;
        body.setOpaque(false);
        table.onSelect(this::show);
        withdraw.addActionListener(e -> table.selected().ifPresent(b -> {
            if (JOptionPane.showConfirmDialog(body, "Withdraw your request for " + b.apartmentTitle() + "?", "Withdraw", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION
                    && Ui.attempt(body, () -> services.bookings.withdraw(student, b.id()))) {
                refresh();
            }
        }));
        view = Ui.screen("My bookings", "Your apartment requests and the agents' replies.", null, body);
        show(Optional.empty());
    }

    @Override
    public String label() {
        return "My bookings";
    }

    @Override
    public JComponent view() {
        return view;
    }

    @Override
    public void refresh() {
        List<BookingRequest> mine = Ui.fetch(view, () -> services.bookings.mine(student));
        if (mine == null) return;
        table.setRows(mine);
        body.removeAll();
        if (mine.isEmpty()) {
            body.add(Ui.empty("No requests yet.", "Find a place you like and send the agent a request."), BorderLayout.NORTH);
        } else {
            body.add(table.view(), BorderLayout.CENTER);
            body.add(Ui.card(Ui.column(detail, Ui.gap(12), Ui.row(0, withdraw))), BorderLayout.SOUTH);
            if (table.selected().isEmpty()) table.select(0);
        }
        body.revalidate();
        body.repaint();
    }

    private void show(Optional<BookingRequest> b) {
        withdraw.setVisible(b.map(x -> x.status() == BookingStatus.PENDING).orElse(false));
        Ui.setParagraph(detail, b.map(x -> {
            String yours = x.message().isBlank() ? "You didn't add a message." : "You wrote: \"" + x.message() + "\"";
            String reply = switch (x.status()) {
                case PENDING -> "Waiting for the agent to reply.";
                case WITHDRAWN -> "You withdrew this request.";
                default -> x.agentNote().isBlank() ? "The agent " + x.status().toString().toLowerCase() + " this request." : "Agent's reply: \"" + x.agentNote() + "\"";
            };
            return yours + "\n\n" + reply;
        }).orElse("Select a request to see the details."), 700);
    }
}

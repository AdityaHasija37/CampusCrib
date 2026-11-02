package com.campuscrib.ui;

import com.campuscrib.domain.RequestCategory;
import com.campuscrib.domain.User;
import com.campuscrib.domain.WorkRequest;
import com.campuscrib.service.Services;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Building requests, seen three ways: a resident's own requests, a service crew's queue for its
 * trade, and building management's view of everything.
 */
public class RequestsScreen implements Screen {
    public enum Mode { RESIDENT, PROVIDER, MANAGER }

    private final Services services;
    private final User user;
    private final Mode mode;
    private final ListTable<WorkRequest> table = new ListTable<>(
            new ListTable.Column<>("Request", 0, WorkRequest::title),
            new ListTable.Column<>("Type · unit", 150, r -> r.subject() + " · " + r.unit()),
            new ListTable.Column<>("Priority", 92, r -> Ui.badge(r.priority())),
            new ListTable.Column<>("Status", 118, r -> Ui.badge(r.status())));
    private final RequestDetailPanel detail;
    private final JCheckBox showClosed = new JCheckBox("Show closed");
    private final JComboBox<String> category = new JComboBox<>(new String[] {"All types", "Maintenance", "Amenity bookings", "Inquiries"});
    private final JPanel body = new JPanel(new BorderLayout(20, 0));
    private final JComponent view;

    public RequestsScreen(Services services, User user, Mode mode) {
        this.services = services;
        this.user = user;
        this.mode = mode;
        this.detail = new RequestDetailPanel(services, user, this::refresh);
        body.setOpaque(false);
        showClosed.setOpaque(false);
        showClosed.setSelected(mode == Mode.RESIDENT);
        showClosed.addActionListener(e -> refresh());
        category.addActionListener(e -> refresh());
        table.onSelect(s -> s.ifPresentOrElse(detail::show, detail::clear));

        JComponent actions;
        if (mode == Mode.RESIDENT) {
            JButton add = Ui.primary("New request");
            add.addActionListener(e -> {
                NewRequestDialog d = new NewRequestDialog(SwingUtilities.getWindowAncestor(body), services, user);
                d.setVisible(true);
                refresh();
                if (d.created() != null) table.selectWhere(r -> r.id() == d.created().id());
            });
            actions = add;
        } else if (mode == Mode.MANAGER) {
            actions = Ui.row(12, category, showClosed);
        } else {
            actions = showClosed;
        }
        view = Ui.screen(title(), subtitle(), actions, body);
    }

    @Override
    public String label() {
        return switch (mode) {
            case RESIDENT -> "My requests";
            case PROVIDER -> "Work queue";
            case MANAGER -> "All requests";
        };
    }

    private String title() {
        return switch (mode) {
            case RESIDENT -> "My requests";
            case PROVIDER -> user.role().organization();
            case MANAGER -> "Building requests";
        };
    }

    private String subtitle() {
        return switch (mode) {
            case RESIDENT -> "Maintenance, amenity bookings and questions for unit " + user.unit() + ".";
            case PROVIDER -> "New " + user.role().trade().orElseThrow().displayName().toLowerCase() + " requests, and the jobs you've taken.";
            case MANAGER -> "Everything residents have raised. Urgent and open requests first.";
        };
    }

    @Override
    public JComponent view() {
        return view;
    }

    @Override
    public void refresh() {
        List<WorkRequest> list = Ui.fetch(view, () -> switch (mode) {
            case RESIDENT -> services.requests.mine(user);
            case PROVIDER -> services.requests.queue(user);
            case MANAGER -> services.requests.all(user);
        });
        if (list == null) return;
        RequestCategory only = switch (category.getSelectedIndex()) {
            case 1 -> RequestCategory.MAINTENANCE;
            case 2 -> RequestCategory.AMENITY;
            case 3 -> RequestCategory.INQUIRY;
            default -> null;
        };
        List<WorkRequest> shown = list.stream()
                .filter(r -> showClosed.isSelected() || !r.status().isClosed())
                .filter(r -> only == null || r.category() == only)
                .toList();
        table.setRows(shown);
        body.removeAll();
        if (shown.isEmpty()) {
            body.add(Ui.empty(emptyTitle(), emptyDetail()), BorderLayout.NORTH);
        } else {
            body.add(table.view(), BorderLayout.CENTER);
            body.add(detail, BorderLayout.EAST);
            if (table.selected().isEmpty()) table.select(0);
            table.selected().ifPresent(detail::show);
        }
        body.revalidate();
        body.repaint();
    }

    private String emptyTitle() {
        return switch (mode) {
            case RESIDENT -> "No requests yet.";
            case PROVIDER -> "Your queue is clear.";
            case MANAGER -> "Nothing open right now.";
        };
    }

    private String emptyDetail() {
        return switch (mode) {
            case RESIDENT -> "Report something broken, book a shared space or ask building management a question.";
            case PROVIDER -> "New requests for your trade will show up here.";
            case MANAGER -> "Tick Show closed to see finished requests.";
        };
    }
}

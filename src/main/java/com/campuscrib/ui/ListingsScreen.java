package com.campuscrib.ui;

import com.campuscrib.domain.Apartment;
import com.campuscrib.domain.ApartmentStatus;
import com.campuscrib.domain.User;
import com.campuscrib.service.Services;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Agents: every listing they own, with create, edit, list/unlist and delete. */
public class ListingsScreen implements Screen {
    private final Services services;
    private final User agent;
    private final ListTable<Apartment> table = new ListTable<>(
            new ListTable.Column<>("Code", 80, Apartment::code),
            new ListTable.Column<>("Title", 0, Apartment::title),
            new ListTable.Column<>("Neighborhood", 140, Apartment::neighborhood),
            new ListTable.Column<>("Rent", 100, a -> Ui.money(a.monthlyRent())),
            new ListTable.Column<>("Beds", 70, a -> SearchScreen.beds(a)),
            new ListTable.Column<>("Status", 110, a -> statusBadge(a.status())));
    private final JButton edit = Ui.secondary("Edit");
    private final JButton toggle = Ui.secondary("Unlist");
    private final JButton preview = Ui.secondary("Preview");
    private final JButton delete = Ui.danger("Delete");
    private final JPanel body = new JPanel(new BorderLayout(0, 14));
    private final JComponent view;

    public ListingsScreen(Services services, User agent) {
        this.services = services;
        this.agent = agent;
        body.setOpaque(false);
        JButton add = Ui.primary("New listing");
        add.addActionListener(e -> open(null));
        edit.addActionListener(e -> table.selected().ifPresent(a -> open(a.id())));
        preview.addActionListener(e -> table.selected().ifPresent(a ->
                new ApartmentDialog(SwingUtilities.getWindowAncestor(body), services, agent, a.id(), () -> {}).setVisible(true)));
        toggle.addActionListener(e -> table.selected().ifPresent(a -> {
            if (Ui.attempt(body, () -> services.listings.setListed(agent, a.id(), a.status() != ApartmentStatus.LISTED))) refresh();
        }));
        delete.addActionListener(e -> table.selected().ifPresent(a -> {
            int ok = JOptionPane.showConfirmDialog(body, "Delete " + a.code() + " and its photos and booking requests? This can't be undone.", "Delete listing", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok == JOptionPane.YES_OPTION && Ui.attempt(body, () -> services.listings.delete(agent, a.id()))) refresh();
        }));
        table.onSelect(this::updateButtons);
        view = Ui.screen("My listings", "Apartments you manage. Students only see listed ones.", add, body);
    }

    @Override
    public String label() {
        return "My listings";
    }

    @Override
    public JComponent view() {
        return view;
    }

    @Override
    public void refresh() {
        List<Apartment> mine = Ui.fetch(view, () -> services.listings.myListings(agent));
        if (mine == null) return;
        table.setRows(mine);
        body.removeAll();
        if (mine.isEmpty()) {
            body.add(Ui.empty("No listings yet.", "Add your first apartment with New listing."), BorderLayout.NORTH);
        } else {
            body.add(table.view(), BorderLayout.CENTER);
            body.add(Ui.row(8, preview, edit, toggle, delete), BorderLayout.SOUTH);
            if (table.selected().isEmpty()) table.select(0);
        }
        updateButtons(table.selected());
        body.revalidate();
        body.repaint();
    }

    private void updateButtons(Optional<Apartment> a) {
        boolean any = a.isPresent();
        for (JButton b : new JButton[] {edit, toggle, preview, delete}) b.setEnabled(any);
        toggle.setEnabled(any && a.get().status() != ApartmentStatus.LEASED);
        toggle.setText(a.map(x -> x.status() == ApartmentStatus.LISTED ? "Unlist" : "List again").orElse("Unlist"));
    }

    private void open(Long id) {
        ListingDialog d = new ListingDialog(SwingUtilities.getWindowAncestor(view), services, agent, id);
        d.setVisible(true);
        refresh();
        if (d.savedId() != null) table.selectWhere(a -> a.id() == d.savedId());
    }

    static JLabel statusBadge(ApartmentStatus s) {
        Color[] c = switch (s) {
            case LISTED -> new Color[] {Theme.SAGE, Theme.SAGE_SOFT};
            case LEASED -> new Color[] {Theme.SLATE, Theme.SLATE_SOFT};
            case UNLISTED -> new Color[] {Theme.MUTED, Theme.SAND};
        };
        return Ui.badge(s.toString(), c[0], c[1]);
    }
}

package com.campuscrib.ui;

import com.campuscrib.domain.RequestCategory;
import com.campuscrib.domain.Role;
import com.campuscrib.domain.User;
import com.campuscrib.service.ReportService;
import com.campuscrib.service.Services;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Building management's numbers: open work, turnaround by trade, and occupancy. */
public class ReportsScreen implements Screen {
    private final Services services;
    private final User user;
    private final JPanel body = new JPanel(new BorderLayout(0, 20));
    private final JComponent view;

    public ReportsScreen(Services services, User user) {
        this.services = services;
        this.user = user;
        body.setOpaque(false);
        view = Ui.screen("Reports", "How the building is doing: open work, turnaround by trade and occupancy.", null, Ui.scroll(body));
    }

    @Override
    public String label() {
        return "Reports";
    }

    @Override
    public JComponent view() {
        return view;
    }

    @Override
    public void refresh() {
        ReportService.Summary s = Ui.fetch(view, () -> services.summaryFor(user));
        if (s == null) return;
        body.removeAll();

        JPanel tiles = new JPanel(new GridLayout(1, 4, 16, 0));
        tiles.setOpaque(false);
        tiles.add(tile(String.valueOf(s.openRequests()), "open requests", Theme.INK));
        tiles.add(tile(String.valueOf(s.urgentOpen()), "urgent and open", s.urgentOpen() > 0 ? Theme.BRICK : Theme.SAGE));
        tiles.add(tile(String.valueOf(s.totalRequests()), "requests all time", Theme.INK));
        long units = s.listed() + s.leased() + s.unlisted();
        tiles.add(tile(units == 0 ? "–" : Math.round(100.0 * s.leased() / units) + "%", "of listings leased", Theme.SAGE));

        ListTable<ReportService.TradeRow> trades = new ListTable<>(
                new ListTable.Column<>("Trade", 0, t -> t.trade().displayName()),
                new ListTable.Column<>("Crew", 0, t -> Role.forTrade(t.trade()).organization()),
                new ListTable.Column<>("Open", 90, t -> t.open()),
                new ListTable.Column<>("Completed", 110, t -> t.completed()),
                new ListTable.Column<>("Avg. time to complete", 190, t -> t.averageHoursToComplete().isPresent() ? hours(t.averageHoursToComplete().getAsDouble()) : "–"));
        trades.setRows(s.trades());
        JComponent tradesView = trades.view();

        JPanel lower = new JPanel(new GridLayout(1, 2, 16, 0));
        lower.setOpaque(false);
        lower.add(Ui.card(Ui.column(Ui.heading("Open by type"), Ui.gap(14),
                bar("Maintenance", s.openByCategory().get(RequestCategory.MAINTENANCE), s.openRequests(), Theme.BRICK),
                bar("Amenity bookings", s.openByCategory().get(RequestCategory.AMENITY), s.openRequests(), Theme.SAGE),
                bar("Inquiries", s.openByCategory().get(RequestCategory.INQUIRY), s.openRequests(), Theme.SLATE))));
        lower.add(Ui.card(Ui.column(Ui.heading("Listings"), Ui.gap(14),
                bar("Listed", s.listed(), units, Theme.SAGE),
                bar("Leased", s.leased(), units, Theme.SLATE),
                bar("Unlisted", s.unlisted(), units, Theme.MUTED))));

        tradesView.setPreferredSize(new Dimension(0, 4 * 40 + 46));
        tradesView.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4 * 40 + 46));
        body.add(Ui.column(tiles, Ui.gap(24), Ui.heading("Maintenance by trade"), Ui.gap(10), tradesView, Ui.gap(24), lower), BorderLayout.NORTH);
        body.revalidate();
        body.repaint();
    }

    static String hours(double h) {
        return h < 24 ? Math.round(h) + " h" : String.format("%.1f days", h / 24);
    }

    private static JComponent tile(String value, String label, Color color) {
        JLabel v = new JLabel(value);
        v.setFont(Theme.serif(34));
        v.setForeground(color);
        return Ui.card(Ui.column(v, Ui.gap(2), Ui.muted(label)));
    }

    private static JComponent bar(String label, long value, long total, Color color) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        JLabel name = new JLabel(label);
        name.setPreferredSize(new Dimension(130, 22));
        JLabel count = new JLabel(String.valueOf(value));
        count.setFont(Theme.sans(Font.BOLD, 13));
        count.setPreferredSize(new Dimension(30, 22));
        double share = total == 0 ? 0 : (double) value / total;
        JComponent track = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int y = getHeight() / 2 - 5;
                g2.setColor(Theme.SAND);
                g2.fillRoundRect(0, y, getWidth(), 10, 10, 10);
                g2.setColor(color);
                g2.fillRoundRect(0, y, (int) Math.round(getWidth() * share), 10, 10, 10);
                g2.dispose();
            }
        };
        row.add(name, BorderLayout.WEST);
        row.add(track, BorderLayout.CENTER);
        row.add(count, BorderLayout.EAST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return Ui.column(row, Ui.gap(8));
    }
}

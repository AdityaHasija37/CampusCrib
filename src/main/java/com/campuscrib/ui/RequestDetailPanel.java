package com.campuscrib.ui;

import com.campuscrib.domain.RequestCategory;
import com.campuscrib.domain.RequestEvent;
import com.campuscrib.domain.RequestStatus;
import com.campuscrib.domain.Role;
import com.campuscrib.domain.User;
import com.campuscrib.domain.WorkRequest;
import com.campuscrib.service.Services;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** One request: what it is, where it's routed, its full history, and the actions this user may take. */
public class RequestDetailPanel extends RoundedPanel {
    private final Services services;
    private final User user;
    private final Runnable onChanged;

    public RequestDetailPanel(Services services, User user, Runnable onChanged) {
        super(new BorderLayout(), Theme.SURFACE, Theme.LINE, 18);
        this.services = services;
        this.user = user;
        this.onChanged = onChanged;
        setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));
        setPreferredSize(new Dimension(380, 0));
        clear();
    }

    public void clear() {
        removeAll();
        add(Ui.column(Ui.heading("Select a request"), Ui.gap(6), Ui.muted("Its details and history appear here.")), BorderLayout.NORTH);
        revalidate();
        repaint();
    }

    public void show(WorkRequest r) {
        removeAll();
        JPanel badges = Ui.row(6, Ui.badge(r.status()), Ui.badge(r.priority()));
        JLabel title = new JLabel("<html>" + Shell.escape(r.title()) + "</html>");
        title.setFont(Theme.serif(21));
        title.setForeground(Theme.INK);

        JPanel facts = Ui.column(
                fact("Unit", r.unit() + " · " + r.residentName()),
                fact("Routed to", r.routedTo()),
                r.category() == RequestCategory.AMENITY ? fact("When", Ui.dateTime(r.slotStart()) + " for " + r.slotHours() + (r.slotHours() == 1 ? " hour" : " hours")) : Ui.gap(0),
                fact("Raised", Ui.ago(r.createdAt())));

        JLabel description = Ui.paragraph(r.description(), 290);
        JPanel content = Ui.column(
                Ui.eyebrow(r.category() + " · " + r.subject()), Ui.gap(8), title, Ui.gap(10), badges, Ui.gap(16),
                facts, Ui.gap(12), description, Ui.gap(20),
                Forms.label("History"), Ui.gap(10), timeline(services.requests.history(r.id())));

        JComponent actions = actions(r);
        if (actions != null) {
            content.add(Ui.gap(18));
            content.add(actions);
        }
        WidthTrackingPanel top = new WidthTrackingPanel(new BorderLayout());
        top.add(content, BorderLayout.NORTH);
        JScrollPane scroll = Ui.scroll(top);
        scroll.getViewport().setBackground(Theme.SURFACE);
        add(scroll, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JComponent actions(WorkRequest r) {
        Set<RequestStatus> next = services.requests.allowedNext(user, r);
        boolean canAssign = user.role() == Role.BUILDING_MANAGER && r.category() == RequestCategory.MAINTENANCE && r.status() == RequestStatus.SUBMITTED;
        if (next.isEmpty() && !canAssign) return null;

        JTextArea note = new JTextArea(3, 20);
        note.setLineWrap(true);
        note.setWrapStyleWord(true);
        note.putClientProperty("JTextArea.placeholderText", notePrompt(next));
        JPanel buttons = Ui.row(8);
        // Forward moves first and filled; cancel last and quiet.
        for (RequestStatus to : order(next)) {
            JButton b = to == RequestStatus.CANCELLED || to == RequestStatus.DECLINED ? Ui.secondary(verb(to, r)) : Ui.primary(verb(to, r));
            b.addActionListener(e -> {
                if (Ui.attempt(this, () -> services.requests.move(user, r.id(), to, note.getText()))) onChanged.run();
            });
            buttons.add(b);
        }
        JPanel box = Ui.column(Forms.label("Update"), Ui.gap(6), Forms.fixedHeight(new JScrollPane(note)), Ui.gap(10), buttons);
        if (canAssign) {
            List<User> crew = services.requests.crewFor(r);
            JComboBox<String> who = new JComboBox<>(crew.stream().map(User::fullName).toArray(String[]::new));
            JButton assign = Ui.secondary("Assign");
            assign.setEnabled(!crew.isEmpty());
            assign.addActionListener(e -> {
                int i = who.getSelectedIndex();
                if (i >= 0 && Ui.attempt(this, () -> services.requests.assign(user, r.id(), crew.get(i).id()))) onChanged.run();
            });
            box.add(Ui.gap(14));
            box.add(Forms.label("Or assign to " + r.trade().displayName().toLowerCase()));
            box.add(Ui.gap(6));
            box.add(Ui.row(8, who, assign));
        }
        return box;
    }

    private static List<RequestStatus> order(Set<RequestStatus> next) {
        return next.stream().sorted((a, b) -> Integer.compare(rank(a), rank(b))).toList();
    }

    private static int rank(RequestStatus s) {
        return switch (s) {
            case CANCELLED -> 3;
            case DECLINED -> 2;
            default -> 1;
        };
    }

    static String verb(RequestStatus to, WorkRequest r) {
        return switch (to) {
            case ACCEPTED -> "Accept job";
            case IN_PROGRESS -> "Start work";
            case COMPLETED -> "Mark completed";
            case APPROVED -> "Approve booking";
            case DECLINED -> "Decline";
            case ANSWERED -> "Send answer";
            case CANCELLED -> r.category() == RequestCategory.MAINTENANCE ? "Cancel request" : "Cancel";
            case SUBMITTED -> "Submit";
        };
    }

    private static String notePrompt(Set<RequestStatus> next) {
        if (next.contains(RequestStatus.ANSWERED)) return "Write the answer for the resident";
        if (next.contains(RequestStatus.COMPLETED)) return "What was done? (required to complete)";
        if (next.contains(RequestStatus.APPROVED)) return "Note to the resident (required to decline)";
        return "Optional note for the resident";
    }

    private static JComponent fact(String label, String value) {
        JLabel l = new JLabel("<html><span style='color:#6B625B'>" + label + "</span>&nbsp;&nbsp;<b>" + Shell.escape(value) + "</b></html>");
        l.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        return l;
    }

    private static JComponent timeline(List<RequestEvent> events) {
        JPanel list = Ui.column();
        for (int i = 0; i < events.size(); i++) {
            RequestEvent e = events.get(i);
            boolean last = i == events.size() - 1;
            Color dot = Ui.colors(e.toStatus())[0];
            JPanel item = new JPanel(new BorderLayout(12, 0));
            item.setOpaque(false);
            item.add(new Dot(dot, !last), BorderLayout.WEST);
            JLabel head = new JLabel("<html><b>" + e.toStatus() + "</b> <span style='color:#6B625B'>by " + Shell.escape(e.actorName()) + " · " + Ui.ago(e.createdAt()) + "</span></html>");
            JPanel text = Ui.column(head);
            if (!e.note().isBlank()) {
                JLabel note = Ui.paragraph(e.note(), 260);
                note.setFont(Theme.sans(Font.ITALIC, 13));
                note.setForeground(Theme.MUTED);
                text.add(Ui.gap(2));
                text.add(note);
            }
            text.setBorder(BorderFactory.createEmptyBorder(0, 0, last ? 0 : 14, 0));
            item.add(text, BorderLayout.CENTER);
            list.add(item);
        }
        return list;
    }

    /** A status dot with the line down to the next event. */
    private static final class Dot extends JComponent {
        private final Color color;
        private final boolean line;

        Dot(Color color, boolean line) {
            this.color = color;
            this.line = line;
            setPreferredSize(new Dimension(14, 20));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (line) {
                g2.setColor(Theme.LINE);
                g2.fillRect(6, 12, 2, getHeight());
            }
            g2.setColor(color);
            g2.fillOval(1, 3, 12, 12);
            g2.dispose();
        }
    }
}

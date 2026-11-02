package com.campuscrib.ui;

import com.campuscrib.domain.User;
import com.campuscrib.service.Services;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** The signed-in window: a dark sidebar with the screens this role can use, and the current screen. */
public class Shell extends JPanel {
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final List<Screen> screens;
    private final List<JButton> navButtons = new ArrayList<>();

    public Shell(Services services, User user, Runnable onSignOut) {
        super(new BorderLayout());
        this.screens = screensFor(services, user);
        content.setBackground(Theme.CREAM);
        for (Screen s : screens) content.add(s.view(), s.label());
        add(sidebar(services, user, onSignOut), BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
        show(0);
    }

    static List<Screen> screensFor(Services s, User u) {
        List<Screen> list = new ArrayList<>();
        switch (u.role()) {
            case STUDENT -> {
                list.add(new SearchScreen(s, u));
                list.add(new MyBookingsScreen(s, u));
            }
            case AGENT -> {
                list.add(new BookingInboxScreen(s, u));
                list.add(new ListingsScreen(s, u));
            }
            case RESIDENT -> list.add(new RequestsScreen(s, u, RequestsScreen.Mode.RESIDENT));
            case BUILDING_MANAGER -> {
                list.add(new RequestsScreen(s, u, RequestsScreen.Mode.MANAGER));
                list.add(new ReportsScreen(s, u));
            }
            case ADMIN -> {
                list.add(new PeopleScreen(s, u));
                list.add(new ReportsScreen(s, u));
            }
            default -> list.add(new RequestsScreen(s, u, RequestsScreen.Mode.PROVIDER));
        }
        return list;
    }

    public void show(int index) {
        Screen s = screens.get(index);
        s.refresh();
        cards.show(content, s.label());
        for (int i = 0; i < navButtons.size(); i++) styleNav(navButtons.get(i), i == index);
    }

    /** For screenshots and tests: show a screen by its sidebar label. */
    public void show(String label) {
        for (int i = 0; i < screens.size(); i++) {
            if (screens.get(i).label().equals(label)) show(i);
        }
    }

    public List<Screen> screens() {
        return screens;
    }

    private JComponent sidebar(Services services, User user, Runnable onSignOut) {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(Theme.SIDEBAR);
        side.setPreferredSize(new Dimension(236, 0));
        side.setBorder(BorderFactory.createEmptyBorder(24, 16, 20, 16));

        side.add(Brand.wordmark(Color.WHITE));
        side.add(Box.createRigidArea(new Dimension(0, 28)));

        JLabel section = new JLabel(user.role().enterprise().displayName().toUpperCase());
        section.setFont(Theme.sans(Font.BOLD, 10));
        section.setForeground(new Color(0xC9B8A6));
        section.setBorder(BorderFactory.createEmptyBorder(0, 10, 8, 0));
        side.add(section);

        for (int i = 0; i < screens.size(); i++) {
            int index = i;
            JButton b = new JButton(screens.get(i).label());
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setContentAreaFilled(true);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            b.setAlignmentX(LEFT_ALIGNMENT);
            b.putClientProperty("JButton.buttonType", "roundRect");
            b.addActionListener(e -> show(index));
            navButtons.add(b);
            side.add(b);
            side.add(Box.createRigidArea(new Dimension(0, 4)));
        }

        side.add(Box.createVerticalGlue());

        JPanel who = new JPanel(new BorderLayout(10, 0));
        who.setOpaque(false);
        who.setAlignmentX(LEFT_ALIGNMENT);
        who.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        who.add(Avatar.of(user.fullName(), 36, Theme.BRICK), BorderLayout.WEST);
        JLabel name = new JLabel("<html><b>" + escape(user.fullName()) + "</b><br><span style='color:#C9B8A6'>" + escape(user.role().displayName())
                + (user.unit() != null ? " · " + escape(user.unit()) : "") + "</span></html>");
        name.setForeground(Color.WHITE);
        who.add(name, BorderLayout.CENTER);
        side.add(who);
        side.add(Box.createRigidArea(new Dimension(0, 12)));

        JPanel actions = new JPanel(new java.awt.GridLayout(1, 2, 8, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(LEFT_ALIGNMENT);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JButton account = sidebarSmall("Password");
        account.addActionListener(e -> new PasswordDialog(this, services, user).setVisible(true));
        JButton signOut = sidebarSmall("Sign out");
        signOut.addActionListener(e -> onSignOut.run());
        actions.add(account);
        actions.add(signOut);
        side.add(actions);
        return side;
    }

    private static JButton sidebarSmall(String text) {
        JButton b = new JButton(text);
        b.setFont(Theme.sans(Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(0x3A332F));
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        return b;
    }

    private static void styleNav(JButton b, boolean active) {
        b.setFont(Theme.sans(active ? Font.BOLD : Font.PLAIN, 14));
        b.setForeground(active ? Color.WHITE : new Color(0xE6DACD));
        b.setBackground(active ? Theme.BRICK : Theme.SIDEBAR);
    }

    static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

}

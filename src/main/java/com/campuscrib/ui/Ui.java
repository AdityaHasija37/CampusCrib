package com.campuscrib.ui;

import com.campuscrib.db.DataAccessException;
import com.campuscrib.domain.BookingStatus;
import com.campuscrib.domain.Priority;
import com.campuscrib.domain.RequestStatus;
import com.campuscrib.service.AccessDeniedException;
import com.campuscrib.service.ValidationException;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;

/** Small building blocks so every screen looks and behaves the same. */
public final class Ui {
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(Locale.US);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("d MMM, HH:mm");

    static {
        MONEY.setMaximumFractionDigits(0);
    }

    private Ui() {}

    /* ---------------- Text ---------------- */

    public static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.serif(26));
        l.setForeground(Theme.INK);
        return l;
    }

    public static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.sans(Font.BOLD, 16));
        l.setForeground(Theme.INK);
        return l;
    }

    public static JLabel muted(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Theme.MUTED);
        return l;
    }

    public static JLabel eyebrow(String text) {
        JLabel l = new JLabel(text.toUpperCase(Locale.ROOT));
        l.setFont(Theme.sans(Font.BOLD, 11));
        l.setForeground(Theme.BRICK);
        return l;
    }

    /** Text that wraps at a fixed width, so it never stretches a layout. */
    public static JLabel paragraph(String text, int width) {
        JLabel l = new JLabel(wrapHtml(text, width));
        l.setForeground(Theme.INK);
        l.setFont(Theme.sans(Font.PLAIN, 14));
        return l;
    }

    public static void setParagraph(JLabel label, String text, int width) {
        label.setText(wrapHtml(text, width));
    }

    private static String wrapHtml(String text, int width) {
        String safe = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
        return "<html><div style='width:" + width + "px'>" + safe + "</div></html>";
    }

    /* ---------------- Buttons ---------------- */

    public static JButton primary(String text) {
        JButton b = new JButton(text);
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.setBackground(Theme.BRICK);
        b.setForeground(Color.WHITE);
        b.setFont(Theme.sans(Font.BOLD, 14));
        b.setMargin(new Insets(8, 18, 8, 18));
        b.setFocusPainted(false);
        return b;
    }

    public static JButton secondary(String text) {
        JButton b = new JButton(text);
        b.setBackground(Theme.SURFACE);
        b.setForeground(Theme.INK);
        b.setFont(Theme.sans(Font.BOLD, 13));
        b.setMargin(new Insets(7, 16, 7, 16));
        b.setFocusPainted(false);
        return b;
    }

    public static JButton danger(String text) {
        JButton b = secondary(text);
        b.setForeground(Theme.BRICK_DARK);
        return b;
    }

    /* ---------------- Surfaces ---------------- */

    public static Border roundedBorder(Color color, int arc) {
        return new RoundBorder(color, arc);
    }

    /** A white card with a soft border. */
    public static RoundedPanel card(JComponent content) {
        RoundedPanel p = new RoundedPanel(new BorderLayout(), Theme.SURFACE, Theme.LINE, 18);
        p.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        p.add(content, BorderLayout.NORTH);
        return p;
    }

    public static JPanel column(Component... parts) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        for (Component c : parts) {
            if (c instanceof JComponent jc) jc.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(c);
        }
        return p;
    }

    public static JPanel row(int gap, Component... parts) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, gap, 0)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        p.setOpaque(false);
        for (Component c : parts) p.add(c);
        return p;
    }

    public static Component gap(int h) {
        return Box.createRigidArea(new Dimension(0, h));
    }

    public static JScrollPane scroll(Component c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(null);
        s.getViewport().setBackground(Theme.CREAM);
        s.getVerticalScrollBar().setUnitIncrement(16);
        return s;
    }

    /** The standard screen layout: title and subtitle on top, then the body. */
    public static JPanel screen(String title, String subtitle, JComponent actions, JComponent body) {
        JPanel head = new JPanel(new BorderLayout(16, 0));
        head.setOpaque(false);
        head.add(column(title(title), gap(4), muted(subtitle)), BorderLayout.CENTER);
        if (actions != null) {
            JPanel a = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
            a.setOpaque(false);
            a.add(actions);
            head.add(a, BorderLayout.EAST);
        }
        JPanel p = new JPanel(new BorderLayout(0, 20));
        p.setBackground(Theme.CREAM);
        p.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        p.add(head, BorderLayout.NORTH);
        p.add(body, BorderLayout.CENTER);
        return p;
    }

    public static JComponent empty(String title, String detail) {
        RoundedPanel p = new RoundedPanel(new BorderLayout(), Theme.SAND, Theme.LINE, 18);
        p.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        p.add(column(heading(title), gap(6), muted(detail)));
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(p, BorderLayout.NORTH);
        return wrap;
    }

    /* ---------------- Badges ---------------- */

    public static JLabel badge(String text, Color fg, Color bg) {
        JLabel l = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(Theme.sans(Font.BOLD, 12));
        l.setForeground(fg);
        l.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    public static JLabel badge(RequestStatus s) {
        Color[] c = colors(s);
        return badge(s.toString(), c[0], c[1]);
    }

    public static JLabel badge(BookingStatus s) {
        Color[] c = switch (s) {
            case PENDING -> new Color[] {Theme.AMBER, Theme.AMBER_SOFT};
            case APPROVED -> new Color[] {Theme.SAGE, Theme.SAGE_SOFT};
            case DECLINED -> new Color[] {Theme.BRICK_DARK, Theme.BRICK_SOFT};
            case WITHDRAWN -> new Color[] {Theme.MUTED, Theme.SAND};
        };
        return badge(s.toString(), c[0], c[1]);
    }

    public static JLabel badge(Priority p) {
        Color[] c = switch (p) {
            case URGENT -> new Color[] {Color.WHITE, Theme.BRICK};
            case HIGH -> new Color[] {Theme.BRICK_DARK, Theme.BRICK_SOFT};
            case NORMAL -> new Color[] {Theme.SLATE, Theme.SLATE_SOFT};
            case LOW -> new Color[] {Theme.MUTED, Theme.SAND};
        };
        return badge(p.toString(), c[0], c[1]);
    }

    static Color[] colors(RequestStatus s) {
        return switch (s) {
            case SUBMITTED -> new Color[] {Theme.AMBER, Theme.AMBER_SOFT};
            case ACCEPTED, IN_PROGRESS -> new Color[] {Theme.SLATE, Theme.SLATE_SOFT};
            case COMPLETED, APPROVED, ANSWERED -> new Color[] {Theme.SAGE, Theme.SAGE_SOFT};
            case DECLINED -> new Color[] {Theme.BRICK_DARK, Theme.BRICK_SOFT};
            case CANCELLED -> new Color[] {Theme.MUTED, Theme.SAND};
        };
    }

    /* ---------------- Formatting ---------------- */

    public static String money(java.math.BigDecimal amount) {
        return MONEY.format(amount);
    }

    public static String date(java.time.LocalDate d) {
        return d == null ? "" : DATE.format(d);
    }

    public static String dateTime(java.time.LocalDateTime d) {
        return d == null ? "" : DATE_TIME.format(d);
    }

    public static String ago(Instant t) {
        if (t == null) return "";
        Duration d = Duration.between(t, Instant.now());
        long m = d.toMinutes();
        if (m < 1) return "just now";
        if (m < 60) return m + " min ago";
        long h = d.toHours();
        if (h < 24) return h + (h == 1 ? " hour ago" : " hours ago");
        long days = d.toDays();
        if (days < 14) return days + (days == 1 ? " day ago" : " days ago");
        return DATE.format(t.atZone(ZoneId.systemDefault()).toLocalDate());
    }

    /* ---------------- Images ---------------- */

    public static ImageIcon image(byte[] bytes, int width, int height) {
        try {
            BufferedImage src = bytes == null ? null : ImageIO.read(new ByteArrayInputStream(bytes));
            BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = out.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, width, height, 14, 14));
            if (src == null) {
                g.setColor(Theme.SAND);
                g.fillRect(0, 0, width, height);
            } else {
                // Cover: scale to fill, crop the overflow from the centre.
                double scale = Math.max((double) width / src.getWidth(), (double) height / src.getHeight());
                int w = (int) Math.round(src.getWidth() * scale);
                int h = (int) Math.round(src.getHeight() * scale);
                g.drawImage(src, (width - w) / 2, (height - h) / 2, w, h, null);
            }
            g.dispose();
            return new ImageIcon(out);
        } catch (IOException e) {
            return new ImageIcon(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
        }
    }

    /* ---------------- Errors ---------------- */

    /** Runs an action and shows any problem the user can act on, instead of a stack trace. */
    public static boolean attempt(Component parent, Runnable action) {
        try {
            action.run();
            return true;
        } catch (ValidationException e) {
            JOptionPane.showMessageDialog(parent, bullets(e.problems()), "Please check", JOptionPane.WARNING_MESSAGE);
        } catch (AccessDeniedException e) {
            JOptionPane.showMessageDialog(parent, e.getMessage(), "Not allowed", JOptionPane.WARNING_MESSAGE);
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(parent, "CampusCrib couldn't reach the database.\n" + e.getCause().getMessage(), "Database problem", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    /** Like attempt, for actions that return something; null if it failed. */
    public static <T> T fetch(Component parent, Supplier<T> action) {
        Object[] out = new Object[1];
        attempt(parent, () -> {
            out[0] = action.get();
        });
        @SuppressWarnings("unchecked")
        T result = (T) out[0];
        return result;
    }

    private static String bullets(java.util.List<String> problems) {
        return problems.size() == 1 ? problems.get(0) : "• " + String.join("\n• ", problems);
    }

    private static final class RoundBorder extends AbstractBorder {
        private final Color color;
        private final int arc;

        RoundBorder(Color color, int arc) {
            this.color = color;
            this.arc = arc;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(x, y, width - 1, height - 1, arc, arc);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }
    }
}

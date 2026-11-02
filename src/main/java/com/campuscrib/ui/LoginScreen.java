package com.campuscrib.ui;

import com.campuscrib.domain.User;
import com.campuscrib.service.Services;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** Sign-in, with the demo accounts one click away when the demo database is in use. */
public class LoginScreen extends JPanel {
    private final JTextField username = new JTextField(22);
    private final JPasswordField password = new JPasswordField(22);

    record DemoAccount(String username, String who) {}

    static final List<DemoAccount> DEMO = List.of(
            new DemoAccount("ananya", "Student"),
            new DemoAccount("sofia", "Real estate agent"),
            new DemoAccount("jordan", "Resident"),
            new DemoAccount("helen", "Building manager"),
            new DemoAccount("tom", "Plumber"),
            new DemoAccount("admin", "System admin"));

    public LoginScreen(Services services, boolean demo, String databaseLabel, String demoPassword, Consumer<User> onSignedIn) {
        super(new GridLayout(1, 2));
        add(new PhotoPanel());

        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Theme.CREAM);

        JButton signIn = Ui.primary("Sign in");
        Runnable submit = () -> Ui.attempt(this, () -> onSignedIn.accept(services.auth.login(username.getText(), new String(password.getPassword()))));
        signIn.addActionListener(e -> submit.run());
        password.addActionListener(e -> submit.run());
        username.addActionListener(e -> password.requestFocusInWindow());
        username.putClientProperty("JTextField.placeholderText", "Username");
        password.putClientProperty("JTextField.placeholderText", "Password");
        password.putClientProperty("JPasswordField.showRevealButton", true);

        JPanel form = Ui.column(
                Brand.wordmark(Theme.INK),
                Ui.gap(36),
                Ui.title("Welcome home."),
                Ui.gap(6),
                Ui.muted("Sign in to find a place, manage listings or get something fixed."),
                Ui.gap(24),
                fieldLabel("Username"), Ui.gap(6), username,
                Ui.gap(14),
                fieldLabel("Password"), Ui.gap(6), password,
                Ui.gap(20),
                signIn);
        for (var c : new javax.swing.JComponent[] {username, password, signIn}) c.setMaximumSize(new Dimension(380, 44));
        username.setPreferredSize(new Dimension(380, 42));
        password.setPreferredSize(new Dimension(380, 42));

        if (demo) {
            form.add(Ui.gap(28));
            JLabel hint = Ui.eyebrow("Demo accounts · password \"" + demoPassword + "\"");
            hint.setAlignmentX(LEFT_ALIGNMENT);
            form.add(hint);
            form.add(Ui.gap(10));
            JPanel chips = new JPanel(new GridLayout(0, 2, 8, 8));
            chips.setOpaque(false);
            chips.setAlignmentX(LEFT_ALIGNMENT);
            chips.setMaximumSize(new Dimension(380, 200));
            for (DemoAccount a : DEMO) {
                RoundedPanel chip = new RoundedPanel(new BorderLayout(), Theme.SURFACE, Theme.LINE, 14);
                chip.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                JLabel l = new JLabel("<html><b>" + a.username + "</b><br><span style='color:#6B625B'>" + a.who + "</span></html>");
                chip.add(l);
                chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                chip.setToolTipText("Fill in " + a.username);
                chip.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        username.setText(a.username);
                        password.setText(demoPassword);
                        password.requestFocusInWindow();
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        chip.setColors(Theme.SURFACE, Theme.BRICK);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        chip.setColors(Theme.SURFACE, Theme.LINE);
                    }
                });
                chips.add(chip);
            }
            form.add(chips);
        }
        form.add(Ui.gap(24));
        JLabel db = Ui.muted("Connected to " + databaseLabel);
        db.setFont(Theme.sans(Font.PLAIN, 12));
        form.add(db);

        right.add(form, new GridBagConstraints());
        add(right);
    }

    private static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.sans(Font.BOLD, 13));
        l.setForeground(Theme.INK);
        return l;
    }

    /** One of the listing photos with the pitch over it. */
    private static final class PhotoPanel extends JPanel {
        private final Image photo;

        PhotoPanel() {
            setLayout(new BorderLayout());
            Image img = null;
            try (InputStream in = LoginScreen.class.getResourceAsStream("/seed/apartments/A101/main.jpg")) {
                if (in != null) img = ImageIO.read(in);
            } catch (IOException ignored) {
                // The panel falls back to a plain colour.
            }
            photo = img;
            JPanel text = Ui.column(
                    label("Student housing, all in one place", Theme.sans(Font.BOLD, 12), new Color(0xF3D3C6)),
                    Ui.gap(10),
                    label("<html>Find the apartment.<br>Then get the tap fixed.</html>", Theme.serif(34), Color.WHITE),
                    Ui.gap(12),
                    label("<html>Students, agents, residents, building managers and service crews,<br>working from the same building.</html>",
                            Theme.sans(Font.PLAIN, 14), new Color(0xF5EDE6)));
            text.setBorder(BorderFactory.createEmptyBorder(0, 44, 48, 44));
            add(text, BorderLayout.SOUTH);
        }

        private static JLabel label(String text, Font font, Color color) {
            JLabel l = new JLabel(text.startsWith("<html>") ? text : text.toUpperCase());
            l.setFont(font);
            l.setForeground(color);
            return l;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(Theme.SAGE);
            g2.fillRect(0, 0, w, h);
            if (photo != null) {
                double scale = Math.max((double) w / photo.getWidth(null), (double) h / photo.getHeight(null));
                int pw = (int) (photo.getWidth(null) * scale);
                int ph = (int) (photo.getHeight(null) * scale);
                g2.drawImage(photo, (w - pw) / 2, (h - ph) / 2, pw, ph, null);
            }
            g2.setPaint(new GradientPaint(0, h * 0.35f, new Color(42, 37, 34, 0), 0, h, new Color(42, 37, 34, 235)));
            g2.fillRect(0, 0, w, h);
            g2.dispose();
        }
    }
}

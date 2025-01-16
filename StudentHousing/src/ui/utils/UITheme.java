package ui.utils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UITheme {
    // Modern color scheme
    public static final Color PRIMARY_COLOR = new Color(66, 133, 244);   // Google Blue
    public static final Color SECONDARY_COLOR = new Color(52, 168, 83);  // Google Green
    public static final Color ACCENT_COLOR = new Color(234, 67, 53);     // Google Red
    public static final Color BACKGROUND_COLOR = new Color(247, 248, 251);
    public static final Color SURFACE_COLOR = Color.WHITE;
    public static final Color TEXT_PRIMARY = new Color(32, 33, 36);
    public static final Color TEXT_SECONDARY = new Color(95, 99, 104);
    public static final Color TEXT_COLOR = new Color(52, 73, 94);

    // Modern fonts
    public static final Font HEADER_FONT = new Font("SF Pro Display", Font.BOLD, 28);
    public static final Font TITLE_FONT = new Font("SF Pro Display", Font.BOLD, 20);
    public static final Font NORMAL_FONT = new Font("SF Pro Text", Font.PLAIN, 14);
    
    // Rounded corners
    public static final int CORNER_RADIUS = 12;
    
    // Shadows
    public static class ShadowPanel extends JPanel {
        public ShadowPanel() {
            setBackground(SURFACE_COLOR);
            setBorder(createShadowBorder());
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw shadow
            g2d.setColor(new Color(0, 0, 0, 20));
            g2d.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, CORNER_RADIUS, CORNER_RADIUS);
            
            // Draw panel background
            g2d.setColor(getBackground());
            g2d.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, CORNER_RADIUS, CORNER_RADIUS);
            
            g2d.dispose();
        }
    }
    
    public static Border createShadowBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(4, 4, 4, 4),
            BorderFactory.createLineBorder(new Color(0, 0, 0, 10), 1, true)
        );
    }
    
    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, PRIMARY_COLOR,
                    0, getHeight(), PRIMARY_COLOR.darker()
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS);
                
                // Draw text
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), textX, textY);
                
                g2d.dispose();
            }
        };
        
        button.setFont(NORMAL_FONT);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    // Modern text field
    public static JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(NORMAL_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0, 50), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setBackground(SURFACE_COLOR);
        return field;
    }

    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(NORMAL_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(SECONDARY_COLOR);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 40));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(SECONDARY_COLOR.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(SECONDARY_COLOR);
            }
        });

        return button;
    }
} 
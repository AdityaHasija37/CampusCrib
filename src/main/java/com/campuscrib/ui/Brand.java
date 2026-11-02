package com.campuscrib.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** The CampusCrib mark (a little house on a brick tile) and wordmark. */
public final class Brand {
    private Brand() {}

    public static JComponent mark(int size) {
        JComponent c = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BRICK);
                g2.fillRoundRect(0, 0, size, size, size / 3, size / 3);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(size / 14f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                float s = size / 24f;
                Path2D roof = new Path2D.Float();
                roof.moveTo(6 * s, 11 * s);
                roof.lineTo(12 * s, 6.5f * s);
                roof.lineTo(18 * s, 11 * s);
                g2.draw(roof);
                Path2D house = new Path2D.Float();
                house.moveTo(7.5f * s, 10.5f * s);
                house.lineTo(7.5f * s, 18 * s);
                house.lineTo(16.5f * s, 18 * s);
                house.lineTo(16.5f * s, 10.5f * s);
                g2.draw(house);
                g2.drawLine(Math.round(10.5f * s), Math.round(18 * s), Math.round(10.5f * s), Math.round(14.5f * s));
                g2.drawLine(Math.round(10.5f * s), Math.round(14.5f * s), Math.round(13.5f * s), Math.round(14.5f * s));
                g2.drawLine(Math.round(13.5f * s), Math.round(14.5f * s), Math.round(13.5f * s), Math.round(18 * s));
                g2.dispose();
            }
        };
        Dimension d = new Dimension(size, size);
        c.setPreferredSize(d);
        c.setMinimumSize(d);
        c.setMaximumSize(d);
        return c;
    }

    public static JPanel wordmark(Color ink) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        p.add(mark(32));
        String brick = String.format("#%06X", Theme.BRICK.getRGB() & 0xFFFFFF);
        String inkHex = String.format("#%06X", ink.getRGB() & 0xFFFFFF);
        JLabel word = new JLabel("<html><span style='color:" + inkHex + "'>Campus</span><span style='color:" + (ink.equals(Color.WHITE) ? "#E58A6C" : brick) + "'>Crib</span></html>");
        word.setFont(Theme.serif(21));
        p.add(word);
        return p;
    }
}

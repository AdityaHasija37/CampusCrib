package com.campuscrib.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/** A panel that paints its own rounded background and border, so corners blend into the page. */
public class RoundedPanel extends JPanel {
    private final int arc;
    private Color fill;
    private Color stroke;

    public RoundedPanel(LayoutManager layout, Color fill, Color stroke, int arc) {
        super(layout);
        this.fill = fill;
        this.stroke = stroke;
        this.arc = arc;
        setOpaque(false);
    }

    public void setColors(Color fill, Color stroke) {
        this.fill = fill;
        this.stroke = stroke;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (fill != null) {
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        }
        if (stroke != null) {
            g2.setColor(stroke);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}

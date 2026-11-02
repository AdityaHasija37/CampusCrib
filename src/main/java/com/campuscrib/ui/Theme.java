package com.campuscrib.ui;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Font;
import javax.swing.UIManager;

/**
 * CampusCrib's look: a warm "home" palette (cream, terracotta brick, deep sage) on FlatLaf.
 * Brick is for actions and accents; body text stays ink on cream so it reads comfortably.
 */
public final class Theme {
    public static final Color CREAM = new Color(0xFBF6EF);
    public static final Color SURFACE = Color.WHITE;
    public static final Color SAND = new Color(0xF3EBDF);
    public static final Color LINE = new Color(0xE9DCCB);
    public static final Color INK = new Color(0x2A2522);
    public static final Color MUTED = new Color(0x6B625B);
    public static final Color BRICK = new Color(0xB5482A);
    public static final Color BRICK_DARK = new Color(0x93391F);
    public static final Color BRICK_SOFT = new Color(0xF8E3DA);
    public static final Color SAGE = new Color(0x2F5D50);
    public static final Color SAGE_SOFT = new Color(0xDFECE6);
    public static final Color AMBER = new Color(0x8A5A00);
    public static final Color AMBER_SOFT = new Color(0xFBEFD3);
    public static final Color SLATE = new Color(0x3E5A73);
    public static final Color SLATE_SOFT = new Color(0xE2EAF1);
    public static final Color SIDEBAR = new Color(0x2A2522);

    private Theme() {}

    public static void install() {
        FlatLightLaf.setup();
        UIManager.put("defaultFont", new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        UIManager.put("Panel.background", CREAM);
        UIManager.put("Component.accentColor", BRICK);
        UIManager.put("Component.focusColor", BRICK);
        UIManager.put("Component.focusedBorderColor", BRICK);
        UIManager.put("Component.borderColor", LINE);
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 999);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Button.default.background", BRICK);
        UIManager.put("Button.default.foreground", Color.WHITE);
        UIManager.put("Button.default.hoverBackground", BRICK_DARK);
        UIManager.put("Button.default.focusedBackground", BRICK);
        UIManager.put("Button.borderColor", LINE);
        UIManager.put("Button.hoverBorderColor", INK);
        UIManager.put("Table.background", SURFACE);
        UIManager.put("Table.selectionBackground", BRICK_SOFT);
        UIManager.put("Table.selectionForeground", INK);
        UIManager.put("Table.rowHeight", 40);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.gridColor", LINE);
        UIManager.put("Table.intercellSpacing", new java.awt.Dimension(0, 1));
        UIManager.put("TableHeader.background", SAND);
        UIManager.put("TableHeader.separatorColor", LINE);
        UIManager.put("TableHeader.bottomSeparatorColor", LINE);
        UIManager.put("TableHeader.height", 34);
        UIManager.put("ScrollPane.background", CREAM);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("CheckBox.icon.selectedBackground", BRICK);
        UIManager.put("CheckBox.icon.selectedBorderColor", BRICK);
        UIManager.put("CheckBox.icon.checkmarkColor", Color.WHITE);
        UIManager.put("RadioButton.icon.centerDiameter", 8);
        UIManager.put("ProgressBar.foreground", BRICK);
        UIManager.put("ToggleButton.selectedBackground", BRICK_SOFT);
        UIManager.put("ToggleButton.selectedForeground", INK);
        UIManager.put("ToggleButton.borderColor", LINE);
        UIManager.put("OptionPane.background", CREAM);
        UIManager.put("TitlePane.unifiedBackground", true);
    }

    public static Font serif(float size) {
        return new Font(Font.SERIF, Font.BOLD, Math.round(size));
    }

    public static Font sans(int style, float size) {
        return new Font(Font.SANS_SERIF, style, Math.round(size));
    }
}

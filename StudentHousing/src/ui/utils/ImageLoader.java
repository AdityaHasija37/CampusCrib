package ui.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ImageLoader {
    private static final String RESOURCE_PATH = "resources/images/";
    
    public static ImageIcon loadImage(String path, int width, int height) {
        try {
            String fullPath = System.getProperty("user.dir") + "/" + RESOURCE_PATH + path;
            BufferedImage img = ImageIO.read(new File(fullPath));
            Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImg);
        } catch (Exception e) {
            System.err.println("Error loading image: " + path);
            return createPlaceholderImage(width, height);
        }
    }
    
    private static ImageIcon createPlaceholderImage(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        
        // Create gradient background
        GradientPaint gradient = new GradientPaint(
            0, 0, UITheme.PRIMARY_COLOR.brighter(),
            width, height, UITheme.SECONDARY_COLOR
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
        
        // Add placeholder text
        g2d.setColor(Color.WHITE);
        g2d.setFont(UITheme.NORMAL_FONT);
        String text = "Image Not Found";
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (width - fm.stringWidth(text)) / 2;
        int textY = (height + fm.getHeight()) / 2;
        g2d.drawString(text, textX, textY);
        
        g2d.dispose();
        return new ImageIcon(img);
    }
}
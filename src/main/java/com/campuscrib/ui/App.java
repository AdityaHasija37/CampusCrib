package com.campuscrib.ui;

import com.campuscrib.config.AppConfig;
import com.campuscrib.domain.User;
import com.campuscrib.seed.DemoData;
import com.campuscrib.service.Services;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/** The main window. Shows sign-in, then the shell for whoever signed in. */
public class App {
    private final JFrame frame = new JFrame("CampusCrib");
    private final Services services;
    private final AppConfig config;

    public App(Services services, AppConfig config) {
        this.services = services;
        this.config = config;
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1100, 720));
        frame.setSize(1320, 840);
        frame.setLocationRelativeTo(null);
        showLogin();
    }

    public JFrame frame() {
        return frame;
    }

    public void showLogin() {
        frame.setContentPane(new LoginScreen(services, config.demo(), config.describe(), DemoData.PASSWORD, this::showShell));
        frame.revalidate();
        frame.repaint();
    }

    public Shell showShell(User user) {
        Shell shell = new Shell(services, user, this::showLogin);
        frame.setContentPane(shell);
        frame.setTitle("CampusCrib · " + user.fullName() + " (" + user.role().displayName() + ")");
        frame.revalidate();
        frame.repaint();
        return shell;
    }
}

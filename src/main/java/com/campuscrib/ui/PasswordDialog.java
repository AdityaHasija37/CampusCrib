package com.campuscrib.ui;

import com.campuscrib.domain.User;
import com.campuscrib.service.Services;
import java.awt.Component;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

/** Change your own password. */
public class PasswordDialog {
    private final Component parent;
    private final Services services;
    private final User user;

    public PasswordDialog(Component parent, Services services, User user) {
        this.parent = parent;
        this.services = services;
        this.user = user;
    }

    public void setVisible(boolean visible) {
        if (!visible) return;
        JPasswordField current = new JPasswordField(20);
        JPasswordField next = new JPasswordField(20);
        Object[] form = {"Current password", current, "New password (at least 8 characters)", next};
        int choice = JOptionPane.showConfirmDialog(parent, form, "Change password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;
        if (Ui.attempt(parent, () -> services.auth.changePassword(user, new String(current.getPassword()), new String(next.getPassword())))) {
            JOptionPane.showMessageDialog(parent, "Your password has been changed.");
        }
    }
}

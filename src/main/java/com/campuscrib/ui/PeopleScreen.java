package com.campuscrib.ui;

import com.campuscrib.domain.Role;
import com.campuscrib.domain.User;
import com.campuscrib.service.Services;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** The admin's view of every account, grouped by enterprise and organization. */
public class PeopleScreen implements Screen {
    private final Services services;
    private final User admin;
    private final ListTable<User> table = new ListTable<>(
            new ListTable.Column<>("Name", 0, User::fullName),
            new ListTable.Column<>("Username", 120, User::username),
            new ListTable.Column<>("Role", 170, u -> u.role().displayName()),
            new ListTable.Column<>("Enterprise · organization", 0, u -> u.role().enterprise().displayName() + " · " + u.role().organization()),
            new ListTable.Column<>("Unit", 80, u -> u.unit() == null ? "" : u.unit()),
            new ListTable.Column<>("Status", 100, u -> u.active() ? Ui.badge("Active", Theme.SAGE, Theme.SAGE_SOFT) : Ui.badge("Inactive", Theme.MUTED, Theme.SAND)));
    private final JComponent view;

    public PeopleScreen(Services services, User admin) {
        this.services = services;
        this.admin = admin;
        JButton add = Ui.primary("Add person");
        add.addActionListener(e -> edit(null));
        JButton edit = Ui.secondary("Edit");
        edit.addActionListener(e -> table.selected().ifPresent(this::edit));
        JButton reset = Ui.secondary("Reset password");
        reset.addActionListener(e -> table.selected().ifPresent(this::resetPassword));
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(table.view(), BorderLayout.CENTER);
        body.add(Ui.row(8, edit, reset), BorderLayout.SOUTH);
        view = Ui.screen("People", "Everyone in the CampusCrib network, by role.", add, body);
    }

    @Override
    public String label() {
        return "People";
    }

    @Override
    public JComponent view() {
        return view;
    }

    @Override
    public void refresh() {
        List<User> all = Ui.fetch(view, () -> services.users.all(admin));
        if (all != null) table.setRows(all);
    }

    private void edit(User existing) {
        JTextField username = new JTextField(existing == null ? "" : existing.username());
        username.setEnabled(existing == null);
        JPasswordField password = new JPasswordField();
        JTextField name = new JTextField(existing == null ? "" : existing.fullName());
        JTextField email = new JTextField(existing == null ? "" : existing.email());
        JComboBox<Role> role = new JComboBox<>(Role.values());
        role.setSelectedItem(existing == null ? Role.STUDENT : existing.role());
        JTextField unit = new JTextField(existing == null || existing.unit() == null ? "" : existing.unit());
        JCheckBox active = new JCheckBox("Active", existing == null || existing.active());

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 4));
        form.add(Forms.label("Full name"));
        form.add(name);
        form.add(Forms.label("Email"));
        form.add(email);
        form.add(Forms.label("Username"));
        form.add(username);
        if (existing == null) {
            form.add(Forms.label("Password (at least 8 characters)"));
            form.add(password);
        }
        form.add(Forms.label("Role"));
        form.add(role);
        form.add(Forms.label("Unit (residents only)"));
        form.add(unit);
        if (existing != null) form.add(active);

        int ok = JOptionPane.showConfirmDialog(view, form, existing == null ? "Add person" : "Edit " + existing.fullName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;
        boolean saved = Ui.attempt(view, () -> {
            if (existing == null) {
                services.users.create(admin, username.getText(), new String(password.getPassword()), name.getText(), email.getText(), (Role) role.getSelectedItem(), unit.getText());
            } else {
                services.users.update(admin, existing.id(), name.getText(), email.getText(), (Role) role.getSelectedItem(), unit.getText(), active.isSelected());
            }
        });
        if (saved) refresh();
    }

    private void resetPassword(User u) {
        JPasswordField password = new JPasswordField();
        int ok = JOptionPane.showConfirmDialog(view, new Object[] {"New password for " + u.fullName() + " (at least 8 characters)", password},
                "Reset password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok == JOptionPane.OK_OPTION && Ui.attempt(view, () -> services.users.resetPassword(admin, u.id(), new String(password.getPassword())))) {
            JOptionPane.showMessageDialog(view, "Password reset. Share it with " + u.firstName() + " securely.");
        }
    }

}

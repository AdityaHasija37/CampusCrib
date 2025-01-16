package ui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import models.UserAccount;

public class LoginPanel extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private final List<UserAccount> userAccounts;
    private final Consumer<UserAccount> onLoginSuccess;

    public LoginPanel(JFrame frame, List<UserAccount> userAccounts, Consumer<UserAccount> onLoginSuccess) {
        this.userAccounts = userAccounts;
        this.onLoginSuccess = onLoginSuccess;
        
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Title
        JLabel titleLabel = new JLabel("Student Housing Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);
        
        // Username
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        add(new JLabel("Username:"), gbc);
        
        usernameField = new JTextField(20);
        gbc.gridx = 1;
        add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Password:"), gbc);
        
        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        add(passwordField, gbc);
        
        // Login button
        loginButton = new JButton("Login");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(loginButton, gbc);
        
        loginButton.addActionListener(e -> handleLogin());
    }
    
    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        UserAccount user = userAccounts.stream()
                .filter(account -> account.getUsername().equals(username) && 
                                 account.getPassword().equals(password))
                .findFirst()
                .orElse(null);
        
        if (user != null) {
            onLoginSuccess.accept(user);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid username or password\nTry: resident1/password",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
} 
package ui;

import com.apartmentbooking.controller.ApartmentController;
import com.apartmentbooking.controller.BookingController;
import com.apartmentbooking.controller.UserController;
import javax.swing.*;
import java.awt.*;
import models.UserAccount;

public class UserDashboard extends JFrame {
    private UserAccount user;
    private UserController userController;
    private ApartmentController apartmentController;
    private BookingController bookingController;
    private BookingRequestPanel bookingRequestPanel;

    public UserDashboard(UserAccount user, UserController userController, ApartmentController apartmentController, BookingController bookingController) {
        if (user == null || userController == null || apartmentController == null || bookingController == null) {
            throw new IllegalArgumentException("Required parameters cannot be null.");
        }
        
        this.user = user;
        this.userController = userController;
        this.apartmentController = apartmentController;
        this.bookingController = bookingController;

        initComponents();
    }

    private void initComponents() {
        // Basic window settings
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("User Dashboard - " + user.getUsername());
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Create a main panel to hold all components
        JPanel mainPanel = new JPanel(new BorderLayout());
        getContentPane().add(mainPanel);

        // Create the tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Browse Apartments", new ApartmentSearchPanel(user, apartmentController, bookingController));
        
        bookingRequestPanel = new BookingRequestPanel(user, user.getRole().equals("Agent"), bookingController);
        tabbedPane.addTab("My Booking Requests", bookingRequestPanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Create a logout panel with a button
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        logoutPanel.add(logoutButton);

        mainPanel.add(logoutPanel, BorderLayout.SOUTH);
    }

    private void logout() {
        // Perform logout actions
        userController.logout();

        // Display confirmation message
        JOptionPane.showMessageDialog(this, "You have logged out successfully!", "Logout", JOptionPane.INFORMATION_MESSAGE);

        // Close the dashboard and return to the login screen or exit
        this.dispose(); // Close the dashboard window

        // You can optionally show the login panel if the application needs to restart from login
        SwingUtilities.invokeLater(() -> {
            LoginPanel loginPanel = new LoginPanel(new JFrame(), null, null); // Pass necessary arguments
            loginPanel.setVisible(true);
        });
    }

    public void refreshBookingRequests() {
        if (bookingRequestPanel != null) {
            bookingRequestPanel.refreshRequests();
        }
    }
}

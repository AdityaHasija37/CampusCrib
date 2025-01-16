/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui;

/**
 *
 * @author nidhinair
 */
import com.apartmentbooking.controller.ApartmentController;
import com.apartmentbooking.controller.BookingController;
import com.apartmentbooking.controller.UserController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import models.UserAccount;
import models.WorkRequest;
 
public class AgentDashboardPanel extends JFrame {
    private UserAccount user;
    private UserController userController;
    private ApartmentController apartmentController;
    private BookingController bookingController;
    private BookingRequestPanel bookingRequestPanel;

    public AgentDashboardPanel(UserAccount user, UserController userController, ApartmentController apartmentController, BookingController bookingController) {
        this.user = user;
        this.userController = userController;
        this.apartmentController = apartmentController;
        this.bookingController = bookingController;
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Agent Dashboard - " + user);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Create the JTabbedPane and tabs
        JTabbedPane tabbedPane = new JTabbedPane();

        // Add My Listings tab (ApartmentSearchPanel)
        ApartmentSearchPanel apartmentSearchPanel = new ApartmentSearchPanel(user, apartmentController, bookingController);
        tabbedPane.addTab("My Listings", apartmentSearchPanel);

        // Add My Bookings tab
        bookingRequestPanel = new BookingRequestPanel(user, true, bookingController);
        tabbedPane.addTab("Booking Requests", bookingRequestPanel);

        // Add the JTabbedPane to the JFrame
        add(tabbedPane, BorderLayout.CENTER);

        // Add the Add Listing button
        JButton addListingButton = new JButton("Add Listing");
        addListingButton.addActionListener(e -> showAddListingForm());
        add(addListingButton, BorderLayout.NORTH);

        // Add the Logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        add(logoutButton, BorderLayout.SOUTH);
    }

    private void showAddListingForm() {
        // Create and show the AddListingDialog when the Add Listing button is clicked
        AddListingDialog addListingDialog = new AddListingDialog(
            this, 
            user, 
            apartmentController, 
            bookingController
        );
        addListingDialog.setVisible(true);

        // Optionally, refresh the listings after closing the dialog if a listing was added
        if (addListingDialog.isListingAdded()) {
            refreshApartmentListings();
        }
    }

    private void logout() {
    // Step 1: Log the user out (clear user session, or do any necessary cleanup)
    userController.logout();  // Assuming userController handles the logout logic (clearing user session, etc.)

    
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

private void onLoginSuccess(UserAccount user) {
    // Define what happens when the user successfully logs in
    System.out.println("Login successful for user: " + user.getUsername());
    // For example, you could initialize the agent dashboard again or update the current view.
}



    private void refreshApartmentListings() {
        // Access the JTabbedPane correctly
        JTabbedPane tabbedPane = (JTabbedPane) getContentPane().getComponent(0); // Get the first component in the content pane, which should be the JTabbedPane
        ApartmentSearchPanel apartmentSearchPanel = (ApartmentSearchPanel) tabbedPane.getComponentAt(0); // Get the first tab (My Listings)
        
        // Refresh the listings
        apartmentSearchPanel.refreshListings();  // Assuming `ApartmentSearchPanel` has a method `refreshListings`
    }

    public void refreshBookingRequests() {
        bookingRequestPanel.refreshRequests();
    }
}

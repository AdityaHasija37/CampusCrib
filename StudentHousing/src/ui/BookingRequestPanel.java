/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui;

import com.apartmentbooking.controller.BookingController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import models.ApartmentBookingRequest;
import models.UserAccount;

/**
 *
 * @author nidhinair
 */
public class BookingRequestPanel extends JPanel {
    private UserAccount user;
    private boolean isAgent;
    private BookingController bookingController;
    private JPanel requestsPanel;

    // Constructor
    public BookingRequestPanel(UserAccount user, boolean isAgent, BookingController bookingController) {
        this.user = user;
        this.isAgent = isAgent;
        this.bookingController = bookingController;
        initComponents();
    }

    // Initialize components
    private void initComponents() {
        setLayout(new BorderLayout());

        requestsPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        JScrollPane scrollPane = new JScrollPane(requestsPanel);
        add(scrollPane, BorderLayout.CENTER);

        refreshRequests();
    }

    // Refresh requests and update the UI
    public void refreshRequests() {
        requestsPanel.removeAll();
        List<ApartmentBookingRequest> requests = isAgent
            ? bookingController.getAgentBookingRequests(user)
            : bookingController.getUserBookingRequests(user); // Fixed the missing parenthesis

        for (ApartmentBookingRequest request : requests) {
            requestsPanel.add(createRequestPanel(request));
        }

        requestsPanel.revalidate();
        requestsPanel.repaint();
    }

    // Create a panel for individual booking requests
    private JPanel createRequestPanel(ApartmentBookingRequest request) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JPanel infoPanel = new JPanel(new GridLayout(0, 1));
        infoPanel.add(new JLabel("Apartment ID: " + request.getApartmentId()));
        infoPanel.add(new JLabel("Status: " + request.getStatus()));
        infoPanel.add(new JLabel("Message: " + request.getMessage()));
        if (!request.getComment().isEmpty()) {
            infoPanel.add(new JLabel("Agent Comment: " + request.getComment()));
        }
        panel.add(infoPanel, BorderLayout.CENTER);

        // Add buttons for agents to approve/deny requests
        if (isAgent && request.getStatus().equals("Pending")) {
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton approveButton = new JButton("Approve");
            JButton denyButton = new JButton("Deny");

            approveButton.addActionListener(e -> updateRequest(request, "Approved"));
            denyButton.addActionListener(e -> updateRequest(request, "Denied"));

            buttonPanel.add(approveButton);
            buttonPanel.add(denyButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);
        }

        return panel;
    }

    // Update the status of a booking request
    private void updateRequest(ApartmentBookingRequest request, String status) {
        String comment = JOptionPane.showInputDialog(this, "Enter a comment for this " + status.toLowerCase() + " request:");
        if (comment != null) {
            bookingController.updateBookingRequest(request.getId(), status, comment);
            refreshRequests();
        }
    }
}

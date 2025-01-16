/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui;

/**
 *
 * @author nidhinair
 */
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import models.UserAccount;
import models.WorkRequest;
import utilities.Reporting;
 
public class BuildingManagerDashboardPanel extends JPanel {
    private DefaultTableModel tableModel;
 
    public BuildingManagerDashboardPanel(UserAccount user, List<WorkRequest> requests, Runnable logoutAction) {
        setLayout(new BorderLayout());
 
        JLabel welcomeLabel = new JLabel("Welcome, " + user.getUsername() + " (Building Manager)");
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
 
        String[] columns = {"Request ID", "Type", "Description", "Status", "Update Allowed"};
        tableModel = new DefaultTableModel(columns, 0);
        JTable requestTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(requestTable);
 
        // Populate the table with relevant requests
        populateTable(requests);
 
        JButton updateStatusButton = new JButton("Update Status");
        updateStatusButton.addActionListener(e -> {
            int selectedRow = requestTable.getSelectedRow();
            if (selectedRow >= 0) {
                String updateAllowed = (String) tableModel.getValueAt(selectedRow, 4);
                if ("Yes".equals(updateAllowed)) {
                    String newStatus = JOptionPane.showInputDialog("Enter new status (e.g., In Progress, Completed):");
                    if (newStatus != null && !newStatus.isEmpty()) {
                        String requestId = (String) tableModel.getValueAt(selectedRow, 0);
                        for (WorkRequest request : requests) {
                            if (request.getId().equals(requestId)) {
                                request.setStatus(newStatus);
                                break;
                            }
                        }
                        refreshTable(requests);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "You can only update amenity booking requests.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a request to update.");
            }
        });
 
        JButton viewReportButton = new JButton("View Reports");
        viewReportButton.addActionListener(e -> {
            Map<String, Integer> report = Reporting.getRequestsByStatus(requests);
            StringBuilder reportMessage = new StringBuilder("Requests by Status:\n");
            report.forEach((status, count) -> reportMessage.append(status).append(": ").append(count).append("\n"));
            JOptionPane.showMessageDialog(this, reportMessage.toString());
        });
 
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logoutAction.run());
 
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(updateStatusButton);
        buttonPanel.add(viewReportButton);
        buttonPanel.add(logoutButton);
 
        add(welcomeLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
 
    private void populateTable(List<WorkRequest> requests) {
        tableModel.setRowCount(0); // Clear existing rows
        for (WorkRequest request : requests) {
            // Show maintenance and amenity booking requests
            if (request.getType().equalsIgnoreCase("Amenity Booking")) {
                tableModel.addRow(new Object[]{request.getId(), request.getType(), request.getDescription(), request.getStatus(), "Yes"});
            } else if (List.of("Plumbing", "Electrical", "Carpenter", "Pest Control").contains(request.getType())) {
                tableModel.addRow(new Object[]{request.getId(), request.getType(), request.getDescription(), request.getStatus(), "No"});
            }
        }
    }
 
    private void refreshTable(List<WorkRequest> requests) {
        populateTable(requests); // Refresh the table with updated data
    }
}
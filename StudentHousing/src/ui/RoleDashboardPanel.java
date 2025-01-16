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
import models.UserAccount;
import models.WorkRequest;
 
public class RoleDashboardPanel extends JPanel {
    private DefaultTableModel tableModel;
 
    public RoleDashboardPanel(UserAccount user, List<WorkRequest> requests, Runnable logoutAction) {
        setLayout(new BorderLayout());
 
        JLabel welcomeLabel = new JLabel("Welcome, " + user.getUsername() + " (" + user.getRole().getRoleName() + ")");
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
 
        String[] columns = {"Request ID", "Type", "Description", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        JTable requestTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(requestTable);
 
        populateTable(user, requests);
 
        JButton updateStatusButton = new JButton("Update Status");
        updateStatusButton.addActionListener(e -> {
            int selectedRow = requestTable.getSelectedRow();
            if (selectedRow >= 0) {
                String requestId = (String) tableModel.getValueAt(selectedRow, 0);
                for (WorkRequest request : requests) {
                    if (request.getId().equals(requestId) && user.getRole().getRoleName().equals(request.getResponsibleRole())) {
                        String newStatus = JOptionPane.showInputDialog("Enter new status (e.g., In Progress, Completed):");
                        if (newStatus != null && !newStatus.isEmpty()) {
                            request.setStatus(newStatus);
                            refreshTable(user, requests);
                        }
                        return;
                    }
                }
                JOptionPane.showMessageDialog(this, "You are not authorized to update this request.");
            } else {
                JOptionPane.showMessageDialog(this, "Please select a request to update.");
            }
        });
 
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logoutAction.run());
 
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(updateStatusButton);
        buttonPanel.add(logoutButton);
 
        add(welcomeLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
 
    private void populateTable(UserAccount user, List<WorkRequest> requests) {
        tableModel.setRowCount(0); // Clear table data
        for (WorkRequest request : requests) {
            if (user.getRole().getRoleName().equals(request.getResponsibleRole())) { // Show only requests for this role
                tableModel.addRow(new Object[]{request.getId(), request.getType(), request.getDescription(), request.getStatus()});
            }
        }
    }
 
    private void refreshTable(UserAccount user, List<WorkRequest> requests) {
        populateTable(user, requests);
    }
}
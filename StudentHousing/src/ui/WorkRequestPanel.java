package ui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import models.AmenityBookingRequest;
import models.ApartmentBookingRequest;
import models.MaintenanceRequest;
import models.UserAccount;
import models.WorkRequest;
import models.GeneralInquiryRequest;
import ui.utils.UITheme;
import ui.utils.UITheme.ShadowPanel;

public class WorkRequestPanel extends JPanel {
    private Runnable onBackAction;
    private List<WorkRequest> requests;
    private UserAccount user;

    public WorkRequestPanel(Runnable onBackAction, List<WorkRequest> requests, UserAccount user) {
        this.onBackAction = onBackAction;
        this.requests = requests;
        this.user = user;
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Navigation Bar
        JPanel navBar = new ShadowPanel();
        navBar.setLayout(new BorderLayout());
        navBar.setPreferredSize(new Dimension(0, 60));
        
        // Back button
        JButton backButton = UITheme.createPrimaryButton("← Back to Dashboard");
        backButton.addActionListener(e -> onBackAction.run());
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonWrapper.setOpaque(false);
        buttonWrapper.add(backButton);
        navBar.add(buttonWrapper, BorderLayout.WEST);
        
        // Title
        JLabel titleLabel = new JLabel("Create Work Request");
        titleLabel.setFont(UITheme.HEADER_FONT);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        navBar.add(titleLabel, BorderLayout.CENTER);
        
        add(navBar, BorderLayout.NORTH);

        // Work Request Form
        JPanel formPanel = createWorkRequestForm();
        add(formPanel, BorderLayout.CENTER);
    }

    private JPanel createWorkRequestForm() {
        ShadowPanel formPanel = new ShadowPanel();
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        // Request Type
        JLabel typeLabel = new JLabel("Request Type");
        typeLabel.setFont(UITheme.TITLE_FONT);
        formPanel.add(typeLabel, gbc);

        String[] requestTypes = {
            "Maintenance Request",
            "Amenity Booking",
            "General Inquiry",
            "Complaint"
        };
        JComboBox<String> typeCombo = new JComboBox<>(requestTypes);
        typeCombo.setFont(UITheme.NORMAL_FONT);
        formPanel.add(typeCombo, gbc);

        // Maintenance Type (dynamic visibility)
        JLabel maintenanceTypeLabel = new JLabel("Maintenance Type");
        maintenanceTypeLabel.setFont(UITheme.TITLE_FONT);
        maintenanceTypeLabel.setVisible(false);
        formPanel.add(maintenanceTypeLabel, gbc);

        String[] maintenanceTypes = {"Plumbing", "Electrical", "Carpenter", "Pest Control"};
        JComboBox<String> maintenanceTypeCombo = new JComboBox<>(maintenanceTypes);
        maintenanceTypeCombo.setFont(UITheme.NORMAL_FONT);
        maintenanceTypeCombo.setVisible(false);
        formPanel.add(maintenanceTypeCombo, gbc);

        typeCombo.addActionListener(e -> {
            boolean isMaintenance = "Maintenance Request".equals(typeCombo.getSelectedItem().toString());
            maintenanceTypeLabel.setVisible(isMaintenance);
            maintenanceTypeCombo.setVisible(isMaintenance);
        });

        // Description
        JLabel descLabel = new JLabel("Description");
        descLabel.setFont(UITheme.TITLE_FONT);
        formPanel.add(descLabel, gbc);

        JTextArea descArea = new JTextArea(5, 30);
        descArea.setFont(UITheme.NORMAL_FONT);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descArea);
        formPanel.add(scrollPane, gbc);

        // Priority
        JLabel priorityLabel = new JLabel("Priority");
        priorityLabel.setFont(UITheme.TITLE_FONT);
        formPanel.add(priorityLabel, gbc);

        String[] priorities = {"Low", "Medium", "High", "Urgent"};
        JComboBox<String> priorityCombo = new JComboBox<>(priorities);
        priorityCombo.setFont(UITheme.NORMAL_FONT);
        formPanel.add(priorityCombo, gbc);

        // Submit Button
        JButton submitButton = UITheme.createPrimaryButton("Submit Request");
        submitButton.addActionListener(e -> handleSubmit(
            typeCombo.getSelectedItem().toString(),
            maintenanceTypeCombo.getSelectedItem() != null ? maintenanceTypeCombo.getSelectedItem().toString() : null,
            descArea.getText(),
            priorityCombo.getSelectedItem().toString()
        ));
        
        gbc.insets = new Insets(20, 10, 10, 10);
        formPanel.add(submitButton, gbc);

        return formPanel;
    }

    private void handleSubmit(String type, String maintenanceType, String description, String priority) {
        if (description.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please provide a description",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create appropriate request type
        WorkRequest request;
        if ("Maintenance Request".equals(type)) {
            if (maintenanceType == null) {
                JOptionPane.showMessageDialog(this,
                    "Please select a maintenance type",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            request = new MaintenanceRequest(maintenanceType);
        } else {
            request = createAppropriateRequest(type);
        }

        request.setDescription(description);
        request.setStatus("Pending");
        request.setCreatedBy(user);
        request.setPriority(priority);

        // Add to the requests list
        requests.add(request);

        JOptionPane.showMessageDialog(this,
            "Work request submitted successfully!",
            "Success",
            JOptionPane.INFORMATION_MESSAGE);

        // Go back to dashboard and refresh
        onBackAction.run();
    }

    private WorkRequest createAppropriateRequest(String type) {
        switch (type) {
            case "Amenity Booking":
                return new AmenityBookingRequest();
            case "General Inquiry":
                return new GeneralInquiryRequest();
            default:
                return new GeneralInquiryRequest(); // Default to general inquiry
        }
    }
}


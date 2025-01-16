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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;
import models.ResidentRole;
import models.UserAccount;
import models.WorkRequest;
import com.apartmentbooking.controller.ApartmentController;
import ui.utils.UITheme;
import ui.utils.UITheme.ShadowPanel;
import ui.utils.ImageLoader;
import javax.swing.table.TableCellRenderer;
import java.util.ArrayList;
import java.util.Collections;

public class ResidentDashboardPanel extends JPanel {
    private DefaultTableModel tableModel;
    private ApartmentController apartmentController;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private List<WorkRequest> requests;
    private UserAccount user;
    
    public ResidentDashboardPanel(UserAccount user, List<WorkRequest> requests, 
                                 Runnable onCreateRequest, Runnable logoutAction) {
        this.user = user;
        this.requests = requests != null ? requests : new ArrayList<>();
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        this.apartmentController = new ApartmentController();

        // Main Panel with Card Layout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setOpaque(false);

        // Dashboard Content
        ShadowPanel contentPanel = new ShadowPanel();
        contentPanel.setLayout(new BorderLayout(20, 20));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top Section with Welcome and Stats
        JPanel topSection = createTopSection(user, this.requests);
        contentPanel.add(topSection, BorderLayout.NORTH);

        // Action Buttons Section
        JPanel actionSection = createActionSection(onCreateRequest, logoutAction);
        contentPanel.add(actionSection, BorderLayout.CENTER);

        // Recent Requests Table
        JPanel requestsSection = createRequestsSection(requests);
        contentPanel.add(requestsSection, BorderLayout.SOUTH);

        mainPanel.add(contentPanel, "dashboard");
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createTopSection(UserAccount user, List<WorkRequest> requests) {
        JPanel topSection = new JPanel(new BorderLayout(20, 0));
        topSection.setOpaque(false);

        // Welcome Message
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome back,");
        welcomeLabel.setFont(UITheme.NORMAL_FONT);
        welcomeLabel.setForeground(UITheme.TEXT_SECONDARY);

        JLabel nameLabel = new JLabel(user.getUsername());
        nameLabel.setFont(UITheme.HEADER_FONT);
        nameLabel.setForeground(UITheme.TEXT_PRIMARY);

        welcomePanel.add(welcomeLabel);
        welcomePanel.add(Box.createVerticalStrut(5));
        welcomePanel.add(nameLabel);

        // Stats Cards
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        statsPanel.setOpaque(false);

        // Calculate initial stats
        int activeRequests = (int) requests.stream()
            .filter(req -> !req.getStatus().equalsIgnoreCase("Completed") && 
                          !req.getStatus().equalsIgnoreCase("Rejected"))
            .count();

        int pendingApprovals = (int) requests.stream()
            .filter(req -> req.getStatus().equalsIgnoreCase("Pending"))
            .count();

        // Create single set of stats cards
        createStatsCard(statsPanel, "Active Requests", String.valueOf(activeRequests), "📋");
        createStatsCard(statsPanel, "Pending Approvals", String.valueOf(pendingApprovals), "⏳");
        createStatsCard(statsPanel, "New Messages", "0", "✉️");

        topSection.add(welcomePanel, BorderLayout.WEST);
        topSection.add(statsPanel, BorderLayout.EAST);

        return topSection;
    }

    private void createStatsCard(JPanel container, String title, String value, String icon) {
        ShadowPanel card = new ShadowPanel();
        card.setPreferredSize(new Dimension(150, 100));
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // Icon
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        gbc.gridy = 0;
        card.add(iconLabel, gbc);

        // Value
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(UITheme.TITLE_FONT);
        valueLabel.setForeground(UITheme.PRIMARY_COLOR);
        gbc.gridy = 1;
        card.add(valueLabel, gbc);

        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.NORMAL_FONT);
        titleLabel.setForeground(UITheme.TEXT_SECONDARY);
        gbc.gridy = 2;
        card.add(titleLabel, gbc);

        container.add(card);
    }

    private JPanel createActionSection(Runnable onCreateRequest, Runnable logoutAction) {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        actionPanel.setOpaque(false);

        /*// Search Apartments Button
        JButton searchButton = createActionButton("Search Apartments", "🔍", UITheme.PRIMARY_COLOR);
        searchButton.addActionListener(e -> showApartmentSearch());*/

        // Create Request Button
        JButton requestButton = createActionButton("Create Request", "📝", UITheme.SECONDARY_COLOR);
        requestButton.addActionListener(e -> showWorkRequestPanel());

        // Profile Button
        JButton profileButton = createActionButton("My Profile", "👤", UITheme.ACCENT_COLOR);
        profileButton.addActionListener(e -> showProfileDialog());

        // Logout Button
        JButton logoutButton = createActionButton("Logout", "🚪", new Color(231, 76, 60));
        logoutButton.addActionListener(e -> {
            if (logoutAction != null) {
                logoutAction.run();
            }
        });

        //actionPanel.add(searchButton);
        actionPanel.add(requestButton);
        actionPanel.add(profileButton);
        actionPanel.add(logoutButton);

        return actionPanel;
    }

    private JButton createActionButton(String text, String icon, Color color) {
        JButton button = new JButton(icon + "  " + text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                   RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, color,
                    0, getHeight(), color.darker()
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                g2d.dispose();
                super.paintComponent(g);
            }
        };

        button.setFont(UITheme.NORMAL_FONT);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(200, 45));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setFont(button.getFont().deriveFont(Font.BOLD));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setFont(UITheme.NORMAL_FONT);
            }
        });

        return button;
    }

    private JPanel createRequestsSection(List<WorkRequest> requests) {
        JPanel requestsPanel = new JPanel(new BorderLayout(0, 10));
        requestsPanel.setOpaque(false);

        // Section Title
        JLabel titleLabel = new JLabel("Recent Requests");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        requestsPanel.add(titleLabel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Request ID", "Type", "Description", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        JTable requestTable = createStyledTable(tableModel);
        
        // Populate table
        refreshTable(requests);

        JScrollPane scrollPane = new JScrollPane(requestTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        requestsPanel.add(scrollPane, BorderLayout.CENTER);

        return requestsPanel;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                if (column == 3) { // Status column
                    String status = (String) getValueAt(row, column);
                    switch (status.toLowerCase()) {
                        case "pending":
                            comp.setForeground(new Color(243, 156, 18));
                            break;
                        case "approved":
                            comp.setForeground(new Color(46, 204, 113));
                            break;
                        case "rejected":
                            comp.setForeground(new Color(231, 76, 60));
                            break;
                        default:
                            comp.setForeground(UITheme.TEXT_PRIMARY);
                    }
                }
                return comp;
            }
        };

        // Style the table
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(UITheme.NORMAL_FONT);
        table.setSelectionBackground(new Color(66, 133, 244, 50));
        table.setSelectionForeground(UITheme.TEXT_PRIMARY);

        // Style the header
        JTableHeader header = table.getTableHeader();
        header.setFont(UITheme.NORMAL_FONT.deriveFont(Font.BOLD));
        header.setBackground(UITheme.PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));

        return table;
    }

    /*private void showApartmentSearch() {
        ApartmentSearchPanel searchPanel = new ApartmentSearchPanel(apartmentController, user);
        mainPanel.add(searchPanel, "search");
        cardLayout.show(mainPanel, "search");
    }*/

    public void refreshTable(List<WorkRequest> requests) {
        tableModel.setRowCount(0);
        
        if (requests != null && !requests.isEmpty()) {
            // Sort requests by date (newest first)
            List<WorkRequest> sortedRequests = new ArrayList<>(requests);
            Collections.sort(sortedRequests, (a, b) -> 
                b.getCreatedDate() != null && a.getCreatedDate() != null ? 
                b.getCreatedDate().compareTo(a.getCreatedDate()) : 0);
            
            for (WorkRequest request : sortedRequests) {
                tableModel.addRow(new Object[]{
                    request.getId(),
                    request.getType(),
                    request.getDescription(),
                    request.getStatus()
                });
            }
        }
        
        // Update stats after refreshing table
        updateStats();
    }

    private void showCreateRequestDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Create Request");
        dialog.setModal(true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Request type
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{
            "Maintenance Request",
            "Amenity Booking",
            "General Inquiry"
        });
        panel.add(new JLabel("Request Type:"), gbc);
        panel.add(typeCombo, gbc);

        // Description
        JTextArea descArea = new JTextArea(5, 20);
        descArea.setLineWrap(true);
        panel.add(new JLabel("Description:"), gbc);
        panel.add(new JScrollPane(descArea), gbc);

        // Submit button
        JButton submitButton = UITheme.createPrimaryButton("Submit Request");
        submitButton.addActionListener(e -> {
            // Handle request submission
            dialog.dispose();
        });
        panel.add(submitButton, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showProfileDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("My Profile");
        dialog.setModal(true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new ShadowPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Profile sections
        addProfileSection(panel, "👤 Personal Information", new String[][]{
            {"Name:", "John Doe"},
            {"Email:", "john.doe@university.edu"},
            {"Student ID:", "12345678"}
        }, gbc);

        addProfileSection(panel, "🏠 Current Residence", new String[][]{
            {"Building:", "Student Village"},
            {"Apartment:", "A101"},
            {"Move-in Date:", "01/09/2023"}
        }, gbc);

        addProfileSection(panel, "💳 Payment Information", new String[][]{
            {"Payment Method:", "Credit Card (**** 1234)"},
            {"Next Payment:", "$800 (Due: 01/01/2024)"}
        }, gbc);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        dialog.add(scrollPane);
        dialog.setVisible(true);
    }

    private void addProfileSection(JPanel panel, String title, String[][] fields, GridBagConstraints gbc) {
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(UITheme.PRIMARY_COLOR);
        panel.add(titleLabel, gbc);

        for (String[] field : fields) {
            JPanel fieldPanel = new JPanel(new BorderLayout(10, 0));
            fieldPanel.setOpaque(false);
            
            JLabel label = new JLabel(field[0]);
            label.setFont(UITheme.NORMAL_FONT.deriveFont(Font.BOLD));
            
            JLabel value = new JLabel(field[1]);
            value.setFont(UITheme.NORMAL_FONT);
            
            fieldPanel.add(label, BorderLayout.WEST);
            fieldPanel.add(value, BorderLayout.CENTER);
            
            panel.add(fieldPanel, gbc);
        }
        
        panel.add(Box.createVerticalStrut(10), gbc);
    }

    private void showWorkRequestPanel() {
        Container parent = getParent();
        if (parent != null) {
            final WorkRequestPanel workRequestPanel = createWorkRequestPanel(parent);
            parent.remove(this);
            parent.add(workRequestPanel);
            parent.revalidate();
            parent.repaint();
        }
    }

    private WorkRequestPanel createWorkRequestPanel(Container parent) {
        return new WorkRequestPanel(
            () -> {
                WorkRequestPanel currentPanel = (WorkRequestPanel) parent.getComponent(0);
                parent.remove(currentPanel);
                parent.add(this);
                refreshTable(requests);
                parent.revalidate();
                parent.repaint();
            },
            requests,
            user
        );
    }

    // Add method to update stats
    public void updateStats() {
        try {
            // Get the content panel (ShadowPanel)
            Component contentComponent = mainPanel.getComponent(0);
            if (!(contentComponent instanceof ShadowPanel)) {
                return;
            }
            ShadowPanel contentPanel = (ShadowPanel) contentComponent;

            // Get the top section
            Component topComponent = contentPanel.getComponent(0);
            if (!(topComponent instanceof JPanel)) {
                return;
            }
            JPanel topSection = (JPanel) topComponent;

            // Get the stats panel
            Component statsComponent = ((BorderLayout) topSection.getLayout()).getLayoutComponent(BorderLayout.EAST);
            if (!(statsComponent instanceof JPanel)) {
                return;
            }
            JPanel statsPanel = (JPanel) statsComponent;

            // Update the stats
            statsPanel.removeAll();

            // Calculate new stats
            int activeRequests = (int) requests.stream()
                .filter(req -> !req.getStatus().equalsIgnoreCase("Completed") && 
                             !req.getStatus().equalsIgnoreCase("Rejected"))
                .count();

            int pendingApprovals = (int) requests.stream()
                .filter(req -> req.getStatus().equalsIgnoreCase("Pending"))
                .count();

            // Create new stats cards
            createStatsCard(statsPanel, "Active Requests", String.valueOf(activeRequests), "📋");
            createStatsCard(statsPanel, "Pending Approvals", String.valueOf(pendingApprovals), "⏳");
            createStatsCard(statsPanel, "New Messages", "0", "✉️");

            // Refresh the panel
            statsPanel.revalidate();
            statsPanel.repaint();
        } catch (Exception e) {
            System.err.println("Error updating stats: " + e.getMessage());
        }
    }
}

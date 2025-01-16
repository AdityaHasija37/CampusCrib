package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import models.Apartment;
import com.apartmentbooking.controller.ApartmentController;
import com.apartmentbooking.controller.BookingController;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import models.AgentRole;
import models.ApartmentBookingRequest;
import models.UserAccount;

public class ApartmentSearchPanel extends JPanel {
    private UserAccount user;
    private ApartmentController apartmentController;
    private BookingController bookingController;
    private JTable apartmentTable;
    private DefaultTableModel apartmentTableModel;
    private JButton bookButton;

    public ApartmentSearchPanel(UserAccount user, ApartmentController apartmentController, BookingController bookingController) {
        this.user = user;
        this.apartmentController = apartmentController;
        this.bookingController = bookingController;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        String[] columnNames = {"ID", "Title", "Rent", "Facilities", "Image"};
        apartmentTableModel = new DefaultTableModel(columnNames, 0);
        apartmentTable = new JTable(apartmentTableModel);

        // Custom renderer to display ImageIcon
        apartmentTable.getColumnModel().getColumn(4).setCellRenderer(new ImageRenderer());

        JScrollPane scrollPane = new JScrollPane(apartmentTable);
        add(scrollPane, BorderLayout.CENTER);

        // Book Button
        bookButton = new JButton("Book Apartment");
        bookButton.setEnabled(false); // Initially disabled
        bookButton.addActionListener(e -> bookApartment());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(bookButton);
        add(buttonPanel, BorderLayout.SOUTH);

        apartmentTable.getSelectionModel().addListSelectionListener(e -> {
            // Enable the book button when an apartment is selected
            if (apartmentTable.getSelectedRow() != -1) {
                bookButton.setEnabled(true);
            } else {
                bookButton.setEnabled(false);
            }
        });

        refreshListings();  // Fetch the listings when the panel is initialized
    }

    public void refreshListings() {
        // Clear existing rows
        apartmentTableModel.setRowCount(0);

        // Get apartment data based on user role
        List<Apartment> apartments;

        if (user.getRole() instanceof AgentRole) {
            // If the user is an agent, show only their listings
            apartments = apartmentController.getApartmentsByAgent(user);  // Fetch apartments by the logged-in agent
        } else {
            // If the user is not an agent, show all apartments
            apartments = apartmentController.getAllApartments();  // Fetch all apartments
        }

        // Populate table with the apartment data
        for (Apartment apartment : apartments) {
            apartmentTableModel.addRow(new Object[]{
                apartment.getId(),
                apartment.getTitle(),
                apartment.getRent(),
                apartment.getFacilities(),
                apartment.getImage()  // This is where you pass the image for rendering
            });
        }
    }

    private void bookApartment() {
        // Get the selected apartment
        int selectedRow = apartmentTable.getSelectedRow();
        if (selectedRow != -1) {
            int apartmentId = (int) apartmentTable.getValueAt(selectedRow, 0);

            // Fetch the apartment by ID
            Apartment selectedApartment = apartmentController.getApartmentById(apartmentId);

            // Create a new ApartmentBookingRequest
            if (selectedApartment != null) {
                // Create a new ApartmentBookingRequest with the necessary details
                String agentUsername = selectedApartment.getAgentUsername();  // Assuming you have this information in the apartment
                String message = "Booking request for " + selectedApartment.getTitle();
                
                ApartmentBookingRequest bookingRequest = new ApartmentBookingRequest(apartmentId, user.getUsername(), agentUsername, message);

                // Add the booking request to the BookingController
                bookingController.addBookingRequest(bookingRequest);

                // Show success message
                JOptionPane.showMessageDialog(this, "Apartment booked successfully!", "Booking Success", JOptionPane.INFORMATION_MESSAGE);

                // Optionally, refresh the listings after booking
                refreshListings();
            }
        }
    }

    // Custom TableCellRenderer for displaying images in the table
    class ImageRenderer extends JLabel implements TableCellRenderer {
        public ImageRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof ImageIcon) {
                setIcon((ImageIcon) value);
            }
            return this;
        }
    }
}

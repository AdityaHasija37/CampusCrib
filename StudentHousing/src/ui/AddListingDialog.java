package ui;

import com.apartmentbooking.controller.ApartmentController;
import com.apartmentbooking.controller.BookingController;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import models.Apartment;
import models.UserAccount;

public class AddListingDialog extends JDialog {
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JTextField rentField;
    private JTextField facilitiesField;
    private JLabel imageLabel;
    private JButton chooseImageButton;
    private JButton addButton;
    private JButton cancelButton;
    private UserAccount user;
    private ApartmentController apartmentController;
    private boolean listingAdded = false;
    private File selectedImage;

    public AddListingDialog(JFrame parent, UserAccount user, ApartmentController apartmentController, BookingController bookingController) {
        super(parent, "Add New Listing", true);
        this.user = user;  // Use UserAccount instead of String username
        this.apartmentController = apartmentController;
        // You may not need bookingController in this class, but it's included for consistency
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setSize(400, 600); // Increased the height to accommodate the larger description area
        setLocationRelativeTo(getParent());

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        titleField = new JTextField(20);
        descriptionArea = new JTextArea(10, 20); // Increased rows from 5 to 10
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
        descriptionScrollPane.setPreferredSize(new Dimension(350, 150)); // Set preferred size for the scroll pane
        rentField = new JTextField(10);
        facilitiesField = new JTextField(20);
        imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(200, 150));
        chooseImageButton = new JButton("Choose Image");

        formPanel.add(new JLabel("Title:"), gbc);
        formPanel.add(titleField, gbc);
        formPanel.add(new JLabel("Description:"), gbc);
        formPanel.add(descriptionScrollPane, gbc); // Added the scroll pane instead of the text area directly
        formPanel.add(new JLabel("Rent:"), gbc);
        formPanel.add(rentField, gbc);
        formPanel.add(new JLabel("Facilities:"), gbc);
        formPanel.add(facilitiesField, gbc);
        formPanel.add(new JLabel("Image:"), gbc);
        formPanel.add(imageLabel, gbc);
        formPanel.add(chooseImageButton, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add Listing");
        cancelButton = new JButton("Cancel");
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);

        chooseImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(AddListingDialog.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedImage = fileChooser.getSelectedFile();
                    ImageIcon icon = new ImageIcon(selectedImage.getPath());
                    Image image = icon.getImage().getScaledInstance(200, 150, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(image));
                }
            }
        });

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addListing();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void addListing() {
        String title = titleField.getText();
        String description = descriptionArea.getText();
        double rent;
        try {
            rent = Double.parseDouble(rentField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid rent amount", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String facilities = facilitiesField.getText();

        if (title.isEmpty() || description.isEmpty() || facilities.isEmpty() || selectedImage == null) {
            JOptionPane.showMessageDialog(this, "Please fill all fields and select an image", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ImageIcon image = new ImageIcon(selectedImage.getPath());
        Image scaledImage = image.getImage().getScaledInstance(200, 150, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        Apartment newApartment = new Apartment(
            apartmentController.getNextId(),
            title,
            description,
            rent,
            facilities,
            scaledIcon,
            user.getUsername()  // Correct usage of UserAccount's username
        );

        apartmentController.addApartment(newApartment);
        listingAdded = true;
        JOptionPane.showMessageDialog(this, "Listing added successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    public boolean isListingAdded() {
        return listingAdded;
    }
}

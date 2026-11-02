package com.campuscrib.ui;

import com.campuscrib.domain.Apartment;
import com.campuscrib.domain.ApartmentDraft;
import com.campuscrib.domain.Photo;
import com.campuscrib.domain.User;
import com.campuscrib.service.Services;
import com.campuscrib.service.ValidationException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Create or edit a listing. Photos can be added once the listing has been saved. */
public class ListingDialog extends JDialog {
    private final Services services;
    private final User agent;
    private Long id;
    private Long savedId;
    private final JTextField code = new JTextField();
    private final JTextField title = new JTextField();
    private final JTextField neighborhood = new JTextField();
    private final JTextField address = new JTextField();
    private final JTextArea description = new JTextArea(4, 20);
    private final JSpinner rent = new JSpinner(new SpinnerNumberModel(2000, 100, 20000, 50));
    private final JSpinner bedrooms = new JSpinner(new SpinnerNumberModel(1, 0, 10, 1));
    private final JSpinner bathrooms = new JSpinner(new SpinnerNumberModel(1, 1, 6, 1));
    private final JSpinner distance = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 50.0, 0.1));
    private final JSpinner available = Forms.datePicker(LocalDate.now());
    private final List<JCheckBox> amenities = new ArrayList<>();
    private final JPanel photoStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

    public ListingDialog(Window owner, Services services, User agent, Long id) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.services = services;
        this.agent = agent;
        this.id = id;
        setTitle(id == null ? "New listing" : "Edit listing");
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        code.putClientProperty("JTextField.placeholderText", "A101");

        JPanel amenityGrid = new JPanel(new GridLayout(0, 2, 8, 4));
        amenityGrid.setOpaque(false);
        for (String a : Apartment.COMMON_AMENITIES) {
            JCheckBox cb = new JCheckBox(a);
            cb.setOpaque(false);
            amenities.add(cb);
            amenityGrid.add(cb);
        }

        JPanel top = new JPanel(new GridLayout(1, 2, 16, 0));
        top.setOpaque(false);
        top.add(Forms.field("Unit code", code));
        top.add(Forms.field("Neighborhood", neighborhood));
        JPanel numbers = new JPanel(new GridLayout(1, 5, 12, 0));
        numbers.setOpaque(false);
        numbers.add(Forms.field("Rent / month ($)", rent));
        numbers.add(Forms.field("Bedrooms", bedrooms));
        numbers.add(Forms.field("Bathrooms", bathrooms));
        numbers.add(Forms.field("Km to campus", distance));
        numbers.add(Forms.field("Available from", available));

        photoStrip.setOpaque(false);
        JButton addPhoto = Ui.secondary("Add photo…");
        addPhoto.addActionListener(e -> addPhoto());

        JPanel form = Ui.column(
                Forms.field("Title", title), top, Forms.field("Street address", address),
                Forms.field("Description", new JScrollPane(description)), numbers,
                Forms.label("Amenities"), Ui.gap(6), amenityGrid, Ui.gap(16),
                Forms.label("Photos"), Ui.gap(6), photoStrip, Ui.gap(8), addPhoto);

        JButton save = Ui.primary("Save listing");
        save.addActionListener(e -> save());
        JButton cancel = Ui.secondary("Close");
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(cancel);
        buttons.add(save);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(Theme.CREAM);
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));
        root.add(Ui.title(id == null ? "New listing" : "Edit listing"), BorderLayout.NORTH);
        root.add(Ui.scroll(form), BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
        if (id != null) load(services.listings.get(id));
        refreshPhotos();
        setSize(820, 780);
        setLocationRelativeTo(owner);
    }

    public Long savedId() {
        return savedId;
    }

    private void load(Apartment a) {
        code.setText(a.code());
        title.setText(a.title());
        neighborhood.setText(a.neighborhood());
        address.setText(a.address());
        description.setText(a.description());
        rent.setValue(a.monthlyRent().intValue());
        bedrooms.setValue(a.bedrooms());
        bathrooms.setValue(a.bathrooms());
        distance.setValue(a.distanceKm().doubleValue());
        available.setValue(java.util.Date.from(a.availableFrom().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
        for (JCheckBox cb : amenities) cb.setSelected(a.amenities().contains(cb.getText()));
    }

    private ApartmentDraft draft() {
        return new ApartmentDraft(code.getText(), title.getText(), neighborhood.getText(), address.getText(), description.getText(),
                BigDecimal.valueOf(((Number) rent.getValue()).longValue()), (Integer) bedrooms.getValue(), (Integer) bathrooms.getValue(),
                BigDecimal.valueOf(((Number) distance.getValue()).doubleValue()).setScale(1, java.math.RoundingMode.HALF_UP),
                amenities.stream().filter(JCheckBox::isSelected).map(JCheckBox::getText).toList(), Forms.date(available));
    }

    private void save() {
        Apartment saved = Ui.fetch(this, () -> id == null ? services.listings.create(agent, draft()) : services.listings.update(agent, id, draft()));
        if (saved == null) return;
        boolean wasNew = id == null;
        id = saved.id();
        savedId = saved.id();
        if (wasNew) {
            setTitle("Edit listing");
            refreshPhotos();
            JOptionPane.showMessageDialog(this, "Listing saved. Add a few photos so students can see the place.");
        } else {
            dispose();
        }
    }

    private void addPhoto() {
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Save the listing first, then add photos.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Images (JPG, PNG)", "jpg", "jpeg", "png"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            byte[] bytes = Files.readAllBytes(chooser.getSelectedFile().toPath());
            String caption = JOptionPane.showInputDialog(this, "Caption (e.g. Kitchen)", "Photo");
            if (Ui.attempt(this, () -> services.listings.addPhoto(agent, id, caption, bytes))) refreshPhotos();
        } catch (IOException ex) {
            Ui.attempt(this, () -> {
                throw new ValidationException("Couldn't read that file: " + ex.getMessage());
            });
        }
    }

    private void refreshPhotos() {
        photoStrip.removeAll();
        List<Photo> photos = id == null ? List.of() : services.listings.photos(id);
        if (photos.isEmpty()) photoStrip.add(Ui.muted(id == null ? "Save the listing to add photos." : "No photos yet."));
        for (Photo p : photos) {
            JLabel img = new JLabel(Ui.image(p.image(), 120, 80));
            JButton remove = Ui.secondary("Remove");
            remove.setFont(Theme.sans(java.awt.Font.PLAIN, 11));
            remove.addActionListener(e -> {
                if (Ui.attempt(this, () -> services.listings.deletePhoto(agent, p))) refreshPhotos();
            });
            JComponent tile = Ui.column(img, Ui.gap(4), Ui.muted(p.caption()), Ui.gap(4), remove);
            photoStrip.add(tile);
        }
        photoStrip.setPreferredSize(new Dimension(700, photos.isEmpty() ? 24 : 150));
        photoStrip.revalidate();
        photoStrip.repaint();
    }
}

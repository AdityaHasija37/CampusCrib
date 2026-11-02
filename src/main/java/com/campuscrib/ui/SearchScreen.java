package com.campuscrib.ui;

import com.campuscrib.domain.Apartment;
import com.campuscrib.domain.SearchCriteria;
import com.campuscrib.domain.User;
import com.campuscrib.service.Services;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/** Students: filter on the left, photo cards on the right. Click a card for photos and booking. */
public class SearchScreen implements Screen {
    private final Services services;
    private final User student;
    private final JTextField text = new JTextField();
    private final JSlider maxRent = new JSlider(1000, 5000, 5000);
    private final JLabel maxRentLabel = new JLabel();
    private final JComboBox<String> bedrooms = new JComboBox<>(new String[] {"Any", "1+", "2+", "3+"});
    private final JSlider maxDistance = new JSlider(1, 10, 10);
    private final JLabel maxDistanceLabel = new JLabel();
    private final JComboBox<SearchCriteria.Sort> sort = new JComboBox<>(SearchCriteria.Sort.values());
    private final List<JCheckBox> amenities = new ArrayList<>();
    private final JPanel results = new JPanel(new GridLayout(0, 2, 16, 16));
    private final JLabel count = Ui.muted("");
    private final JComponent view;
    private final Timer debounce;

    public SearchScreen(Services services, User student) {
        this.services = services;
        this.student = student;
        results.setOpaque(false);
        debounce = new Timer(250, e -> refresh());
        debounce.setRepeats(false);

        text.putClientProperty("JTextField.placeholderText", "Neighborhood, street or keyword");
        text.putClientProperty("JTextField.showClearButton", true);
        text.getDocument().addDocumentListener(new SimpleDocumentListener(debounce::restart));
        maxRent.addChangeListener(e -> {
            updateLabels();
            if (!maxRent.getValueIsAdjusting()) refresh();
        });
        maxDistance.addChangeListener(e -> {
            updateLabels();
            if (!maxDistance.getValueIsAdjusting()) refresh();
        });
        bedrooms.addActionListener(e -> refresh());
        sort.addActionListener(e -> refresh());
        updateLabels();

        JPanel amenityBox = new JPanel(new GridLayout(0, 1, 0, 2));
        amenityBox.setOpaque(false);
        for (String a : Apartment.COMMON_AMENITIES) {
            JCheckBox cb = new JCheckBox(a);
            cb.setOpaque(false);
            cb.addActionListener(e -> refresh());
            amenities.add(cb);
            amenityBox.add(cb);
        }
        JButton clear = Ui.secondary("Clear filters");
        clear.addActionListener(e -> clear());

        JPanel filters = Ui.column(
                label("Search"), Ui.gap(6), text, Ui.gap(16),
                label("Max rent"), maxRentLabel, maxRent, Ui.gap(12),
                label("Bedrooms"), Ui.gap(6), bedrooms, Ui.gap(16),
                label("Distance to campus"), maxDistanceLabel, maxDistance, Ui.gap(12),
                label("Must have"), Ui.gap(4), amenityBox, Ui.gap(16),
                label("Sort by"), Ui.gap(6), sort, Ui.gap(18),
                clear);
        for (JComponent c : new JComponent[] {text, bedrooms, sort}) c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        RoundedPanel filterCard = Ui.card(filters);
        filterCard.setPreferredSize(new Dimension(260, 0));

        JPanel right = new JPanel(new BorderLayout(0, 12));
        right.setOpaque(false);
        right.add(count, BorderLayout.NORTH);
        WidthTrackingPanel resultsWrap = new WidthTrackingPanel(new BorderLayout());
        resultsWrap.add(results, BorderLayout.NORTH);
        right.add(Ui.scroll(resultsWrap), BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(20, 0));
        body.setOpaque(false);
        body.add(Ui.scroll(filterCard), BorderLayout.WEST);
        body.add(right, BorderLayout.CENTER);
        view = Ui.screen("Find a place", "Apartments near campus, listed by real estate agents.", null, body);
    }

    @Override
    public String label() {
        return "Find a place";
    }

    @Override
    public JComponent view() {
        return view;
    }

    @Override
    public void refresh() {
        int beds = bedrooms.getSelectedIndex();
        SearchCriteria c = new SearchCriteria(
                text.getText(),
                maxRent.getValue() >= maxRent.getMaximum() ? null : BigDecimal.valueOf(maxRent.getValue()),
                beds == 0 ? null : beds,
                maxDistance.getValue() >= maxDistance.getMaximum() ? null : BigDecimal.valueOf(maxDistance.getValue()),
                amenities.stream().filter(JCheckBox::isSelected).map(JCheckBox::getText).toList(),
                (SearchCriteria.Sort) sort.getSelectedItem());
        List<Apartment> found = Ui.fetch(view, () -> services.listings.search(c));
        if (found == null) return;
        results.removeAll();
        count.setText(found.size() == 1 ? "1 apartment" : found.size() + " apartments");
        if (found.isEmpty()) {
            results.setLayout(new BorderLayout());
            results.add(Ui.empty("Nothing matches those filters.", "Try a higher rent, fewer must-haves or a longer distance."));
        } else {
            results.setLayout(new GridLayout(0, 2, 16, 16));
            for (Apartment a : found) results.add(card(a));
        }
        results.revalidate();
        results.repaint();
    }

    private JComponent card(Apartment a) {
        JLabel photo = new JLabel(Ui.image(services.listings.cover(a.id()).orElse(null), 340, 200));
        JLabel price = new JLabel("<html><span style='font-size:15px'><b>" + Ui.money(a.monthlyRent()) + "</b></span><span style='color:#6B625B'> / month</span></html>");
        price.setForeground(Theme.INK);
        JLabel title = Ui.heading(a.title());
        JLabel meta = Ui.muted(a.neighborhood() + " · " + beds(a) + " · " + a.bathrooms() + " bath · " + a.distanceKm() + " km to campus");
        JLabel amen = new JLabel(String.join("  ·  ", a.amenities()));
        amen.setFont(Theme.sans(Font.PLAIN, 12));
        amen.setForeground(Theme.SAGE);
        JPanel content = Ui.column(photo, Ui.gap(12), price, Ui.gap(4), title, Ui.gap(4), meta, Ui.gap(8), amen);
        RoundedPanel card = Ui.card(content);
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 14, 12));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setToolTipText("See photos and request " + a.title());
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new ApartmentDialog(SwingUtilities.getWindowAncestor(view), services, student, a.id(), SearchScreen.this::refresh).setVisible(true);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setColors(Theme.SURFACE, Theme.BRICK);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setColors(Theme.SURFACE, Theme.LINE);
            }
        });
        return card;
    }

    static String beds(Apartment a) {
        return a.bedrooms() == 0 ? "Studio" : a.bedrooms() + (a.bedrooms() == 1 ? " bed" : " beds");
    }

    /** For screenshots and tests: open the first result's detail dialog. */
    public ApartmentDialog openFirst() {
        List<Apartment> found = services.listings.search(SearchCriteria.any());
        return new ApartmentDialog(SwingUtilities.getWindowAncestor(view), services, student, found.get(0).id(), this::refresh);
    }

    private void updateLabels() {
        maxRentLabel.setText(maxRent.getValue() >= maxRent.getMaximum() ? "Any" : "Up to " + Ui.money(BigDecimal.valueOf(maxRent.getValue())));
        maxDistanceLabel.setText(maxDistance.getValue() >= maxDistance.getMaximum() ? "Any distance" : "Within " + maxDistance.getValue() + " km");
        maxRentLabel.setForeground(Theme.MUTED);
        maxDistanceLabel.setForeground(Theme.MUTED);
    }

    private void clear() {
        text.setText("");
        maxRent.setValue(maxRent.getMaximum());
        maxDistance.setValue(maxDistance.getMaximum());
        bedrooms.setSelectedIndex(0);
        sort.setSelectedIndex(0);
        amenities.forEach(cb -> cb.setSelected(false));
        refresh();
    }

    private static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.sans(Font.BOLD, 13));
        l.setForeground(Theme.INK);
        return l;
    }
}

package models;

import javax.swing.ImageIcon;

public class Apartment {
    private int id;  // Apartment ID
    private String title;
    private String description;
    private double rent;
    private String facilities;
    private ImageIcon image;
    private String agentUsername;  // Username of the agent who listed the apartment

    // Constructor
    public Apartment(int id, String title, String description, double rent, String facilities, ImageIcon image, String agentUsername) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.rent = rent;
        this.facilities = facilities;
        this.image = image;
        this.agentUsername = agentUsername;
    }

    // Getter and Setter for ID
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getters and Setters for other fields
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getRent() {
        return rent;
    }

    public void setRent(double rent) {
        this.rent = rent;
    }

    public String getFacilities() {
        return facilities;
    }

    public void setFacilities(String facilities) {
        this.facilities = facilities;
    }

    public ImageIcon getImage() {
        return image;
    }

    public void setImage(ImageIcon image) {
        this.image = image;
    }

    public String getAgentUsername() {
        return agentUsername;
    }

    public void setAgentUsername(String agentUsername) {
        this.agentUsername = agentUsername;
    }
}

package com.apartmentbooking.controller;

import java.util.ArrayList;
import java.util.List;
import models.Apartment;
import models.UserAccount;

public class ApartmentController {
    private List<Apartment> apartments;
    private List<UserAccount> users;
    private int nextId;

    public ApartmentController() {
        apartments = new ArrayList<>();
        users = new ArrayList<>();  // Assuming you need to manage users
        nextId = 1;
    }

    /**
     * Fetches apartments listed by a specific agent (by username).
     * @param agent The user object representing the agent
     * @return List of apartments listed by the agent
     */
    public List<Apartment> getApartmentsByAgent(UserAccount agent) {
        List<Apartment> agentApartments = new ArrayList<>();
        for (Apartment apartment : apartments) {
            if (apartment.getAgentUsername().equals(agent.getUsername())) {
                agentApartments.add(apartment);
            }
        }
        return agentApartments;
    }

    /**
     * Fetches all apartments (for non-agent users).
     * @return List of all apartments
     */
    public List<Apartment> getAllApartments() {
        return new ArrayList<>(apartments);
    }

    /**
     * Adds a new apartment listing (for agents).
     * @param apartment The apartment to be added
     */
    public void addApartment(Apartment apartment) {
        apartment.setId(getNextId());  // Setting the next available ID
        apartments.add(apartment);
    }

    /**
     * Generates the next available ID for apartments.
     * @return The next available ID
     */
    public int getNextId() {
        return nextId++;
    }

    /**
     * Prepopulates the list of users if needed (e.g., loading from DB or testing).
     * @param userList The list of UserAccount objects to populate
     */
    public void setUsers(List<UserAccount> userList) {
        this.users = userList;
    }
    public Apartment getApartmentById(int id) {
        for (Apartment apartment : apartments) {
            if (apartment.getId() == id) {
                return apartment;  // Return the apartment if it matches the ID
            }
        }
        return null;  // Return null if no apartment with the given ID is found
    }
    
    /**
     * Fetches a user by username
     * @param username The username of the user
     * @return The user account, or null if not found
     */
    public UserAccount getUserByUsername(String username) {
        for (UserAccount user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;  // Return null if no user found
    }
}

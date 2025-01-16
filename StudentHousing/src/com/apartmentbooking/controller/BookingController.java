package com.apartmentbooking.controller;

import models.AmenityBookingRequest;
import models.ApartmentBookingRequest;
import models.WorkRequest;
import models.UserAccount;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import models.Apartment;


public class BookingController {
    private List<ApartmentBookingRequest> bookingRequests;
    private int nextId;

    public BookingController() {
        bookingRequests = new ArrayList<>();
        nextId = 1;
    }

    // Get booking requests for a specific user by their username
    public List<ApartmentBookingRequest> getUserBookingRequests(UserAccount user) {
        List<ApartmentBookingRequest> userRequests = new ArrayList<>();
        for (ApartmentBookingRequest request : bookingRequests) {
            if (request.getUserUsername().equals(user.getUsername())) {
                userRequests.add(request);
            }
        }
        return userRequests;
    }

    // Get booking requests for a specific agent by their username
    public List<ApartmentBookingRequest> getAgentBookingRequests(UserAccount agent) {
        List<ApartmentBookingRequest> agentRequests = new ArrayList<>();
        for (ApartmentBookingRequest request : bookingRequests) {
            if (request.getAgentUsername().equals(agent.getUsername())) {
                agentRequests.add(request);
            }
        }
        return agentRequests;
    }

    // Add a new booking request to the list
    public void addBookingRequest(ApartmentBookingRequest request) {
        request.setId(nextId++);
        bookingRequests.add(request);
    }

    // Update the status and comment of an existing booking request
    public void updateBookingRequest(int requestId, String status, String comment) {
        boolean updated = false;
        for (ApartmentBookingRequest request : bookingRequests) {
            if (request.getId() == requestId) {
                request.setStatus(status);
                request.setComment(comment);
                updated = true;
                break;
            }
        }
        if (!updated) {
            System.out.println("Booking request with ID " + requestId + " not found.");
        }
    }
    
}

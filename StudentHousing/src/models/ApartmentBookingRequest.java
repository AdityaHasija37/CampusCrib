package models;

/**
 * Represents a booking request for an apartment.
 */
public class ApartmentBookingRequest {
    private int id;
    private int apartmentId;
    private String userUsername;
    private String agentUsername;
    private String status;
    private String message;
    private String comment;

    /**
     * Constructor to create a new apartment booking request.
     * @param apartmentId ID of the apartment being booked.
     * @param userUsername Username of the user making the booking.
     * @param agentUsername Username of the agent handling the apartment.
     * @param message A message from the user related to the booking.
     */
    public ApartmentBookingRequest(int apartmentId, String userUsername, String agentUsername, String message) {
        this.apartmentId = apartmentId;
        this.userUsername = userUsername;
        this.agentUsername = agentUsername;
        this.message = message;
        this.status = "Pending";  // Default status when request is created
        this.comment = "";       // Empty comment by default
    }

    // Getters and setters for all fields

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getApartmentId() {
        return apartmentId;
    }

    public String getUserUsername() {
        return userUsername;
    }

    public String getAgentUsername() {
        return agentUsername;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

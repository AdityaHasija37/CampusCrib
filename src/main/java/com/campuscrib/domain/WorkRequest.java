package com.campuscrib.domain;

import java.time.Instant;
import java.time.LocalDateTime;

public record WorkRequest(
        long id,
        long residentId,
        String residentName,
        RequestCategory category,
        Trade trade,
        Amenity amenity,
        LocalDateTime slotStart,
        Integer slotHours,
        String title,
        String description,
        String unit,
        Priority priority,
        RequestStatus status,
        Long assigneeId,
        String assigneeName,
        Instant createdAt,
        Instant updatedAt,
        Instant closedAt) {

    /** Who is expected to act on this request next, in plain words. */
    public String routedTo() {
        return switch (category) {
            case MAINTENANCE -> assigneeName != null ? assigneeName : Role.forTrade(trade).organization();
            case AMENITY, INQUIRY -> "Building management";
        };
    }

    /** The specific thing requested: the trade, the amenity, or "Inquiry". */
    public String subject() {
        return switch (category) {
            case MAINTENANCE -> trade.displayName();
            case AMENITY -> amenity.toString();
            case INQUIRY -> "Inquiry";
        };
    }
}

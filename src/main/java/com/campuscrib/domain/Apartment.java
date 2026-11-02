package com.campuscrib.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record Apartment(
        long id,
        long agentId,
        String agentName,
        String code,
        String title,
        String neighborhood,
        String address,
        String description,
        BigDecimal monthlyRent,
        int bedrooms,
        int bathrooms,
        BigDecimal distanceKm,
        List<String> amenities,
        LocalDate availableFrom,
        ApartmentStatus status) {

    /** The amenities residents commonly filter on. Listings may include others. */
    public static final List<String> COMMON_AMENITIES =
            List.of("In-unit laundry", "Dishwasher", "Furnished", "Heat included", "Gym", "Parking", "Pets allowed", "Study room");
}

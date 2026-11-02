package com.campuscrib.domain;

import java.time.LocalDateTime;

/** What a resident fills in. Trade is required for maintenance; amenity and slot for amenity bookings. */
public record NewRequest(
        RequestCategory category,
        Trade trade,
        Amenity amenity,
        LocalDateTime slotStart,
        Integer slotHours,
        String title,
        String description,
        Priority priority) {}

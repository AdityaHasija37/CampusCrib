package com.campuscrib.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** What an agent fills in to create or edit a listing. */
public record ApartmentDraft(
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
        LocalDate availableFrom) {}

package com.campuscrib.domain;

import java.math.BigDecimal;
import java.util.List;

/** Student search filters. Null or empty values mean "any". */
public record SearchCriteria(String text, BigDecimal maxRent, Integer minBedrooms, BigDecimal maxDistanceKm, List<String> amenities, Sort sort) {

    public enum Sort {
        RENT_LOW("Rent: low to high"),
        RENT_HIGH("Rent: high to low"),
        CLOSEST("Closest to campus"),
        NEWEST("Newest");

        private final String displayName;

        Sort(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public static SearchCriteria any() {
        return new SearchCriteria(null, null, null, null, List.of(), Sort.RENT_LOW);
    }
}

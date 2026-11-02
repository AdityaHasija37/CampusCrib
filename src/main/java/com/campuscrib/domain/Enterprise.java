package com.campuscrib.domain;

/** The enterprises in the CampusCrib network, as in the original Group 60 design. */
public enum Enterprise {
    NETWORK("CampusCrib Network"),
    CUSTOMER("Customer"),
    REAL_ESTATE_AGENCY("Real Estate Agency"),
    RESIDENT_ASSOCIATION("Resident Association"),
    BUILDING_MANAGEMENT("Building Management");

    private final String displayName;

    Enterprise(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}

package com.campuscrib.domain;

public enum RequestCategory {
    MAINTENANCE("Maintenance"),
    AMENITY("Amenity booking"),
    INQUIRY("General inquiry");

    private final String displayName;

    RequestCategory(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

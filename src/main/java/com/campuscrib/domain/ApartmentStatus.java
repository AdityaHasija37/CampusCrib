package com.campuscrib.domain;

public enum ApartmentStatus {
    LISTED("Listed"),
    LEASED("Leased"),
    UNLISTED("Unlisted");

    private final String displayName;

    ApartmentStatus(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

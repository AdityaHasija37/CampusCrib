package com.campuscrib.domain;

/** Shared building spaces residents can book. */
public enum Amenity {
    GYM("Gym"),
    STUDY_ROOM("Study room"),
    LOUNGE("Lounge"),
    ROOFTOP("Rooftop terrace"),
    LAUNDRY("Laundry room");

    private final String displayName;

    Amenity(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

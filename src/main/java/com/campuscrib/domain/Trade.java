package com.campuscrib.domain;

/** Maintenance trades. Each one is handled by its own service organization. */
public enum Trade {
    PLUMBING("Plumbing"),
    ELECTRICAL("Electrical"),
    CARPENTRY("Carpentry"),
    PEST_CONTROL("Pest control");

    private final String displayName;

    Trade(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

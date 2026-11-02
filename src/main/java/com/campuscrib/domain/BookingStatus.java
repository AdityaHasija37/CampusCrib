package com.campuscrib.domain;

public enum BookingStatus {
    PENDING("Pending"),
    APPROVED("Approved"),
    DECLINED("Declined"),
    WITHDRAWN("Withdrawn");

    private final String displayName;

    BookingStatus(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

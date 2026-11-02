package com.campuscrib.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Lifecycle of a work request. Which transitions are allowed depends on the category:
 * <ul>
 *   <li>Maintenance: SUBMITTED → ACCEPTED → IN_PROGRESS → COMPLETED (service crew)</li>
 *   <li>Amenity booking: SUBMITTED → APPROVED | DECLINED (building manager)</li>
 *   <li>Inquiry: SUBMITTED → ANSWERED (building manager)</li>
 * </ul>
 * Anything still open can be CANCELLED by the resident who raised it or by the building manager.
 */
public enum RequestStatus {
    SUBMITTED("Submitted"),
    ACCEPTED("Accepted"),
    IN_PROGRESS("In progress"),
    COMPLETED("Completed"),
    APPROVED("Approved"),
    DECLINED("Declined"),
    ANSWERED("Answered"),
    CANCELLED("Cancelled");

    private static final Set<RequestStatus> CLOSED = EnumSet.of(COMPLETED, APPROVED, DECLINED, ANSWERED, CANCELLED);

    private final String displayName;

    RequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public boolean isClosed() {
        return CLOSED.contains(this);
    }

    /** Statuses a request of this category can move to next, before checking who is asking. */
    public Set<RequestStatus> next(RequestCategory category) {
        if (isClosed()) return EnumSet.noneOf(RequestStatus.class);
        return switch (category) {
            case MAINTENANCE -> switch (this) {
                case SUBMITTED -> EnumSet.of(ACCEPTED, CANCELLED);
                case ACCEPTED -> EnumSet.of(IN_PROGRESS, CANCELLED);
                case IN_PROGRESS -> EnumSet.of(COMPLETED);
                default -> EnumSet.noneOf(RequestStatus.class);
            };
            case AMENITY -> this == SUBMITTED ? EnumSet.of(APPROVED, DECLINED, CANCELLED) : EnumSet.noneOf(RequestStatus.class);
            case INQUIRY -> this == SUBMITTED ? EnumSet.of(ANSWERED, CANCELLED) : EnumSet.noneOf(RequestStatus.class);
        };
    }

    @Override
    public String toString() {
        return displayName;
    }
}

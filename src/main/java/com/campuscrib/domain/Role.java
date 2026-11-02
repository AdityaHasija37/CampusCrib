package com.campuscrib.domain;

import java.util.Arrays;
import java.util.Optional;

/** Every role belongs to one enterprise and organization; service roles also carry their trade. */
public enum Role {
    STUDENT("Student", Enterprise.CUSTOMER, "Students", null),
    RESIDENT("Resident", Enterprise.RESIDENT_ASSOCIATION, "Residents", null),
    AGENT("Real estate agent", Enterprise.REAL_ESTATE_AGENCY, "Leasing", null),
    BUILDING_MANAGER("Building manager", Enterprise.BUILDING_MANAGEMENT, "Management", null),
    PLUMBER("Plumber", Enterprise.BUILDING_MANAGEMENT, "Plumbing services", Trade.PLUMBING),
    ELECTRICIAN("Electrician", Enterprise.BUILDING_MANAGEMENT, "Electrical services", Trade.ELECTRICAL),
    CARPENTER("Carpenter", Enterprise.BUILDING_MANAGEMENT, "Carpentry services", Trade.CARPENTRY),
    PEST_CONTROL("Pest control technician", Enterprise.BUILDING_MANAGEMENT, "Pest control services", Trade.PEST_CONTROL),
    ADMIN("System admin", Enterprise.NETWORK, "Administration", null);

    private final String displayName;
    private final Enterprise enterprise;
    private final String organization;
    private final Trade trade;

    Role(String displayName, Enterprise enterprise, String organization, Trade trade) {
        this.displayName = displayName;
        this.enterprise = enterprise;
        this.organization = organization;
        this.trade = trade;
    }

    public String displayName() {
        return displayName;
    }

    public Enterprise enterprise() {
        return enterprise;
    }

    public String organization() {
        return organization;
    }

    /** The trade a service role handles, if it is one. */
    public Optional<Trade> trade() {
        return Optional.ofNullable(trade);
    }

    public boolean isServiceProvider() {
        return trade != null;
    }

    /** The service role responsible for a trade. */
    public static Role forTrade(Trade trade) {
        return Arrays.stream(values()).filter(r -> r.trade == trade).findFirst().orElseThrow();
    }

    @Override
    public String toString() {
        return displayName;
    }
}

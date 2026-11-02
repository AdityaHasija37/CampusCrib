package com.campuscrib.domain;

import java.time.Instant;

public record User(long id, String username, String fullName, String email, Role role, String unit, boolean active, Instant createdAt) {

    public String firstName() {
        int space = fullName.indexOf(' ');
        return space > 0 ? fullName.substring(0, space) : fullName;
    }
}

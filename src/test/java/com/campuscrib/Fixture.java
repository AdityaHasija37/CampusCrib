package com.campuscrib;

import com.campuscrib.db.Database;
import com.campuscrib.domain.Apartment;
import com.campuscrib.domain.ApartmentDraft;
import com.campuscrib.domain.NewRequest;
import com.campuscrib.domain.Priority;
import com.campuscrib.domain.RequestCategory;
import com.campuscrib.domain.Role;
import com.campuscrib.domain.Trade;
import com.campuscrib.domain.User;
import com.campuscrib.service.PasswordHasher;
import com.campuscrib.service.Services;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/** Shared setup: a database, services on a fixed clock, and helpers to create people and listings. */
public class Fixture {
    public static final Instant NOW = Instant.parse("2026-09-01T15:00:00Z");
    public static final ZoneId ZONE = ZoneId.of("America/New_York");
    public static final LocalDate TODAY = LocalDate.ofInstant(NOW, ZONE);

    public final Database db = TestDb.fresh();
    public final Services s = new Services(db, Clock.fixed(NOW, ZONE));
    private int n;

    public User person(Role role) {
        return person(role, role == Role.RESIDENT ? "B-204" : null);
    }

    public User person(Role role, String unit) {
        n++;
        String username = role.name().toLowerCase() + n;
        long id = s.userRepository().insert(username, PasswordHasher.hash("password123"), "Person " + n, username + "@example.test", role, unit);
        return s.userRepository().findById(id).orElseThrow();
    }

    public static ApartmentDraft draft(String code, int rent) {
        return new ApartmentDraft(code, "Bright flat " + code, "Fenway", "1 Test Street, Boston", "A bright and quiet flat close to campus.",
                BigDecimal.valueOf(rent), 2, 1, new BigDecimal("1.0"), List.of("Dishwasher", "Gym"), TODAY);
    }

    public Apartment listing(User agent, String code, int rent) {
        return s.listings.create(agent, draft(code, rent));
    }

    public static NewRequest maintenance(Trade trade) {
        return new NewRequest(RequestCategory.MAINTENANCE, trade, null, null, null, "Something broke", "It stopped working this morning.", Priority.NORMAL);
    }
}

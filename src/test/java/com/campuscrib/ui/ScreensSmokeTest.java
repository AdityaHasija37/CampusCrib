package com.campuscrib.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.campuscrib.TestDb;
import com.campuscrib.db.Database;
import com.campuscrib.domain.Role;
import com.campuscrib.domain.User;
import com.campuscrib.seed.DemoData;
import com.campuscrib.service.Services;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Signs in as every demo role and loads every screen it can reach, headless. */
class ScreensSmokeTest {
    static Services services;

    @BeforeAll
    static void seed() {
        Database db = TestDb.fresh();
        services = new Services(db, Clock.systemDefaultZone());
        DemoData.load(db, services);
        Theme.install();
    }

    @Test
    void everyRoleSeesItsScreens() throws Exception {
        Map<String, List<String>> expected = Map.of(
                "ananya", List.of("Find a place", "My bookings"),
                "sofia", List.of("Booking requests", "My listings"),
                "jordan", List.of("My requests"),
                "tom", List.of("Work queue"),
                "helen", List.of("All requests", "Reports"),
                "admin", List.of("People", "Reports"));
        for (var e : expected.entrySet()) {
            User u = services.auth.login(e.getKey(), DemoData.PASSWORD);
            SwingUtilities.invokeAndWait(() -> {
                Shell shell = new Shell(services, u, () -> {});
                assertEquals(e.getValue(), shell.screens().stream().map(Screen::label).toList(), u.username());
                for (int i = 0; i < shell.screens().size(); i++) shell.show(i);
            });
        }
    }

    @Test
    void everyServiceRoleHasAQueue() throws Exception {
        for (Role r : Role.values()) {
            if (!r.isServiceProvider()) continue;
            User u = services.users.byRole(r).get(0);
            SwingUtilities.invokeAndWait(() -> {
                Shell shell = new Shell(services, u, () -> {});
                shell.show(0);
                assertFalse(shell.screens().isEmpty());
            });
        }
    }
}

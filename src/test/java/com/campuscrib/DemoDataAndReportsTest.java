package com.campuscrib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.campuscrib.db.Database;
import com.campuscrib.db.Migrator;
import com.campuscrib.domain.RequestCategory;
import com.campuscrib.domain.Role;
import com.campuscrib.domain.SearchCriteria;
import com.campuscrib.domain.Trade;
import com.campuscrib.domain.User;
import com.campuscrib.seed.DemoData;
import com.campuscrib.service.AccessDeniedException;
import com.campuscrib.service.Services;
import java.time.Clock;
import org.junit.jupiter.api.Test;

class DemoDataAndReportsTest {

    @Test
    void demoDataGivesEveryRoleSomethingToDo() {
        Database db = TestDb.fresh();
        Services s = new Services(db, Clock.systemDefaultZone());
        assertTrue(DemoData.isEmpty(s));
        DemoData.load(db, s);
        assertFalse(DemoData.isEmpty(s));

        User student = s.auth.login("ananya", DemoData.PASSWORD);
        User agent = s.auth.login("sofia", DemoData.PASSWORD);
        User resident = s.auth.login("jordan", DemoData.PASSWORD);
        User plumber = s.auth.login("tom", DemoData.PASSWORD);
        User manager = s.auth.login("helen", DemoData.PASSWORD);

        assertEquals(4, s.listings.search(SearchCriteria.any()).size()); // B201 is leased
        assertEquals(4, s.listings.photos(s.listings.search(SearchCriteria.any()).get(0).id()).size());
        assertEquals(1, s.bookings.mine(student).size());
        assertTrue(s.bookings.inbox(agent).size() >= 4);
        assertEquals(3, s.requests.mine(resident).size());
        assertEquals(2, s.requests.queue(plumber).size());

        var summary = s.summaryFor(manager);
        assertEquals(9, summary.totalRequests());
        assertEquals(1, summary.urgentOpen());
        assertEquals(1, summary.openByCategory().get(RequestCategory.INQUIRY));
        assertEquals(1, summary.leased());
        var plumbing = summary.trades().stream().filter(t -> t.trade() == Trade.PLUMBING).findFirst().orElseThrow();
        assertEquals(1, plumbing.completed());
        assertEquals(52.0, plumbing.averageHoursToComplete().orElseThrow(), 0.1);

        assertThrows(AccessDeniedException.class, () -> s.summaryFor(student));
    }

    @Test
    void migrationsRunOnceAndSkipCommentLines() {
        Database db = TestDb.fresh();
        assertEquals(0, Migrator.migrate(db));
        assertEquals(2, MigratorAccess.statements("-- note\nCREATE TABLE a (x INT);\n\nCREATE TABLE b (y INT);\n").size());
        assertEquals(Role.values().length, 9);
    }
}

package com.campuscrib.seed;

import com.campuscrib.db.Database;
import com.campuscrib.domain.Amenity;
import com.campuscrib.domain.ApartmentDraft;
import com.campuscrib.domain.BookingRequest;
import com.campuscrib.domain.NewRequest;
import com.campuscrib.domain.Priority;
import com.campuscrib.domain.RequestCategory;
import com.campuscrib.domain.RequestStatus;
import com.campuscrib.domain.Role;
import com.campuscrib.domain.Trade;
import com.campuscrib.domain.User;
import com.campuscrib.domain.WorkRequest;
import com.campuscrib.service.PasswordHasher;
import com.campuscrib.service.Services;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A small, realistic building so every role has something to do on first launch. Data goes in
 * through the real services, so it passes the same rules a user's input would; afterwards a few
 * timestamps are backdated so history and reports look like a few weeks of use.
 */
public final class DemoData {
    public static final String PASSWORD = "campuscrib";

    private record Person(String username, String name, Role role, String unit) {}

    private static final List<Person> PEOPLE = List.of(
            new Person("admin", "Morgan Lee", Role.ADMIN, null),
            new Person("sofia", "Sofia Rivera", Role.AGENT, null),
            new Person("daniel", "Daniel Chen", Role.AGENT, null),
            new Person("ananya", "Ananya Rao", Role.STUDENT, null),
            new Person("marcus", "Marcus Bell", Role.STUDENT, null),
            new Person("lucia", "Lucia Romero", Role.STUDENT, null),
            new Person("jordan", "Jordan Kim", Role.RESIDENT, "B-204"),
            new Person("kai", "Kai Thompson", Role.RESIDENT, "C-310"),
            new Person("amara", "Amara Okafor", Role.RESIDENT, "D-115"),
            new Person("helen", "Helen Ortiz", Role.BUILDING_MANAGER, null),
            new Person("tom", "Tom Walsh", Role.PLUMBER, null),
            new Person("ravi", "Ravi Kumar", Role.ELECTRICIAN, null),
            new Person("ella", "Ella Novak", Role.CARPENTER, null),
            new Person("sam", "Sam Dupont", Role.PEST_CONTROL, null));

    private DemoData() {}

    public static boolean isEmpty(Services services) {
        return services.userRepository().count() == 0;
    }

    public static void load(Database db, Services s) {
        Map<String, User> u = new HashMap<>();
        for (Person p : PEOPLE) {
            long id = s.userRepository().insert(p.username, PasswordHasher.hash(PASSWORD), p.name, p.username + "@campuscrib.test", p.role, p.unit);
            u.put(p.username, s.userRepository().findById(id).orElseThrow());
        }

        LocalDate today = LocalDate.now();
        Map<String, Long> apt = new HashMap<>();
        apt.put("A101", listing(s, u.get("sofia"), "A101", "Sunny two-bed by the Fens", "Fenway", "48 Park Drive, Boston", 3200, 2, 1, "0.6",
                List.of("Dishwasher", "Heat included", "Study room"), today,
                "Bright corner unit a ten-minute walk from campus, with tall windows over the Back Bay Fens. Heat and hot water are included, and the building has a quiet study room."));
        apt.put("A102", listing(s, u.get("sofia"), "A102", "Furnished one-bed on Mission Hill", "Mission Hill", "17 Calumet Street, Boston", 2350, 1, 1, "0.9",
                List.of("Furnished", "In-unit laundry", "Pets allowed"), today.plusWeeks(2),
                "Move-in ready one-bed with a desk, bed and sofa included. In-unit washer and dryer, and cats are welcome. Two stops from campus on the E line."));
        apt.put("B201", listing(s, u.get("sofia"), "B201", "Back Bay brownstone, two beds", "Back Bay", "302 Marlborough Street, Boston", 3850, 2, 2, "1.4",
                List.of("Dishwasher", "In-unit laundry", "Heat included"), today,
                "Second-floor unit in a classic brownstone with two equal bedrooms, each with its own bathroom. Ideal for two roommates who want privacy."));
        apt.put("C301", listing(s, u.get("daniel"), "C301", "Three-bed with a big kitchen", "Jamaica Plain", "11 Burroughs Street, Boston", 4200, 3, 2, "3.2",
                List.of("Dishwasher", "Parking", "Pets allowed", "Gym"), today.plusMonths(1),
                "A roomy three-bed near Jamaica Pond with an open kitchen big enough to cook for the whole flat. One off-street parking space and a building gym."));
        apt.put("D401", listing(s, u.get("daniel"), "D401", "Top-floor two-bed in Allston", "Allston", "140 Brighton Avenue, Boston", 2900, 2, 1, "4.1",
                List.of("Furnished", "Gym", "In-unit laundry"), today,
                "Top-floor, fully furnished two-bed with city views, close to the 57 bus and the Green Line. Building gym and in-unit laundry."));

        for (var e : apt.entrySet()) {
            for (String room : List.of("main", "living", "bedroom", "kitchen")) {
                s.listings.addPhoto(agentOf(u, e.getKey()), e.getValue(), caption(room), photo(e.getKey(), room));
            }
        }

        // Booking requests at every stage
        BookingRequest lucia = s.bookings.request(u.get("lucia"), apt.get("B201"), today.plusWeeks(3), 12, "Second-year grad student, looking for a quiet place for two years. Happy to share references.");
        s.bookings.request(u.get("marcus"), apt.get("B201"), today.plusWeeks(3), 12, "Is the second bedroom big enough for a desk?");
        s.bookings.decide(u.get("sofia"), lucia.id(), true, "Welcome! I'll email the lease and key pickup details this week.");
        s.bookings.request(u.get("ananya"), apt.get("A102"), today.plusWeeks(3), 9, "Starting a nine-month master's in September. Could I see it on a video call first?");
        s.bookings.request(u.get("marcus"), apt.get("A102"), today.plusWeeks(4), 12, "");
        BookingRequest marcusC = s.bookings.request(u.get("marcus"), apt.get("C301"), today.plusMonths(1), 6, "Looking for a six-month sublet.");
        s.bookings.decide(u.get("daniel"), marcusC.id(), false, "The owner needs a 12-month lease for this one, sorry.");

        // Building requests: one per lifecycle stage, across all trades and categories
        WorkRequest tap = s.requests.submit(u.get("jordan"), maintenance(Trade.PLUMBING, "Kitchen tap keeps dripping", "The kitchen tap drips constantly even when fully closed. It's been getting worse over the week.", Priority.HIGH));
        WorkRequest outlet = s.requests.submit(u.get("kai"), maintenance(Trade.ELECTRICAL, "Bedroom outlet sparks", "The outlet by my desk sparked when I plugged in my laptop charger. I've stopped using it.", Priority.URGENT));
        s.requests.move(u.get("ravi"), outlet.id(), RequestStatus.ACCEPTED, "On it today, please keep the outlet unplugged.");
        s.requests.move(u.get("ravi"), outlet.id(), RequestStatus.IN_PROGRESS, "Replacing the outlet and checking the circuit.");
        WorkRequest ants = s.requests.submit(u.get("amara"), maintenance(Trade.PEST_CONTROL, "Ants along the kitchen window", "A line of small ants along the kitchen window sill every morning.", Priority.NORMAL));
        s.requests.move(u.get("sam"), ants.id(), RequestStatus.ACCEPTED, "");
        s.requests.move(u.get("sam"), ants.id(), RequestStatus.IN_PROGRESS, "");
        s.requests.move(u.get("sam"), ants.id(), RequestStatus.COMPLETED, "Sealed the window gap and placed bait stations. Call again if they're back in a week.");
        WorkRequest cabinet = s.requests.submit(u.get("jordan"), maintenance(Trade.CARPENTRY, "Cabinet door hanging loose", "The top hinge on the cabinet above the sink has come away from the frame.", Priority.LOW));
        s.requests.assign(u.get("helen"), cabinet.id(), u.get("ella").id());
        WorkRequest leak = s.requests.submit(u.get("kai"), maintenance(Trade.PLUMBING, "Shower drains slowly", "Water pools in the shower for several minutes after use.", Priority.NORMAL));
        s.requests.move(u.get("tom"), leak.id(), RequestStatus.ACCEPTED, "");
        s.requests.move(u.get("tom"), leak.id(), RequestStatus.IN_PROGRESS, "");
        s.requests.move(u.get("tom"), leak.id(), RequestStatus.COMPLETED, "Cleared the trap and the drain line. Draining normally now.");

        LocalDateTime tomorrowEvening = today.plusDays(1).atTime(18, 0);
        s.requests.submit(u.get("jordan"), amenity(Amenity.STUDY_ROOM, tomorrowEvening, 2, "Group project session", "Four of us working on a group presentation."));
        WorkRequest rooftop = s.requests.submit(u.get("amara"), amenity(Amenity.ROOFTOP, today.plusDays(3).atTime(17, 0), 3, "Birthday get-together", "Small get-together for about eight friends."));
        s.requests.move(u.get("helen"), rooftop.id(), RequestStatus.APPROVED, "Approved. Please be done by 20:00 and take rubbish down with you.");
        WorkRequest fire = s.requests.submit(u.get("kai"), new NewRequest(RequestCategory.INQUIRY, null, null, null, null, "When is the next fire alarm test?", "I work night shifts and would like to plan around the alarm test.", Priority.LOW));
        s.requests.move(u.get("helen"), fire.id(), RequestStatus.ANSWERED, "The next test is the first Monday of the month at 10:00, for about ten minutes.");
        s.requests.submit(u.get("amara"), new NewRequest(RequestCategory.INQUIRY, null, null, null, null, "Can I get a second mailbox key?", "My roommate is moving in next month and needs a mailbox key.", Priority.NORMAL));

        // Spread history over the past few weeks so reports have realistic turnaround times.
        Instant now = Instant.now();
        backdate(db, ants.id(), now.minus(9, ChronoUnit.DAYS), Duration.ofHours(30));
        backdate(db, leak.id(), now.minus(14, ChronoUnit.DAYS), Duration.ofHours(52));
        backdate(db, fire.id(), now.minus(6, ChronoUnit.DAYS), Duration.ofHours(4));
        backdate(db, outlet.id(), now.minus(1, ChronoUnit.DAYS), null);
        backdate(db, tap.id(), now.minus(3, ChronoUnit.HOURS), null);
        backdate(db, cabinet.id(), now.minus(2, ChronoUnit.DAYS), null);
    }

    private static long listing(Services s, User agent, String code, String title, String area, String address, int rent, int beds, int baths, String km,
                                List<String> amenities, LocalDate from, String description) {
        return s.listings.create(agent, new ApartmentDraft(code, title, area, address, description, BigDecimal.valueOf(rent), beds, baths, new BigDecimal(km), amenities, from)).id();
    }

    private static User agentOf(Map<String, User> u, String code) {
        return code.startsWith("A") || code.startsWith("B") ? u.get("sofia") : u.get("daniel");
    }

    private static NewRequest maintenance(Trade trade, String title, String description, Priority priority) {
        return new NewRequest(RequestCategory.MAINTENANCE, trade, null, null, null, title, description, priority);
    }

    private static NewRequest amenity(Amenity amenity, LocalDateTime start, int hours, String title, String description) {
        return new NewRequest(RequestCategory.AMENITY, null, amenity, start, hours, title, description, Priority.NORMAL);
    }

    private static String caption(String room) {
        return switch (room) {
            case "main" -> "Building";
            case "living" -> "Living room";
            case "bedroom" -> "Bedroom";
            default -> "Kitchen";
        };
    }

    private static byte[] photo(String code, String room) {
        String path = "/seed/apartments/" + code + "/" + room + ".jpg";
        try (InputStream in = DemoData.class.getResourceAsStream(path)) {
            if (in == null) throw new IllegalStateException("Missing seed photo " + path);
            return in.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Moves a request's creation (and its events) back in time; closed requests close `took` later. */
    private static void backdate(Database db, long requestId, Instant created, Duration took) {
        db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement("UPDATE work_requests SET created_at = ?, updated_at = ?, closed_at = CASE WHEN closed_at IS NULL THEN NULL ELSE ? END WHERE id = ?")) {
                Instant closed = took == null ? created : created.plus(took);
                ps.setTimestamp(1, Timestamp.from(created));
                ps.setTimestamp(2, Timestamp.from(closed));
                ps.setTimestamp(3, Timestamp.from(closed));
                ps.setLong(4, requestId);
                ps.executeUpdate();
            }
            // Space the events evenly between creation and the last update.
            try (PreparedStatement list = c.prepareStatement("SELECT id FROM request_events WHERE request_id = ? ORDER BY id")) {
                list.setLong(1, requestId);
                List<Long> ids = new java.util.ArrayList<>();
                try (var rs = list.executeQuery()) {
                    while (rs.next()) ids.add(rs.getLong(1));
                }
                Duration span = took == null ? Duration.ofMinutes(Math.max(1, ids.size() - 1) * 40L) : took;
                for (int i = 0; i < ids.size(); i++) {
                    Instant at = ids.size() == 1 ? created : created.plus(span.multipliedBy(i).dividedBy(ids.size() - 1));
                    try (PreparedStatement ps = c.prepareStatement("UPDATE request_events SET created_at = ? WHERE id = ?")) {
                        ps.setTimestamp(1, Timestamp.from(at));
                        ps.setLong(2, ids.get(i));
                        ps.executeUpdate();
                    }
                }
            }
            return null;
        });
    }
}

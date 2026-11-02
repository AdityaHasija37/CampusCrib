package com.campuscrib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.campuscrib.domain.Apartment;
import com.campuscrib.domain.ApartmentStatus;
import com.campuscrib.domain.BookingRequest;
import com.campuscrib.domain.BookingStatus;
import com.campuscrib.domain.Role;
import com.campuscrib.domain.SearchCriteria;
import com.campuscrib.domain.User;
import com.campuscrib.service.AccessDeniedException;
import com.campuscrib.service.ValidationException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ListingsAndBookingsTest {
    final Fixture f = new Fixture();
    final User agent = f.person(Role.AGENT);

    @Test
    void onlyAgentsCreateListingsAndOnlyTheirOwnCanBeChanged() {
        User student = f.person(Role.STUDENT);
        User otherAgent = f.person(Role.AGENT);
        assertThrows(AccessDeniedException.class, () -> f.s.listings.create(student, Fixture.draft("X1", 2000)));
        Apartment a = f.listing(agent, "A1", 2000);
        assertThrows(AccessDeniedException.class, () -> f.s.listings.update(otherAgent, a.id(), Fixture.draft("A1", 1)));
        assertThrows(AccessDeniedException.class, () -> f.s.listings.delete(otherAgent, a.id()));
        assertEquals(1, f.s.listings.myListings(agent).size());
    }

    @Test
    void validatesListingsWithEveryProblemAtOnce() {
        var bad = new com.campuscrib.domain.ApartmentDraft("", "", "", "", "short", BigDecimal.TEN, 20, 0, new BigDecimal("99"), List.of(), null);
        var e = assertThrows(ValidationException.class, () -> f.s.listings.create(agent, bad));
        assertEquals(10, e.problems().size());
    }

    @Test
    void searchFiltersSortsAndHidesUnlistedApartments() {
        f.listing(agent, "A1", 2000);
        Apartment pricey = f.listing(agent, "A2", 3500);
        Apartment hidden = f.listing(agent, "A3", 1500);
        f.s.listings.setListed(agent, hidden.id(), false);

        assertEquals(List.of("A1", "A2"), codes(f.s.listings.search(SearchCriteria.any())));
        var underThree = new SearchCriteria(null, BigDecimal.valueOf(3000), null, null, List.of(), SearchCriteria.Sort.RENT_LOW);
        assertEquals(List.of("A1"), codes(f.s.listings.search(underThree)));
        var priciest = new SearchCriteria("fenway", null, 2, null, List.of("Gym"), SearchCriteria.Sort.RENT_HIGH);
        assertEquals(pricey.code(), f.s.listings.search(priciest).get(0).code());
        var missingAmenity = new SearchCriteria(null, null, null, null, List.of("Parking"), SearchCriteria.Sort.RENT_LOW);
        assertTrue(f.s.listings.search(missingAmenity).isEmpty());
    }

    @Test
    void storesPhotosInOrderWithASizeLimit() {
        Apartment a = f.listing(agent, "A1", 2000);
        f.s.listings.addPhoto(agent, a.id(), "Kitchen", new byte[] {1, 2, 3});
        f.s.listings.addPhoto(agent, a.id(), "", new byte[] {4});
        var photos = f.s.listings.photos(a.id());
        assertEquals(List.of("Kitchen", "Photo"), photos.stream().map(p -> p.caption()).toList());
        assertEquals(3, f.s.listings.cover(a.id()).orElseThrow().length);
        assertThrows(ValidationException.class, () -> f.s.listings.addPhoto(agent, a.id(), "Huge", new byte[6 * 1024 * 1024]));
    }

    @Test
    void approvingOneRequestLeasesTheApartmentAndDeclinesTheRest() {
        Apartment a = f.listing(agent, "A1", 2000);
        User s1 = f.person(Role.STUDENT);
        User s2 = f.person(Role.STUDENT);
        BookingRequest first = f.s.bookings.request(s1, a.id(), Fixture.TODAY.plusDays(10), 12, "Hi!");
        BookingRequest second = f.s.bookings.request(s2, a.id(), Fixture.TODAY.plusDays(10), 12, "");
        assertThrows(ValidationException.class, () -> f.s.bookings.request(s1, a.id(), Fixture.TODAY.plusDays(10), 12, "again"));

        f.s.bookings.decide(agent, first.id(), true, "Welcome");

        assertEquals(ApartmentStatus.LEASED, f.s.listings.get(a.id()).status());
        assertEquals(BookingStatus.DECLINED, f.s.bookings.mine(s2).get(0).status());
        assertTrue(f.s.bookings.mine(s2).get(0).agentNote().contains("another applicant"));
        assertThrows(ValidationException.class, () -> f.s.bookings.decide(agent, second.id(), true, ""));
        assertTrue(f.s.listings.search(SearchCriteria.any()).isEmpty());
    }

    @Test
    void bookingRulesForStudentsAndAgents() {
        Apartment a = f.listing(agent, "A1", 2000);
        User student = f.person(Role.STUDENT);
        User resident = f.person(Role.RESIDENT);
        User otherAgent = f.person(Role.AGENT);

        assertThrows(AccessDeniedException.class, () -> f.s.bookings.request(resident, a.id(), Fixture.TODAY.plusDays(5), 12, ""));
        var dates = assertThrows(ValidationException.class, () -> f.s.bookings.request(student, a.id(), Fixture.TODAY.minusDays(1), 30, ""));
        assertEquals(2, dates.problems().size());

        BookingRequest b = f.s.bookings.request(student, a.id(), Fixture.TODAY.plusDays(5), 12, "");
        assertThrows(AccessDeniedException.class, () -> f.s.bookings.decide(otherAgent, b.id(), true, ""));
        assertThrows(ValidationException.class, () -> f.s.bookings.decide(agent, b.id(), false, " "));
        assertEquals(1, f.s.bookings.inbox(agent).size());

        f.s.bookings.withdraw(student, b.id());
        assertEquals(BookingStatus.WITHDRAWN, f.s.bookings.mine(student).get(0).status());
        assertThrows(ValidationException.class, () -> f.s.bookings.withdraw(student, b.id()));
    }

    @Test
    void wontBookBeforeTheApartmentIsAvailable() {
        var later = new com.campuscrib.domain.ApartmentDraft("L1", "Later flat", "Fenway", "2 Test St", "Available from next month onwards.",
                BigDecimal.valueOf(2000), 1, 1, BigDecimal.ONE, List.of(), Fixture.TODAY.plusMonths(1));
        Apartment a = f.s.listings.create(agent, later);
        User student = f.person(Role.STUDENT);
        var e = assertThrows(ValidationException.class, () -> f.s.bookings.request(student, a.id(), Fixture.TODAY.plusDays(3), 12, ""));
        assertTrue(e.getMessage().contains("available from"));
    }

    private static List<String> codes(List<Apartment> list) {
        return list.stream().map(Apartment::code).toList();
    }
}

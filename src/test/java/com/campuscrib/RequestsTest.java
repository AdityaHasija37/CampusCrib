package com.campuscrib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.campuscrib.domain.Amenity;
import com.campuscrib.domain.NewRequest;
import com.campuscrib.domain.Priority;
import com.campuscrib.domain.RequestCategory;
import com.campuscrib.domain.RequestStatus;
import com.campuscrib.domain.Role;
import com.campuscrib.domain.Trade;
import com.campuscrib.domain.User;
import com.campuscrib.domain.WorkRequest;
import com.campuscrib.service.AccessDeniedException;
import com.campuscrib.service.ValidationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RequestsTest {
    final Fixture f = new Fixture();
    final User resident = f.person(Role.RESIDENT);
    final User manager = f.person(Role.BUILDING_MANAGER);
    final User plumber = f.person(Role.PLUMBER);
    final User electrician = f.person(Role.ELECTRICIAN);

    @Test
    void maintenanceGoesToTheRightCrewAndThroughItsLifecycle() {
        WorkRequest r = f.s.requests.submit(resident, Fixture.maintenance(Trade.PLUMBING));
        assertEquals("Plumbing services", r.routedTo());
        assertEquals("B-204", r.unit());

        assertEquals(1, f.s.requests.queue(plumber).size());
        assertTrue(f.s.requests.queue(electrician).isEmpty());
        assertEquals(Set.of(), f.s.requests.allowedNext(electrician, r));
        assertThrows(AccessDeniedException.class, () -> f.s.requests.move(electrician, r.id(), RequestStatus.ACCEPTED, ""));

        f.s.requests.move(plumber, r.id(), RequestStatus.ACCEPTED, "");
        assertEquals(plumber.id(), f.s.requests.get(r.id()).assigneeId());
        f.s.requests.move(plumber, r.id(), RequestStatus.IN_PROGRESS, "");
        assertThrows(ValidationException.class, () -> f.s.requests.move(plumber, r.id(), RequestStatus.COMPLETED, ""));
        WorkRequest done = f.s.requests.move(plumber, r.id(), RequestStatus.COMPLETED, "Replaced the washer.");

        assertEquals(RequestStatus.COMPLETED, done.status());
        assertNotNull(done.closedAt());
        assertEquals(List.of(RequestStatus.SUBMITTED, RequestStatus.ACCEPTED, RequestStatus.IN_PROGRESS, RequestStatus.COMPLETED),
                f.s.requests.history(r.id()).stream().map(e -> e.toStatus()).toList());
        assertThrows(ValidationException.class, () -> f.s.requests.move(plumber, r.id(), RequestStatus.IN_PROGRESS, ""));
    }

    @Test
    void anAcceptedJobBelongsToWhoeverTookIt() {
        User secondPlumber = f.person(Role.PLUMBER);
        WorkRequest r = f.s.requests.submit(resident, Fixture.maintenance(Trade.PLUMBING));
        f.s.requests.move(plumber, r.id(), RequestStatus.ACCEPTED, "");
        assertTrue(f.s.requests.queue(secondPlumber).isEmpty());
        assertThrows(AccessDeniedException.class, () -> f.s.requests.move(secondPlumber, r.id(), RequestStatus.IN_PROGRESS, ""));
    }

    @Test
    void managersAssignOnlyToTheMatchingTrade() {
        WorkRequest r = f.s.requests.submit(resident, Fixture.maintenance(Trade.PLUMBING));
        assertEquals(List.of(plumber.id()), f.s.requests.crewFor(r).stream().map(User::id).toList());
        assertThrows(ValidationException.class, () -> f.s.requests.assign(manager, r.id(), electrician.id()));
        assertThrows(AccessDeniedException.class, () -> f.s.requests.assign(plumber, r.id(), plumber.id()));
        WorkRequest assigned = f.s.requests.assign(manager, r.id(), plumber.id());
        assertEquals(RequestStatus.ACCEPTED, assigned.status());
        assertEquals(plumber.fullName(), assigned.routedTo());
    }

    @Test
    void amenityBookingsAreApprovedWithoutDoubleBooking() {
        User neighbour = f.person(Role.RESIDENT, "C-310");
        LocalDateTime six = Fixture.TODAY.plusDays(1).atTime(18, 0);
        WorkRequest a = f.s.requests.submit(resident, amenity(Amenity.STUDY_ROOM, six, 2));
        WorkRequest b = f.s.requests.submit(neighbour, amenity(Amenity.STUDY_ROOM, six.plusHours(1), 2));
        WorkRequest gym = f.s.requests.submit(neighbour, amenity(Amenity.GYM, six, 1));
        assertEquals("Building management", a.routedTo());

        assertThrows(AccessDeniedException.class, () -> f.s.requests.move(plumber, a.id(), RequestStatus.APPROVED, ""));
        f.s.requests.move(manager, a.id(), RequestStatus.APPROVED, "");
        var clash = assertThrows(ValidationException.class, () -> f.s.requests.move(manager, b.id(), RequestStatus.APPROVED, ""));
        assertTrue(clash.getMessage().contains("overlaps"));
        f.s.requests.move(manager, gym.id(), RequestStatus.APPROVED, "");
        assertThrows(ValidationException.class, () -> f.s.requests.move(manager, b.id(), RequestStatus.DECLINED, ""));
        assertEquals(RequestStatus.DECLINED, f.s.requests.move(manager, b.id(), RequestStatus.DECLINED, "Already booked").status());
    }

    @Test
    void validatesAmenitySlots() {
        LocalDateTime past = Fixture.TODAY.minusDays(1).atTime(12, 0);
        LocalDateTime late = Fixture.TODAY.plusDays(1).atTime(22, 0);
        assertThrows(ValidationException.class, () -> f.s.requests.submit(resident, amenity(Amenity.GYM, past, 1)));
        assertThrows(ValidationException.class, () -> f.s.requests.submit(resident, amenity(Amenity.GYM, late, 2)));
        assertThrows(ValidationException.class, () -> f.s.requests.submit(resident, amenity(Amenity.GYM, late.minusHours(4), 5)));
        var noTrade = new NewRequest(RequestCategory.MAINTENANCE, null, null, null, null, "Broken", "Something is broken here.", Priority.HIGH);
        assertThrows(ValidationException.class, () -> f.s.requests.submit(resident, noTrade));
    }

    @Test
    void inquiriesAreAnsweredByManagement() {
        var q = new NewRequest(RequestCategory.INQUIRY, null, null, null, null, "Mailbox key", "Can I get a spare mailbox key?", Priority.LOW);
        WorkRequest r = f.s.requests.submit(resident, q);
        assertEquals(Set.of(RequestStatus.ANSWERED, RequestStatus.CANCELLED), f.s.requests.allowedNext(manager, r));
        assertThrows(ValidationException.class, () -> f.s.requests.move(manager, r.id(), RequestStatus.ANSWERED, ""));
        WorkRequest answered = f.s.requests.move(manager, r.id(), RequestStatus.ANSWERED, "Pick one up at the front desk.");
        assertEquals("Pick one up at the front desk.", f.s.requests.history(answered.id()).get(1).note());
    }

    @Test
    void residentsCancelTheirOwnOpenRequestsOnly() {
        User other = f.person(Role.RESIDENT, "D-115");
        WorkRequest r = f.s.requests.submit(resident, Fixture.maintenance(Trade.ELECTRICAL));
        assertEquals(Set.of(), f.s.requests.allowedNext(other, r));
        assertThrows(AccessDeniedException.class, () -> f.s.requests.move(other, r.id(), RequestStatus.CANCELLED, "no"));
        assertEquals(RequestStatus.CANCELLED, f.s.requests.move(resident, r.id(), RequestStatus.CANCELLED, "").status());
        assertTrue(f.s.requests.mine(resident).get(0).status().isClosed());
    }

    @Test
    void onlyResidentsRaiseRequests() {
        User student = f.person(Role.STUDENT);
        assertThrows(AccessDeniedException.class, () -> f.s.requests.submit(student, Fixture.maintenance(Trade.PLUMBING)));
        assertThrows(AccessDeniedException.class, () -> f.s.requests.queue(resident));
        assertThrows(AccessDeniedException.class, () -> f.s.requests.all(resident));
    }

    private static NewRequest amenity(Amenity amenity, LocalDateTime start, int hours) {
        return new NewRequest(RequestCategory.AMENITY, null, amenity, start, hours, "Booking", "Booking for a study group.", Priority.NORMAL);
    }
}

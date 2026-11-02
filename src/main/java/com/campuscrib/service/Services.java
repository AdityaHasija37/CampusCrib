package com.campuscrib.service;

import com.campuscrib.db.Database;
import com.campuscrib.domain.Role;
import com.campuscrib.domain.User;
import com.campuscrib.repo.ApartmentRepository;
import com.campuscrib.repo.BookingRepository;
import com.campuscrib.repo.RequestRepository;
import com.campuscrib.repo.UserRepository;
import java.time.Clock;
import java.util.List;

/** Wires repositories into services. One instance for the whole app (and one per test). */
public final class Services {
    public final AuthService auth;
    public final ListingService listings;
    public final BookingService bookings;
    public final RequestService requests;
    public final ReportService reports;
    public final UserAdminService users;
    private final UserRepository userRepo;

    public Services(Database db, Clock clock) {
        userRepo = new UserRepository(db);
        ApartmentRepository apartmentRepo = new ApartmentRepository(db);
        RequestRepository requestRepo = new RequestRepository(db);
        auth = new AuthService(userRepo);
        listings = new ListingService(apartmentRepo);
        bookings = new BookingService(db, new BookingRepository(db), apartmentRepo, clock);
        requests = new RequestService(db, requestRepo, userRepo, clock);
        reports = new ReportService(requestRepo, apartmentRepo);
        users = new UserAdminService(userRepo);
    }

    /** Occupancy across every agent's listings, for building management. */
    public ReportService.Summary summaryFor(User viewer) {
        List<Long> agents = userRepo.findActiveByRole(Role.AGENT).stream().map(User::id).toList();
        return reports.summary(viewer, agents);
    }

    public UserRepository userRepository() {
        return userRepo;
    }
}

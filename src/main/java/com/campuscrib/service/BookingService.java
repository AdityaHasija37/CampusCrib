package com.campuscrib.service;

import com.campuscrib.db.Database;
import com.campuscrib.domain.Apartment;
import com.campuscrib.domain.ApartmentStatus;
import com.campuscrib.domain.BookingRequest;
import com.campuscrib.domain.BookingStatus;
import com.campuscrib.domain.Role;
import com.campuscrib.domain.User;
import com.campuscrib.repo.ApartmentRepository;
import com.campuscrib.repo.BookingRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/** Students request apartments; the listing agent approves or declines. */
public class BookingService {
    static final String LEASED_TO_SOMEONE_ELSE = "This apartment has been leased to another applicant.";
    private final Database db;
    private final BookingRepository bookings;
    private final ApartmentRepository apartments;
    private final Clock clock;

    public BookingService(Database db, BookingRepository bookings, ApartmentRepository apartments, Clock clock) {
        this.db = db;
        this.bookings = bookings;
        this.apartments = apartments;
        this.clock = clock;
    }

    public BookingRequest request(User student, long apartmentId, LocalDate moveIn, int leaseMonths, String message) {
        if (student.role() != Role.STUDENT) throw new AccessDeniedException("Only students can request an apartment.");
        Apartment a = apartments.findById(apartmentId).orElseThrow(() -> new ValidationException("That apartment no longer exists."));
        if (a.status() != ApartmentStatus.LISTED) throw new ValidationException("This apartment isn't taking requests right now.");
        if (bookings.hasOpenRequest(apartmentId, student.id())) throw new ValidationException("You already have a pending request for this apartment.");
        LocalDate today = LocalDate.now(clock);
        new Problems()
                .check(moveIn != null && !moveIn.isBefore(today), "Pick a move-in date from today onward.")
                .check(moveIn == null || moveIn.isBefore(today) || !moveIn.isBefore(a.availableFrom()), "This apartment is available from " + a.availableFrom() + ".")
                .check(leaseMonths >= 1 && leaseMonths <= 24, "Lease length must be between 1 and 24 months.")
                .check(message == null || message.length() <= 1000, "Keep your message under 1,000 characters.")
                .throwIfAny();
        long id = bookings.insert(apartmentId, student.id(), moveIn, leaseMonths, message == null ? "" : message.strip());
        return get(id);
    }

    public List<BookingRequest> mine(User student) {
        return bookings.findByStudent(student.id());
    }

    public List<BookingRequest> inbox(User agent) {
        if (agent.role() != Role.AGENT) throw new AccessDeniedException("Only agents review booking requests.");
        return bookings.findByAgent(agent.id());
    }

    /**
     * Approving leases the apartment and declines every other pending request for it, in one
     * transaction, so two students can never both be approved for the same unit.
     */
    public BookingRequest decide(User agent, long bookingId, boolean approve, String note) {
        BookingRequest b = get(bookingId);
        if (agent.role() != Role.AGENT || b.agentId() != agent.id()) throw new AccessDeniedException("Only the listing agent can decide on this request.");
        if (b.status() != BookingStatus.PENDING) throw new ValidationException("This request has already been " + b.status().toString().toLowerCase() + ".");
        if (!approve && Problems.blank(note)) throw new ValidationException("Add a short note so the student knows why.");
        String cleanNote = note == null ? "" : note.strip();
        db.inTransaction(c -> {
            bookings.decide(c, bookingId, approve ? BookingStatus.APPROVED : BookingStatus.DECLINED, cleanNote);
            if (approve) {
                apartments.setStatus(c, b.apartmentId(), ApartmentStatus.LEASED);
                bookings.declineOthers(c, b.apartmentId(), bookingId, LEASED_TO_SOMEONE_ELSE);
            }
            return null;
        });
        return get(bookingId);
    }

    public void withdraw(User student, long bookingId) {
        BookingRequest b = get(bookingId);
        if (b.studentId() != student.id()) throw new AccessDeniedException("You can only withdraw your own requests.");
        if (b.status() != BookingStatus.PENDING) throw new ValidationException("Only pending requests can be withdrawn.");
        bookings.withdraw(bookingId);
    }

    private BookingRequest get(long id) {
        return bookings.findById(id).orElseThrow(() -> new ValidationException("That booking request no longer exists."));
    }
}

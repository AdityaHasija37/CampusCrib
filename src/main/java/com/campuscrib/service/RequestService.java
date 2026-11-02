package com.campuscrib.service;

import com.campuscrib.db.Database;
import com.campuscrib.domain.NewRequest;
import com.campuscrib.domain.Priority;
import com.campuscrib.domain.RequestCategory;
import com.campuscrib.domain.RequestEvent;
import com.campuscrib.domain.RequestStatus;
import com.campuscrib.domain.Role;
import com.campuscrib.domain.User;
import com.campuscrib.domain.WorkRequest;
import com.campuscrib.repo.RequestRepository;
import com.campuscrib.repo.UserRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Residents raise requests; each is routed to whoever is responsible. Maintenance goes to the
 * crew for its trade, amenity bookings and inquiries go to building management. Every status
 * change is recorded as an event, so a request carries its own history.
 */
public class RequestService {
    static final int OPENING_HOUR = 6;
    static final int CLOSING_HOUR = 23;
    private final Database db;
    private final RequestRepository requests;
    private final UserRepository users;
    private final Clock clock;

    public RequestService(Database db, RequestRepository requests, UserRepository users, Clock clock) {
        this.db = db;
        this.requests = requests;
        this.users = users;
        this.clock = clock;
    }

    public WorkRequest submit(User resident, NewRequest r) {
        if (resident.role() != Role.RESIDENT) throw new AccessDeniedException("Only residents can raise building requests.");
        if (Problems.blank(resident.unit())) throw new ValidationException("Your account has no unit yet. Ask building management to add it.");
        validate(r);
        long id = db.inTransaction(c -> {
            long newId = requests.insert(c, resident.id(), resident.unit(), r);
            requests.addEvent(c, newId, resident.id(), null, RequestStatus.SUBMITTED, "");
            return newId;
        });
        return get(id);
    }

    public WorkRequest get(long id) {
        return requests.findById(id).orElseThrow(() -> new ValidationException("That request no longer exists."));
    }

    public List<RequestEvent> history(long id) {
        return requests.events(id);
    }

    public List<WorkRequest> mine(User resident) {
        return requests.findByResident(resident.id());
    }

    public List<WorkRequest> queue(User provider) {
        var trade = provider.role().trade().orElseThrow(() -> new AccessDeniedException("Only service crews have a work queue."));
        return requests.queueFor(trade, provider.id());
    }

    public List<WorkRequest> all(User viewer) {
        if (viewer.role() != Role.BUILDING_MANAGER && viewer.role() != Role.ADMIN) {
            throw new AccessDeniedException("Only building management sees every request.");
        }
        return requests.findAll();
    }

    /** The service crew members who could take a maintenance request, for the manager's assign menu. */
    public List<User> crewFor(WorkRequest r) {
        if (r.category() != RequestCategory.MAINTENANCE) return List.of();
        return users.findActiveByRole(Role.forTrade(r.trade()));
    }

    /** What this user may move the request to right now. The UI shows exactly these actions. */
    public Set<RequestStatus> allowedNext(User user, WorkRequest r) {
        Set<RequestStatus> allowed = EnumSet.noneOf(RequestStatus.class);
        for (RequestStatus to : r.status().next(r.category())) {
            if (mayMove(user, r, to)) allowed.add(to);
        }
        return allowed;
    }

    public WorkRequest move(User user, long requestId, RequestStatus to, String note) {
        WorkRequest r = get(requestId);
        if (!r.status().next(r.category()).contains(to)) {
            throw new ValidationException("A " + r.status().toString().toLowerCase() + " request can't be marked " + to.toString().toLowerCase() + ".");
        }
        if (!mayMove(user, r, to)) throw new AccessDeniedException("You can't mark this request " + to.toString().toLowerCase() + ".");
        boolean needsNote = to == RequestStatus.DECLINED || to == RequestStatus.ANSWERED || to == RequestStatus.COMPLETED
                || (to == RequestStatus.CANCELLED && user.id() != r.residentId());
        if (needsNote && Problems.blank(note)) {
            throw new ValidationException(switch (to) {
                case ANSWERED -> "Write the answer for the resident.";
                case COMPLETED -> "Add a short note on what was done.";
                default -> "Add a short note so the resident knows why.";
            });
        }
        Long assignee = to == RequestStatus.ACCEPTED ? Long.valueOf(user.id()) : r.assigneeId();
        db.inTransaction(c -> {
            if (to == RequestStatus.APPROVED && requests.amenitySlotTaken(c, r.amenity(), r.slotStart(), r.slotStart().plusHours(r.slotHours()), r.id())) {
                throw new ValidationException("Another booking for the " + r.amenity().toString().toLowerCase() + " overlaps this time. Decline it or ask the resident for another slot.");
            }
            requests.updateStatus(c, r.id(), to, assignee);
            requests.addEvent(c, r.id(), user.id(), r.status(), to, note);
            return null;
        });
        return get(requestId);
    }

    /** Building managers hand a maintenance request to a specific crew member. */
    public WorkRequest assign(User manager, long requestId, long providerId) {
        if (manager.role() != Role.BUILDING_MANAGER) throw new AccessDeniedException("Only building managers assign work.");
        WorkRequest r = get(requestId);
        if (r.category() != RequestCategory.MAINTENANCE || r.status() != RequestStatus.SUBMITTED) {
            throw new ValidationException("Only new maintenance requests can be assigned.");
        }
        User provider = users.findById(providerId).orElseThrow(() -> new ValidationException("That person no longer exists."));
        if (provider.role() != Role.forTrade(r.trade()) || !provider.active()) {
            throw new ValidationException(provider.fullName() + " doesn't handle " + r.trade().displayName().toLowerCase() + ".");
        }
        db.inTransaction(c -> {
            requests.updateStatus(c, r.id(), RequestStatus.ACCEPTED, provider.id());
            requests.addEvent(c, r.id(), manager.id(), r.status(), RequestStatus.ACCEPTED, "Assigned to " + provider.fullName());
            return null;
        });
        return get(requestId);
    }

    private static boolean mayMove(User user, WorkRequest r, RequestStatus to) {
        boolean manager = user.role() == Role.BUILDING_MANAGER;
        if (to == RequestStatus.CANCELLED) return manager || user.id() == r.residentId();
        return switch (r.category()) {
            case MAINTENANCE -> {
                boolean rightCrew = user.role().trade().map(t -> t == r.trade()).orElse(false);
                yield switch (to) {
                    case ACCEPTED -> rightCrew && r.assigneeId() == null;
                    case IN_PROGRESS, COMPLETED -> rightCrew && r.assigneeId() != null && r.assigneeId() == user.id();
                    default -> false;
                };
            }
            case AMENITY, INQUIRY -> manager;
        };
    }

    void validate(NewRequest r) {
        Problems p = new Problems()
                .check(r.category() != null, "Choose what kind of request this is.")
                .check(!Problems.blank(r.title()) && r.title().strip().length() <= 120, "Add a short title (up to 120 characters).")
                .check(!Problems.blank(r.description()) && r.description().strip().length() >= 10, "Describe the request in at least 10 characters.")
                .check(r.priority() != null, "Choose a priority.");
        if (r.category() == RequestCategory.MAINTENANCE) {
            p.check(r.trade() != null, "Choose the kind of maintenance: plumbing, electrical, carpentry or pest control.");
        }
        if (r.category() == RequestCategory.AMENITY) {
            LocalDateTime now = LocalDateTime.now(clock);
            p.check(r.amenity() != null, "Choose the space you'd like to book.")
                    .check(r.slotStart() != null && r.slotStart().isAfter(now), "Pick a time in the future.")
                    .check(r.slotHours() != null && r.slotHours() >= 1 && r.slotHours() <= 4, "Bookings are 1 to 4 hours.")
                    .check(r.slotStart() == null || r.slotHours() == null
                                    || (r.slotStart().getHour() >= OPENING_HOUR && r.slotStart().plusHours(r.slotHours()).getHour() <= CLOSING_HOUR
                                            && !r.slotStart().plusHours(r.slotHours()).toLocalDate().isAfter(r.slotStart().toLocalDate())),
                            "Shared spaces are open " + OPENING_HOUR + ":00 to " + CLOSING_HOUR + ":00.");
            p.check(r.priority() == null || r.priority() != Priority.URGENT, "Amenity bookings can't be urgent.");
        }
        p.throwIfAny();
    }
}

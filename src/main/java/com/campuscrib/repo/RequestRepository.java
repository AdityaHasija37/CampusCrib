package com.campuscrib.repo;

import com.campuscrib.db.Database;
import com.campuscrib.domain.Amenity;
import com.campuscrib.domain.NewRequest;
import com.campuscrib.domain.Priority;
import com.campuscrib.domain.RequestCategory;
import com.campuscrib.domain.RequestEvent;
import com.campuscrib.domain.RequestStatus;
import com.campuscrib.domain.Role;
import com.campuscrib.domain.Trade;
import com.campuscrib.domain.WorkRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RequestRepository {
    private static final String SELECT =
            "SELECT w.*, r.full_name AS resident_name, s.full_name AS assignee_name FROM work_requests w "
                    + "JOIN users r ON r.id = w.resident_id LEFT JOIN users s ON s.id = w.assignee_id";
    private static final String OPEN_FIRST =
            " ORDER BY CASE WHEN w.closed_at IS NULL THEN 0 ELSE 1 END, "
                    + "CASE w.priority WHEN 'URGENT' THEN 0 WHEN 'HIGH' THEN 1 WHEN 'NORMAL' THEN 2 ELSE 3 END, w.created_at DESC, w.id DESC";
    private final Database db;

    public RequestRepository(Database db) {
        this.db = db;
    }

    public long insert(Connection c, long residentId, String unit, NewRequest r) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO work_requests (resident_id, category, trade, amenity, slot_start, slot_hours, title, description, unit, priority, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'SUBMITTED')", Sql.keys())) {
            ps.setLong(1, residentId);
            ps.setString(2, r.category().name());
            ps.setString(3, r.trade() == null ? null : r.trade().name());
            ps.setString(4, r.amenity() == null ? null : r.amenity().name());
            ps.setTimestamp(5, r.slotStart() == null ? null : Timestamp.valueOf(r.slotStart()));
            ps.setObject(6, r.slotHours());
            ps.setString(7, r.title().strip());
            ps.setString(8, r.description().strip());
            ps.setString(9, unit);
            ps.setString(10, r.priority().name());
            ps.executeUpdate();
            return Sql.generatedKey(ps);
        }
    }

    public void updateStatus(Connection c, long id, RequestStatus status, Long assigneeId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE work_requests SET status = ?, assignee_id = ?, updated_at = ?, closed_at = ? WHERE id = ?")) {
            Timestamp now = Sql.now();
            ps.setString(1, status.name());
            ps.setObject(2, assigneeId);
            ps.setTimestamp(3, now);
            ps.setTimestamp(4, status.isClosed() ? now : null);
            ps.setLong(5, id);
            ps.executeUpdate();
        }
    }

    public void addEvent(Connection c, long requestId, long actorId, RequestStatus from, RequestStatus to, String note) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO request_events (request_id, actor_id, from_status, to_status, note) VALUES (?, ?, ?, ?, ?)")) {
            ps.setLong(1, requestId);
            ps.setLong(2, actorId);
            ps.setString(3, from == null ? null : from.name());
            ps.setString(4, to.name());
            ps.setString(5, note == null ? "" : note.strip());
            ps.executeUpdate();
        }
    }

    public Optional<WorkRequest> findById(long id) {
        List<WorkRequest> found = query(SELECT + " WHERE w.id = ?", id);
        return found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
    }

    public List<WorkRequest> findByResident(long residentId) {
        return query(SELECT + " WHERE w.resident_id = ?" + OPEN_FIRST, residentId);
    }

    /** A service crew's queue: unassigned requests for its trade, plus anything already assigned to this person. */
    public List<WorkRequest> queueFor(Trade trade, long providerId) {
        return query(SELECT + " WHERE w.category = 'MAINTENANCE' AND w.trade = ? AND (w.assignee_id IS NULL OR w.assignee_id = ?)" + OPEN_FIRST,
                trade.name(), providerId);
    }

    public List<WorkRequest> findAll() {
        return query(SELECT + OPEN_FIRST);
    }

    /** Approved bookings for the same amenity that overlap [start, end). */
    public boolean amenitySlotTaken(Connection c, Amenity amenity, LocalDateTime start, LocalDateTime end, long excludeId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT slot_start, slot_hours FROM work_requests WHERE category = 'AMENITY' AND amenity = ? AND status = 'APPROVED' AND id <> ?")) {
            ps.setString(1, amenity.name());
            ps.setLong(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDateTime otherStart = rs.getTimestamp(1).toLocalDateTime();
                    LocalDateTime otherEnd = otherStart.plusHours(rs.getInt(2));
                    if (start.isBefore(otherEnd) && otherStart.isBefore(end)) return true;
                }
            }
        }
        return false;
    }

    public List<RequestEvent> events(long requestId) {
        return db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT e.*, u.full_name AS actor_name, u.role AS actor_role FROM request_events e JOIN users u ON u.id = e.actor_id "
                            + "WHERE e.request_id = ? ORDER BY e.created_at, e.id")) {
                ps.setLong(1, requestId);
                try (ResultSet rs = ps.executeQuery()) {
                    List<RequestEvent> out = new ArrayList<>();
                    while (rs.next()) {
                        out.add(new RequestEvent(
                                rs.getLong("id"),
                                rs.getLong("request_id"),
                                rs.getString("actor_name"),
                                Role.valueOf(rs.getString("actor_role")),
                                Sql.enumOrNull(RequestStatus.class, rs.getString("from_status")),
                                RequestStatus.valueOf(rs.getString("to_status")),
                                rs.getString("note"),
                                Sql.instant(rs, "created_at")));
                    }
                    return out;
                }
            }
        });
    }

    private List<WorkRequest> query(String sql, Object... params) {
        return db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
                try (ResultSet rs = ps.executeQuery()) {
                    List<WorkRequest> out = new ArrayList<>();
                    while (rs.next()) out.add(map(rs));
                    return out;
                }
            }
        });
    }

    private static WorkRequest map(ResultSet rs) throws SQLException {
        return new WorkRequest(
                rs.getLong("id"),
                rs.getLong("resident_id"),
                rs.getString("resident_name"),
                RequestCategory.valueOf(rs.getString("category")),
                Sql.enumOrNull(Trade.class, rs.getString("trade")),
                Sql.enumOrNull(Amenity.class, rs.getString("amenity")),
                Sql.localDateTime(rs, "slot_start"),
                Sql.nullableInt(rs, "slot_hours"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("unit"),
                Priority.valueOf(rs.getString("priority")),
                RequestStatus.valueOf(rs.getString("status")),
                Sql.nullableLong(rs, "assignee_id"),
                rs.getString("assignee_name"),
                Sql.instant(rs, "created_at"),
                Sql.instant(rs, "updated_at"),
                Sql.instant(rs, "closed_at"));
    }
}

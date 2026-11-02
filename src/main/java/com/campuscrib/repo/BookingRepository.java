package com.campuscrib.repo;

import com.campuscrib.db.Database;
import com.campuscrib.domain.BookingRequest;
import com.campuscrib.domain.BookingStatus;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingRepository {
    private static final String SELECT =
            "SELECT b.*, a.title AS apartment_title, a.code AS apartment_code, a.agent_id, s.full_name AS student_name "
                    + "FROM booking_requests b JOIN apartments a ON a.id = b.apartment_id JOIN users s ON s.id = b.student_id";
    private final Database db;

    public BookingRepository(Database db) {
        this.db = db;
    }

    public long insert(long apartmentId, long studentId, LocalDate moveIn, int leaseMonths, String message) {
        return db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO booking_requests (apartment_id, student_id, move_in, lease_months, message) VALUES (?, ?, ?, ?, ?)", Sql.keys())) {
                ps.setLong(1, apartmentId);
                ps.setLong(2, studentId);
                ps.setDate(3, Date.valueOf(moveIn));
                ps.setInt(4, leaseMonths);
                ps.setString(5, message);
                ps.executeUpdate();
                return Sql.generatedKey(ps);
            }
        });
    }

    public Optional<BookingRequest> findById(long id) {
        List<BookingRequest> found = query(SELECT + " WHERE b.id = ?", id);
        return found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
    }

    public List<BookingRequest> findByStudent(long studentId) {
        return query(SELECT + " WHERE b.student_id = ? ORDER BY b.created_at DESC, b.id DESC", studentId);
    }

    public List<BookingRequest> findByAgent(long agentId) {
        return query(SELECT + " WHERE a.agent_id = ? ORDER BY CASE b.status WHEN 'PENDING' THEN 0 ELSE 1 END, b.created_at DESC, b.id DESC", agentId);
    }

    public boolean hasOpenRequest(long apartmentId, long studentId) {
        return db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM booking_requests WHERE apartment_id = ? AND student_id = ? AND status = 'PENDING'")) {
                ps.setLong(1, apartmentId);
                ps.setLong(2, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    public void decide(Connection c, long id, BookingStatus status, String note) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE booking_requests SET status = ?, agent_note = ?, decided_at = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setString(2, note);
            ps.setTimestamp(3, Sql.now());
            ps.setLong(4, id);
            ps.executeUpdate();
        }
    }

    /** Declines every other pending request for an apartment once one is approved. Returns how many. */
    public int declineOthers(Connection c, long apartmentId, long approvedId, String note) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE booking_requests SET status = 'DECLINED', agent_note = ?, decided_at = ? WHERE apartment_id = ? AND id <> ? AND status = 'PENDING'")) {
            ps.setString(1, note);
            ps.setTimestamp(2, Sql.now());
            ps.setLong(3, apartmentId);
            ps.setLong(4, approvedId);
            return ps.executeUpdate();
        }
    }

    public void withdraw(long id) {
        db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement("UPDATE booking_requests SET status = 'WITHDRAWN', decided_at = ? WHERE id = ? AND status = 'PENDING'")) {
                ps.setTimestamp(1, Sql.now());
                ps.setLong(2, id);
                return ps.executeUpdate();
            }
        });
    }

    private List<BookingRequest> query(String sql, long param) {
        return db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setLong(1, param);
                try (ResultSet rs = ps.executeQuery()) {
                    List<BookingRequest> out = new ArrayList<>();
                    while (rs.next()) out.add(map(rs));
                    return out;
                }
            }
        });
    }

    private static BookingRequest map(ResultSet rs) throws SQLException {
        return new BookingRequest(
                rs.getLong("id"),
                rs.getLong("apartment_id"),
                rs.getString("apartment_title"),
                rs.getString("apartment_code"),
                rs.getLong("student_id"),
                rs.getString("student_name"),
                rs.getLong("agent_id"),
                rs.getDate("move_in").toLocalDate(),
                rs.getInt("lease_months"),
                rs.getString("message"),
                BookingStatus.valueOf(rs.getString("status")),
                rs.getString("agent_note"),
                Sql.instant(rs, "created_at"),
                Sql.instant(rs, "decided_at"));
    }
}

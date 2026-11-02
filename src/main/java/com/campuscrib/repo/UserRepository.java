package com.campuscrib.repo;

import com.campuscrib.db.Database;
import com.campuscrib.domain.Role;
import com.campuscrib.domain.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {
    private static final String COLUMNS = "id, username, full_name, email, role, unit, active, created_at";
    private final Database db;

    public UserRepository(Database db) {
        this.db = db;
    }

    public long insert(String username, String passwordHash, String fullName, String email, Role role, String unit) {
        return db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO users (username, password_hash, full_name, email, role, unit) VALUES (?, ?, ?, ?, ?, ?)", Sql.keys())) {
                ps.setString(1, username);
                ps.setString(2, passwordHash);
                ps.setString(3, fullName);
                ps.setString(4, email);
                ps.setString(5, role.name());
                ps.setString(6, unit);
                ps.executeUpdate();
                return Sql.generatedKey(ps);
            }
        });
    }

    public Optional<User> findById(long id) {
        return one("SELECT " + COLUMNS + " FROM users WHERE id = ?", id);
    }

    public Optional<User> findByUsername(String username) {
        return one("SELECT " + COLUMNS + " FROM users WHERE LOWER(username) = LOWER(?)", username);
    }

    /** The stored hash, read separately so it never travels inside a User. */
    public Optional<String> passwordHash(long userId) {
        return db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement("SELECT password_hash FROM users WHERE id = ?")) {
                ps.setLong(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(rs.getString(1)) : Optional.empty();
                }
            }
        });
    }

    public List<User> findAll() {
        return many("SELECT " + COLUMNS + " FROM users ORDER BY role, full_name");
    }

    public List<User> findActiveByRole(Role role) {
        return many("SELECT " + COLUMNS + " FROM users WHERE role = ? AND active = TRUE ORDER BY full_name", role.name());
    }

    public void update(long id, String fullName, String email, Role role, String unit, boolean active) {
        db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement("UPDATE users SET full_name = ?, email = ?, role = ?, unit = ?, active = ? WHERE id = ?")) {
                ps.setString(1, fullName);
                ps.setString(2, email);
                ps.setString(3, role.name());
                ps.setString(4, unit);
                ps.setBoolean(5, active);
                ps.setLong(6, id);
                return ps.executeUpdate();
            }
        });
    }

    public void updatePassword(long id, String passwordHash) {
        db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement("UPDATE users SET password_hash = ? WHERE id = ?")) {
                ps.setString(1, passwordHash);
                ps.setLong(2, id);
                return ps.executeUpdate();
            }
        });
    }

    public int count() {
        return db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM users"); ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        });
    }

    private Optional<User> one(String sql, Object param) {
        List<User> found = many(sql, param);
        return found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
    }

    private List<User> many(String sql, Object... params) {
        return db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
                try (ResultSet rs = ps.executeQuery()) {
                    List<User> out = new ArrayList<>();
                    while (rs.next()) out.add(map(rs));
                    return out;
                }
            }
        });
    }

    private static User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("full_name"),
                rs.getString("email"),
                Role.valueOf(rs.getString("role")),
                rs.getString("unit"),
                rs.getBoolean("active"),
                Sql.instant(rs, "created_at"));
    }
}

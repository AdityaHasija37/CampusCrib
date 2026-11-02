package com.campuscrib.repo;

import com.campuscrib.db.Database;
import com.campuscrib.domain.Apartment;
import com.campuscrib.domain.ApartmentDraft;
import com.campuscrib.domain.ApartmentStatus;
import com.campuscrib.domain.Photo;
import com.campuscrib.domain.SearchCriteria;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ApartmentRepository {
    private static final String SELECT =
            "SELECT a.*, u.full_name AS agent_name FROM apartments a JOIN users u ON u.id = a.agent_id";
    private final Database db;

    public ApartmentRepository(Database db) {
        this.db = db;
    }

    public long insert(long agentId, ApartmentDraft d) {
        return db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO apartments (agent_id, code, title, neighborhood, address, description, monthly_rent, bedrooms, bathrooms, distance_km, amenities, available_from) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Sql.keys())) {
                ps.setLong(1, agentId);
                bindDraft(ps, 2, d);
                ps.executeUpdate();
                return Sql.generatedKey(ps);
            }
        });
    }

    public void update(long id, ApartmentDraft d) {
        db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE apartments SET code = ?, title = ?, neighborhood = ?, address = ?, description = ?, monthly_rent = ?, bedrooms = ?, bathrooms = ?, distance_km = ?, amenities = ?, available_from = ? WHERE id = ?")) {
                int next = bindDraft(ps, 1, d);
                ps.setLong(next, id);
                return ps.executeUpdate();
            }
        });
    }

    public void setStatus(Connection c, long id, ApartmentStatus status) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE apartments SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public void setStatus(long id, ApartmentStatus status) {
        db.withConnection(c -> {
            setStatus(c, id, status);
            return null;
        });
    }

    public Optional<Apartment> findById(long id) {
        List<Apartment> found = query(SELECT + " WHERE a.id = ?", List.of(id));
        return found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
    }

    public List<Apartment> findByAgent(long agentId) {
        return query(SELECT + " WHERE a.agent_id = ? ORDER BY a.created_at DESC, a.id DESC", List.of(agentId));
    }

    /** Listed apartments matching the filters. Amenity matching is done in Java: the list is small and stored as text. */
    public List<Apartment> search(SearchCriteria s) {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE a.status = 'LISTED'");
        List<Object> params = new ArrayList<>();
        if (s.text() != null && !s.text().isBlank()) {
            sql.append(" AND (LOWER(a.title) LIKE ? OR LOWER(a.neighborhood) LIKE ? OR LOWER(a.address) LIKE ? OR LOWER(a.description) LIKE ?)");
            String like = "%" + s.text().toLowerCase().strip() + "%";
            for (int i = 0; i < 4; i++) params.add(like);
        }
        if (s.maxRent() != null) {
            sql.append(" AND a.monthly_rent <= ?");
            params.add(s.maxRent());
        }
        if (s.minBedrooms() != null) {
            sql.append(" AND a.bedrooms >= ?");
            params.add(s.minBedrooms());
        }
        if (s.maxDistanceKm() != null) {
            sql.append(" AND a.distance_km <= ?");
            params.add(s.maxDistanceKm());
        }
        sql.append(switch (s.sort() == null ? SearchCriteria.Sort.RENT_LOW : s.sort()) {
            case RENT_LOW -> " ORDER BY a.monthly_rent ASC, a.id";
            case RENT_HIGH -> " ORDER BY a.monthly_rent DESC, a.id";
            case CLOSEST -> " ORDER BY a.distance_km ASC, a.id";
            case NEWEST -> " ORDER BY a.created_at DESC, a.id DESC";
        });
        List<Apartment> results = query(sql.toString(), params);
        if (s.amenities() == null || s.amenities().isEmpty()) return results;
        return results.stream().filter(a -> a.amenities().containsAll(s.amenities())).toList();
    }

    public void delete(long id) {
        db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM apartments WHERE id = ?")) {
                ps.setLong(1, id);
                return ps.executeUpdate();
            }
        });
    }

    public long addPhoto(long apartmentId, String caption, byte[] image) {
        return db.withConnection(c -> {
            int position;
            try (PreparedStatement ps = c.prepareStatement("SELECT COALESCE(MAX(position), -1) + 1 FROM apartment_photos WHERE apartment_id = ?")) {
                ps.setLong(1, apartmentId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    position = rs.getInt(1);
                }
            }
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO apartment_photos (apartment_id, caption, position, image) VALUES (?, ?, ?, ?)", Sql.keys())) {
                ps.setLong(1, apartmentId);
                ps.setString(2, caption);
                ps.setInt(3, position);
                ps.setBytes(4, image);
                ps.executeUpdate();
                return Sql.generatedKey(ps);
            }
        });
    }

    public void deletePhoto(long photoId) {
        db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM apartment_photos WHERE id = ?")) {
                ps.setLong(1, photoId);
                return ps.executeUpdate();
            }
        });
    }

    public List<Photo> photos(long apartmentId) {
        return db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement("SELECT id, apartment_id, caption, position, image FROM apartment_photos WHERE apartment_id = ? ORDER BY position")) {
                ps.setLong(1, apartmentId);
                try (ResultSet rs = ps.executeQuery()) {
                    List<Photo> out = new ArrayList<>();
                    while (rs.next()) {
                        out.add(new Photo(rs.getLong("id"), rs.getLong("apartment_id"), rs.getString("caption"), rs.getInt("position"), rs.getBytes("image")));
                    }
                    return out;
                }
            }
        });
    }

    /** The first photo of each apartment, for result cards. */
    public Optional<byte[]> coverPhoto(long apartmentId) {
        return db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement("SELECT image FROM apartment_photos WHERE apartment_id = ? ORDER BY position LIMIT 1")) {
                ps.setLong(1, apartmentId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(rs.getBytes(1)) : Optional.empty();
                }
            }
        });
    }

    private static int bindDraft(PreparedStatement ps, int start, ApartmentDraft d) throws SQLException {
        int i = start;
        ps.setString(i++, d.code().strip());
        ps.setString(i++, d.title().strip());
        ps.setString(i++, d.neighborhood().strip());
        ps.setString(i++, d.address().strip());
        ps.setString(i++, d.description().strip());
        ps.setBigDecimal(i++, d.monthlyRent());
        ps.setInt(i++, d.bedrooms());
        ps.setInt(i++, d.bathrooms());
        ps.setBigDecimal(i++, d.distanceKm());
        ps.setString(i++, String.join(",", d.amenities()));
        ps.setDate(i++, Date.valueOf(d.availableFrom()));
        return i;
    }

    private List<Apartment> query(String sql, List<?> params) {
        return db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
                try (ResultSet rs = ps.executeQuery()) {
                    List<Apartment> out = new ArrayList<>();
                    while (rs.next()) out.add(map(rs));
                    return out;
                }
            }
        });
    }

    private static Apartment map(ResultSet rs) throws SQLException {
        String amenities = rs.getString("amenities");
        return new Apartment(
                rs.getLong("id"),
                rs.getLong("agent_id"),
                rs.getString("agent_name"),
                rs.getString("code"),
                rs.getString("title"),
                rs.getString("neighborhood"),
                rs.getString("address"),
                rs.getString("description"),
                rs.getBigDecimal("monthly_rent"),
                rs.getInt("bedrooms"),
                rs.getInt("bathrooms"),
                rs.getBigDecimal("distance_km"),
                amenities == null || amenities.isBlank() ? List.of() : Arrays.stream(amenities.split(",")).map(String::strip).toList(),
                rs.getDate("available_from").toLocalDate(),
                ApartmentStatus.valueOf(rs.getString("status")));
    }
}

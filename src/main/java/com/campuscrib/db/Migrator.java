package com.campuscrib.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Applies db/V{n}__*.sql scripts in order, once each, and records them in schema_version.
 * Small on purpose: two databases, a handful of scripts, no need for a migration framework.
 */
public final class Migrator {
    static final List<String> SCRIPTS = List.of("V1__schema.sql");

    private Migrator() {}

    public static int migrate(Database db) {
        return db.withConnection(c -> {
            try (Statement s = c.createStatement()) {
                s.execute("CREATE TABLE IF NOT EXISTS schema_version (version INT PRIMARY KEY, script VARCHAR(100) NOT NULL, applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            }
            int current = currentVersion(c);
            int applied = 0;
            for (int i = current; i < SCRIPTS.size(); i++) {
                String script = SCRIPTS.get(i);
                c.setAutoCommit(false);
                try (Statement s = c.createStatement()) {
                    for (String sql : statements(read(script))) s.execute(sql);
                    s.executeUpdate("INSERT INTO schema_version (version, script) VALUES (" + (i + 1) + ", '" + script + "')");
                    c.commit();
                    applied++;
                } catch (SQLException e) {
                    c.rollback();
                    throw new SQLException("Migration " + script + " failed: " + e.getMessage(), e);
                } finally {
                    c.setAutoCommit(true);
                }
            }
            return applied;
        });
    }

    private static int currentVersion(Connection c) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT COALESCE(MAX(version), 0) FROM schema_version")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static String read(String script) {
        try (InputStream in = Migrator.class.getResourceAsStream("/db/" + script)) {
            if (in == null) throw new IllegalStateException("Missing migration /db/" + script);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Splits a script on semicolons, dropping comment-only lines. Scripts must not use ';' inside strings. */
    static List<String> statements(String script) {
        StringBuilder clean = new StringBuilder();
        for (String line : script.split("\n")) {
            if (!line.strip().startsWith("--")) clean.append(line).append('\n');
        }
        List<String> out = new ArrayList<>();
        for (String part : clean.toString().split(";")) {
            if (!part.isBlank()) out.add(part.strip());
        }
        return out;
    }
}

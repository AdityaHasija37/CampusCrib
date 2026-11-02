package com.campuscrib;

import com.campuscrib.db.Database;
import com.campuscrib.db.Migrator;
import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;

/**
 * A fresh, migrated database per test. H2 in memory by default; set CAMPUSCRIB_TEST_DB_URL
 * (plus _USER and _PASSWORD) to run the same tests against a real MySQL server, as CI does.
 */
public final class TestDb {
    private TestDb() {}

    public static Database fresh() {
        String url = System.getenv("CAMPUSCRIB_TEST_DB_URL");
        Database db;
        if (url == null || url.isBlank()) {
            db = new Database("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1", "sa", "");
        } else {
            db = new Database(url, System.getenv().getOrDefault("CAMPUSCRIB_TEST_DB_USER", "root"), System.getenv().getOrDefault("CAMPUSCRIB_TEST_DB_PASSWORD", ""));
            dropAll(db);
        }
        Migrator.migrate(db);
        return db;
    }

    private static void dropAll(Database db) {
        db.withConnection(c -> {
            try (Statement s = c.createStatement()) {
                s.execute("SET FOREIGN_KEY_CHECKS = 0");
                for (String t : new String[] {"request_events", "work_requests", "booking_requests", "apartment_photos", "apartments", "users", "schema_version"}) {
                    s.execute("DROP TABLE IF EXISTS " + t);
                }
                s.execute("SET FOREIGN_KEY_CHECKS = 1");
            }
            return (Connection) null;
        });
    }
}

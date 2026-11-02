package com.campuscrib.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/**
 * Where the database lives. Looked up in this order:
 * environment variables (CAMPUSCRIB_DB_URL, _USER, _PASSWORD), then campuscrib.properties in the
 * working directory, then the embedded demo database in ~/.campuscrib.
 */
public record AppConfig(String dbUrl, String dbUser, String dbPassword, boolean demo) {

    public static final String DEMO_URL =
            "jdbc:h2:file:" + Path.of(System.getProperty("user.home"), ".campuscrib", "demo") + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE";

    public static AppConfig load() {
        return load(System.getenv(), Path.of("campuscrib.properties"));
    }

    static AppConfig load(Map<String, String> env, Path propertiesFile) {
        Properties props = new Properties();
        if (Files.isRegularFile(propertiesFile)) {
            try (InputStream in = Files.newInputStream(propertiesFile)) {
                props.load(in);
            } catch (IOException e) {
                throw new IllegalStateException("Couldn't read " + propertiesFile.toAbsolutePath(), e);
            }
        }
        String url = first(env.get("CAMPUSCRIB_DB_URL"), props.getProperty("db.url"));
        if (url == null) return new AppConfig(DEMO_URL, "sa", "", true);
        return new AppConfig(
                url,
                first(env.get("CAMPUSCRIB_DB_USER"), props.getProperty("db.user"), "campuscrib"),
                first(env.get("CAMPUSCRIB_DB_PASSWORD"), props.getProperty("db.password"), ""),
                false);
    }

    private static String first(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v.trim();
        }
        return null;
    }

    /** The URL without credentials, for showing to the user. */
    public String describe() {
        return demo ? "embedded demo database (~/.campuscrib)" : dbUrl.replaceAll("\\?.*$", "");
    }
}

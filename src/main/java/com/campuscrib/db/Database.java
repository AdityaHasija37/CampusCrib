package com.campuscrib.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Hands out JDBC connections. A desktop app with one user at a time doesn't need a pool;
 * each repository call opens a connection and closes it.
 */
public final class Database {
    private final String url;
    private final String user;
    private final String password;

    public Database(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public boolean isMySql() {
        return url.startsWith("jdbc:mysql:");
    }

    /** Runs work in one transaction: commits if it returns, rolls back if it throws. */
    public <T> T inTransaction(SqlWork<T> work) {
        try (Connection c = connect()) {
            c.setAutoCommit(false);
            try {
                T result = work.run(c);
                c.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> T withConnection(SqlWork<T> work) {
        try (Connection c = connect()) {
            return work.run(c);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @FunctionalInterface
    public interface SqlWork<T> {
        T run(Connection connection) throws SQLException;
    }
}

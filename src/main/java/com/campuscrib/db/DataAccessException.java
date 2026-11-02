package com.campuscrib.db;

/** A database problem the UI can report without a stack trace. */
public class DataAccessException extends RuntimeException {
    public DataAccessException(Throwable cause) {
        super("Database error: " + cause.getMessage(), cause);
    }
}

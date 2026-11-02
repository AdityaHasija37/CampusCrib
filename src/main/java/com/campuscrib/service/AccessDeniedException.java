package com.campuscrib.service;

/** The signed-in user isn't allowed to do this. */
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}

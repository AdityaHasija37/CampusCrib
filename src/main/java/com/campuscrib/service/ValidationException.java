package com.campuscrib.service;

import java.util.List;

/** Input the user can fix. Messages are written to be shown as-is. */
public class ValidationException extends RuntimeException {
    private final List<String> problems;

    public ValidationException(List<String> problems) {
        super(String.join("\n", problems));
        this.problems = List.copyOf(problems);
    }

    public ValidationException(String problem) {
        this(List.of(problem));
    }

    public List<String> problems() {
        return problems;
    }
}

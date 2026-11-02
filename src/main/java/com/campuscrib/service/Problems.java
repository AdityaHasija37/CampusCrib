package com.campuscrib.service;

import java.util.ArrayList;
import java.util.List;

/** Collects validation problems, then throws them together so the user sees everything at once. */
final class Problems {
    private final List<String> list = new ArrayList<>();

    Problems check(boolean ok, String problem) {
        if (!ok) list.add(problem);
        return this;
    }

    static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    void throwIfAny() {
        if (!list.isEmpty()) throw new ValidationException(list);
    }
}

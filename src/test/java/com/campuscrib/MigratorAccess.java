package com.campuscrib;

import java.lang.reflect.Method;
import java.util.List;

/** Reaches the package-private statement splitter without widening its visibility. */
final class MigratorAccess {
    private MigratorAccess() {}

    @SuppressWarnings("unchecked")
    static List<String> statements(String script) {
        try {
            Method m = com.campuscrib.db.Migrator.class.getDeclaredMethod("statements", String.class);
            m.setAccessible(true);
            return (List<String>) m.invoke(null, script);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}

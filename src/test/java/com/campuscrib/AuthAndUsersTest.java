package com.campuscrib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.campuscrib.domain.Role;
import com.campuscrib.domain.User;
import com.campuscrib.service.AccessDeniedException;
import com.campuscrib.service.PasswordHasher;
import com.campuscrib.service.ValidationException;
import org.junit.jupiter.api.Test;

class AuthAndUsersTest {
    final Fixture f = new Fixture();

    @Test
    void passwordsAreHashedAndVerified() {
        String hash = PasswordHasher.hash("correct horse");
        assertTrue(hash.startsWith("pbkdf2$"));
        assertTrue(PasswordHasher.matches("correct horse", hash));
        assertFalse(PasswordHasher.matches("wrong", hash));
        assertFalse(PasswordHasher.matches("anything", "plaintext"));
    }

    @Test
    void signsInCaseInsensitivelyWithTheSameErrorForUnknownAndWrong() {
        User u = f.person(Role.STUDENT);
        assertEquals(u.id(), f.s.auth.login(u.username().toUpperCase(), "password123").id());
        var wrong = assertThrows(ValidationException.class, () -> f.s.auth.login(u.username(), "nope"));
        var unknown = assertThrows(ValidationException.class, () -> f.s.auth.login("ghost", "nope"));
        assertEquals(wrong.getMessage(), unknown.getMessage());
    }

    @Test
    void deactivatedAccountsCantSignIn() {
        User admin = f.person(Role.ADMIN);
        User u = f.person(Role.STUDENT);
        f.s.users.update(admin, u.id(), u.fullName(), u.email(), u.role(), null, false);
        assertThrows(AccessDeniedException.class, () -> f.s.auth.login(u.username(), "password123"));
    }

    @Test
    void onlyAdminsManageAccounts() {
        User admin = f.person(Role.ADMIN);
        User manager = f.person(Role.BUILDING_MANAGER);
        assertThrows(AccessDeniedException.class, () -> f.s.users.all(manager));

        User created = f.s.users.create(admin, "new.resident", "longenough", "New Resident", "new@example.test", Role.RESIDENT, "A-1");
        assertEquals(Role.RESIDENT, created.role());
        assertEquals("New Resident", f.s.auth.login("new.resident", "longenough").fullName());

        assertThrows(ValidationException.class, () -> f.s.users.create(admin, "new.resident", "longenough", "Dup", "d@example.test", Role.STUDENT, null));
        var noUnit = assertThrows(ValidationException.class, () -> f.s.users.create(admin, "nounit", "longenough", "No Unit", "n@example.test", Role.RESIDENT, ""));
        assertTrue(noUnit.getMessage().contains("unit"));
        assertThrows(ValidationException.class, () -> f.s.users.update(admin, admin.id(), admin.fullName(), admin.email(), Role.STUDENT, null, true));
    }

    @Test
    void changesOwnPassword() {
        User u = f.person(Role.STUDENT);
        assertThrows(ValidationException.class, () -> f.s.auth.changePassword(u, "wrong", "newpassword1"));
        f.s.auth.changePassword(u, "password123", "newpassword1");
        assertEquals(u.id(), f.s.auth.login(u.username(), "newpassword1").id());
    }

    @Test
    void rolesBelongToTheDesignedEnterprises() {
        assertEquals(Role.PLUMBER, Role.forTrade(com.campuscrib.domain.Trade.PLUMBING));
        assertTrue(Role.PEST_CONTROL.isServiceProvider());
        assertFalse(Role.BUILDING_MANAGER.isServiceProvider());
        assertEquals("Real Estate Agency", Role.AGENT.enterprise().displayName());
    }
}

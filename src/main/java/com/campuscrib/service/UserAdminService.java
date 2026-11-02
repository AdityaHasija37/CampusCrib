package com.campuscrib.service;

import com.campuscrib.domain.Role;
import com.campuscrib.domain.User;
import com.campuscrib.repo.UserRepository;
import java.util.List;

/** The system admin creates accounts, changes roles and deactivates people. */
public class UserAdminService {
    private final UserRepository users;

    public UserAdminService(UserRepository users) {
        this.users = users;
    }

    public List<User> all(User admin) {
        requireAdmin(admin);
        return users.findAll();
    }

    public List<User> byRole(Role role) {
        return users.findActiveByRole(role);
    }

    public User create(User admin, String username, String password, String fullName, String email, Role role, String unit) {
        requireAdmin(admin);
        validate(fullName, email, role, unit);
        new Problems()
                .check(username != null && username.strip().matches("[A-Za-z0-9_.]{3,40}"), "Usernames are 3–40 letters, numbers, dots or underscores.")
                .throwIfAny();
        checkPassword(password);
        if (users.findByUsername(username.strip()).isPresent()) throw new ValidationException("That username is taken.");
        long id = users.insert(username.strip(), PasswordHasher.hash(password), fullName.strip(), email.strip(), role, blankToNull(unit));
        return users.findById(id).orElseThrow();
    }

    public User update(User admin, long id, String fullName, String email, Role role, String unit, boolean active) {
        requireAdmin(admin);
        validate(fullName, email, role, unit);
        if (id == admin.id() && (!active || role != Role.ADMIN)) throw new ValidationException("You can't remove your own admin access.");
        users.update(id, fullName.strip(), email.strip(), role, blankToNull(unit), active);
        return users.findById(id).orElseThrow();
    }

    public void resetPassword(User admin, long id, String password) {
        requireAdmin(admin);
        checkPassword(password);
        users.updatePassword(id, PasswordHasher.hash(password));
    }

    static void checkPassword(String password) {
        if (password == null || password.length() < 8) throw new ValidationException("Passwords need at least 8 characters.");
    }

    private static void validate(String fullName, String email, Role role, String unit) {
        new Problems()
                .check(!Problems.blank(fullName), "Add the person's full name.")
                .check(email != null && email.strip().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"), "Enter a valid email address.")
                .check(role != null, "Choose a role.")
                .check(role != Role.RESIDENT || !Problems.blank(unit), "Residents need a unit, e.g. A101.")
                .throwIfAny();
    }

    private static String blankToNull(String s) {
        return Problems.blank(s) ? null : s.strip();
    }

    private static void requireAdmin(User user) {
        if (user.role() != Role.ADMIN) throw new AccessDeniedException("Only the system admin manages accounts.");
    }
}

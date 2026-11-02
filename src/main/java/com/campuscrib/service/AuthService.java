package com.campuscrib.service;

import com.campuscrib.domain.User;
import com.campuscrib.repo.UserRepository;

public class AuthService {
    private final UserRepository users;

    public AuthService(UserRepository users) {
        this.users = users;
    }

    /** Same message for an unknown username and a wrong password, so accounts can't be probed. */
    public User login(String username, String password) {
        if (Problems.blank(username) || password == null || password.isEmpty()) {
            throw new ValidationException("Enter your username and password.");
        }
        User user = users.findByUsername(username.strip())
                .filter(u -> users.passwordHash(u.id()).map(h -> PasswordHasher.matches(password, h)).orElse(false))
                .orElseThrow(() -> new ValidationException("That username and password don't match."));
        if (!user.active()) throw new AccessDeniedException("This account has been deactivated. Contact your administrator.");
        return user;
    }

    public void changePassword(User user, String current, String next) {
        boolean ok = users.passwordHash(user.id()).map(h -> PasswordHasher.matches(current, h)).orElse(false);
        if (!ok) throw new ValidationException("Your current password is incorrect.");
        UserAdminService.checkPassword(next);
        users.updatePassword(user.id(), PasswordHasher.hash(next));
    }
}

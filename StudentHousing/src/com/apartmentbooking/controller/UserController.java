package com.apartmentbooking.controller;

import models.UserAccount;
import models.Roles;
import models.ResidentRole;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserController {
    private List<UserAccount> users;
    
    public UserController() {
        users = new ArrayList<>();
        initializeDefaultUsers();
    }
    
    private void initializeDefaultUsers() {
        // Add default users if needed
    }
    
    public Optional<UserAccount> authenticateUser(String username, String password) {
        return users.stream()
            .filter(user -> user.getUsername().equals(username) && 
                           user.getPassword().equals(password))
            .findFirst();
    }
    
    public UserAccount createUser(String username, String password, Roles role) {
        if (isUsernameAvailable(username)) {
            UserAccount newUser = new UserAccount(username, password, role);
            users.add(newUser);
            return newUser;
        }
        return null;
    }
    
    private boolean isUsernameAvailable(String username) {
        return users.stream()
            .noneMatch(user -> user.getUsername().equals(username));
    }
    
    public List<UserAccount> getUsersByRole(Class<? extends Roles> roleClass) {
        return users.stream()
            .filter(user -> roleClass.isInstance(user.getRole()))
            .collect(Collectors.toList());
    }
    
    public boolean updateUserPassword(String username, String oldPassword, String newPassword) {
        return users.stream()
            .filter(user -> user.getUsername().equals(username) && 
                           user.getPassword().equals(oldPassword))
            .findFirst()
            .map(user -> {
                user.setPassword(newPassword);
                return true;
            })
            .orElse(false);
    }
    public void logout() {
        users = null;
    }
} 
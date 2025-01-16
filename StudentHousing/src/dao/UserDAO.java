/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author nidhinair
 */
 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import models.AgentRole;
import models.BuildingManagerRole;
import models.CarpenterRole;
import models.ElectricianRole;
import models.PestControlRole;
import models.PlumberRole;
import models.ResidentRole;
import models.Roles;
import models.UserAccount;
import models.UserRole;
 
public class UserDAO {
    public static List<UserAccount> getAllUsers() {
        List<UserAccount> users = new ArrayList<>();
        String query = "SELECT username, password, role FROM users";
 
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
 
            while (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");
                String role = rs.getString("role");
 
                Roles userRole = mapRole(role);
                users.add(new UserAccount(username, password, userRole));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }
 
    private static Roles mapRole(String roleName) {
        switch (roleName) {
            case "Resident":
                return new ResidentRole();
            case "Agent":
                return new AgentRole();
            case "Building Manager":
                return new BuildingManagerRole();
            case "Plumber":
                return new PlumberRole();
            case "Electrician":
                return new ElectricianRole();
            case "Pest Control Role":
                return new PestControlRole();
            case "Carpenter":
                return new CarpenterRole();
            case "UserRole":
                return new UserRole();
            default:
                throw new IllegalArgumentException("Unknown role: " + roleName);
        }
    }
}
package ui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import models.UserAccount;
import models.WorkRequest;
import models.ResidentRole;
import com.apartmentbooking.controller.ApartmentController;
import com.apartmentbooking.controller.BookingController;
import com.apartmentbooking.controller.UserController;
import dao.UserDAO;
import models.AgentRole;
import models.BuildingManagerRole;
import models.CarpenterRole;
import models.ElectricianRole;
import models.MaintenanceRequest;
import models.PestControlRole;
import models.PlumberRole;
import models.UserRole;

public class StudentHousing {
    private static JFrame frame;
    private static LoginPanel loginPanel;
    private static ResidentDashboardPanel residentDashboard;
    private static List<UserAccount> userAccounts;
    private static List<WorkRequest> requests;
    private static ApartmentController apartmentController;
    private static BookingController bookingController;
    private static UserController userController;

    public static void main(String[] args) {
        initializeData();
        SwingUtilities.invokeLater(StudentHousing::createAndShowGUI);
    }

    private static void initializeData() {
        // Initialize controllers
        apartmentController = new ApartmentController();
        bookingController = new BookingController();
        userController = new UserController();

        // Load users from the database
        initializeUsers();

        // Initialize requests - assuming you're adding requests via UI interaction
        requests = new ArrayList<>();
    }

    private static void initializeUsers() {
        try {
            userAccounts = UserDAO.getAllUsers(); // Assuming `UserDAO.getAllUsers` returns a List<UserAccount>
            System.out.println("Users loaded from the database: " + userAccounts.size());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading users from the database.");
            userAccounts = List.of(); // Fallback to an empty list if database loading fails
        }
    }

    private static void createAndShowGUI() {
        frame = new JFrame("Student Housing Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);

        showLoginPanel();
        frame.setVisible(true);
    }

    private static void showLoginPanel() {
        if (loginPanel == null) {
            loginPanel = new LoginPanel(frame, userAccounts, StudentHousing::handleLoginSuccess);
        }
        frame.getContentPane().removeAll();
        frame.getContentPane().add(loginPanel);
        frame.revalidate();
        frame.repaint();
    }

    private static void handleLoginSuccess(UserAccount loggedInUser) {
        System.out.println("User logged in: " + loggedInUser.getUsername());
        System.out.println("Role: " + loggedInUser.getRole().getRoleName());
 
        if (loggedInUser.getRole() instanceof ResidentRole) {
            showResidentDashboard(loggedInUser);
        } else if (loggedInUser.getRole() instanceof BuildingManagerRole) {
            showBuildingManagerDashboard(loggedInUser);
        } else if (loggedInUser.getRole() instanceof AgentRole) {
            showAgentDashboard(loggedInUser);
        } else if (loggedInUser.getRole() instanceof PlumberRole ||
                   loggedInUser.getRole() instanceof ElectricianRole ||
                   loggedInUser.getRole() instanceof CarpenterRole ||
                   loggedInUser.getRole() instanceof PestControlRole) {
            showRoleDashboard(loggedInUser);
        } else if (loggedInUser.getRole() instanceof UserRole) {
            showUserDashboard(loggedInUser);}
        else {
            System.out.println("Unknown role: " + loggedInUser.getRole().getRoleName());
        }
    }

    private static void showResidentDashboard(UserAccount user) {
        if (residentDashboard == null) {
            residentDashboard = new ResidentDashboardPanel(user, requests, () -> {
                // Handle create request action
            }, StudentHousing::showLoginPanel);
        }
        residentDashboard.refreshTable(requests); // Fixed: removed user parameter

        frame.getContentPane().removeAll();
        frame.getContentPane().add(residentDashboard);
        frame.revalidate();
        frame.repaint();
    }

    private static void showBuildingManagerDashboard(UserAccount user) {
        BuildingManagerDashboardPanel panel = new BuildingManagerDashboardPanel(user, requests, StudentHousing::showLoginPanel);
        updateFrame(panel);
    }
 
    private static void showUserDashboard(UserAccount user) {
    UserDashboard dashboard = new UserDashboard(user, userController, apartmentController, bookingController);
    dashboard.setVisible(true); // Display UserDashboard as a new JFrame
}

    // Ensure this method works for AgentRole
    private static void showAgentDashboard(UserAccount user) {
    // Check if AddListingDialog is a JDialog (which it likely is)
    //AddListingDialog dialog = new AddListingDialog(frame, user, apartmentController, bookingController);
    AgentDashboardPanel panel = new AgentDashboardPanel(user, userController,  apartmentController, bookingController);
    panel.setVisible(true); // Make the dialog visible instead of adding it to the frame
    }

 
    private static void showRoleDashboard(UserAccount user) {
        RoleDashboardPanel panel = new RoleDashboardPanel(user, requests, StudentHousing::showLoginPanel);
        updateFrame(panel);
    }
 
    private static void showWorkRequestPanel(UserAccount user) {
        WorkRequestPanel panel = new WorkRequestPanel(() -> showResidentDashboard(user),requests, user);
        updateFrame(panel);
    }
 
    private static void updateFrame(JPanel panel) {
        frame.getContentPane().removeAll();
        frame.getContentPane().add(panel);
        frame.revalidate();
        frame.repaint();
    }

}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

/**
 *
 * @author nidhinair
 */
public class MaintenanceRequest extends WorkRequest {
    public MaintenanceRequest(String type) {
        super(type, determineResponsibleRole(type), true); // Role-specific maintenance request
    }
 
    private static String determineResponsibleRole(String type) {
        switch (type.toLowerCase()) {
            case "plumbing":
                return "Plumber";
            case "electrical":
                return "Electrician";
            case "carpenter":
                return "Carpenter";
            case "pest control":
                return "Pest Control Role";
            default:
                throw new IllegalArgumentException("Invalid maintenance type: " + type);
        }
    }
}
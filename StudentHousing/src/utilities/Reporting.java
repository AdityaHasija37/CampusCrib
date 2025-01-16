/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utilities;

/**
 *
 * @author nidhinair
 */
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.WorkRequest;
 
public class Reporting {
    public static Map<String, Integer> getRequestsByStatus(List<WorkRequest> requests) {
        Map<String, Integer> statusCount = new HashMap<>();
        for (WorkRequest request : requests) {
            if (request.getType().equalsIgnoreCase("Amenity Booking") ||
                List.of("Plumbing", "Electrical", "Carpenter", "Pest Control").contains(request.getType())) {
                statusCount.put(request.getStatus(), statusCount.getOrDefault(request.getStatus(), 0) + 1);
            }
        }
        return statusCount;
    }
}
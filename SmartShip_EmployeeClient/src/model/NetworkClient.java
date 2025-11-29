package model;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;

/**
 * Handles all network communication with the SmartShip server
 */
public class NetworkClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5000;
    
    /**
     * Generic method to send a command and receive response
     */
    private static Object sendCommand(String action, Object... params) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            
            // Send action
            out.writeObject(action);
            out.flush();
            
            // Send parameters
            for (Object param : params) {
                out.writeObject(param);
                out.flush();
            }
            
            // Receive response
            return in.readObject();
            
        } catch (Exception e) {
            System.err.println("Network error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * LOGIN
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> login(String username, String password) {
        return (Map<String, String>) sendCommand("LOGIN", username, password);
    }
    
    /**
     * REGISTER
     */
    public static boolean register(String username, String password, String email, 
                                   String phone, String address, String role) {
        Object result = sendCommand("REGISTER", username, password, email, phone, address, role);
        return result != null && (Boolean) result;
    }
    
    /**
     * GET PENDING SHIPMENTS (for Manager)
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, String>> getPendingShipments() {
        return (List<Map<String, String>>) sendCommand("GET_PENDING_SHIPMENTS");
    }
    
    /**
     * ASSIGN SHIPMENT TO VEHICLE (Updated)
     * @return "SUCCESS" if successful, "ERROR: message" with details if failed
     */
    public static String assignShipment(int shipmentId, int vehicleId) {
        Object result = sendCommand("ASSIGN_SHIPMENT", shipmentId, vehicleId);
        
        if (result == null) {
            return "ERROR: No response from server";
        }
        
        // Handle both boolean (old) and String (new) responses
        if (result instanceof Boolean) {
            return ((Boolean) result) ? "SUCCESS" : "ERROR: Assignment failed";
        }
        
        return (String) result;
    }
    
    /**
     * GET AVAILABLE VEHICLES (for Manager)
     * Returns vehicles that can accommodate a shipment in a specific zone
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, String>> getAvailableVehicles(int zone, double requiredCapacity) {
        return (List<Map<String, String>>) sendCommand("GET_AVAILABLE_VEHICLES", zone, requiredCapacity);
    }
    
    /**
     * GET VEHICLE DETAILS (for Manager)
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> getVehicleDetails(int vehicleId) {
        return (Map<String, String>) sendCommand("GET_VEHICLE_DETAILS", vehicleId);
    }
    
    /**
     * GENERATE DAILY REPORT (for Manager)
     */
    public static String generateDailyReport() {
        return (String) sendCommand("GENERATE_DAILY_REPORT");
    }
    
    /**
     * GET DRIVER DELIVERIES (for Driver)
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, String>> getDriverDeliveries(int userId) {
        return (List<Map<String, String>>) sendCommand("GET_DRIVER_DELIVERIES", userId);
    }
    
    /**
     * UPDATE DELIVERY STATUS (for Driver)
     */
    public static boolean updateDeliveryStatus(String trackingNumber, String status) {
        Object result = sendCommand("UPDATE_DELIVERY_STATUS", trackingNumber, status);
        return result != null && (Boolean) result;
    }
    
    /**
     * TRACK PACKAGE (for Clerk)
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> trackPackage(String trackingNumber) {
        return (Map<String, String>) sendCommand("TRACK_PACKAGE", trackingNumber);
    }
    
    /**
     * GET EMPLOYEE INFO
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> getEmployeeInfo(int userId, String role) {
        // Note: This would require adding GET_EMPLOYEE_INFO command to server
        // For now, we'll work with the data we get from login -PLEASE REMEBER TEAM BECAUSE I HAVEN'T IMPLEMENT THE CHANGES
        return null;
    }
    


    /**
     * GET ALL VEHICLES (for Manager)
     * Returns complete list of all vehicles with driver and load information
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, String>> getAllVehicles() {
        return (List<Map<String, String>>) sendCommand("GET_ALL_VEHICLES");
    }

    /**
     * GET VEHICLE PACKAGES (for Manager)
     * Returns all packages/shipments assigned to a specific vehicle
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, String>> getVehiclePackages(int vehicleId) {
        return (List<Map<String, String>>) sendCommand("GET_VEHICLE_PACKAGES", vehicleId);
    }
    
    /**
     * GET FLEET OVERVIEW (for Manager)
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, String>> getFleetOverview() {
        return (List<Map<String, String>>) sendCommand("GET_FLEET_OVERVIEW");
    }

    /**
     * GET VEHICLE SHIPMENTS (for Manager)
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, String>> getVehicleShipments(int vehicleId) {
        return (List<Map<String, String>>) sendCommand("GET_VEHICLE_SHIPMENTS", vehicleId);
    }

    /**
     * GET DRIVER FLEET INFO (for Manager)
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getDriverFleetInfo(int driverId) {
        return (Map<String, Object>) sendCommand("GET_DRIVER_FLEET_INFO", driverId);
    }

    /**
     * UNASSIGN SHIPMENT FROM VEHICLE (for Manager)
     */
    public static boolean unassignShipmentFromVehicle(String trackingNumber) {
        Object result = sendCommand("UNASSIGN_SHIPMENT_FROM_VEHICLE", trackingNumber);
        return result != null && (Boolean) result;
    }

    /**
     * GET FLEET STATISTICS (for Manager)
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> getFleetStatistics() {
        return (Map<String, String>) sendCommand("GET_FLEET_STATISTICS");
    }
}
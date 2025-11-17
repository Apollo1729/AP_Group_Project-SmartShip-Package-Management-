package database;

import java.sql.*;
import java.util.*;

/**
 * Handles all database operations for the SmartShip system.
 * This class talks to MySQL and performs CRUD operations
 * (Create, Read, Update, Delete) for users and shipments.
 */
public class DatabaseHelper {
    // Database connection info
    private static final String URL = "jdbc:mysql://localhost:3307/sspms";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    
    /**
     * Creates a connection to the database.
     * Every time we need to run a query, we call this first.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    /**
     * REGISTER NEW USER
     * Creates a new customer account in the database.
     * Default role is set to 'Customer'.
     * 
     * @return true if registration succeeded, false if it failed
     */
    public static boolean registerUser(String username, String password, String email, String phone, String address , String role) {
        // SQL query with ? placeholders to prevent SQL injection
        String sql = "INSERT INTO users (username, password, email, phone, address, role) VALUES (?, ?, ?, ?, ?, ?)";
        
        // try-with-resources automatically closes the connection and statement
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Fill in the ? placeholders with actual values
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, email);
            stmt.setString(4, phone);
            stmt.setString(5, address);
            stmt.setString(6, role);
            // executeUpdate returns number of rows affected
            // If > 0, the insert worked
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * LOGIN
     * Checks if username and password match a user in the database.
     * 
     * @return Map with user info if login succeeds, null if it fails
     */
    public static Map<String, String> login(String username, String password) {
        String sql = "SELECT user_id, username, email, phone, address, role FROM users WHERE username = ? AND password = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            
            // If we found a matching user
            if (rs.next()) {
                // Put all their info in a map to send back
                Map<String, String> userInfo = new HashMap<>();
                userInfo.put("userId", String.valueOf(rs.getInt("user_id")));
                userInfo.put("username", rs.getString("username"));
                userInfo.put("email", rs.getString("email"));
                userInfo.put("phone", rs.getString("phone"));
                userInfo.put("address", rs.getString("address"));
                userInfo.put("role", rs.getString("role"));
                return userInfo;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;  // Login failed
    }
    
    /**
     * GET CUSTOMER INFO
     * Retrieves a user's profile information.
     */
    public static Map<String, String> getCustomerInfo(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Map<String, String> info = new HashMap<>();
                info.put("username", rs.getString("username"));
                info.put("email", rs.getString("email"));
                info.put("phone", rs.getString("phone"));
                info.put("address", rs.getString("address"));
                return info;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * UPDATE CUSTOMER INFO
     * Lets users edit their email, phone, and address.
     */
    public static boolean updateCustomerInfo(int userId, String email, String phone, String address) {
        String sql = "UPDATE users SET email = ?, phone = ?, address = ? WHERE user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            stmt.setString(2, phone);
            stmt.setString(3, address);
            stmt.setInt(4, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * CREATE SHIPMENT
     * Customer creates a new delivery order.
     * Automatically generates a tracking number and calculates cost.
     * 
     * @return tracking number if successful, null if failed
     */
    public static String createShipment(int userId, String senderInfo, String recipientInfo, 
                                       double weight, String dimensions, String type, int zone) {
        String sql = "INSERT INTO shipments (user_id, sender_info, recipient_info, weight, " +
                    "dimensions, package_type, zone, status, tracking_number, cost) VALUES (?, ?, ?, ?, ?, ?, ?, 'Pending', ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Generate unique tracking number using current time
            String trackingNumber = "SHIP" + System.currentTimeMillis();
            // Calculate how much to charge based on weight, zone, and type
            double cost = calculateShippingCost(weight, zone, type);
            
            stmt.setInt(1, userId);
            stmt.setString(2, senderInfo);
            stmt.setString(3, recipientInfo);
            stmt.setDouble(4, weight);
            stmt.setString(5, dimensions);
            stmt.setString(6, type);
            stmt.setInt(7, zone);
            stmt.setString(8, trackingNumber);
            stmt.setDouble(9, cost);
            
            stmt.executeUpdate();
            return trackingNumber;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Helper method to calculate shipping cost.
     * Formula: (weight * 5) + (zone * 10) + type surcharge
     * Express adds $20, Fragile adds $15
     */
    private static double calculateShippingCost(double weight, int zone, String type) {
        double baseCost = weight * 5.0;
        double zoneCost = zone * 10.0;
        double typeCost = type.equals("Express") ? 20.0 : (type.equals("Fragile") ? 15.0 : 0.0);
        return baseCost + zoneCost + typeCost;
    }
    
    /**
     * GET CUSTOMER ORDERS
     * Retrieves all shipments belonging to a customer.
     * Ordered by most recent first.
     * 
     * @return List of maps, each map contains one shipment's details
     */
    public static List<Map<String, String>> getCustomerOrders(int userId) {
        String sql = "SELECT shipment_id, tracking_number, recipient_info, weight, package_type, zone, status, cost, payment_status " +
                    "FROM shipments WHERE user_id = ? ORDER BY created_at DESC";
        List<Map<String, String>> orders = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            // Loop through all the shipments and add them to our list
            while (rs.next()) {
                Map<String, String> order = new HashMap<>();
                order.put("shipmentId", String.valueOf(rs.getInt("shipment_id")));
                order.put("trackingNumber", rs.getString("tracking_number"));
                order.put("recipientInfo", rs.getString("recipient_info"));
                order.put("weight", String.valueOf(rs.getDouble("weight")));
                order.put("packageType", rs.getString("package_type"));
                order.put("zone", String.valueOf(rs.getInt("zone")));
                order.put("status", rs.getString("status"));
                order.put("cost", String.format("%.2f", rs.getDouble("cost")));
                order.put("paymentStatus", rs.getString("payment_status"));
                orders.add(order);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }
    
    /**
     * MAKE PAYMENT
     * Marks a shipment as paid and records the payment method.
     */
    public static boolean makePayment(int shipmentId, String paymentMethod) {
        String sql = "UPDATE shipments SET payment_status = 'Paid', payment_method = ? WHERE shipment_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, paymentMethod);
            stmt.setInt(2, shipmentId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * GET PENDING SHIPMENTS (for clerks/managers)
     * Shows all shipments waiting to be assigned to a vehicle.
     * Joins with users table to show customer name.
     */
    public static List<Map<String, String>> getPendingShipments() {
        String sql = "SELECT s.shipment_id, s.tracking_number, s.weight, s.zone, s.package_type, u.username " +
                    "FROM shipments s JOIN users u ON s.user_id = u.user_id WHERE s.status = 'Pending'";
        List<Map<String, String>> shipments = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, String> shipment = new HashMap<>();
                shipment.put("shipmentId", String.valueOf(rs.getInt("shipment_id")));
                shipment.put("trackingNumber", rs.getString("tracking_number"));
                shipment.put("weight", String.valueOf(rs.getDouble("weight")));
                shipment.put("zone", String.valueOf(rs.getInt("zone")));
                shipment.put("packageType", rs.getString("package_type"));
                shipment.put("customer", rs.getString("username"));
                shipments.add(shipment);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return shipments;
    }
    
    /**
     * ASSIGN SHIPMENT TO VEHICLE
     * Clerk assigns a pending shipment to a delivery vehicle.
     * Changes status from 'Pending' to 'Assigned'.
     */
    public static boolean assignShipmentToVehicle(int shipmentId, int vehicleId) {
        String sql = "UPDATE shipments SET vehicle_id = ?, status = 'Assigned' WHERE shipment_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, vehicleId);
            stmt.setInt(2, shipmentId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * GET DRIVER DELIVERIES
     * Shows a driver all their assigned deliveries.
     * Joins shipments with vehicles to match driver to their packages.
     * Only shows packages that are 'Assigned' or 'In Transit'.
     */
    public static List<Map<String, String>> getDriverDeliveries(int userId) {
        String sql = "SELECT s.tracking_number, s.recipient_info, s.status, s.address " +
                    "FROM shipments s JOIN vehicles v ON s.vehicle_id = v.vehicle_id " +
                    "WHERE v.driver_id = ? AND s.status IN ('Assigned', 'In Transit')";
        List<Map<String, String>> deliveries = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> delivery = new HashMap<>();
                delivery.put("trackingNumber", rs.getString("tracking_number"));
                delivery.put("recipientInfo", rs.getString("recipient_info"));
                delivery.put("status", rs.getString("status"));
                delivery.put("address", rs.getString("address"));
                deliveries.add(delivery);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return deliveries;
    }
    
    /**
     * UPDATE DELIVERY STATUS
     * Driver updates package status as they deliver.
     * Common statuses: 'In Transit', 'Out for Delivery', 'Delivered'
     */
    public static boolean updateDeliveryStatus(String trackingNumber, String status) {
        String sql = "UPDATE shipments SET status = ? WHERE tracking_number = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setString(2, trackingNumber);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * GENERATE DAILY REPORT (for managers)
     * Creates a summary of today's shipments.
     * Shows total count, total weight, and total revenue.
     * CURDATE() only gets shipments from today.
     */
    public static String generateDailyReport() {
        String sql = "SELECT COUNT(*) as total, SUM(weight) as total_weight, SUM(cost) as total_revenue " +
                    "FROM shipments WHERE DATE(created_at) = CURDATE()";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return "DAILY REPORT:\n" +
                       "Total Shipments: " + rs.getInt("total") + "\n" +
                       "Total Weight: " + rs.getDouble("total_weight") + " kg\n" +
                       "Total Revenue: $" + String.format("%.2f", rs.getDouble("total_revenue"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Error generating report";
    }
}
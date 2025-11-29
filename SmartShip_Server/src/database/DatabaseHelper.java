package database;

import java.text.SimpleDateFormat;
import java.sql.*;
import java.util.*;

/**
 * Handles all database operations for the SmartShip system.
 * Cleaned and corrected:
 *  - Single createShipment method (with recipientAddress)
 *  - Transactional registerUser (users + role table)
 *  - Transactional invoice creation and payment
 *  - Defensive checks (customer record existence, parameter counts)
 *  - Added wrapper methods expected by GUI/controller
 */
public class DatabaseHelper {
    private static final String URL = "jdbc:mysql://localhost:3307/sspms";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    /**
     * Creates a connection to the database.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * REGISTER NEW USER (transactional)
     * Inserts into users table AND the appropriate role-specific table.
     *
     * @return true if registration succeeded, false if it failed
     */
    public static boolean registerUser(String username, String password, String email,
                                      String phone, String address, String role) {
        String userSql = "INSERT INTO users (username, password, email, phone, address, role) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {

            conn.setAutoCommit(false); // begin transaction

            userStmt.setString(1, username);
            userStmt.setString(2, password);
            userStmt.setString(3, email);
            userStmt.setString(4, phone);
            userStmt.setString(5, address);
            userStmt.setString(6, role);

            int rowsAffected = userStmt.executeUpdate();
            if (rowsAffected == 0) {
                conn.rollback();
                return false;
            }

            ResultSet generatedKeys = userStmt.getGeneratedKeys();
            if (!generatedKeys.next()) {
                conn.rollback();
                return false;
            }

            int userId = generatedKeys.getInt(1);

            // Insert into role-specific table using same connection (transactional)
            boolean roleInserted = insertIntoRoleTable(conn, userId, role);
            if (!roleInserted) {
                conn.rollback();
                return false;
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Helper method to insert into role-specific tables
     * Uses the provided connection (transactional scope)
     */
    private static boolean insertIntoRoleTable(Connection conn, int userId, String role) throws SQLException {
        switch (role) {
            case "Customer":
                return insertCustomer(conn, userId);
            case "Manager":
                return insertManager(conn, userId);
            case "Driver":
                return insertDriver(conn, userId);
            case "Clerk":
                return insertClerk(conn, userId);
            default:
                return false;
        }
    }

    private static boolean insertCustomer(Connection conn, int userId) throws SQLException {
        String sql = "INSERT INTO customers (user_id, membership_tier) VALUES (?, 'Standard')";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    private static boolean insertManager(Connection conn, int userId) throws SQLException {
        String sql = "INSERT INTO managers (user_id, department) VALUES (?, 'Operations')";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    private static boolean insertDriver(Connection conn, int userId) throws SQLException {
        String sql = "INSERT INTO drivers (user_id, status) VALUES (?, 'Active')";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    private static boolean insertClerk(Connection conn, int userId) throws SQLException {
        String sql = "INSERT INTO clerks (user_id, department) VALUES (?, 'Operations')";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * LOGIN
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

            if (rs.next()) {
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
        return null;
    }

    /**
     * GET CUSTOMER INFO
     */
    public static Map<String, String> getCustomerInfo(int userId) {
        String sql = "SELECT u.*, c.customer_id, c.company_name, c.account_balance, c.membership_tier, c.total_shipments " +
                    "FROM users u LEFT JOIN customers c ON u.user_id = c.user_id WHERE u.user_id = ?";

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
                info.put("role", rs.getString("role"));

                // Customer-specific fields (may be null)
                int customerId = rs.getInt("customer_id");
                if (!rs.wasNull() && customerId != 0) {
                    info.put("customerId", String.valueOf(customerId));
                    info.put("companyName", rs.getString("company_name"));
                    info.put("accountBalance", String.format("%.2f", rs.getDouble("account_balance")));
                    info.put("membershipTier", rs.getString("membership_tier"));
                    info.put("totalShipments", String.valueOf(rs.getInt("total_shipments")));
                }
                return info;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Wrapper expected by GUI/controller (keeps naming consistent)
     */
    public static Map<String, String> getCustomerProfile(int userId) {
        return getCustomerInfo(userId);
    }

    /**
     * GET EMPLOYEE INFO
     */
    public static Map<String, String> getEmployeeInfo(int userId, String role) {
        Map<String, String> info = new HashMap<>();

        try (Connection conn = getConnection()) {
            String userSql = "SELECT * FROM users WHERE user_id = ?";
            try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                userStmt.setInt(1, userId);
                ResultSet rs = userStmt.executeQuery();

                if (rs.next()) {
                    info.put("username", rs.getString("username"));
                    info.put("email", rs.getString("email"));
                    info.put("phone", rs.getString("phone"));
                    info.put("address", rs.getString("address"));
                    info.put("role", rs.getString("role"));
                }
            }

            if ("Manager".equals(role)) {
                String managerSql = "SELECT * FROM managers WHERE user_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(managerSql)) {
                    stmt.setInt(1, userId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        info.put("department", rs.getString("department"));
                        info.put("employeeId", rs.getString("employee_id"));
                        if (rs.getDate("hire_date") != null) {
                            info.put("hireDate", rs.getDate("hire_date").toString());
                        }
                    }
                }
            } else if ("Driver".equals(role)) {
                String driverSql = "SELECT * FROM drivers WHERE user_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(driverSql)) {
                    stmt.setInt(1, userId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        info.put("licenseNumber", rs.getString("license_number"));
                        if (rs.getDate("license_expiry") != null) {
                            info.put("licenseExpiry", rs.getDate("license_expiry").toString());
                        }
                        info.put("totalDeliveries", String.valueOf(rs.getInt("total_deliveries")));
                        info.put("rating", String.valueOf(rs.getDouble("rating")));
                        info.put("status", rs.getString("status"));
                    }
                }
            } else if ("Clerk".equals(role)) {
                String clerkSql = "SELECT * FROM clerks WHERE user_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(clerkSql)) {
                    stmt.setInt(1, userId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        info.put("department", rs.getString("department"));
                        info.put("employeeId", rs.getString("employee_id"));
                        info.put("shiftTime", rs.getString("shift_time"));
                        if (rs.getDate("hire_date") != null) {
                            info.put("hireDate", rs.getDate("hire_date").toString());
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return info;
    }

    /**
     * UPDATE CUSTOMER INFO
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
     * CREATE SHIPMENT (single definitive version)
     * Stores recipient address and creates invoice (transactionally)
     *
     * @return trackingNumber on success, null on failure
     */
		    public static String createShipment(int userId, String senderInfo, String recipientInfo,
		            double weight, String dimensions, String type, int zone, String recipientAddress) {
		    		String insertShipmentSql = "INSERT INTO shipments (user_id, sender_info, recipient_info, weight, dimensions, package_type, zone, status, tracking_number, cost, address, created_at) " +
		    									"VALUES (?, ?, ?, ?, ?, ?, ?, 'Pending', ?, ?, ?, NOW())";
		
		    		try (Connection conn = getConnection();
		    				PreparedStatement insertStmt = conn.prepareStatement(insertShipmentSql)) {
		
		    					conn.setAutoCommit(false);
		
								String trackingNumber = "SHIP" + System.currentTimeMillis();
								double cost = calculateShippingCost(weight, zone, type);
								
								insertStmt.setInt(1, userId);
								insertStmt.setString(2, senderInfo);
								insertStmt.setString(3, recipientInfo);
								insertStmt.setDouble(4, weight);
								insertStmt.setString(5, dimensions);
								insertStmt.setString(6, type);
								insertStmt.setInt(7, zone);
								insertStmt.setString(8, trackingNumber);
								insertStmt.setDouble(9, cost);
								insertStmt.setString(10, recipientAddress);
								
								int inserted = insertStmt.executeUpdate();
								if (inserted == 0) {
								conn.rollback();
								return null;
								}
		
				// Create invoice for this shipment (transactional; uses same connection)
					boolean invoiceCreated = createInvoiceForShipment(conn, userId, trackingNumber, cost);
						if (!invoiceCreated) {
							conn.rollback();
							return null;
						}
				
						conn.commit();
						return trackingNumber;
		
		    } catch (SQLException e) {
		    	e.printStackTrace();
		    	return null;
		    	}
		}


    /**
     * Helper method to calculate shipping cost
     */
    private static double calculateShippingCost(double weight, int zone, String type) {
        double baseCost = weight * 5.0;
        double zoneCost = zone * 10.0;
        double typeCost = "Express".equals(type) ? 20.0 : ("Fragile".equals(type) ? 15.0 : 0.0);
        return baseCost + zoneCost + typeCost;
    }

    /**
     * GET CUSTOMER ORDERS
     */
    public static List<Map<String, String>> getCustomerOrders(int userId) {
        String sql = "SELECT shipment_id, tracking_number, recipient_info, weight, package_type, zone, status, cost, payment_status " +
                    "FROM shipments WHERE user_id = ? ORDER BY created_at DESC";
        List<Map<String, String>> orders = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

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
     * Wrapper expected by GUI/controller
     */
    public static List<Map<String, String>> getCustomerShipments(int userId) {
        return getCustomerOrders(userId);
    }

    /**
     * MAKE PAYMENT ON SHIPMENT (legacy)
     * Updates shipments table payment_status and payment_method.
     */
    public static boolean makeShipmentPayment(int shipmentId, String paymentMethod) {
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
     * GET PENDING SHIPMENTS
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
    * ASSIGN SHIPMENT TO VEHICLE (Enhanced with zone validation and capacity status update)
    * - Validates zone matching between shipment and vehicle
    * - Checks available capacity
    * - Updates vehicle load
    * - Automatically sets vehicle status to "In Use" when capacity is full or near full
    */
   public static String assignShipmentToVehicle(int shipmentId, int vehicleId) {
       String getShipmentSql = "SELECT weight, zone, tracking_number FROM shipments WHERE shipment_id = ?";
       String checkVehicleSql = "SELECT capacity, COALESCE(current_weight, 0) as current_weight, " +
                                "zone, status, " +
                                "COALESCE(vehicle_number, CONCAT('VEH-', vehicle_id)) as vehicle_number " +
                                "FROM vehicles WHERE vehicle_id = ?";
       String updateShipmentSql = "UPDATE shipments SET vehicle_id = ?, status = 'Assigned' WHERE shipment_id = ?";
       String updateVehicleSql = "UPDATE vehicles SET " +
                                "current_weight = current_weight + ?, " +
                                "current_item_count = current_item_count + 1, " +
                                "status = ? " +
                                "WHERE vehicle_id = ?";

       try (Connection conn = getConnection()) {
           conn.setAutoCommit(false);
           
           // 1. Get shipment details
           double shipmentWeight = 0;
           int shipmentZone = 0;
           String trackingNumber = "";
           
           try (PreparedStatement stmt = conn.prepareStatement(getShipmentSql)) {
               stmt.setInt(1, shipmentId);
               ResultSet rs = stmt.executeQuery();
               if (rs.next()) {
                   shipmentWeight = rs.getDouble("weight");
                   shipmentZone = rs.getInt("zone");
                   trackingNumber = rs.getString("tracking_number");
               } else {
                   conn.rollback();
                   System.err.println("ERROR: Shipment not found: " + shipmentId);
                   return "ERROR: Shipment not found";
               }
           }
           
           // 2. Check vehicle capacity and zone
           double capacity = 0;
           double currentWeight = 0;
           int vehicleZone = 0;
           String vehicleStatus = "";
           String vehicleNumber = "";
           
           try (PreparedStatement stmt = conn.prepareStatement(checkVehicleSql)) {
               stmt.setInt(1, vehicleId);
               ResultSet rs = stmt.executeQuery();
               if (rs.next()) {
                   capacity = rs.getDouble("capacity");
                   currentWeight = rs.getDouble("current_weight");
                   vehicleZone = rs.getInt("zone");
                   vehicleStatus = rs.getString("status");
                   vehicleNumber = rs.getString("vehicle_number");
               } else {
                   conn.rollback();
                   System.err.println("ERROR: Vehicle not found: " + vehicleId);
                   return "ERROR: Vehicle not found";
               }
           }
           
           // 3. VALIDATE ZONE MATCH
           if (shipmentZone != vehicleZone) {
               conn.rollback();
               String errorMsg = String.format(
                   "ERROR: Zone mismatch - Shipment is in Zone %d but vehicle %s operates in Zone %d",
                   shipmentZone, vehicleNumber, vehicleZone
               );
               System.err.println(errorMsg);
               return errorMsg;
           }
           
           // 4. VALIDATE CAPACITY
           double availableCapacity = capacity - currentWeight;
           if (availableCapacity < shipmentWeight) {
               conn.rollback();
               String errorMsg = String.format(
                   "ERROR: Insufficient capacity - Vehicle %s has %.2f kg available but shipment requires %.2f kg",
                   vehicleNumber, availableCapacity, shipmentWeight
               );
               System.err.println(errorMsg);
               return errorMsg;
           }
           
           // 5. CHECK IF VEHICLE IS AVAILABLE
           if (!"Available".equals(vehicleStatus) && !"In Use".equals(vehicleStatus)) {
               conn.rollback();
               String errorMsg = String.format(
                   "ERROR: Vehicle %s is not available for assignment (Status: %s)",
                   vehicleNumber, vehicleStatus
               );
               System.err.println(errorMsg);
               return errorMsg;
           }
           
           // 6. Calculate new weight and determine new status
           double newWeight = currentWeight + shipmentWeight;
           double utilizationPercent = (newWeight / capacity) * 100;
           
           // Determine new vehicle status based on capacity
           String newStatus;
           if (utilizationPercent >= 95) {
               newStatus = "Full"; // 95% or more = Full
           } else if (utilizationPercent >= 1) {
               newStatus = "In Use"; // Any load = In Use
           } else {
               newStatus = "Available"; // Empty = Available
           }
           
           // 7. Update shipment
           try (PreparedStatement stmt = conn.prepareStatement(updateShipmentSql)) {
               stmt.setInt(1, vehicleId);
               stmt.setInt(2, shipmentId);
               int updated = stmt.executeUpdate();
               if (updated == 0) {
                   conn.rollback();
                   return "ERROR: Failed to update shipment";
               }
           }
           
           // 8. Update vehicle load and status
           try (PreparedStatement stmt = conn.prepareStatement(updateVehicleSql)) {
               stmt.setDouble(1, shipmentWeight);
               stmt.setString(2, newStatus);
               stmt.setInt(3, vehicleId);
               stmt.executeUpdate();
           }
           
           conn.commit();
           
           System.out.println(String.format(
               "SUCCESS: Assigned shipment %s (%.2f kg) to vehicle %s. " +
               "New load: %.2f/%.2f kg (%.1f%%). Status: %s",
               trackingNumber, shipmentWeight, vehicleNumber,
               newWeight, capacity, utilizationPercent, newStatus
           ));
           
           return "SUCCESS";
           
       } catch (SQLException e) {
           System.err.println("Database error during assignment: " + e.getMessage());
           e.printStackTrace();
           return "ERROR: Database error - " + e.getMessage();
       }
   }


    /**
     * GET DRIVER DELIVERIES
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
     * GENERATE DAILY REPORT
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

    /**
     * GET SHIPMENT BY TRACKING NUMBER
     */
    public static Map<String, String> getShipmentByTrackingNumber(String trackingNumber) {
        String sql = "SELECT shipment_id, tracking_number, recipient_info, weight, package_type, zone, status, cost, payment_status, address " +
                    "FROM shipments WHERE tracking_number = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, trackingNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, String> shipment = new HashMap<>();
                shipment.put("shipmentId", String.valueOf(rs.getInt("shipment_id")));
                shipment.put("trackingNumber", rs.getString("tracking_number"));
                shipment.put("recipientInfo", rs.getString("recipient_info"));
                shipment.put("weight", String.valueOf(rs.getDouble("weight")));
                shipment.put("packageType", rs.getString("package_type"));
                shipment.put("zone", String.valueOf(rs.getInt("zone")));
                shipment.put("status", rs.getString("status"));
                shipment.put("cost", String.format("%.2f", rs.getDouble("cost")));
                shipment.put("paymentStatus", rs.getString("payment_status"));
                shipment.put("address", rs.getString("address"));
                return shipment;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * GET CUSTOMER INVOICES
     */
    public static List<Map<String, String>> getCustomerInvoices(int userId) {
        String sql = "SELECT i.invoice_id, i.shipment_id, i.subtotal, i.tax, i.discount, i.surcharge, i.total, i.status, i.invoice_date " +
                    "FROM invoices i " +
                    "JOIN shipments s ON i.shipment_id = s.shipment_id " +
                    "WHERE s.user_id = ? " +
                    "ORDER BY i.invoice_date DESC";

        List<Map<String, String>> invoices = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, String> invoice = new HashMap<>();
                invoice.put("invoiceId", String.valueOf(rs.getInt("invoice_id")));
                invoice.put("shipmentId", String.valueOf(rs.getInt("shipment_id")));
                invoice.put("subtotal", String.format("%.2f", rs.getDouble("subtotal")));
                invoice.put("tax", String.format("%.2f", rs.getDouble("tax")));
                invoice.put("discount", String.format("%.2f", rs.getDouble("discount")));
                invoice.put("surcharge", String.format("%.2f", rs.getDouble("surcharge")));
                invoice.put("total", String.format("%.2f", rs.getDouble("total")));
                invoice.put("status", rs.getString("status"));

                Timestamp timestamp = rs.getTimestamp("invoice_date");
                if (timestamp != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    invoice.put("invoiceDate", sdf.format(new java.util.Date(timestamp.getTime())));
                }

                invoices.add(invoice);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }

    /**
     * MAKE INVOICE PAYMENT (transactional)
     */
    public static boolean makeInvoicePayment(int invoiceId, double amount, String paymentMethod) {
        String selectInvoiceSql = "SELECT shipment_id FROM invoices WHERE invoice_id = ?";
        String updateInvoiceSql = "UPDATE invoices SET status = 'Paid' WHERE invoice_id = ?";
        String updateShipmentSql = "UPDATE shipments SET payment_status = 'Paid', payment_method = ? WHERE shipment_id = ?";
        String insertPaymentSql = "INSERT INTO payments (invoice_id, amount, payment_method, payment_date, status) VALUES (?, ?, ?, NOW(), 'Completed')";

        try (Connection conn = getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectInvoiceSql);
             PreparedStatement updateInvoiceStmt = conn.prepareStatement(updateInvoiceSql);
             PreparedStatement updateShipmentStmt = conn.prepareStatement(updateShipmentSql);
             PreparedStatement paymentStmt = conn.prepareStatement(insertPaymentSql)) {

            conn.setAutoCommit(false);

            // 1) find shipment_id related to the invoice
            int shipmentId = 0;
            selectStmt.setInt(1, invoiceId);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    shipmentId = rs.getInt("shipment_id");
                } else {
                    System.err.println("makeInvoicePayment: invoice not found: " + invoiceId);
                    conn.rollback();
                    return false;
                }
            }

            // 2) update invoice status
            updateInvoiceStmt.setInt(1, invoiceId);
            updateInvoiceStmt.executeUpdate();

            // 3) update shipment payment status & method
            updateShipmentStmt.setString(1, paymentMethod);
            updateShipmentStmt.setInt(2, shipmentId);
            updateShipmentStmt.executeUpdate();

            // 4) insert payment record
            paymentStmt.setInt(1, invoiceId);
            paymentStmt.setDouble(2, amount);
            paymentStmt.setString(3, paymentMethod);
            paymentStmt.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                Connection conn = getConnection();
                conn.rollback();
                conn.close();
            } catch (Exception ex) {
                // ignore
            }
            return false;
        }
    }


    /**
     * Helper to create invoice for a shipment (uses provided connection; transactional)
     * Returns true if created successfully.
     */
    private static boolean createInvoiceForShipment(Connection conn, int userId, String trackingNumber, double subtotal) throws SQLException {
        String getShipmentSql = "SELECT shipment_id FROM shipments WHERE tracking_number = ?";
        String getCustomerSql = "SELECT customer_id FROM customers WHERE user_id = ?";
        String createInvoiceSql = "INSERT INTO invoices (customer_id, shipment_id, tracking_number, subtotal, tax, discount, surcharge, total, status, invoice_date) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        // Get shipment ID
        int shipmentId = 0;
        try (PreparedStatement getShipmentStmt = conn.prepareStatement(getShipmentSql)) {
            getShipmentStmt.setString(1, trackingNumber);
            ResultSet shipmentRs = getShipmentStmt.executeQuery();
            if (shipmentRs.next()) {
                shipmentId = shipmentRs.getInt("shipment_id");
            } else {
                System.err.println("createInvoiceForShipment: shipment not found for tracking " + trackingNumber);
                return false;
            }
        }

        // Get customer ID
        int customerId = 0;
        try (PreparedStatement getCustomerStmt = conn.prepareStatement(getCustomerSql)) {
            getCustomerStmt.setInt(1, userId);
            ResultSet customerRs = getCustomerStmt.executeQuery();
            if (customerRs.next()) {
                customerId = customerRs.getInt("customer_id");
            } else {
                System.err.println("createInvoiceForShipment: customer record not found for user " + userId);
                return false;
            }
        }

        // Prepare invoice numbers
        double tax = subtotal * 0.10; // 10% assumed
        double total = subtotal + tax;

        try (PreparedStatement createInvoiceStmt = conn.prepareStatement(createInvoiceSql)) {
            createInvoiceStmt.setInt(1, customerId);
            createInvoiceStmt.setInt(2, shipmentId);
            createInvoiceStmt.setString(3, trackingNumber);   // <-- store same tracking number
            createInvoiceStmt.setDouble(4, subtotal);
            createInvoiceStmt.setDouble(5, tax);
            createInvoiceStmt.setDouble(6, 0.0); // discount
            createInvoiceStmt.setDouble(7, 0.0); // surcharge
            createInvoiceStmt.setDouble(8, total);
            createInvoiceStmt.setString(9, "Unpaid");
            int rows = createInvoiceStmt.executeUpdate();
            return rows > 0;
        }
    }
    
    /**
     * Get complete invoice and shipment details by invoice ID
     * Used for building receipt information
     */
    public static Map<String, String> getInvoiceAndShipmentDetails(int invoiceId) {
        String sql = "SELECT i.invoice_id, i.shipment_id, i.tracking_number, i.total, i.status AS invoice_status, " +
                     "s.tracking_number AS ship_tracking, s.status AS ship_status, s.address, " +
                     "u.username, u.email, u.address AS user_address, u.phone " +
                     "FROM invoices i " +
                     "JOIN shipments s ON i.shipment_id = s.shipment_id " +
                     "JOIN users u ON s.user_id = u.user_id " +
                     "WHERE i.invoice_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, invoiceId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, String> details = new HashMap<>();
                details.put("invoiceId", String.valueOf(rs.getInt("invoice_id")));
                details.put("shipmentId", String.valueOf(rs.getInt("shipment_id")));
                details.put("trackingNumber", rs.getString("ship_tracking"));
                details.put("customerName", rs.getString("username"));
                details.put("email", rs.getString("email"));
                details.put("address", rs.getString("user_address"));
                details.put("phone", rs.getString("phone"));
                details.put("status", rs.getString("ship_status"));
                details.put("total", String.format("%.2f", rs.getDouble("total")));
                
                return details;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching invoice and shipment details: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
		/**
		 * GET AVAILABLE VEHICLES (Updated to exclude Full vehicles and enforce zone matching)
		 */
		public static List<Map<String, String>> getAvailableVehicles(int zone, double requiredCapacity) {
		    String sql = "SELECT v.vehicle_id, " +
		                "COALESCE(v.vehicle_number, CONCAT('VEH-', v.vehicle_id)) as vehicle_number, " +
		                "v.vehicle_type as type, " +
		                "v.capacity, " +
		                "COALESCE(v.current_weight, 0) as current_load, " +
		                "(v.capacity - COALESCE(v.current_weight, 0)) as available_capacity, " +
		                "COALESCE(v.zone, 1) as zone, " +
		                "v.status, " +
		                "COALESCE(u.username, 'Unassigned') as driver_name " +
		                "FROM vehicles v " +
		                "LEFT JOIN drivers d ON v.driver_id = d.user_id " +
		                "LEFT JOIN users u ON d.user_id = u.user_id " +
		                "WHERE v.zone = ? " +  // MUST match zone exactly
		                "AND v.status IN ('Available', 'In Use') " +  // Exclude 'Full' vehicles
		                "AND (v.capacity - COALESCE(v.current_weight, 0)) >= ? " +
		                "ORDER BY available_capacity DESC";
		    
		    List<Map<String, String>> vehicles = new ArrayList<>();
		    
		    try (Connection conn = getConnection();
		         PreparedStatement stmt = conn.prepareStatement(sql)) {
		        
		        stmt.setInt(1, zone);
		        stmt.setDouble(2, requiredCapacity);
		        ResultSet rs = stmt.executeQuery();
		        
		        while (rs.next()) {
		            Map<String, String> vehicle = new HashMap<>();
		            vehicle.put("vehicleId", String.valueOf(rs.getInt("vehicle_id")));
		            vehicle.put("vehicleNumber", rs.getString("vehicle_number"));
		            vehicle.put("type", rs.getString("type"));
		            vehicle.put("capacity", String.format("%.2f", rs.getDouble("capacity")));
		            vehicle.put("currentLoad", String.format("%.2f", rs.getDouble("current_load")));
		            vehicle.put("availableCapacity", String.format("%.2f", rs.getDouble("available_capacity")));
		            vehicle.put("zone", String.valueOf(rs.getInt("zone")));
		            vehicle.put("status", rs.getString("status"));
		            vehicle.put("driverName", rs.getString("driver_name"));
		            vehicles.add(vehicle);
		        }
		    } catch (SQLException e) {
		        e.printStackTrace();
		    }
		    
		    return vehicles;
		}

    /**
     * GET VEHICLE DETAILS
     */
    public static Map<String, String> getVehicleDetails(int vehicleId) {
        String sql = "SELECT v.vehicle_id, " +
                    "COALESCE(v.vehicle_number, CONCAT('VEH-', v.vehicle_id)) as vehicle_number, " +
                    "v.vehicle_type, v.license_plate, v.capacity, " +
                    "COALESCE(v.current_weight, 0) as current_weight, " +
                    "v.current_item_count, v.status, " +
                    "COALESCE(v.zone, 1) as zone, " +
                    "u.username as driver_name, u.phone as driver_phone, " +
                    "d.license_number, d.rating " +
                    "FROM vehicles v " +
                    "LEFT JOIN drivers d ON v.driver_id = d.user_id " +
                    "LEFT JOIN users u ON d.user_id = u.user_id " +
                    "WHERE v.vehicle_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, vehicleId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Map<String, String> vehicle = new HashMap<>();
                vehicle.put("vehicleId", String.valueOf(rs.getInt("vehicle_id")));
                vehicle.put("vehicleNumber", rs.getString("vehicle_number"));
                vehicle.put("vehicleType", rs.getString("vehicle_type"));
                vehicle.put("licensePlate", rs.getString("license_plate"));
                vehicle.put("capacity", String.format("%.2f", rs.getDouble("capacity")));
                vehicle.put("currentWeight", String.format("%.2f", rs.getDouble("current_weight")));
                vehicle.put("itemCount", String.valueOf(rs.getInt("current_item_count")));
                vehicle.put("status", rs.getString("status"));
                vehicle.put("zone", String.valueOf(rs.getInt("zone")));
                vehicle.put("driverName", rs.getString("driver_name") != null ? 
                           rs.getString("driver_name") : "Unassigned");
                vehicle.put("driverPhone", rs.getString("driver_phone") != null ? 
                           rs.getString("driver_phone") : "N/A");
                vehicle.put("driverLicense", rs.getString("license_number") != null ? 
                           rs.getString("license_number") : "N/A");
                vehicle.put("driverRating", rs.getDouble("rating") > 0 ? 
                           String.format("%.2f", rs.getDouble("rating")) : "N/A");
                return vehicle;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
 // Add these methods to DatabaseHelper.java

    /**
     * GET ALL VEHICLES
     * Returns complete list of all vehicles with driver information and current load
     */
    public static List<Map<String, String>> getAllVehicles() {
        String sql = "SELECT " +
                    "v.vehicle_id, " +
                    "COALESCE(v.vehicle_number, CONCAT('VEH-', v.vehicle_id)) as vehicle_number, " +
                    "v.vehicle_type as type, " +
                    "v.license_plate, " +
                    "v.capacity, " +
                    "COALESCE(v.current_weight, 0) as current_load, " +
                    "COALESCE(v.current_item_count, 0) as item_count, " +
                    "COALESCE(v.zone, 1) as zone, " +
                    "v.status, " +
                    "COALESCE(u.username, 'Unassigned') as driver_name, " +
                    "u.phone as driver_phone " +
                    "FROM vehicles v " +
                    "LEFT JOIN drivers d ON v.driver_id = d.user_id " +
                    "LEFT JOIN users u ON d.user_id = u.user_id " +
                    "ORDER BY v.vehicle_id";
        
        List<Map<String, String>> vehicles = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, String> vehicle = new HashMap<>();
                vehicle.put("vehicleId", String.valueOf(rs.getInt("vehicle_id")));
                vehicle.put("vehicleNumber", rs.getString("vehicle_number"));
                vehicle.put("type", rs.getString("type"));
                vehicle.put("licensePlate", rs.getString("license_plate"));
                vehicle.put("capacity", String.format("%.2f", rs.getDouble("capacity")));
                vehicle.put("currentLoad", String.format("%.2f", rs.getDouble("current_load")));
                vehicle.put("itemCount", String.valueOf(rs.getInt("item_count")));
                vehicle.put("zone", String.valueOf(rs.getInt("zone")));
                vehicle.put("status", rs.getString("status"));
                vehicle.put("driverName", rs.getString("driver_name"));
                vehicle.put("driverPhone", rs.getString("driver_phone") != null ? 
                           rs.getString("driver_phone") : "N/A");
                vehicles.add(vehicle);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all vehicles: " + e.getMessage());
            e.printStackTrace();
        }
        
        return vehicles;
    }

    /**
     * GET VEHICLE PACKAGES
     * Returns all shipments currently assigned to a specific vehicle
     */
    public static List<Map<String, String>> getVehiclePackages(int vehicleId) {
        String sql = "SELECT " +
                    "s.shipment_id, " +
                    "s.tracking_number, " +
                    "s.recipient_info, " +
                    "s.address, " +
                    "s.weight, " +
                    "s.package_type, " +
                    "s.zone, " +
                    "s.status, " +
                    "s.cost, " +
                    "s.payment_status, " +
                    "s.created_at, " +
                    "u.username as customer_name, " +
                    "u.phone as customer_phone " +
                    "FROM shipments s " +
                    "JOIN users u ON s.user_id = u.user_id " +
                    "WHERE s.vehicle_id = ? " +
                    "AND s.status IN ('Assigned', 'In Transit', 'Out for Delivery') " +
                    "ORDER BY s.created_at DESC";
        
        List<Map<String, String>> packages = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, vehicleId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> pkg = new HashMap<>();
                pkg.put("shipmentId", String.valueOf(rs.getInt("shipment_id")));
                pkg.put("trackingNumber", rs.getString("tracking_number"));
                pkg.put("recipientInfo", rs.getString("recipient_info"));
                pkg.put("address", rs.getString("address") != null ? 
                       rs.getString("address") : "N/A");
                pkg.put("weight", String.format("%.2f", rs.getDouble("weight")));
                pkg.put("packageType", rs.getString("package_type"));
                pkg.put("zone", String.valueOf(rs.getInt("zone")));
                pkg.put("status", rs.getString("status"));
                pkg.put("cost", String.format("%.2f", rs.getDouble("cost")));
                pkg.put("paymentStatus", rs.getString("payment_status"));
                pkg.put("customerName", rs.getString("customer_name"));
                pkg.put("customerPhone", rs.getString("customer_phone"));
                
                Timestamp timestamp = rs.getTimestamp("created_at");
                if (timestamp != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    pkg.put("createdAt", sdf.format(new java.util.Date(timestamp.getTime())));
                }
                
                packages.add(pkg);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting vehicle packages: " + e.getMessage());
            e.printStackTrace();
        }
        
        return packages;
    }

    /**
     * GET VEHICLE STATISTICS
     * Returns summary statistics for a vehicle (useful for dashboard)
     */
    public static Map<String, String> getVehicleStatistics(int vehicleId) {
        String sql = "SELECT " +
                    "COUNT(s.shipment_id) as total_shipments, " +
                    "SUM(s.weight) as total_weight, " +
                    "SUM(s.cost) as total_revenue, " +
                    "COUNT(CASE WHEN s.status = 'Delivered' THEN 1 END) as delivered_count, " +
                    "COUNT(CASE WHEN s.status = 'In Transit' THEN 1 END) as in_transit_count " +
                    "FROM shipments s " +
                    "WHERE s.vehicle_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, vehicleId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Map<String, String> stats = new HashMap<>();
                stats.put("totalShipments", String.valueOf(rs.getInt("total_shipments")));
                stats.put("totalWeight", String.format("%.2f", rs.getDouble("total_weight")));
                stats.put("totalRevenue", String.format("%.2f", rs.getDouble("total_revenue")));
                stats.put("deliveredCount", String.valueOf(rs.getInt("delivered_count")));
                stats.put("inTransitCount", String.valueOf(rs.getInt("in_transit_count")));
                return stats;
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting vehicle statistics: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }/**
     * UNASSIGN SHIPMENT FROM VEHICLE
     * Removes shipment from vehicle and updates vehicle capacity/status
     * 
     */
    public static boolean unassignShipmentFromVehicle(int shipmentId) {
        String getShipmentSql = "SELECT weight, vehicle_id FROM shipments WHERE shipment_id = ?";
        String updateShipmentSql = "UPDATE shipments SET vehicle_id = NULL, status = 'Pending' WHERE shipment_id = ?";
        String getVehicleSql = "SELECT capacity, current_weight FROM vehicles WHERE vehicle_id = ?";
        String updateVehicleSql = "UPDATE vehicles SET " +
                                 "current_weight = current_weight - ?, " +
                                 "current_item_count = current_item_count - 1, " +
                                 "status = ? " +
                                 "WHERE vehicle_id = ?";
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            // Get shipment info
            double weight = 0;
            Integer vehicleId = null;
            
            try (PreparedStatement stmt = conn.prepareStatement(getShipmentSql)) {
                stmt.setInt(1, shipmentId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    weight = rs.getDouble("weight");
                    vehicleId = rs.getInt("vehicle_id");
                    if (rs.wasNull()) {
                        vehicleId = null;
                    }
                }
            }
            
            if (vehicleId == null) {
                conn.rollback();
                System.err.println("Shipment not assigned to any vehicle");
                return false;
            }
            
            // Update shipment
            try (PreparedStatement stmt = conn.prepareStatement(updateShipmentSql)) {
                stmt.setInt(1, shipmentId);
                stmt.executeUpdate();
            }
            
            // Get current vehicle state
            double capacity = 0;
            double currentWeight = 0;
            
            try (PreparedStatement stmt = conn.prepareStatement(getVehicleSql)) {
                stmt.setInt(1, vehicleId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    capacity = rs.getDouble("capacity");
                    currentWeight = rs.getDouble("current_weight");
                }
            }
            
            // Calculate new status
            double newWeight = currentWeight - weight;
            double utilizationPercent = (newWeight / capacity) * 100;
            
            String newStatus;
            if (utilizationPercent >= 95) {
                newStatus = "Full";
            } else if (utilizationPercent >= 1) {
                newStatus = "In Use";
            } else {
                newStatus = "Available";
            }
            
            // Update vehicle
            try (PreparedStatement stmt = conn.prepareStatement(updateVehicleSql)) {
                stmt.setDouble(1, weight);
                stmt.setString(2, newStatus);
                stmt.setInt(3, vehicleId);
                stmt.executeUpdate();
            }
            
            conn.commit();
            System.out.println("Unassigned shipment " + shipmentId + " from vehicle " + vehicleId);
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * GET SHIPMENT STATISTICS BY DATE RANGE
     * Returns shipment counts and totals for a given date range
     */
    public static Map<String, Object> getShipmentStatsByDateRange(java.sql.Date startDate, java.sql.Date endDate) {
        String sql = "SELECT " +
                    "COUNT(*) as total_shipments, " +
                    "SUM(weight) as total_weight, " +
                    "SUM(cost) as total_revenue, " +
                    "AVG(weight) as avg_weight, " +
                    "COUNT(CASE WHEN status = 'Delivered' THEN 1 END) as delivered_count, " +
                    "COUNT(CASE WHEN status = 'In Transit' THEN 1 END) as in_transit_count, " +
                    "COUNT(CASE WHEN status = 'Pending' THEN 1 END) as pending_count, " +
                    "COUNT(CASE WHEN status = 'Assigned' THEN 1 END) as assigned_count, " +
                    "COUNT(CASE WHEN package_type = 'Express' THEN 1 END) as express_count, " +
                    "COUNT(CASE WHEN package_type = 'Standard' THEN 1 END) as standard_count, " +
                    "COUNT(CASE WHEN package_type = 'Fragile' THEN 1 END) as fragile_count " +
                    "FROM shipments " +
                    "WHERE DATE(created_at) BETWEEN ? AND ?";
        
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                stats.put("totalShipments", rs.getInt("total_shipments"));
                stats.put("totalWeight", rs.getDouble("total_weight"));
                stats.put("totalRevenue", rs.getDouble("total_revenue"));
                stats.put("avgWeight", rs.getDouble("avg_weight"));
                stats.put("deliveredCount", rs.getInt("delivered_count"));
                stats.put("inTransitCount", rs.getInt("in_transit_count"));
                stats.put("pendingCount", rs.getInt("pending_count"));
                stats.put("assignedCount", rs.getInt("assigned_count"));
                stats.put("expressCount", rs.getInt("express_count"));
                stats.put("standardCount", rs.getInt("standard_count"));
                stats.put("fragileCount", rs.getInt("fragile_count"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting shipment stats: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }

    /**
     * GET DAILY SHIPMENT COUNTS
     * Returns shipment counts for each day in the date range
     */
    public static List<Map<String, Object>> getDailyShipmentCounts(java.sql.Date startDate, java.sql.Date endDate) {
        String sql = "SELECT " +
                    "DATE(created_at) as ship_date, " +
                    "COUNT(*) as count, " +
                    "SUM(cost) as revenue " +
                    "FROM shipments " +
                    "WHERE DATE(created_at) BETWEEN ? AND ? " +
                    "GROUP BY DATE(created_at) " +
                    "ORDER BY ship_date";
        
        List<Map<String, Object>> dailyCounts = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> day = new HashMap<>();
                day.put("date", rs.getDate("ship_date"));
                day.put("count", rs.getInt("count"));
                day.put("revenue", rs.getDouble("revenue"));
                dailyCounts.add(day);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting daily counts: " + e.getMessage());
            e.printStackTrace();
        }
        
        return dailyCounts;
    }

    /**
     * GET DELIVERY PERFORMANCE STATISTICS
     * Analyzes on-time vs delayed deliveries
     */
    public static Map<String, Object> getDeliveryPerformanceStats(java.sql.Date startDate, java.sql.Date endDate) {
        String sql = "SELECT " +
                    "COUNT(CASE WHEN status = 'Delivered' THEN 1 END) as delivered, " +
                    "COUNT(CASE WHEN status = 'Delivered' AND actual_delivery_date <= expected_delivery_date THEN 1 END) as on_time, " +
                    "COUNT(CASE WHEN status = 'Delivered' AND actual_delivery_date > expected_delivery_date THEN 1 END) as delayed, " +
                    "COUNT(CASE WHEN status IN ('In Transit', 'Assigned', 'Out for Delivery') THEN 1 END) as in_progress, " +
                    "COUNT(CASE WHEN status = 'Pending' THEN 1 END) as pending, " +
                    "AVG(DATEDIFF(actual_delivery_date, created_at)) as avg_delivery_days " +
                    "FROM shipments " +
                    "WHERE DATE(created_at) BETWEEN ? AND ?";
        
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int delivered = rs.getInt("delivered");
                int onTime = rs.getInt("on_time");
                int delayed = rs.getInt("delayed");
                
                stats.put("delivered", delivered);
                stats.put("onTime", onTime);
                stats.put("delayed", delayed);
                stats.put("inProgress", rs.getInt("in_progress"));
                stats.put("pending", rs.getInt("pending"));
                stats.put("avgDeliveryDays", rs.getDouble("avg_delivery_days"));
                
                // Calculate percentage
                if (delivered > 0) {
                    stats.put("onTimePercent", (onTime * 100.0) / delivered);
                    stats.put("delayedPercent", (delayed * 100.0) / delivered);
                } else {
                    stats.put("onTimePercent", 0.0);
                    stats.put("delayedPercent", 0.0);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting delivery performance: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }

    /**
     * GET REVENUE STATISTICS
     * Returns revenue breakdown by various categories
     */
    public static Map<String, Object> getRevenueStats(java.sql.Date startDate, java.sql.Date endDate) {
        String sql = "SELECT " +
                    "SUM(cost) as total_revenue, " +
                    "SUM(CASE WHEN payment_status = 'Paid' THEN cost ELSE 0 END) as paid_revenue, " +
                    "SUM(CASE WHEN payment_status = 'Unpaid' THEN cost ELSE 0 END) as unpaid_revenue, " +
                    "SUM(CASE WHEN package_type = 'Express' THEN cost ELSE 0 END) as express_revenue, " +
                    "SUM(CASE WHEN package_type = 'Standard' THEN cost ELSE 0 END) as standard_revenue, " +
                    "SUM(CASE WHEN package_type = 'Fragile' THEN cost ELSE 0 END) as fragile_revenue, " +
                    "SUM(CASE WHEN zone = 1 THEN cost ELSE 0 END) as zone1_revenue, " +
                    "SUM(CASE WHEN zone = 2 THEN cost ELSE 0 END) as zone2_revenue, " +
                    "AVG(cost) as avg_shipment_cost, " +
                    "COUNT(*) as total_shipments " +
                    "FROM shipments " +
                    "WHERE DATE(created_at) BETWEEN ? AND ?";
        
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                stats.put("totalRevenue", rs.getDouble("total_revenue"));
                stats.put("paidRevenue", rs.getDouble("paid_revenue"));
                stats.put("unpaidRevenue", rs.getDouble("unpaid_revenue"));
                stats.put("expressRevenue", rs.getDouble("express_revenue"));
                stats.put("standardRevenue", rs.getDouble("standard_revenue"));
                stats.put("fragileRevenue", rs.getDouble("fragile_revenue"));
                stats.put("zone1Revenue", rs.getDouble("zone1_revenue"));
                stats.put("zone2Revenue", rs.getDouble("zone2_revenue"));
                stats.put("avgShipmentCost", rs.getDouble("avg_shipment_cost"));
                stats.put("totalShipments", rs.getInt("total_shipments"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting revenue stats: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }

    /**
     * GET VEHICLE UTILIZATION STATISTICS
     * Returns capacity utilization for all vehicles
     */
    public static List<Map<String, Object>> getVehicleUtilizationStats() {
        String sql = "SELECT " +
                    "v.vehicle_id, " +
                    "COALESCE(v.vehicle_number, CONCAT('VEH-', v.vehicle_id)) as vehicle_number, " +
                    "v.vehicle_type, " +
                    "v.capacity, " +
                    "COALESCE(v.current_weight, 0) as current_weight, " +
                    "COALESCE(v.current_item_count, 0) as item_count, " +
                    "v.status, " +
                    "v.zone, " +
                    "COALESCE(u.username, 'Unassigned') as driver_name, " +
                    "ROUND((COALESCE(v.current_weight, 0) / v.capacity) * 100, 2) as utilization_percent, " +
                    "COUNT(s.shipment_id) as total_shipments_ever, " +
                    "SUM(CASE WHEN s.status = 'Delivered' THEN 1 ELSE 0 END) as delivered_shipments " +
                    "FROM vehicles v " +
                    "LEFT JOIN drivers d ON v.driver_id = d.user_id " +
                    "LEFT JOIN users u ON d.user_id = u.user_id " +
                    "LEFT JOIN shipments s ON v.vehicle_id = s.vehicle_id " +
                    "GROUP BY v.vehicle_id " +
                    "ORDER BY utilization_percent DESC";
        
        List<Map<String, Object>> vehicleStats = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> vehicle = new HashMap<>();
                vehicle.put("vehicleId", rs.getInt("vehicle_id"));
                vehicle.put("vehicleNumber", rs.getString("vehicle_number"));
                vehicle.put("vehicleType", rs.getString("vehicle_type"));
                vehicle.put("capacity", rs.getDouble("capacity"));
                vehicle.put("currentWeight", rs.getDouble("current_weight"));
                vehicle.put("itemCount", rs.getInt("item_count"));
                vehicle.put("status", rs.getString("status"));
                vehicle.put("zone", rs.getInt("zone"));
                vehicle.put("driverName", rs.getString("driver_name"));
                vehicle.put("utilizationPercent", rs.getDouble("utilization_percent"));
                vehicle.put("totalShipmentsEver", rs.getInt("total_shipments_ever"));
                vehicle.put("deliveredShipments", rs.getInt("delivered_shipments"));
                vehicleStats.add(vehicle);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting vehicle utilization: " + e.getMessage());
            e.printStackTrace();
        }
        
        return vehicleStats;
    }

    /**
     * GET DRIVER PERFORMANCE STATISTICS
     * Returns delivery performance by driver
     */
    public static List<Map<String, Object>> getDriverPerformanceStats(java.sql.Date startDate, java.sql.Date endDate) {
        String sql = "SELECT " +
                    "u.user_id, " +
                    "u.username as driver_name, " +
                    "COUNT(s.shipment_id) as total_deliveries, " +
                    "COUNT(CASE WHEN s.status = 'Delivered' THEN 1 END) as completed_deliveries, " +
                    "COUNT(CASE WHEN s.status = 'Delivered' AND s.actual_delivery_date <= s.expected_delivery_date THEN 1 END) as on_time_deliveries, " +
                    "AVG(CASE WHEN s.status = 'Delivered' THEN DATEDIFF(s.actual_delivery_date, s.created_at) END) as avg_delivery_days, " +
                    "d.rating, " +
                    "d.total_deliveries as career_total " +
                    "FROM drivers d " +
                    "JOIN users u ON d.user_id = u.user_id " +
                    "LEFT JOIN vehicles v ON d.user_id = v.driver_id " +
                    "LEFT JOIN shipments s ON v.vehicle_id = s.vehicle_id " +
                    "    AND DATE(s.created_at) BETWEEN ? AND ? " +
                    "WHERE d.status = 'Active' " +
                    "GROUP BY u.user_id " +
                    "ORDER BY completed_deliveries DESC";
        
        List<Map<String, Object>> driverStats = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> driver = new HashMap<>();
                driver.put("userId", rs.getInt("user_id"));
                driver.put("driverName", rs.getString("driver_name"));
                driver.put("totalDeliveries", rs.getInt("total_deliveries"));
                driver.put("completedDeliveries", rs.getInt("completed_deliveries"));
                driver.put("onTimeDeliveries", rs.getInt("on_time_deliveries"));
                driver.put("avgDeliveryDays", rs.getDouble("avg_delivery_days"));
                driver.put("rating", rs.getDouble("rating"));
                driver.put("careerTotal", rs.getInt("career_total"));
                
                int completed = rs.getInt("completed_deliveries");
                int onTime = rs.getInt("on_time_deliveries");
                if (completed > 0) {
                    driver.put("onTimePercent", (onTime * 100.0) / completed);
                } else {
                    driver.put("onTimePercent", 0.0);
                }
                
                driverStats.add(driver);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting driver performance: " + e.getMessage());
            e.printStackTrace();
        }
        
        return driverStats;
    }

    /**
     * GET TOP CUSTOMERS BY REVENUE
     * Returns customers with highest revenue in date range
     */
    public static List<Map<String, Object>> getTopCustomersByRevenue(java.sql.Date startDate, java.sql.Date endDate, int limit) {
        String sql = "SELECT " +
                    "u.user_id, " +
                    "u.username, " +
                    "c.company_name, " +
                    "COUNT(s.shipment_id) as shipment_count, " +
                    "SUM(s.cost) as total_revenue, " +
                    "AVG(s.cost) as avg_shipment_cost " +
                    "FROM users u " +
                    "JOIN customers c ON u.user_id = c.user_id " +
                    "JOIN shipments s ON u.user_id = s.user_id " +
                    "WHERE DATE(s.created_at) BETWEEN ? AND ? " +
                    "GROUP BY u.user_id " +
                    "ORDER BY total_revenue DESC " +
                    "LIMIT ?";
        
        List<Map<String, Object>> customers = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            stmt.setInt(3, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> customer = new HashMap<>();
                customer.put("userId", rs.getInt("user_id"));
                customer.put("username", rs.getString("username"));
                customer.put("companyName", rs.getString("company_name"));
                customer.put("shipmentCount", rs.getInt("shipment_count"));
                customer.put("totalRevenue", rs.getDouble("total_revenue"));
                customer.put("avgShipmentCost", rs.getDouble("avg_shipment_cost"));
                customers.add(customer);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting top customers: " + e.getMessage());
            e.printStackTrace();
        }
        
        return customers;
    }
    public static List<Map<String, String>> getAllOrders() {
        String sql = "SELECT shipment_id, tracking_number, recipient_info, weight, " +
                     "package_type, zone, status, cost, payment_status " +
                     "FROM shipments ORDER BY created_at DESC";

        List<Map<String, String>> orders = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

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
    * This method gets all the available drivers that exists within the database
    * Returns drivers with status 'Active'
    */
   public static List<Map<String, String>> getAvailableDrivers() {
       String sql = "SELECT d.driver_id, d.user_id, d.license_number, d.license_expiry, " +
                    "d.vehicle_id, d.total_deliveries, d.rating, d.status, " +
                    "u.username " +
                    "FROM drivers d " +
                    "JOIN users u ON d.user_id = u.user_id " +
                    "WHERE d.status = 'Active' " +
                    "ORDER BY u.username";
       
       List<Map<String, String>> drivers = new ArrayList<>();
       try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {
           
           while (rs.next()) {
               Map<String, String> driver = new HashMap<>();
               driver.put("driverId", String.valueOf(rs.getInt("driver_id")));
               driver.put("userId", String.valueOf(rs.getInt("user_id")));
               driver.put("username", rs.getString("username"));
               driver.put("licenseNumber", rs.getString("license_number"));
               driver.put("totalDeliveries", String.valueOf(rs.getInt("total_deliveries")));
               driver.put("rating", String.format("%.1f", rs.getDouble("rating")));
               driver.put("status", rs.getString("status"));
               drivers.add(driver);
           }
       } catch (SQLException e) {
           e.printStackTrace();
       }
       return drivers;
   }

   /**
    * Method that gets all the available vehicles within the database so the clerk can assign shipment to a particular readily available vehicle
    * Returns vehicles with status indicating availability
    */
   public static List<Map<String, String>> getAvailableVehicles1() {
       String sql = "SELECT vehicle_id, driver_id, vehicle_type, license_plate, " +
                    "capacity, status, current_weight, current_item_count, " +
                    "last_maintenance_date, next_maintenance_date " +
                    "FROM vehicles " +
                    "WHERE status IN ('Available', 'Active') " +
                    "ORDER BY vehicle_type, license_plate";
       
       List<Map<String, String>> vehicles = new ArrayList<>();
       try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {
           
           while (rs.next()) {
               Map<String, String> vehicle = new HashMap<>();
               vehicle.put("vehicleId", String.valueOf(rs.getInt("vehicle_id")));
               vehicle.put("vehicleType", rs.getString("vehicle_type"));
               vehicle.put("licensePlate", rs.getString("license_plate"));
               vehicle.put("capacity", String.format("%.2f", rs.getDouble("capacity")));
               vehicle.put("currentWeight", String.format("%.2f", rs.getDouble("current_weight")));
               vehicle.put("currentItemCount", String.valueOf(rs.getInt("current_item_count")));
               vehicle.put("status", rs.getString("status"));
               vehicles.add(vehicle);
           }
       } catch (SQLException e) {
           e.printStackTrace();
       }
       return vehicles;
   }



   
   /**
    * Returns shipments with customer username included
    */
   public static List<Map<String, String>> getAllOrdersWithCustomerNames() {
       String sql = "SELECT s.shipment_id, s.tracking_number, s.recipient_info, s.weight, " +
                    "s.package_type, s.zone, s.status, s.cost, s.payment_status, s.payment_method, " +
                    "s.sender_info, s.created_at, u.username as customer_name " +
                    "FROM shipments s " +
                    "JOIN users u ON s.user_id = u.user_id " +
                    "ORDER BY s.created_at DESC";

       List<Map<String, String>> orders = new ArrayList<>();

       try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

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
               
               String paymentMethod = rs.getString("payment_method");
               order.put("paymentMethod", paymentMethod != null ? paymentMethod : "N/A");
               
               order.put("senderInfo", rs.getString("sender_info"));
               order.put("customerName", rs.getString("customer_name"));
               
               Timestamp timestamp = rs.getTimestamp("created_at");
               if (timestamp != null) {
                   SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                   order.put("date", sdf.format(new java.util.Date(timestamp.getTime())));
               }
               
               orders.add(order);
           }

       } catch (SQLException e) {
           System.err.println("Error in getAllOrdersWithCustomerNames: " + e.getMessage());
           e.printStackTrace();
       }

       return orders;
   }
   public static boolean updateShipment(String trackingNumber, String newStatus, String newPaymentStatus) {
       String sql = "UPDATE shipments SET status = ?, payment_status = ? WHERE tracking_number = ?";

       try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

           stmt.setString(1, newStatus);
           stmt.setString(2, newPaymentStatus);  
           stmt.setString(3, trackingNumber);

           return stmt.executeUpdate() > 0;

       } catch (Exception e) {
           e.printStackTrace();
           return false;
       }
   }
///---------------------------------------------NEW ASSIGNMENTS---------------------------///
// Replace these methods in DatabaseHelper.java

/**
 * Returns current driver, vehicle, and route assignments for shipments
 * Fixed to match actual database schema - driver comes from vehicles table
 */
public static Map<String, Map<String, String>> getShipmentAssignments() {
    String sql = "SELECT s.tracking_number, " +
                "v.vehicle_id, v.license_plate, v.vehicle_type, " +
                "v.driver_id, u.username as driver_name, " +
                "sa.status, sa.assigned_date " +
                "FROM shipments s " +
                "LEFT JOIN shipment_assignments sa ON s.shipment_id = sa.shipment_id " +
                "LEFT JOIN vehicles v ON sa.vehicle_id = v.vehicle_id " +
                "LEFT JOIN drivers d ON v.driver_id = d.user_id " +
                "LEFT JOIN users u ON d.user_id = u.user_id " +
                "WHERE sa.assignment_id IS NOT NULL";
    
    Map<String, Map<String, String>> assignments = new HashMap<>();
    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
        
        while (rs.next()) {
            String trackingNumber = rs.getString("tracking_number");
            Map<String, String> assignment = new HashMap<>();
            
            // Get vehicle info
            int vehicleId = rs.getInt("vehicle_id");
            if (!rs.wasNull()) {
                assignment.put("vehicleId", String.valueOf(vehicleId));
                assignment.put("licensePlate", rs.getString("license_plate"));
                assignment.put("vehicleType", rs.getString("vehicle_type"));
            }
            
            // Get driver info (from vehicle)
            int driverId = rs.getInt("driver_id");
            if (!rs.wasNull()) {
                assignment.put("driverId", String.valueOf(driverId));
                String driverName = rs.getString("driver_name");
                if (driverName != null) {
                    assignment.put("driverName", driverName);
                }
            }
            
            // Get assignment status
            String status = rs.getString("status");
            if (status != null) {
                assignment.put("status", status);
            }
            
            java.sql.Timestamp assignedDate = rs.getTimestamp("assigned_date");
            if (assignedDate != null) {
                assignment.put("assignedDate", assignedDate.toString());
            }
            
            assignments.put(trackingNumber, assignment);
        }
    } catch (SQLException e) {
        System.err.println("Error in getShipmentAssignments: " + e.getMessage());
        e.printStackTrace();
    }
    return assignments;
}

/**
 * This method assigns a shipment to a vehicle and optionally a driver
 * Driver is assigned to the vehicle, not directly to the shipment
 */
public static boolean assignShipment(String trackingNumber, Integer driverId, 
                                     Integer vehicleId, String route) {
    // Get shipment_id from tracking number
    String getShipmentIdSql = "SELECT shipment_id FROM shipments WHERE tracking_number = ?";
    
    // Check if assignment already exists
    String checkAssignmentSql = "SELECT assignment_id FROM shipment_assignments WHERE shipment_id = ?";
    
    // Insert new assignment
    String insertAssignmentSql = 
        "INSERT INTO shipment_assignments (shipment_id, vehicle_id, assigned_date, route_order, status) " +
        "VALUES (?, ?, NOW(), 1, 'Assigned')";
    
    // Update existing assignment
    String updateAssignmentSql = 
        "UPDATE shipment_assignments SET " +
        "vehicle_id = ?, " +
        "assigned_date = NOW() " +
        "WHERE shipment_id = ?";
    
    // Update vehicle's driver
    String updateVehicleDriverSql = "UPDATE vehicles SET driver_id = ? WHERE vehicle_id = ?";
    
    // Update driver delivery count (only for new assignments)
    String updateDriverSql = "UPDATE drivers SET total_deliveries = total_deliveries + 1 WHERE user_id = ?";
    
    try (Connection conn = getConnection()) {
        conn.setAutoCommit(false);
        
        // Get shipment_id
        int shipmentId = 0;
        try (PreparedStatement stmt = conn.prepareStatement(getShipmentIdSql)) {
            stmt.setString(1, trackingNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                shipmentId = rs.getInt("shipment_id");
            } else {
                System.err.println("Shipment not found: " + trackingNumber);
                conn.rollback();
                return false;
            }
        }
        
        // Only proceed if vehicleId is provided
        if (vehicleId != null) {
            // Check if assignment exists
            boolean assignmentExists = false;
            try (PreparedStatement stmt = conn.prepareStatement(checkAssignmentSql)) {
                stmt.setInt(1, shipmentId);
                ResultSet rs = stmt.executeQuery();
                assignmentExists = rs.next();
            }
            
            // Insert or update assignment
            if (assignmentExists) {
                // Update existing
                try (PreparedStatement stmt = conn.prepareStatement(updateAssignmentSql)) {
                    stmt.setInt(1, vehicleId);
                    stmt.setInt(2, shipmentId);
                    stmt.executeUpdate();
                }
            } else {
                // Insert new
                try (PreparedStatement stmt = conn.prepareStatement(insertAssignmentSql)) {
                    stmt.setInt(1, shipmentId);
                    stmt.setInt(2, vehicleId);
                    stmt.executeUpdate();
                }
            }
            
            // If driver is provided, assign driver to vehicle
            if (driverId != null) {
                try (PreparedStatement stmt = conn.prepareStatement(updateVehicleDriverSql)) {
                    stmt.setInt(1, driverId);
                    stmt.setInt(2, vehicleId);
                    stmt.executeUpdate();
                }
                
                // Update driver delivery count (only if new assignment)
                if (!assignmentExists) {
                    try (PreparedStatement stmt = conn.prepareStatement(updateDriverSql)) {
                        stmt.setInt(1, driverId);
                        stmt.executeUpdate();
                    }
                }
            }
        } else if (driverId != null) {
            // If only driver is provided (no vehicle), we can't do anything
            // because drivers are assigned to vehicles, not directly to shipments
            System.out.println("Cannot assign driver without vehicle");
        }
        
        conn.commit();
        System.out.println("Successfully assigned shipment " + trackingNumber);
        return true;
        
    } catch (SQLException e) {
        System.err.println("Error in assignShipment: " + e.getMessage());
        e.printStackTrace();
        try {
            Connection conn = getConnection();
            conn.rollback();
        } catch (Exception ex) {
            // ignore
        }
        return false;
    }
}

/**
 * Returns all shipments assigned to vehicles driven by a specific driver
 */
public static List<Map<String, String>> getAssignmentsByDriver(int driverId) {
    String sql = "SELECT s.tracking_number, s.recipient_info, s.address, s.status, " +
                "v.license_plate, v.vehicle_type, sa.assigned_date " +
                "FROM vehicles v " +
                "JOIN shipment_assignments sa ON v.vehicle_id = sa.vehicle_id " +
                "JOIN shipments s ON sa.shipment_id = s.shipment_id " +
                "JOIN drivers d ON v.driver_id = d.user_id " +
                "WHERE d.user_id = ? " +
                "ORDER BY sa.assigned_date DESC";
    
    List<Map<String, String>> assignments = new ArrayList<>();
    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, driverId);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            Map<String, String> assignment = new HashMap<>();
            assignment.put("trackingNumber", rs.getString("tracking_number"));
            assignment.put("recipientInfo", rs.getString("recipient_info"));
            assignment.put("address", rs.getString("address"));
            assignment.put("status", rs.getString("status"));
            assignment.put("licensePlate", rs.getString("license_plate"));
            assignment.put("vehicleType", rs.getString("vehicle_type"));
            
            java.sql.Timestamp assignedDate = rs.getTimestamp("assigned_date");
            if (assignedDate != null) {
                assignment.put("assignedDate", assignedDate.toString());
            }
            
            assignments.add(assignment);
        }
    } catch (SQLException e) {
        System.err.println("Error in getAssignmentsByDriver: " + e.getMessage());
        e.printStackTrace();
    }
    return assignments;
}

/**
 * Returns all shipments assigned to a specific vehicle
 */
public static List<Map<String, String>> getAssignmentsByVehicle(int vehicleId) {
    String sql = "SELECT s.tracking_number, s.recipient_info, s.weight, s.status, sa.assigned_date " +
                "FROM shipment_assignments sa " +
                "JOIN shipments s ON sa.shipment_id = s.shipment_id " +
                "WHERE sa.vehicle_id = ? " +
                "ORDER BY sa.assigned_date DESC";
    
    List<Map<String, String>> assignments = new ArrayList<>();
    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, vehicleId);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            Map<String, String> assignment = new HashMap<>();
            assignment.put("trackingNumber", rs.getString("tracking_number"));
            assignment.put("recipientInfo", rs.getString("recipient_info"));
            assignment.put("weight", String.valueOf(rs.getDouble("weight")));
            assignment.put("status", rs.getString("status"));
            
            java.sql.Timestamp assignedDate = rs.getTimestamp("assigned_date");
            if (assignedDate != null) {
                assignment.put("assignedDate", assignedDate.toString());
            }
            
            assignments.add(assignment);
        }
    } catch (SQLException e) {
        System.err.println("Error in getAssignmentsByVehicle: " + e.getMessage());
        e.printStackTrace();
    }
    return assignments;
}

/**
 * Removes an assignment by deleting it from shipment_assignments
 */
public static boolean removeAssignment(String trackingNumber, String assignmentType) {
    String getShipmentIdSql = "SELECT shipment_id FROM shipments WHERE tracking_number = ?";
    String deleteAssignmentSql = "DELETE FROM shipment_assignments WHERE shipment_id = ?";
    
    try (Connection conn = getConnection()) {
        conn.setAutoCommit(false);
        
        // Get shipment_id
        int shipmentId = 0;
        try (PreparedStatement stmt = conn.prepareStatement(getShipmentIdSql)) {
            stmt.setString(1, trackingNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                shipmentId = rs.getInt("shipment_id");
            } else {
                conn.rollback();
                return false;
            }
        }
        
        // Delete assignment
        try (PreparedStatement stmt = conn.prepareStatement(deleteAssignmentSql)) {
            stmt.setInt(1, shipmentId);
            int deleted = stmt.executeUpdate();
            
            if (deleted > 0) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        }
    } catch (SQLException e) {
        System.err.println("Error in removeAssignment: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}
/**************************************Fleet Management ************************************/


/**
 * GET FLEET OVERVIEW - Complete fleet status for Manager
 * Returns all vehicles with current load, driver info, and assigned shipments count
 */
public static List<Map<String, String>> getFleetOverview() {
    String sql = "SELECT " +
                "v.vehicle_id, " +
                "COALESCE(v.vehicle_number, CONCAT('VEH-', v.vehicle_id)) as vehicle_number, " +
                "v.vehicle_type, " +
                "v.license_plate, " +
                "v.capacity, " +
                "COALESCE(v.current_weight, 0) as current_weight, " +
                "COALESCE(v.current_item_count, 0) as item_count, " +
                "COALESCE(v.zone, 1) as zone, " +
                "v.status, " +
                "COALESCE(u.username, 'Unassigned') as driver_name, " +
                "u.phone as driver_phone, " +
                "d.total_deliveries, " +
                "d.rating, " +
                "COUNT(sa.assignment_id) as assigned_shipments, " +
                "ROUND((COALESCE(v.current_weight, 0) / v.capacity) * 100, 1) as utilization " +
                "FROM vehicles v " +
                "LEFT JOIN drivers d ON v.driver_id = d.user_id " +
                "LEFT JOIN users u ON d.user_id = u.user_id " +
                "LEFT JOIN shipment_assignments sa ON v.vehicle_id = sa.vehicle_id " +
                "GROUP BY v.vehicle_id " +
                "ORDER BY v.vehicle_id";
    
    List<Map<String, String>> fleet = new ArrayList<>();
    
    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
        
        while (rs.next()) {
            Map<String, String> vehicle = new HashMap<>();
            vehicle.put("vehicleId", String.valueOf(rs.getInt("vehicle_id")));
            vehicle.put("vehicleNumber", rs.getString("vehicle_number"));
            vehicle.put("vehicleType", rs.getString("vehicle_type"));
            vehicle.put("licensePlate", rs.getString("license_plate"));
            vehicle.put("capacity", String.format("%.2f", rs.getDouble("capacity")));
            vehicle.put("currentWeight", String.format("%.2f", rs.getDouble("current_weight")));
            vehicle.put("itemCount", String.valueOf(rs.getInt("item_count")));
            vehicle.put("zone", String.valueOf(rs.getInt("zone")));
            vehicle.put("status", rs.getString("status"));
            vehicle.put("driverName", rs.getString("driver_name"));
            vehicle.put("driverPhone", rs.getString("driver_phone") != null ? rs.getString("driver_phone") : "N/A");
            vehicle.put("totalDeliveries", String.valueOf(rs.getInt("total_deliveries")));
            vehicle.put("rating", String.format("%.1f", rs.getDouble("rating")));
            vehicle.put("assignedShipments", String.valueOf(rs.getInt("assigned_shipments")));
            vehicle.put("utilization", String.format("%.1f", rs.getDouble("utilization")));
            fleet.add(vehicle);
        }
        
    } catch (SQLException e) {
        System.err.println("Error getting fleet overview: " + e.getMessage());
        e.printStackTrace();
    }
    
    return fleet;
}

/**
 * GET VEHICLE SHIPMENTS - All shipments assigned to a specific vehicle
 * For Manager to see what's loaded on each vehicle
 */
public static List<Map<String, String>> getVehicleShipments(int vehicleId) {
    String sql = "SELECT " +
                "s.shipment_id, " +
                "s.tracking_number, " +
                "s.recipient_info, " +
                "s.address, " +
                "s.weight, " +
                "s.package_type, " +
                "s.zone, " +
                "s.status, " +
                "s.cost, " +
                "s.created_at, " +
                "u.username as customer_name, " +
                "sa.assigned_date, " +
                "sa.status as assignment_status " +
                "FROM shipment_assignments sa " +
                "JOIN shipments s ON sa.shipment_id = s.shipment_id " +
                "JOIN users u ON s.user_id = u.user_id " +
                "WHERE sa.vehicle_id = ? " +
                "ORDER BY sa.assigned_date DESC";
    
    List<Map<String, String>> shipments = new ArrayList<>();
    
    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setInt(1, vehicleId);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            Map<String, String> shipment = new HashMap<>();
            shipment.put("shipmentId", String.valueOf(rs.getInt("shipment_id")));
            shipment.put("trackingNumber", rs.getString("tracking_number"));
            shipment.put("recipientInfo", rs.getString("recipient_info"));
            shipment.put("address", rs.getString("address") != null ? rs.getString("address") : "N/A");
            shipment.put("weight", String.format("%.2f", rs.getDouble("weight")));
            shipment.put("packageType", rs.getString("package_type"));
            shipment.put("zone", String.valueOf(rs.getInt("zone")));
            shipment.put("status", rs.getString("status"));
            shipment.put("cost", String.format("%.2f", rs.getDouble("cost")));
            shipment.put("customerName", rs.getString("customer_name"));
            shipment.put("assignmentStatus", rs.getString("assignment_status"));
            
            Timestamp created = rs.getTimestamp("created_at");
            if (created != null) {
                shipment.put("createdAt", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(created));
            }
            
            Timestamp assigned = rs.getTimestamp("assigned_date");
            if (assigned != null) {
                shipment.put("assignedDate", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(assigned));
            }
            
            shipments.add(shipment);
        }
        
    } catch (SQLException e) {
        System.err.println("Error getting vehicle shipments: " + e.getMessage());
        e.printStackTrace();
    }
    
    return shipments;
}

/**
 * GET DRIVER FLEET INFO - All vehicles and shipments for a specific driver
 * Shows what vehicles a driver is assigned to and their current loads
 */
public static Map<String, Object> getDriverFleetInfo(int driverId) {
    Map<String, Object> info = new HashMap<>();
    
    // Get driver's vehicles
    String vehicleSql = "SELECT " +
                       "v.vehicle_id, " +
                       "COALESCE(v.vehicle_number, CONCAT('VEH-', v.vehicle_id)) as vehicle_number, " +
                       "v.vehicle_type, " +
                       "v.license_plate, " +
                       "v.capacity, " +
                       "COALESCE(v.current_weight, 0) as current_weight, " +
                       "COALESCE(v.current_item_count, 0) as item_count, " +
                       "v.status " +
                       "FROM vehicles v " +
                       "JOIN drivers d ON v.driver_id = d.user_id " +
                       "WHERE d.user_id = ?";
    
    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(vehicleSql)) {
        
        stmt.setInt(1, driverId);
        ResultSet rs = stmt.executeQuery();
        
        List<Map<String, String>> vehicles = new ArrayList<>();
        while (rs.next()) {
            Map<String, String> vehicle = new HashMap<>();
            vehicle.put("vehicleId", String.valueOf(rs.getInt("vehicle_id")));
            vehicle.put("vehicleNumber", rs.getString("vehicle_number"));
            vehicle.put("vehicleType", rs.getString("vehicle_type"));
            vehicle.put("licensePlate", rs.getString("license_plate"));
            vehicle.put("capacity", String.format("%.2f", rs.getDouble("capacity")));
            vehicle.put("currentWeight", String.format("%.2f", rs.getDouble("current_weight")));
            vehicle.put("itemCount", String.valueOf(rs.getInt("item_count")));
            vehicle.put("status", rs.getString("status"));
            vehicles.add(vehicle);
        }
        info.put("vehicles", vehicles);
        
        // Get assigned shipments
        List<Map<String, String>> shipments = getAssignmentsByDriver(driverId);
        info.put("shipments", shipments);
        
    } catch (SQLException e) {
        System.err.println("Error getting driver fleet info: " + e.getMessage());
        e.printStackTrace();
    }
    
    return info;
}

/**
 * UNASSIGN SHIPMENT FROM VEHICLE - Manager can remove a shipment from a vehicle
 * Updates vehicle capacity and removes assignment
 */
public static boolean unassignShipmentFromVehicle(String trackingNumber) {
    String getShipmentSql = "SELECT s.shipment_id, s.weight, sa.vehicle_id " +
                           "FROM shipments s " +
                           "JOIN shipment_assignments sa ON s.shipment_id = sa.shipment_id " +
                           "WHERE s.tracking_number = ?";
    
    String deleteAssignmentSql = "DELETE FROM shipment_assignments WHERE shipment_id = ?";
    
    String updateVehicleSql = "UPDATE vehicles SET " +
                             "current_weight = current_weight - ?, " +
                             "current_item_count = current_item_count - 1 " +
                             "WHERE vehicle_id = ?";
    
    String updateShipmentSql = "UPDATE shipments SET status = 'Pending', vehicle_id = NULL WHERE shipment_id = ?";
    
    try (Connection conn = getConnection()) {
        conn.setAutoCommit(false);
        
        int shipmentId = 0;
        double weight = 0;
        int vehicleId = 0;
        
        // Get shipment info
        try (PreparedStatement stmt = conn.prepareStatement(getShipmentSql)) {
            stmt.setString(1, trackingNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                shipmentId = rs.getInt("shipment_id");
                weight = rs.getDouble("weight");
                vehicleId = rs.getInt("vehicle_id");
            } else {
                conn.rollback();
                return false;
            }
        }
        
        // Delete assignment
        try (PreparedStatement stmt = conn.prepareStatement(deleteAssignmentSql)) {
            stmt.setInt(1, shipmentId);
            stmt.executeUpdate();
        }
        
        // Update vehicle
        try (PreparedStatement stmt = conn.prepareStatement(updateVehicleSql)) {
            stmt.setDouble(1, weight);
            stmt.setInt(2, vehicleId);
            stmt.executeUpdate();
        }
        
        // Update shipment status
        try (PreparedStatement stmt = conn.prepareStatement(updateShipmentSql)) {
            stmt.setInt(1, shipmentId);
            stmt.executeUpdate();
        }
        
        conn.commit();
        System.out.println("Unassigned shipment " + trackingNumber + " from vehicle " + vehicleId);
        return true;
        
    } catch (SQLException e) {
        System.err.println("Error unassigning shipment: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}

/**
 * GET FLEET STATISTICS - Overall fleet performance metrics
 */
public static Map<String, String> getFleetStatistics() {
    String sql = "SELECT " +
                "COUNT(v.vehicle_id) as total_vehicles, " +
                "COUNT(CASE WHEN v.status = 'Available' THEN 1 END) as available, " +
                "COUNT(CASE WHEN v.status = 'In Use' THEN 1 END) as in_use, " +
                "COUNT(CASE WHEN v.status = 'Full' THEN 1 END) as full, " +
                "COUNT(CASE WHEN v.driver_id IS NULL THEN 1 END) as unassigned_drivers, " +
                "AVG(COALESCE(v.current_weight, 0) / v.capacity * 100) as avg_utilization, " +
                "SUM(COALESCE(v.current_weight, 0)) as total_load, " +
                "SUM(v.capacity) as total_capacity, " +
                "COUNT(DISTINCT sa.shipment_id) as total_assigned_shipments " +
                "FROM vehicles v " +
                "LEFT JOIN shipment_assignments sa ON v.vehicle_id = sa.vehicle_id";
    
    Map<String, String> stats = new HashMap<>();
    
    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
        
        if (rs.next()) {
            stats.put("totalVehicles", String.valueOf(rs.getInt("total_vehicles")));
            stats.put("available", String.valueOf(rs.getInt("available")));
            stats.put("inUse", String.valueOf(rs.getInt("in_use")));
            stats.put("full", String.valueOf(rs.getInt("full")));
            stats.put("unassignedDrivers", String.valueOf(rs.getInt("unassigned_drivers")));
            stats.put("avgUtilization", String.format("%.1f", rs.getDouble("avg_utilization")));
            stats.put("totalLoad", String.format("%.2f", rs.getDouble("total_load")));
            stats.put("totalCapacity", String.format("%.2f", rs.getDouble("total_capacity")));
            stats.put("totalAssignedShipments", String.valueOf(rs.getInt("total_assigned_shipments")));
        }
        
    } catch (SQLException e) {
        System.err.println("Error getting fleet statistics: " + e.getMessage());
        e.printStackTrace();
    }
    
    return stats;
}

    // --- End of class ---
}

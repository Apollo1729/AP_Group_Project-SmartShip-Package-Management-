package network_server;

import database.DatabaseHelper;
import java.io.ObjectInputStream;

/**
 * Command interface that all command classes implement.
 * Each command knows how to execute itself by reading data from the client
 * and returning a result.
 */
interface Command {
    Object execute(ObjectInputStream in) throws Exception;
}

/**
 * This class holds all the different commands our server can handle.
 * Each command is a separate inner class that does one specific task.
 */
public class Commands {
    
    /**
     * REGISTER Command - creates a new user account
     * Reads: username, password, email, phone, address
     * Returns: success/failure message or the new user object
     */
    public static class RegisterCommand implements Command {
        @Override
        public Object execute(ObjectInputStream in) throws Exception {
            // Read all the registration info the client sent
            String username = (String) in.readObject();
            String password = (String) in.readObject();
            String email = (String) in.readObject();
            String phone = (String) in.readObject();
            String address = (String) in.readObject();
            String role = (String) in.readObject();
            
            // Pass it to the database to create the account
            return DatabaseHelper.registerUser(username, password, email, phone, address,role);
        }
    }
    
    /**
     * LOGIN Command - authenticates a user
     * Reads: username, password
     * Returns: user object if successful, null if login fails
     */
    public static class LoginCommand implements Command {
        @Override
        public Object execute(ObjectInputStream in) throws Exception {
            String username = (String) in.readObject();
            String password = (String) in.readObject();
            return DatabaseHelper.login(username, password);
        }
    }
    
    /**
     * GET CUSTOMER INFO Command - retrieves a user's profile
     * Reads: userId
     * Returns: customer information object
     */
    public static class GetCustomerInfoCommand implements Command {
        @Override
        public Object execute(ObjectInputStream in) throws Exception {
            int userId = (int) in.readObject();
            return DatabaseHelper.getCustomerInfo(userId);
        }
    }
    
    /**
     * UPDATE CUSTOMER INFO Command - lets users edit their profile
     * Reads: userId, email, phone, address
     * Returns: success/failure indicator
     */
    public static class UpdateCustomerInfoCommand implements Command {
        @Override
        public Object execute(ObjectInputStream in) throws Exception {
            int userId = (int) in.readObject();
            String email = (String) in.readObject();
            String phone = (String) in.readObject();
            String address = (String) in.readObject();
            return DatabaseHelper.updateCustomerInfo(userId, email, phone, address);
        }
    }
    
    /**
     * CREATE SHIPMENT Command - customer creates a new delivery order
     * Reads: userId, sender info, recipient info, weight, dimensions, type, zone
     * Returns: the new shipment object with tracking number
     */
    public static class CreateShipmentCommand implements Command {
        @Override
        public Object execute(ObjectInputStream in) throws Exception {
            int userId = (int) in.readObject();
            String senderInfo = (String) in.readObject();
            String recipientInfo = (String) in.readObject();
            double weight = (double) in.readObject();
            String dimensions = (String) in.readObject();
            String type = (String) in.readObject();
            int zone = (int) in.readObject();
            return DatabaseHelper.createShipment(userId, senderInfo, recipientInfo, weight, dimensions, type, zone);
        }
    }
    
    /**
     * GET CUSTOMER ORDERS Command - shows all shipments for a customer
     * Reads: userId
     * Returns: list of all shipments this customer has made
     */
    public static class GetCustomerOrdersCommand implements Command {
        @Override
        public Object execute(ObjectInputStream in) throws Exception {
            int userId = (int) in.readObject();
            return DatabaseHelper.getCustomerOrders(userId);
        }
    }
    
    /**
     * MAKE PAYMENT Command - processes payment for a shipment
     * Reads: shipmentId, paymentMethod (e.g. "credit card", "cash")
     * Returns: payment confirmation or receipt
     */
    public static class MakePaymentCommand implements Command {
        @Override
        public Object execute(ObjectInputStream in) throws Exception {
            int shipmentId = (int) in.readObject();
            String paymentMethod = (String) in.readObject();
            return DatabaseHelper.makePayment(shipmentId, paymentMethod);
        }
    }
    
    /**
     * GET PENDING SHIPMENTS Command - for managers to see what needs delivery
     * Reads: nothing
     * Returns: list of all shipments waiting to be assigned to a vehicle
     */
    public static class GetPendingShipmentsCommand implements Command {
        @Override
        public Object execute(ObjectInputStream in) throws Exception {
            return DatabaseHelper.getPendingShipments();
        }
    }
    
    /**
     * ASSIGN SHIPMENT Command - manager assigns a shipment to a delivery vehicle
     * Reads: shipmentId, vehicleId
     * Returns: confirmation that assignment was successful
     */
    public static class AssignShipmentCommand implements Command {
        @Override
        public Object execute(ObjectInputStream in) throws Exception {
            int shipmentId = (int) in.readObject();
            int vehicleId = (int) in.readObject();
            return DatabaseHelper.assignShipmentToVehicle(shipmentId, vehicleId);
        }
    }
    
    /**
     * GET DRIVER DELIVERIES Command - shows driver their assigned deliveries
     * Reads: userId (the driver's id)
     * Returns: list of shipments this driver needs to deliver
     */
    public static class GetDriverDeliveriesCommand implements Command {
        @Override
        public Object execute(ObjectInputStream in) throws Exception {
            int userId = (int) in.readObject();
            return DatabaseHelper.getDriverDeliveries(userId);
        }
    }
    
    /**
     * UPDATE DELIVERY STATUS Command - driver updates where package is
     * Reads: trackingNumber, status (e.g. "out for delivery", "delivered")
     * Returns: confirmation that status was updated
     */
    public static class UpdateDeliveryStatusCommand implements Command {
        @Override
        public Object execute(ObjectInputStream in) throws Exception {
            String trackingNumber = (String) in.readObject();
            String status = (String) in.readObject();
            return DatabaseHelper.updateDeliveryStatus(trackingNumber, status);
        }
    }
    
    /**
     * GENERATE DAILY REPORT Command - creates summary of the day's activity
     * Reads: nothing
     * Returns: report object with stats like total deliveries, revenue, etc.
     */
    public static class GenerateDailyReportCommand implements Command {
        @Override
        public Object execute(ObjectInputStream in) throws Exception {
            return DatabaseHelper.generateDailyReport();
        }
    }
}
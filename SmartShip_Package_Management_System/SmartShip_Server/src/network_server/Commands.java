package network_server;

import database.DatabaseHelper;
import java.io.ObjectInputStream;

// Command interface
interface Command {
    Object execute(ObjectInputStream in) throws Exception;
}

// ALL COMMANDS 
public class Commands {
    
    // REGISTER Command
    public static class RegisterCommand implements Command {
        @Override
        public Object execute(ObjectInputStream in) throws Exception {
            String username = (String) in.readObject();
            String password = (String) in.readObject();
            String email = (String) in.readObject();
            String phone = (String) in.readObject();
            String address = (String) in.readObject();
            return DatabaseHelper.registerUser(username, password, email, phone, address);
        }
    }
    
    // LOGIN Command
    public static class LoginCommand implements Command {
        @Override
        public Object execute(ObjectInputStream in) throws Exception {
            String username = (String) in.readObject();
            String password = (String) in.readObject();
            return DatabaseHelper.login(username, password);
        }
    }
    
    // GET CUSTOMER INFO Command
    public static class GetCustomerInfoCommand implements Command {
        @Override
        public Object execute(ObjectInputStream in) throws Exception {
            int userId = (int) in.readObject();
            return DatabaseHelper.getCustomerInfo(userId);
        }
    }
    
    // UPDATE CUSTOMER INFO Command
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
    
    // CREATE SHIPMENT Command
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
    
    // GET CUSTOMER ORDERS Command
    public static class GetCustomerOrdersCommand implements Command {
        @Override
        public Object execute(ObjectInputStream in) throws Exception {
            int userId = (int) in.readObject();
            return DatabaseHelper.getCustomerOrders(userId);
        }
    }
    
    // MAKE PAYMENT Command
    public static class MakePaymentCommand implements Command {
        @Override
        public Object execute(ObjectInputStream in) throws Exception {
            int shipmentId = (int) in.readObject();
            String paymentMethod = (String) in.readObject();
            return DatabaseHelper.makePayment(shipmentId, paymentMethod);
        }
    }
    
    // GET PENDING SHIPMENTS Command
    public static class GetPendingShipmentsCommand implements Command {
        @Override
        public Object execute(ObjectInputStream in) throws Exception {
            return DatabaseHelper.getPendingShipments();
        }
    }
    
    // ASSIGN SHIPMENT Command
    public static class AssignShipmentCommand implements Command {
        @Override
        public Object execute(ObjectInputStream in) throws Exception {
            int shipmentId = (int) in.readObject();
            int vehicleId = (int) in.readObject();
            return DatabaseHelper.assignShipmentToVehicle(shipmentId, vehicleId);
        }
    }
    
    // GET DRIVER DELIVERIES Command
    public static class GetDriverDeliveriesCommand implements Command {
        @Override
        public Object execute(ObjectInputStream in) throws Exception {
            int userId = (int) in.readObject();
            return DatabaseHelper.getDriverDeliveries(userId);
        }
    }
    
    // UPDATE DELIVERY STATUS Command
    public static class UpdateDeliveryStatusCommand implements Command {
        @Override
        public Object execute(ObjectInputStream in) throws Exception {
            String trackingNumber = (String) in.readObject();
            String status = (String) in.readObject();
            return DatabaseHelper.updateDeliveryStatus(trackingNumber, status);
        }
    }
    
    // GENERATE DAILY REPORT Command
    public static class GenerateDailyReportCommand implements Command {
        @Override
        public Object execute(ObjectInputStream in) throws Exception {
            return DatabaseHelper.generateDailyReport();
        }
    }
}


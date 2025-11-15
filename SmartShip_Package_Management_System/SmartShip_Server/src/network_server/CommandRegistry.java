package network_server;

import network_server.Commands.*;
import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {
    private static Map<String, Command> commands = new HashMap<>();
    
    static {
        // Register all commands
        commands.put("REGISTER", new RegisterCommand());
        commands.put("LOGIN", new LoginCommand());
        commands.put("GET_CUSTOMER_INFO", new GetCustomerInfoCommand());
        commands.put("UPDATE_CUSTOMER_INFO", new UpdateCustomerInfoCommand());
        commands.put("CREATE_SHIPMENT", new CreateShipmentCommand());
        commands.put("GET_CUSTOMER_ORDERS", new GetCustomerOrdersCommand());
        commands.put("MAKE_PAYMENT", new MakePaymentCommand());
        commands.put("GET_PENDING_SHIPMENTS", new GetPendingShipmentsCommand());
        commands.put("ASSIGN_SHIPMENT", new AssignShipmentCommand());
        commands.put("GET_DRIVER_DELIVERIES", new GetDriverDeliveriesCommand());
        commands.put("UPDATE_DELIVERY_STATUS", new UpdateDeliveryStatusCommand());
        commands.put("GENERATE_DAILY_REPORT", new GenerateDailyReportCommand());
    }
    
    public static Command getCommand(String action) {
        return commands.get(action);
    }
}
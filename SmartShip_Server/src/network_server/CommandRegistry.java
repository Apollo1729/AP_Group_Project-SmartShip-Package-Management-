package network_server;
import network_server.Commands.*;
import java.util.HashMap;
import java.util.Map;

/**
 * This class acts as a lookup table for all available commands.
 
 */
public class CommandRegistry {
    // Maps action names (like "LOGIN") to their corresponding command objects
    private static Map<String, Command> commands = new HashMap<>();
    
    // static block runs once when the class is loaded
    // This sets up all our commands at startup
    static {
        // Register all commands by putting them in the map
        // Key = what the client sends, Value = the command object to run
        commands.put("REGISTER", new RegisterCommand());
        commands.put("LOGIN", new LoginCommand());
        commands.put("GET_CUSTOMER_INFO", new GetCustomerInfoCommand());
        commands.put("UPDATE_CUSTOMER_INFO", new UpdateCustomerInfoCommand());
        commands.put("GET_CUSTOMER_ORDERS", new GetCustomerOrdersCommand());
        commands.put("GET_PENDING_SHIPMENTS", new GetPendingShipmentsCommand());
        commands.put("ASSIGN_SHIPMENT", new AssignShipmentCommand());
        commands.put("GET_DRIVER_DELIVERIES", new GetDriverDeliveriesCommand());
        commands.put("UPDATE_DELIVERY_STATUS", new UpdateDeliveryStatusCommand());
        commands.put("GENERATE_DAILY_REPORT", new GenerateDailyReportCommand());
        commands.put("TRACK_PACKAGE", new TrackPackageCommand());
        commands.put("GET_CUSTOMER_INVOICES", new GetCustomerInvoicesCommand());
        commands.put("MAKE_PAYMENT", new MakePaymentCommand());
        commands.put("CREATE_SHIPMENT", new CreateShipmentCommand());
        commands.put("MAKE_PAYMENT_AND_GET_RECEIPT", new MakePaymentAndGetReceiptCommand());
        commands.put("GET_AVAILABLE_VEHICLES", new GetAvailableVehiclesCommand());
        
        commands.put("GET_VEHICLE_PACKAGES", new GetVehiclePackagesCommand());
        commands.put("GET_SHIPMENT_STATS_BY_DATE_RANGE", new GetShipmentStatsByDateRangeCommand());
        commands.put("GET_DAILY_SHIPMENT_COUNTS", new GetDailyShipmentCountsCommand());
        commands.put("GET_DELIVERY_PERFORMANCE_STATS", new GetDeliveryPerformanceStatsCommand());
        commands.put("GET_REVENUE_STATS", new GetRevenueStatsCommand());
        commands.put("GET_VEHICLE_UTILIZATION_STATS", new GetVehicleUtilizationStatsCommand());
        commands.put("GET_DRIVER_PERFORMANCE_STATS", new GetDriverPerformanceStatsCommand());
        commands.put("GET_ALL_ORDERS_WITH_CUSTOMER_NAMES", new GetAllOrdersWithCustomerNamesCommand());
        commands.put("UPDATE_SHIPMENT", new UpdateShipmentCommand()); 
        commands.put("GET_AVAILABLE_VEHICLES", new GetAvailableVehiclesCommand());
        commands.put("ASSIGN_SHIPMENT_FULL", new AssignShipmentFullCommand()); 
        commands.put("GET_SHIPMENT_ASSIGNMENTS", new GetShipmentAssignmentsCommand());
        commands.put("GET_ALL_ORDERS", new GetAllOrdersCommand());
        commands.put("GET_ALL_ORDERS_WITH_CUSTOMER_NAMES", new GetAllOrdersWithCustomerNamesCommand()); 
        commands.put("GET_EMPLOYEE_INFO", new GetEmployeeInfoCommand());
        commands.put("GET_EMPLOYEE_INFO", new Commands.GetEmployeeInfoCommand());
        commands.put("GET_AVAILABLE_VEHICLES", new Commands.GetAvailableVehiclesCommand());
        commands.put("GET_ALL_AVAILABLE_VEHICLES", new Commands.GetAllAvailableVehiclesCommand());      
        commands.put("GET_AVAILABLE_DRIVERS", new Commands.GetAvailableDriversCommand());

        commands.put("GET_FLEET_OVERVIEW", new Commands.GetFleetOverviewCommand());
        commands.put("GET_VEHICLE_SHIPMENTS", new Commands.GetVehicleShipmentsCommand());
        commands.put("GET_DRIVER_FLEET_INFO", new Commands.GetDriverFleetInfoCommand());
        commands.put("UNASSIGN_SHIPMENT_FROM_VEHICLE", new Commands.UnassignShipmentFromVehicleCommand());
        commands.put("GET_FLEET_STATISTICS", new Commands.GetFleetStatisticsCommand());
        
        

    }
    
    /**
     * Looks up a command by its action name.
     
     */
    public static Command getCommand(String action) {
        return commands.get(action);
    }
}
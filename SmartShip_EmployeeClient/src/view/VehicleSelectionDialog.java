package view;

import model.NetworkClient;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Smart dialog for selecting appropriate vehicle for shipment assignment
 * Shows only available vehicles with sufficient capacity
 */
public class VehicleSelectionDialog extends JDialog {
    private JTable vehicleTable;
    private DefaultTableModel tableModel;
    private int selectedVehicleId = -1;
    private boolean assignmentConfirmed = false;
    
    private final Color PRIMARY_COLOR = new Color(163, 67, 53);
    private final Color HEADER_COLOR = new Color(52, 73, 94);
    
    public VehicleSelectionDialog(JFrame parent, int shipmentId, String trackingNumber, 
                                 double weight, int zone) {
        super(parent, "Assign Vehicle - " + trackingNumber, true);
        
        setSize(900, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(15, 15));
        
        // Header panel
        JPanel headerPanel = createHeaderPanel(trackingNumber, weight, zone);
        add(headerPanel, BorderLayout.NORTH);
        
        // Table panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Load available vehicles
        loadAvailableVehicles(zone, weight);
    }
    
    /**
     * Create header showing shipment details
     */
    private JPanel createHeaderPanel(String trackingNumber, double weight, int zone) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel("Select Vehicle for Assignment");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(HEADER_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel shipmentInfoLabel = new JLabel(String.format(
            "📦 Tracking: %s  |  ⚖️ Weight: %.2f kg  |  🗺️ Zone: %d",
            trackingNumber, weight, zone
        ));
        shipmentInfoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        shipmentInfoLabel.setForeground(Color.DARK_GRAY);
        shipmentInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        shipmentInfoLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        panel.add(titleLabel);
        panel.add(shipmentInfoLabel);
        
        return panel;
    }
    
    /**
     * Create table showing available vehicles
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        // Info label
        JLabel infoLabel = new JLabel("Available vehicles with sufficient capacity:");
        infoLabel.setFont(new Font("Arial", Font.BOLD, 12));
        infoLabel.setForeground(HEADER_COLOR);
        panel.add(infoLabel, BorderLayout.NORTH);
        
        // Table
        String[] columns = {
            "Vehicle ID", "Vehicle #", "Type", "Driver", 
            "Capacity (kg)", "Current Load (kg)", "Available (kg)", "Zone", "Status"
        };
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        vehicleTable = new JTable(tableModel);
        vehicleTable.setFont(new Font("Arial", Font.PLAIN, 12));
        vehicleTable.setRowHeight(35);
        vehicleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Header styling
        vehicleTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        vehicleTable.getTableHeader().setBackground(HEADER_COLOR);
        vehicleTable.getTableHeader().setForeground(Color.WHITE);
        vehicleTable.getTableHeader().setReorderingAllowed(false);
        
        // Column widths
        vehicleTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // ID
        vehicleTable.getColumnModel().getColumn(1).setPreferredWidth(90);  // Number
        vehicleTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Type
        vehicleTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Driver
        vehicleTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Capacity
        vehicleTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Current
        vehicleTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Available
        vehicleTable.getColumnModel().getColumn(7).setPreferredWidth(60);  // Zone
        vehicleTable.getColumnModel().getColumn(8).setPreferredWidth(90);  // Status
        
        JScrollPane scrollPane = new JScrollPane(vehicleTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create button panel
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(220, 220, 220)));
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        cancelBtn.setPreferredSize(new Dimension(120, 40));
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> {
            assignmentConfirmed = false;
            dispose();
        });
        
        JButton assignBtn = new JButton("Assign Vehicle");
        assignBtn.setFont(new Font("Arial", Font.BOLD, 14));
        assignBtn.setBackground(PRIMARY_COLOR);
        assignBtn.setForeground(Color.WHITE);
        assignBtn.setPreferredSize(new Dimension(150, 40));
        assignBtn.setFocusPainted(false);
        assignBtn.setBorderPainted(false);
        assignBtn.addActionListener(e -> confirmAssignment());
        
        panel.add(cancelBtn);
        panel.add(assignBtn);
        
        return panel;
    }
    
    /**
     * Load available vehicles from server
     */
    private void loadAvailableVehicles(int zone, double requiredCapacity) {
        SwingWorker<List<Map<String, String>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, String>> doInBackground() throws Exception {
                return NetworkClient.getAvailableVehicles(zone, requiredCapacity);
            }
            
            @Override
            protected void done() {
                try {
                    List<Map<String, String>> vehicles = get();
                    tableModel.setRowCount(0);
                    
                    if (vehicles != null && !vehicles.isEmpty()) {
                        for (Map<String, String> vehicle : vehicles) {
                            tableModel.addRow(new Object[]{
                                vehicle.get("vehicleId"),
                                vehicle.get("vehicleNumber"),
                                vehicle.get("type"),
                                vehicle.get("driverName"),
                                vehicle.get("capacity"),
                                vehicle.get("currentLoad"),
                                vehicle.get("availableCapacity"),
                                vehicle.get("zone"),
                                vehicle.get("status")
                            });
                        }
                    } else {
                        JOptionPane.showMessageDialog(VehicleSelectionDialog.this,
                            "No available vehicles found with sufficient capacity.\n" +
                            "Please check vehicle availability or capacity.",
                            "No Vehicles Available",
                            JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(VehicleSelectionDialog.this,
                        "Error loading vehicles: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    /**
     * Confirm assignment
     */
    private void confirmAssignment() {
        int selectedRow = vehicleTable.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a vehicle from the table",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        selectedVehicleId = Integer.parseInt(
            vehicleTable.getValueAt(selectedRow, 0).toString()
        );
        
        String vehicleNumber = vehicleTable.getValueAt(selectedRow, 1).toString();
        String driverName = vehicleTable.getValueAt(selectedRow, 3).toString();
        
        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("Assign shipment to:\n\nVehicle: %s\nDriver: %s\n\nContinue?",
                vehicleNumber, driverName),
            "Confirm Assignment",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            assignmentConfirmed = true;
            dispose();
        }
    }
    
    /**
     * Get selected vehicle ID
     */
    public int getSelectedVehicleId() {
        return selectedVehicleId;
    }
    
    /**
     * Check if assignment was confirmed
     */
    public boolean isAssignmentConfirmed() {
        return assignmentConfirmed;
    }
}
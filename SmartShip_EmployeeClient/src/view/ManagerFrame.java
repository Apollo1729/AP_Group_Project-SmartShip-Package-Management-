package view;

import model.User;
import model.NetworkClient;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.swing.Timer;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;


/**
 * Manager Dashboard - handles shipment assignment and reporting
 */
public class ManagerFrame extends JFrame {
    private static final long serialVersionUID = 1L;
	private User currentUser;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private Timer autoRefreshTimer;
    private boolean autoRefreshEnabled = false;
    private JPanel currentDashboardStatsPanel;
    private JTextArea currentReportArea;
    
    private JPanel fleetStatsPanel;
    private JTable fleetTable;
    private DefaultTableModel fleetTableModel;
    
    // Color scheme
    private final Color PRIMARY_COLOR = new Color(163, 67, 53);
    private final Color SIDEBAR_COLOR = new Color(52, 73, 94);
    private final Color CONTENT_BG = new Color(249, 249, 249);
    private final Color CARD_BG = Color.WHITE;
    
    private void setupKeyboardShortcuts() {
        // F5 to refresh current view
        KeyStroke f5 = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f5, "refresh");
        contentPanel.getActionMap().put("refresh", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshCurrentView();
            }
        });
        
        // Ctrl+R as alternative refresh
        KeyStroke ctrlR = KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK);
        contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlR, "refresh");
    }

    /**
     * NEW: Refresh Currently Active View
     */
    private void refreshCurrentView() {
        // Find which panel is currently visible
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        
        // Refresh based on current view
        // This is a simple approach - you might want to track current view with a variable
        System.out.println("Quick refresh triggered (F5 or Ctrl+R)");
        
        // For now, just refresh dashboard and reports
        refreshDashboard(null);
        refreshReport(null);
        refreshPendingShipments();
        refreshVehiclesTable();
        
        // Show notification
        JOptionPane.showMessageDialog(this,
            "All views refreshed successfully!",
            "Refresh Complete",
            JOptionPane.INFORMATION_MESSAGE,
            null);
    }
   
    
    public ManagerFrame(User user) {
        this.currentUser = user;
        
        setTitle("SmartShip Manager Portal - " + user.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        ImageIcon icon = new ImageIcon("./assets/iutLogo.png");
        if (icon.getIconWidth() > 0) {
            setIconImage(icon.getImage());
        }
        
        setLayout(new BorderLayout());
        add(createSidebar(), BorderLayout.WEST);
        
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(CONTENT_BG);
        
        // Add different panels
        contentPanel.add(createDashboardPanel(), "DASHBOARD");
        contentPanel.add(createPendingShipmentsPanel(), "PENDING");
        contentPanel.add(createFleetManagementPanel(), "FLEET");  // ADD THIS LINE
        contentPanel.add(createReportsPanel(), "REPORTS");
        contentPanel.add(createProfilePanel(), "PROFILE");
        
        add(contentPanel, BorderLayout.CENTER);
        cardLayout.show(contentPanel, "DASHBOARD");
        
        setVisible(true);
    }

    
    /**
     * Creates the sidebar navigation
     */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        // Logo/Title area
        JLabel titleLabel = new JLabel("MANAGER");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(titleLabel);
        
        JLabel subtitleLabel = new JLabel("Control Panel");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(189, 195, 199));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(subtitleLabel);
        
        sidebar.add(Box.createRigidArea(new Dimension(0, 40)));
        
        // Navigation buttons
        addNavButton(sidebar, "📊 Dashboard", "DASHBOARD");
        addNavButton(sidebar, "📦 Pending Shipments", "PENDING");
        addNavButton(sidebar, "🚚 Fleet Management", "FLEET");  // ADD THIS LINE
        addNavButton(sidebar, "📈 Reports", "REPORTS");
        addNavButton(sidebar, "👤 Profile", "PROFILE");
        
        sidebar.add(Box.createVerticalGlue());
        
        // Logout button
        JButton logoutBtn = createStyledButton("🚪 Logout", PRIMARY_COLOR);
        logoutBtn.setMaximumSize(new Dimension(200, 40));
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.addActionListener(e -> logout());
        sidebar.add(logoutBtn);
        
        return sidebar;
    }
    /**
     * NEW METHOD: Create Fleet Management Panel
     */
    private JPanel createFleetManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(CONTENT_BG);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        // Header with refresh button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CONTENT_BG);
        
        JLabel headerLabel = new JLabel("Fleet Management");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 28));
        headerLabel.setForeground(SIDEBAR_COLOR);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        
        JButton refreshBtn = createStyledButton("🔄 Refresh", PRIMARY_COLOR);
        refreshBtn.addActionListener(e -> refreshFleetData());
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Create split panel: stats on top, fleet table below
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBackground(CONTENT_BG);
        
        // Statistics panel
        JPanel statsPanel = createFleetStatsPanel();
        contentPanel.add(statsPanel, BorderLayout.NORTH);
        
        // Fleet table
        JPanel tablePanel = createFleetTablePanel();
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        // Load initial data
        loadFleetData();
        
        return panel;
    }

    /**
     * Create Fleet Statistics Panel (cards showing metrics)
     */
    private JPanel createFleetStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setBackground(CONTENT_BG);
        
        // Create stat cards (will be populated by loadFleetData)
        statsPanel.add(createStatCard("Total Vehicles", "Loading...", "🚛"));
        statsPanel.add(createStatCard("Available", "Loading...", "✅"));
        statsPanel.add(createStatCard("In Use", "Loading...", "🔄"));
        statsPanel.add(createStatCard("Avg Utilization", "Loading...", "📊"));
        
        return statsPanel;
    }

    /**
     * Create Fleet Table Panel
     */
    private JPanel createFleetTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBackground(CONTENT_BG);
        
        JLabel tableLabel = new JLabel("Fleet Overview:");
        tableLabel.setFont(new Font("Arial", Font.BOLD, 16));
        tableLabel.setForeground(SIDEBAR_COLOR);
        tablePanel.add(tableLabel, BorderLayout.NORTH);
        
        // Create table
        String[] columns = {
            "Vehicle ID", "Vehicle #", "Type", "License Plate", "Driver", 
            "Zone", "Status", "Capacity (kg)", "Current Load (kg)", 
            "Items", "Utilization %", "Assigned Shipments"
        };
        
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable fleetTable = new JTable(model);
        fleetTable.setFont(new Font("Arial", Font.PLAIN, 11));
        fleetTable.setRowHeight(30);
        fleetTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        fleetTable.getTableHeader().setBackground(SIDEBAR_COLOR);
        fleetTable.getTableHeader().setForeground(Color.WHITE);
        fleetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Add double-click listener to view vehicle details
        fleetTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = fleetTable.getSelectedRow();
                    if (row != -1) {
                        int vehicleId = Integer.parseInt(fleetTable.getValueAt(row, 0).toString());
                        showVehicleDetails(vehicleId);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(fleetTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Action panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        actionPanel.setBackground(CONTENT_BG);
        
        JButton viewDetailsBtn = createStyledButton("View Details", SIDEBAR_COLOR);
        viewDetailsBtn.addActionListener(e -> {
            int row = fleetTable.getSelectedRow();
            if (row != -1) {
                int vehicleId = Integer.parseInt(fleetTable.getValueAt(row, 0).toString());
                showVehicleDetails(vehicleId);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please select a vehicle",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        actionPanel.add(viewDetailsBtn);
        tablePanel.add(actionPanel, BorderLayout.SOUTH);
        
        return tablePanel;
    }

    /**
     * Load Fleet Data - THIS IS WHERE YOU USE THE METHODS
     */
    private void loadFleetData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private List<Map<String, String>> fleetData;
            private Map<String, String> statsData;
            
            @Override
            protected Void doInBackground() throws Exception {
                // HERE'S WHERE YOU USE THE METHODS!
                fleetData = NetworkClient.getFleetOverview();
                statsData = NetworkClient.getFleetStatistics();
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    // Update statistics cards
                    updateFleetStats(statsData);
                    
                    // Update fleet table
                    updateFleetTable(fleetData);
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ManagerFrame.this,
                        "Error loading fleet data: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /**
     * Update Fleet Statistics Display
     */
    private void updateFleetStats(Map<String, String> stats) {
        if (stats == null) return;
        
        // Find the stats panel and update it
        Component[] components = contentPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel p = (JPanel) comp;
                Component[] children = p.getComponents();
                for (Component child : children) {
                    if (child instanceof JPanel) {
                        JPanel contentPanel = (JPanel) child;
                        Component[] subChildren = contentPanel.getComponents();
                        for (Component subChild : subChildren) {
                            if (subChild instanceof JPanel) {
                                JPanel statsPanel = (JPanel) subChild;
                                statsPanel.removeAll();
                                
                                statsPanel.add(createStatCard("Total Vehicles", 
                                    stats.getOrDefault("totalVehicles", "0"), "🚛"));
                                statsPanel.add(createStatCard("Available", 
                                    stats.getOrDefault("available", "0"), "✅"));
                                statsPanel.add(createStatCard("In Use", 
                                    stats.getOrDefault("inUse", "0"), "🔄"));
                                statsPanel.add(createStatCard("Avg Utilization", 
                                    stats.getOrDefault("avgUtilization", "0") + "%", "📊"));
                                
                                statsPanel.revalidate();
                                statsPanel.repaint();
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Update Fleet Table
     */
    private void updateFleetTable(List<Map<String, String>> fleet) {
        if (fleet == null) return;
        
        // Find the table and update it
        Component[] components = contentPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                findAndUpdateTable((JPanel) comp, fleet);
            }
        }
    }

    private void findAndUpdateTable(JPanel panel, List<Map<String, String>> fleet) {
        Component[] children = panel.getComponents();
        for (Component child : children) {
            if (child instanceof JScrollPane) {
                JScrollPane sp = (JScrollPane) child;
                if (sp.getViewport().getView() instanceof JTable) {
                    JTable table = (JTable) sp.getViewport().getView();
                    DefaultTableModel model = (DefaultTableModel) table.getModel();
                    model.setRowCount(0);
                    
                    for (Map<String, String> vehicle : fleet) {
                        model.addRow(new Object[]{
                            vehicle.get("vehicleId"),
                            vehicle.get("vehicleNumber"),
                            vehicle.get("vehicleType"),
                            vehicle.get("licensePlate"),
                            vehicle.get("driverName"),
                            vehicle.get("zone"),
                            vehicle.get("status"),
                            vehicle.get("capacity"),
                            vehicle.get("currentWeight"),
                            vehicle.get("itemCount"),
                            vehicle.get("utilization") + "%",
                            vehicle.get("assignedShipments")
                        });
                    }
                    return;
                }
            } else if (child instanceof JPanel) {
                findAndUpdateTable((JPanel) child, fleet);
            }
        }
    }

    /**
     * Refresh Fleet Data
     */
    private void refreshFleetData() {
        loadFleetData();
    }

    /**
     * Show Vehicle Details Dialog - THIS IS WHERE YOU USE getVehicleShipments
     */
    private void showVehicleDetails(int vehicleId) {
        // Create dialog
        JDialog dialog = new JDialog(this, "Vehicle Details - ID: " + vehicleId, true);
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Color.WHITE);
        
        // Header
        JLabel headerLabel = new JLabel("Shipments on Vehicle #" + vehicleId);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(SIDEBAR_COLOR);
        contentPanel.add(headerLabel, BorderLayout.NORTH);
        
        // Table for shipments
        String[] columns = {
            "Tracking #", "Customer", "Recipient", "Address", 
            "Weight (kg)", "Type", "Status", "Cost"
        };
        
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable shipmentsTable = new JTable(model);
        shipmentsTable.setFont(new Font("Arial", Font.PLAIN, 12));
        shipmentsTable.setRowHeight(30);
        shipmentsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(shipmentsTable);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Load shipments for this vehicle - HERE'S WHERE YOU USE getVehicleShipments!
        SwingWorker<List<Map<String, String>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, String>> doInBackground() throws Exception {
                return NetworkClient.getVehicleShipments(vehicleId);
            }
            
            @Override
            protected void done() {
                try {
                    List<Map<String, String>> shipments = get();
                    if (shipments != null && !shipments.isEmpty()) {
                        for (Map<String, String> shipment : shipments) {
                            model.addRow(new Object[]{
                                shipment.get("trackingNumber"),
                                shipment.get("customerName"),
                                shipment.get("recipientInfo"),
                                shipment.get("address"),
                                shipment.get("weight"),
                                shipment.get("packageType"),
                                shipment.get("status"),
                                "$" + shipment.get("cost")
                            });
                        }
                    } else {
                        JLabel noData = new JLabel("No shipments assigned to this vehicle", SwingConstants.CENTER);
                        noData.setFont(new Font("Arial", Font.ITALIC, 14));
                        contentPanel.add(noData, BorderLayout.CENTER);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog,
                        "Error loading vehicle shipments: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
        
        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        JButton closeBtn = createStyledButton("Close", SIDEBAR_COLOR);
        closeBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeBtn);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(contentPanel);
        dialog.setVisible(true);
    }
    /**
     * Helper to add navigation buttons
     */
    private void addNavButton(JPanel sidebar, String text, String panelName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.PLAIN, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(SIDEBAR_COLOR);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(250, 50));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addActionListener(e -> cardLayout.show(contentPanel, panelName));
        
        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(41, 58, 75));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(SIDEBAR_COLOR);
            }
        });
        
        sidebar.add(btn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
    }
    
    /**
     * Dashboard Panel - Overview statistics
     */
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(CONTENT_BG);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        // Header with refresh controls
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CONTENT_BG);
        
        JLabel headerLabel = new JLabel("Manager Dashboard");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 28));
        headerLabel.setForeground(SIDEBAR_COLOR);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        
        // Refresh controls panel
        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        refreshPanel.setBackground(CONTENT_BG);
        
        // Auto-refresh toggle
        JCheckBox autoRefreshCheckbox = new JCheckBox("Auto-refresh (30s)");
        autoRefreshCheckbox.setFont(new Font("Arial", Font.PLAIN, 12));
        autoRefreshCheckbox.setBackground(CONTENT_BG);
        autoRefreshCheckbox.setFocusPainted(false);
        autoRefreshCheckbox.addActionListener(e -> {
            autoRefreshEnabled = autoRefreshCheckbox.isSelected();
            if (autoRefreshEnabled) {
                startAutoRefresh();
            } else {
                stopAutoRefresh();
            }
        });
        
        // Manual refresh button
        JButton refreshBtn = createStyledButton("🔄 Refresh Now", PRIMARY_COLOR);
        refreshBtn.setPreferredSize(new Dimension(150, 35));
        refreshBtn.addActionListener(e -> {
            refreshBtn.setEnabled(false);
            refreshBtn.setText("Refreshing...");
            refreshDashboard(() -> {
                refreshBtn.setEnabled(true);
                refreshBtn.setText("🔄 Refresh Now");
            });
        });
        
        refreshPanel.add(autoRefreshCheckbox);
        refreshPanel.add(refreshBtn);
        headerPanel.add(refreshPanel, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Stats cards panel (will be updated on refresh)
        currentDashboardStatsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        currentDashboardStatsPanel.setBackground(CONTENT_BG);
        
        // Initial loading state
        currentDashboardStatsPanel.add(createStatCard("Pending Shipments", "Loading...", "📦"));
        currentDashboardStatsPanel.add(createStatCard("Today's Deliveries", "Loading...", "🚚"));
        currentDashboardStatsPanel.add(createStatCard("Total Revenue", "Loading...", "💰"));
        
        panel.add(currentDashboardStatsPanel, BorderLayout.CENTER);
        
        // Quick actions
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        actionsPanel.setBackground(CONTENT_BG);
        
        JButton viewPendingBtn = createStyledButton("View Pending Shipments", PRIMARY_COLOR);
        viewPendingBtn.addActionListener(e -> cardLayout.show(contentPanel, "PENDING"));
        
        JButton generateReportBtn = createStyledButton("Generate Report", SIDEBAR_COLOR);
        generateReportBtn.addActionListener(e -> cardLayout.show(contentPanel, "REPORTS"));
        
            
        JButton viewFleetBtn = createStyledButton("View Fleet", SIDEBAR_COLOR);
        viewFleetBtn.addActionListener(e -> cardLayout.show(contentPanel, "FLEET"));

       
        
        actionsPanel.add(viewPendingBtn);
        actionsPanel.add(generateReportBtn);
        actionsPanel.add(viewFleetBtn);
        
        panel.add(actionsPanel, BorderLayout.SOUTH);
        
        // Load initial data
        refreshDashboard(null);
        
        return panel;
    }
    /**
     * Load dashboard statistics
     */
    /**
     * Load dashboard statistics - ENHANCED VERSION
     */
    private void loadDashboardStats(JPanel statsPanel) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private int pendingCount = 0;
            private String dailyReport = "";
            private Map<String, String> fleetStats = null;
            
            @Override
            protected Void doInBackground() throws Exception {
                List<Map<String, String>> pending = NetworkClient.getPendingShipments();
                pendingCount = (pending != null) ? pending.size() : 0;
                dailyReport = NetworkClient.generateDailyReport();
                fleetStats = NetworkClient.getFleetStatistics(); // ADD THIS
                return null;
            }
            
            @Override
            protected void done() {
                statsPanel.removeAll();
                statsPanel.add(createStatCard("Pending Shipments", String.valueOf(pendingCount), "📦"));
                
                // Parse daily report for stats
                String deliveries = "N/A";
                String revenue = "N/A";
                
                if (dailyReport != null && !dailyReport.isEmpty()) {
                    String[] lines = dailyReport.split("\n");
                    for (String line : lines) {
                        if (line.contains("Total Shipments:")) {
                            deliveries = line.split(":")[1].trim();
                        } else if (line.contains("Total Revenue:")) {
                            revenue = line.split(":")[1].trim();
                        }
                    }
                }
                
                statsPanel.add(createStatCard("Today's Deliveries", deliveries, "🚚"));
                statsPanel.add(createStatCard("Total Revenue", revenue, "💰"));
                
                // ADD FLEET STAT 
                if (fleetStats != null) {
                    String available = fleetStats.getOrDefault("available", "N/A");
                    // add this as a 4th stat 
                    statsPanel.add(createStatCard("Available Vehicles", available, "🚛"));
                }
                
                statsPanel.revalidate();
                statsPanel.repaint();
            }
        };
        worker.execute();
    }
    
    /**
     * Create a stat card
     */
    private JPanel createStatCard(String title, String value, String icon) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 36));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        titleLabel.setForeground(Color.GRAY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(SIDEBAR_COLOR);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(iconLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(valueLabel);
        
        return card;
    }
    
    /**
     * Pending Shipments Panel - View and assign shipments
     */
    private JPanel createPendingShipmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(CONTENT_BG);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CONTENT_BG);
        
        JLabel headerLabel = new JLabel("Pending Shipments");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 28));
        headerLabel.setForeground(SIDEBAR_COLOR);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        
        JButton refreshBtn = createStyledButton("🔄 Refresh", PRIMARY_COLOR);
        refreshBtn.addActionListener(e -> refreshPendingShipments());
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"Shipment ID", "Tracking #", "Customer", "Weight (kg)", "Zone", "Type"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(SIDEBAR_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Action panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        actionPanel.setBackground(CONTENT_BG);
        
        JButton assignBtn = createStyledButton("Assign to Vehicle", PRIMARY_COLOR);
        assignBtn.addActionListener(e -> assignSelectedShipment(table));
        
        actionPanel.add(assignBtn);
        panel.add(actionPanel, BorderLayout.SOUTH);
        
        // Load data
        loadPendingShipments(model);
        
        return panel;
    }
    
    /**
     * Load pending shipments into table
     */
    private void loadPendingShipments(DefaultTableModel model) {
        SwingWorker<List<Map<String, String>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, String>> doInBackground() throws Exception {
                return NetworkClient.getPendingShipments();
            }
            
            @Override
            protected void done() {
                try {
                    List<Map<String, String>> shipments = get();
                    model.setRowCount(0);
                    
                    if (shipments != null && !shipments.isEmpty()) {
                        for (Map<String, String> shipment : shipments) {
                            model.addRow(new Object[]{
                                shipment.get("shipmentId"),
                                shipment.get("trackingNumber"),
                                shipment.get("customer"),
                                shipment.get("weight"),
                                shipment.get("zone"),
                                shipment.get("packageType")
                            });
                        }
                    } else {
                        JOptionPane.showMessageDialog(ManagerFrame.this,
                            "No pending shipments found",
                            "Information",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ManagerFrame.this,
                        "Error loading shipments: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    /**
     * Refresh pending shipments
     */
    private void refreshPendingShipments() {
        Component[] components = contentPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel p = (JPanel) comp;
                Component[] children = p.getComponents();
                for (Component child : children) {
                    if (child instanceof JScrollPane) {
                        JScrollPane sp = (JScrollPane) child;
                        JTable table = (JTable) sp.getViewport().getView();
                        loadPendingShipments((DefaultTableModel) table.getModel());
                        return;
                    }
                }
            }
        }
    }
    
    /**
     * Assign selected shipment to vehicle (ENHANCED VERSION)
     */
    private void assignSelectedShipment(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a shipment to assign",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get shipment details from table
        int shipmentId = Integer.parseInt(table.getValueAt(selectedRow, 0).toString());
        String trackingNumber = table.getValueAt(selectedRow, 1).toString();
        double weight = Double.parseDouble(table.getValueAt(selectedRow, 3).toString());
        int zone = Integer.parseInt(table.getValueAt(selectedRow, 4).toString());
        
        // Open smart vehicle selection dialog
        VehicleSelectionDialog dialog = new VehicleSelectionDialog(
            this, shipmentId, trackingNumber, weight, zone
        );
        dialog.setVisible(true);
        
        // Check if user confirmed assignment
        if (dialog.isAssignmentConfirmed()) {
            int vehicleId = dialog.getSelectedVehicleId();
            
            // Show progress dialog
            JDialog progressDialog = new JDialog(this, "Assigning...", true);
            JLabel progressLabel = new JLabel("Assigning shipment to vehicle...", SwingConstants.CENTER);
            progressLabel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
            progressDialog.add(progressLabel);
            progressDialog.setSize(300, 120);
            progressDialog.setLocationRelativeTo(this);
            
            // Perform assignment in background
            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override
                protected String doInBackground() throws Exception {
                    return NetworkClient.assignShipment(shipmentId, vehicleId);
                }
                
                @Override
                protected void done() {
                    progressDialog.dispose();
                    try {
                        String result = get();
                        
                        if ("SUCCESS".equals(result)) {
                            JOptionPane.showMessageDialog(ManagerFrame.this,
                                "✓ Shipment assigned successfully!\n\n" +
                                "Tracking: " + trackingNumber + "\n" +
                                "Vehicle ID: " + vehicleId,
                                "Assignment Successful",
                                JOptionPane.INFORMATION_MESSAGE);
                            refreshPendingShipments();
                        } else {
                            // Show detailed error message
                            JOptionPane.showMessageDialog(ManagerFrame.this,
                                result.replace("ERROR: ", ""),
                                "Assignment Failed",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(ManagerFrame.this,
                            "Error during assignment: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            
            worker.execute();
            progressDialog.setVisible(true);
        }
    }
    
    /**
     * Reports Panel - with graphs and PDF export
     */
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(CONTENT_BG);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CONTENT_BG);
        
        JLabel headerLabel = new JLabel("Reports & Analytics");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 28));
        headerLabel.setForeground(SIDEBAR_COLOR);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Center panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(CONTENT_BG);
        
        // Date Range Card with spinners
        JPanel dateRangeCard = createReportCard("📅 Select Report Period");
        dateRangeCard.setLayout(new GridLayout(3, 2, 15, 15));
        
        // Date spinners (simpler than JCalendar)
        JLabel startLabel = new JLabel("Days from today:");
        startLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        JSpinner daysBackSpinner = new JSpinner(new SpinnerNumberModel(7, 1, 365, 1));
        daysBackSpinner.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Quick selection buttons
        JPanel quickPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        quickPanel.setOpaque(false);
        
        JButton todayBtn = createQuickDateButton("Today", 0, daysBackSpinner);
        JButton last7Btn = createQuickDateButton("Last 7 Days", 7, daysBackSpinner);
        JButton last30Btn = createQuickDateButton("Last 30 Days", 30, daysBackSpinner);
        JButton thisMonthBtn = createQuickDateButton("This Month", getCurrentDayOfMonth(), daysBackSpinner);
        
        quickPanel.add(todayBtn);
        quickPanel.add(last7Btn);
        quickPanel.add(last30Btn);
        quickPanel.add(thisMonthBtn);
        
        dateRangeCard.add(startLabel);
        dateRangeCard.add(daysBackSpinner);
        dateRangeCard.add(new JLabel("")); // Spacer
        dateRangeCard.add(quickPanel);
        
        centerPanel.add(dateRangeCard);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Report Types Card
        JPanel reportTypesCard = createReportCard("📊 Available Reports");
        reportTypesCard.setLayout(new GridLayout(2, 2, 15, 15));
        
        JButton comprehensiveBtn = createReportTypeButton(
            "📈 Comprehensive",
            "All metrics and charts"
        );
        
        JButton shipmentBtn = createReportTypeButton(
            "📦 Shipment Analysis",
            "Volume and trends"
        );
        
        JButton performanceBtn = createReportTypeButton(
            "🎯 Performance",
            "On-time deliveries"
        );
        
        JButton revenueBtn = createReportTypeButton(
            "💰 Revenue",
            "Financial breakdown"
        );
        
        // Report actions
        comprehensiveBtn.addActionListener(e -> generateSimpleReport("Comprehensive", daysBackSpinner));
        shipmentBtn.addActionListener(e -> generateSimpleReport("Shipment", daysBackSpinner));
        performanceBtn.addActionListener(e -> generateSimpleReport("Performance", daysBackSpinner));
        revenueBtn.addActionListener(e -> generateSimpleReport("Revenue", daysBackSpinner));
        
        reportTypesCard.add(comprehensiveBtn);
        reportTypesCard.add(shipmentBtn);
        reportTypesCard.add(performanceBtn);
        reportTypesCard.add(revenueBtn);
        
        centerPanel.add(reportTypesCard);
        
        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.setBorder(null);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBackground(new Color(230, 240, 250));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 220, 240)),
            new EmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel infoLabel = new JLabel("ℹ️ Reports will be exported as PDF files. Select days back from today and choose a report type.");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        infoPanel.add(infoLabel);
        
        panel.add(infoPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createReportCard(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            new EmptyBorder(20, 20, 20, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(SIDEBAR_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        
        return card;
    }

    private JButton createQuickDateButton(String text, int days, JSpinner spinner) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.PLAIN, 11));
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(8, 12, 8, 12)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addActionListener(e -> spinner.setValue(days));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(240, 240, 240));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.WHITE);
            }
        });
        
        return btn;
    }

    private JButton createReportTypeButton(String title, String description) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());
        btn.setBackground(CARD_BG);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(15, 15, 15, 15)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 13));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        descLabel.setForeground(Color.GRAY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        textPanel.add(titleLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(descLabel);
        
        btn.add(textPanel, BorderLayout.CENTER);
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(245, 245, 250));
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                    new EmptyBorder(14, 14, 14, 14)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(CARD_BG);
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    new EmptyBorder(15, 15, 15, 15)
                ));
            }
        });
        
        return btn;
    }

    private int getCurrentDayOfMonth() {
        return java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH) - 1;
    }

    private void generateSimpleReport(String reportType, JSpinner daysBackSpinner) {
        int daysBack = (Integer) daysBackSpinner.getValue();
        
        // Calculate dates
        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.sql.Date endDate = new java.sql.Date(cal.getTimeInMillis());
        
        cal.add(java.util.Calendar.DAY_OF_YEAR, -daysBack);
        java.sql.Date startDate = new java.sql.Date(cal.getTimeInMillis());
        
        // Show progress
        JDialog progressDialog = new JDialog(this, "Generating Report...", true);
        JPanel progressPanel = new JPanel(new BorderLayout(10, 10));
        progressPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        JLabel progressLabel = new JLabel("Collecting data and generating PDF...", SwingConstants.CENTER);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        
        progressPanel.add(progressLabel, BorderLayout.NORTH);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressDialog.add(progressPanel);
        progressDialog.setSize(400, 150);
        progressDialog.setLocationRelativeTo(this);
        
        // Generate in background
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                util.PDFReportGenerator.generateAnalyticsReport(
                    reportType,
                    startDate,
                    endDate,
                    currentUser.getUsername()
                );
                return null;
            }
            
            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    get();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ManagerFrame.this,
                        "Error generating report: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        };
        
        worker.execute();
        progressDialog.setVisible(true);
    }
    
    private void refreshDashboard(Runnable onComplete) {
        SwingWorker<DashboardData, Void> worker = new SwingWorker<DashboardData, Void>() {
            @Override
            protected DashboardData doInBackground() throws Exception {
                DashboardData data = new DashboardData();
                
                // Fetch all data
                List<Map<String, String>> pending = NetworkClient.getPendingShipments();
                data.pendingCount = (pending != null) ? pending.size() : 0;
                
                String dailyReport = NetworkClient.generateDailyReport();
                data.dailyReport = dailyReport;
                
                // Parse daily report for stats
                if (dailyReport != null && !dailyReport.isEmpty()) {
                    String[] lines = dailyReport.split("\n");
                    for (String line : lines) {
                        if (line.contains("Total Shipments:")) {
                            data.deliveries = line.split(":")[1].trim();
                        } else if (line.contains("Total Revenue:")) {
                            data.revenue = line.split(":")[1].trim();
                        }
                    }
                }
                
                return data;
            }
            
            @Override
            protected void done() {
                try {
                    DashboardData data = get();
                    updateDashboardStats(data);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ManagerFrame.this,
                        "Error refreshing dashboard: " + ex.getMessage(),
                        "Refresh Error",
                        JOptionPane.ERROR_MESSAGE);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }
        };
        worker.execute();
    }
    
    
    private void updateDashboardStats(DashboardData data) {
        currentDashboardStatsPanel.removeAll();
        
        currentDashboardStatsPanel.add(createStatCard(
            "Pending Shipments", 
            String.valueOf(data.pendingCount), 
            "📦"
        ));
        
        currentDashboardStatsPanel.add(createStatCard(
            "Today's Deliveries", 
            data.deliveries != null ? data.deliveries : "N/A", 
            "🚚"
        ));
        
        currentDashboardStatsPanel.add(createStatCard(
            "Total Revenue", 
            data.revenue != null ? data.revenue : "N/A", 
            "💰"
        ));
        
        currentDashboardStatsPanel.revalidate();
        currentDashboardStatsPanel.repaint();
    }

    /**
     * NEW: Refresh Report Data
     */
    private void refreshReport(Runnable onComplete) {
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return NetworkClient.generateDailyReport();
            }
            
            @Override
            protected void done() {
                try {
                    String report = get();
                    if (report != null && !report.isEmpty()) {
                        currentReportArea.setText(formatReport(report));
                        updateLastRefreshTime();
                    } else {
                        currentReportArea.setText("No report data available");
                    }
                    if (onComplete != null) {
                        onComplete.run();
                    }
                } catch (Exception ex) {
                    currentReportArea.setText("Error loading report: " + ex.getMessage());
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }
        };
        worker.execute();
    }

    /**
     * NEW: Update Last Refresh Time Label
     */
    private void updateLastRefreshTime() {
        // Find the last updated label in the reports panel
        Component[] components = contentPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                findAndUpdateLabel((JPanel) comp);
            }
        }
    }

    private void findAndUpdateLabel(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JPanel) {
                findAndUpdateLabel((JPanel) comp);
            } else if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if ("lastUpdatedLabel".equals(label.getName())) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
                    label.setText("Last updated: " + sdf.format(new Date()));
                    return;
                }
            }
        }
    }

    /**
     * NEW: Start Auto-Refresh Timer
     */
    private void startAutoRefresh() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.stop();
        }
        
        // Refresh every 30 seconds
        autoRefreshTimer = new Timer(30000, e -> {
            if (autoRefreshEnabled) {
                System.out.println("Auto-refreshing dashboard...");
                refreshDashboard(null);
            }
        });
        autoRefreshTimer.start();
        
        System.out.println("Auto-refresh enabled (30 second interval)");
    }

    /**
     * NEW: Stop Auto-Refresh Timer
     */
    private void stopAutoRefresh() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.stop();
            autoRefreshTimer = null;
        }
        System.out.println("Auto-refresh disabled");
    }

    /**
     * NEW: Inner class to hold dashboard data
     */
    private static class DashboardData {
        int pendingCount = 0;
        String deliveries = "N/A";
        String revenue = "N/A";
        String dailyReport = "";
    }
    
    /**
     * Load daily report
     */
    private void loadDailyReport(JTextArea reportArea) {
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return NetworkClient.generateDailyReport();
            }
            
            @Override
            protected void done() {
                try {
                    String report = get();
                    if (report != null && !report.isEmpty()) {
                        reportArea.setText(formatReport(report));
                    } else {
                        reportArea.setText("No report data available");
                    }
                } catch (Exception ex) {
                    reportArea.setText("Error loading report: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    /**
     * Format report for better display
     */
    private String formatReport(String report) {
        StringBuilder formatted = new StringBuilder();
        formatted.append("═══════════════════════════════════════════════════\n");
        formatted.append("          SMARTSHIP DAILY OPERATIONS REPORT        \n");
        formatted.append("═══════════════════════════════════════════════════\n\n");
        formatted.append(report);
        formatted.append("\n\n═══════════════════════════════════════════════════\n");
        formatted.append("Generated on: ").append(new java.util.Date()).append("\n");
        formatted.append("Manager: ").append(currentUser.getUsername()).append("\n");
        formatted.append("═══════════════════════════════════════════════════\n");
        return formatted.toString();
    }
    
    /**
     * Export report to PDF using PDFReportGenerator
     */
    private void exportReportToPDF() {
        // Show loading message
        JDialog loadingDialog = new JDialog(this, "Generating PDF...", true);
        JLabel loadingLabel = new JLabel("Please wait while the report is being generated...", SwingConstants.CENTER);
        loadingLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
        loadingDialog.add(loadingLabel);
        loadingDialog.setSize(400, 100);
        loadingDialog.setLocationRelativeTo(this);
        
        // Generate PDF in background
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private String reportData;
            
            @Override
            protected Boolean doInBackground() throws Exception {
                reportData = NetworkClient.generateDailyReport();
                if (reportData == null || reportData.isEmpty()) {
                    return false;
                }
                
                // Import the PDFReportGenerator class
                // util.PDFReportGenerator.generateDailyReportPDF(reportData, currentUser.getUsername());
                
                // For now, show a message if the class isn't available
                return true;
            }
            
            @Override
            protected void done() {
                loadingDialog.dispose();
                try {
                    if (get()) {
                        // Attempt to use PDFReportGenerator
                        try {
                            Class<?> pdfGenClass = Class.forName("util.PDFReportGenerator");
                            java.lang.reflect.Method method = pdfGenClass.getMethod(
                                "generateDailyReportPDF", String.class, String.class);
                            method.invoke(null, reportData, currentUser.getUsername());
                        } catch (ClassNotFoundException e) {
                            JOptionPane.showMessageDialog(ManagerFrame.this,
                                "PDFReportGenerator class not found.\n" +
                                "Please ensure util.PDFReportGenerator is in your project\n" +
                                "and iText + JFreeChart libraries are added.",
                                "Missing Class",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(ManagerFrame.this,
                            "No report data available to export",
                            "Export Failed",
                            JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ManagerFrame.this,
                        "Error exporting PDF: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
        loadingDialog.setVisible(true);
    }
    
    /**
     * Profile Panel
     */
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(CONTENT_BG);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        JLabel headerLabel = new JLabel("Manager Profile");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 28));
        headerLabel.setForeground(SIDEBAR_COLOR);
        panel.add(headerLabel, BorderLayout.NORTH);
        
        // Profile info card
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(CARD_BG);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            new EmptyBorder(30, 30, 30, 30)
        ));
        
        addProfileField(infoPanel, "Username:", currentUser.getUsername());
        addProfileField(infoPanel, "Email:", currentUser.getEmail());
        addProfileField(infoPanel, "Phone:", currentUser.getPhone());
        addProfileField(infoPanel, "Address:", currentUser.getAddress());
        addProfileField(infoPanel, "Role:", currentUser.getRole());
        addProfileField(infoPanel, "Department:", currentUser.getDepartment());
        
        panel.add(infoPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Add profile field
     */
    private void addProfileField(JPanel panel, String label, String value) {
        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        fieldPanel.setBackground(CARD_BG);
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Arial", Font.BOLD, 14));
        labelComp.setPreferredSize(new Dimension(120, 25));
        
        JLabel valueComp = new JLabel(value != null ? value : "N/A");
        valueComp.setFont(new Font("Arial", Font.PLAIN, 14));
        
        fieldPanel.add(labelComp);
        fieldPanel.add(valueComp);
        
        panel.add(fieldPanel);
    }
    private JPanel createVehiclesPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(CONTENT_BG);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        // Header with actions
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CONTENT_BG);
        
        JLabel headerLabel = new JLabel("Fleet Management");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 28));
        headerLabel.setForeground(SIDEBAR_COLOR);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        
        JPanel headerActionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerActionsPanel.setBackground(CONTENT_BG);
        
        JButton refreshBtn = createStyledButton("🔄 Refresh", PRIMARY_COLOR);
        refreshBtn.addActionListener(e -> refreshVehiclesTable());
        
        JButton addVehicleBtn = createStyledButton("➕ Add Vehicle", SIDEBAR_COLOR);
        addVehicleBtn.addActionListener(e -> showAddVehicleDialog());
        
        headerActionsPanel.add(refreshBtn);
        headerActionsPanel.add(addVehicleBtn);
        headerPanel.add(headerActionsPanel, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Table
        String[] columns = {
            "Vehicle ID", "Vehicle #", "Type", "License Plate", 
            "Driver", "Capacity (kg)", "Current Load (kg)", 
            "Available (kg)", "Items", "Zone", "Status"
        };
        
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(35);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Header styling
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.getTableHeader().setBackground(SIDEBAR_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);
        
        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(70);   // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(90);   // Vehicle #
        table.getColumnModel().getColumn(2).setPreferredWidth(80);   // Type
        table.getColumnModel().getColumn(3).setPreferredWidth(100);  // License
        table.getColumnModel().getColumn(4).setPreferredWidth(120);  // Driver
        table.getColumnModel().getColumn(5).setPreferredWidth(90);   // Capacity
        table.getColumnModel().getColumn(6).setPreferredWidth(110);  // Current
        table.getColumnModel().getColumn(7).setPreferredWidth(100);  // Available
        table.getColumnModel().getColumn(8).setPreferredWidth(60);   // Items
        table.getColumnModel().getColumn(9).setPreferredWidth(50);   // Zone
        table.getColumnModel().getColumn(10).setPreferredWidth(80);  // Status
        
        // Custom renderer for status column
        table.getColumnModel().getColumn(10).setCellRenderer((table1, value, isSelected, hasFocus, row, column) -> {
            JLabel label = new JLabel(value.toString());
            label.setOpaque(true);
            label.setFont(new Font("Arial", Font.BOLD, 11));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            
            if (isSelected) {
                label.setBackground(table1.getSelectionBackground());
                label.setForeground(table1.getSelectionForeground());
            } else {
                label.setBackground(Color.WHITE);
                String status = value.toString();
                if ("Available".equals(status)) {
                    label.setForeground(new Color(39, 174, 96));
                } else if ("In Use".equals(status)) {
                    label.setForeground(new Color(230, 126, 34));
                } else {
                    label.setForeground(Color.GRAY);
                }
            }
            return label;
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Action panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        actionPanel.setBackground(CONTENT_BG);
        
        JButton viewDetailsBtn = createStyledButton("📋 View Details", PRIMARY_COLOR);
        viewDetailsBtn.addActionListener(e -> viewVehicleDetails(table));
        
        JButton viewPackagesBtn = createStyledButton("📦 View Packages", SIDEBAR_COLOR);
        viewPackagesBtn.addActionListener(e -> viewVehiclePackages(table));
        
        actionPanel.add(viewDetailsBtn);
        actionPanel.add(viewPackagesBtn);
        panel.add(actionPanel, BorderLayout.SOUTH);
        
        // Load data
        loadVehiclesData(model);
        
        return panel;
    }
    private void loadVehiclesData(DefaultTableModel model) {
        SwingWorker<List<Map<String, String>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, String>> doInBackground() throws Exception {
                return NetworkClient.getAllVehicles();
            }
            
            @Override
            protected void done() {
                try {
                    List<Map<String, String>> vehicles = get();
                    model.setRowCount(0);
                    
                    if (vehicles != null && !vehicles.isEmpty()) {
                        for (Map<String, String> vehicle : vehicles) {
                            double capacity = Double.parseDouble(vehicle.get("capacity"));
                            double currentLoad = Double.parseDouble(vehicle.get("currentLoad"));
                            double available = capacity - currentLoad;
                            
                            model.addRow(new Object[]{
                                vehicle.get("vehicleId"),
                                vehicle.get("vehicleNumber"),
                                vehicle.get("type"),
                                vehicle.get("licensePlate"),
                                vehicle.get("driverName"),
                                String.format("%.2f", capacity),
                                String.format("%.2f", currentLoad),
                                String.format("%.2f", available),
                                vehicle.get("itemCount"),
                                vehicle.get("zone"),
                                vehicle.get("status")
                            });
                        }
                    } else {
                        JOptionPane.showMessageDialog(ManagerFrame.this,
                            "No vehicles found in the system",
                            "Information",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ManagerFrame.this,
                        "Error loading vehicles: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /**
     * Refresh vehicles table
     */
    private void refreshVehiclesTable() {
        Component[] components = contentPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel p = (JPanel) comp;
                Component[] children = p.getComponents();
                for (Component child : children) {
                    if (child instanceof JScrollPane) {
                        JScrollPane sp = (JScrollPane) child;
                        if (sp.getViewport().getView() instanceof JTable) {
                            JTable table = (JTable) sp.getViewport().getView();
                            if (table.getColumnCount() == 11) { // Vehicles table has 11 columns
                                loadVehiclesData((DefaultTableModel) table.getModel());
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * View detailed information about selected vehicle
     */
    private void viewVehicleDetails(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a vehicle to view details",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int vehicleId = Integer.parseInt(table.getValueAt(selectedRow, 0).toString());
        
        // Show loading dialog
        JDialog loadingDialog = new JDialog(this, "Loading...", true);
        JLabel loadingLabel = new JLabel("Loading vehicle details...", SwingConstants.CENTER);
        loadingLabel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        loadingDialog.add(loadingLabel);
        loadingDialog.setSize(300, 120);
        loadingDialog.setLocationRelativeTo(this);
        
        SwingWorker<Map<String, String>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, String> doInBackground() throws Exception {
                return NetworkClient.getVehicleDetails(vehicleId);
            }
            
            @Override
            protected void done() {
                loadingDialog.dispose();
                try {
                    Map<String, String> details = get();
                    if (details != null) {
                        showVehicleDetailsDialog(details);
                    } else {
                        JOptionPane.showMessageDialog(ManagerFrame.this,
                            "Could not load vehicle details",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ManagerFrame.this,
                        "Error loading details: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
        loadingDialog.setVisible(true);
    }

    /**
     * Show vehicle details dialog
     */
    private void showVehicleDetailsDialog(Map<String, String> details) {
        JDialog dialog = new JDialog(this, "Vehicle Details", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(15, 15));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(SIDEBAR_COLOR);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("🚛 " + details.get("vehicleNumber"));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        dialog.add(headerPanel, BorderLayout.NORTH);
        
        // Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        // Vehicle Information Section
        addSectionHeader(contentPanel, "Vehicle Information");
        addDetailRow(contentPanel, "Vehicle ID:", details.get("vehicleId"));
        addDetailRow(contentPanel, "Vehicle Number:", details.get("vehicleNumber"));
        addDetailRow(contentPanel, "Type:", details.get("vehicleType"));
        addDetailRow(contentPanel, "License Plate:", details.get("licensePlate"));
        addDetailRow(contentPanel, "Zone:", details.get("zone"));
        addDetailRow(contentPanel, "Status:", details.get("status"));
        
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Capacity Section
        addSectionHeader(contentPanel, "Capacity Information");
        addDetailRow(contentPanel, "Total Capacity:", details.get("capacity") + " kg");
        addDetailRow(contentPanel, "Current Load:", details.get("currentWeight") + " kg");
        
        double capacity = Double.parseDouble(details.get("capacity"));
        double currentLoad = Double.parseDouble(details.get("currentWeight"));
        double available = capacity - currentLoad;
        addDetailRow(contentPanel, "Available Capacity:", String.format("%.2f kg", available));
        addDetailRow(contentPanel, "Items Loaded:", details.get("itemCount"));
        
        // Utilization bar
        JPanel utilizationPanel = new JPanel(new BorderLayout(10, 5));
        utilizationPanel.setBackground(Color.WHITE);
        utilizationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        utilizationPanel.setMaximumSize(new Dimension(500, 40));
        
        JLabel utilizationLabel = new JLabel("Utilization:");
        utilizationLabel.setFont(new Font("Arial", Font.BOLD, 12));
        utilizationLabel.setPreferredSize(new Dimension(150, 20));
        
        JProgressBar progressBar = new JProgressBar(0, 100);
        int utilization = (int)((currentLoad / capacity) * 100);
        progressBar.setValue(utilization);
        progressBar.setString(utilization + "%");
        progressBar.setStringPainted(true);
        
        if (utilization < 50) {
            progressBar.setForeground(new Color(39, 174, 96));
        } else if (utilization < 80) {
            progressBar.setForeground(new Color(243, 156, 18));
        } else {
            progressBar.setForeground(new Color(231, 76, 60));
        }
        
        utilizationPanel.add(utilizationLabel, BorderLayout.WEST);
        utilizationPanel.add(progressBar, BorderLayout.CENTER);
        contentPanel.add(utilizationPanel);
        
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Driver Section
        addSectionHeader(contentPanel, "Driver Information");
        addDetailRow(contentPanel, "Driver Name:", details.get("driverName"));
        addDetailRow(contentPanel, "Driver Phone:", details.get("driverPhone"));
        addDetailRow(contentPanel, "License Number:", details.get("driverLicense"));
        addDetailRow(contentPanel, "Driver Rating:", details.get("driverRating"));
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        
        JButton closeBtn = createStyledButton("Close", SIDEBAR_COLOR);
        closeBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeBtn);
        
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * View packages assigned to selected vehicle
     */
    private void viewVehiclePackages(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a vehicle to view packages",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int vehicleId = Integer.parseInt(table.getValueAt(selectedRow, 0).toString());
        String vehicleNumber = table.getValueAt(selectedRow, 1).toString();
        
        // Show loading dialog
        JDialog loadingDialog = new JDialog(this, "Loading...", true);
        JLabel loadingLabel = new JLabel("Loading packages...", SwingConstants.CENTER);
        loadingLabel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        loadingDialog.add(loadingLabel);
        loadingDialog.setSize(300, 120);
        loadingDialog.setLocationRelativeTo(this);
        
        SwingWorker<List<Map<String, String>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, String>> doInBackground() throws Exception {
                return NetworkClient.getVehiclePackages(vehicleId);
            }
            
            @Override
            protected void done() {
                loadingDialog.dispose();
                try {
                    List<Map<String, String>> packages = get();
                    showVehiclePackagesDialog(vehicleNumber, packages);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ManagerFrame.this,
                        "Error loading packages: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
        loadingDialog.setVisible(true);
    }

    /**
     * Show packages dialog
     */
    private void showVehiclePackagesDialog(String vehicleNumber, List<Map<String, String>> packages) {
        JDialog dialog = new JDialog(this, "Packages - " + vehicleNumber, true);
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(15, 15));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(SIDEBAR_COLOR);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("📦 Packages on " + vehicleNumber);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        dialog.add(headerPanel, BorderLayout.NORTH);
        
        // Table
        String[] columns = {
            "Tracking #", "Recipient", "Destination", 
            "Weight (kg)", "Type", "Status"
        };
        
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable packagesTable = new JTable(model);
        packagesTable.setFont(new Font("Arial", Font.PLAIN, 12));
        packagesTable.setRowHeight(30);
        packagesTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        packagesTable.getTableHeader().setBackground(SIDEBAR_COLOR);
        packagesTable.getTableHeader().setForeground(Color.WHITE);
        
        if (packages != null && !packages.isEmpty()) {
            for (Map<String, String> pkg : packages) {
                model.addRow(new Object[]{
                    pkg.get("trackingNumber"),
                    pkg.get("recipientInfo"),
                    pkg.get("address"),
                    pkg.get("weight"),
                    pkg.get("packageType"),
                    pkg.get("status")
                });
            }
        } else {
            JLabel emptyLabel = new JLabel("No packages assigned to this vehicle", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            emptyLabel.setForeground(Color.GRAY);
            dialog.add(emptyLabel, BorderLayout.CENTER);
        }
        
        JScrollPane scrollPane = new JScrollPane(packagesTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton closeBtn = createStyledButton("Close", SIDEBAR_COLOR);
        closeBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeBtn);
        
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Helper: Add section header
     */
    private void addSectionHeader(JPanel panel, String text) {
        JLabel header = new JLabel(text);
        header.setFont(new Font("Arial", Font.BOLD, 16));
        header.setForeground(SIDEBAR_COLOR);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(header);
    }

    /**
     * Helper: Add detail row
     */
    private void addDetailRow(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row.setBackground(Color.WHITE);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(500, 30));
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Arial", Font.BOLD, 13));
        labelComp.setPreferredSize(new Dimension(180, 20));
        
        JLabel valueComp = new JLabel(value != null ? value : "N/A");
        valueComp.setFont(new Font("Arial", Font.PLAIN, 13));
        
        row.add(labelComp);
        row.add(valueComp);
        panel.add(row);
    }

    /**
     * Show add vehicle dialog (placeholder)
     */
    private void showAddVehicleDialog() {
        JOptionPane.showMessageDialog(this,
            "Add Vehicle functionality coming soon!\n\n" +
            "This feature will allow you to:\n" +
            "• Add new vehicles to the fleet\n" +
            "• Assign drivers to vehicles\n" +
            "• Set vehicle capacity and zone",
            "Feature Coming Soon",
            JOptionPane.INFORMATION_MESSAGE);
    }
    /**
     * Create styled button
     */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 40));
        return btn;
    }
    
    
    /**
     * Logout
     */
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Stop auto-refresh before logging out
            stopAutoRefresh();
            new LoginFrame();
            dispose();
        }
    }
    public static void main(String [] args) {
    	 SwingUtilities.invokeLater(() -> {
             new LoginFrame(); // User logs in first
         });
    }
}
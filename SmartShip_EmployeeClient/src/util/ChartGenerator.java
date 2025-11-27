package util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;

import java.awt.*;
import java.text.SimpleDateFormat;

/**
 * Utility class for generating various charts for reports
 * Uses JFreeChart library to create professional visualizations
 */
public class ChartGenerator {
    
    // Color scheme for SmartShip
    private static final Color PRIMARY_COLOR = new Color(163, 67, 53);
    private static final Color SECONDARY_COLOR = new Color(52, 73, 94);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color WARNING_COLOR = new Color(243, 156, 18);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color INFO_COLOR = new Color(52, 152, 219);
    
    /**
     * Create shipment volume bar chart (daily/weekly/monthly)
     */
    public static JFreeChart createShipmentVolumeChart(java.util.List<java.util.Map<String, Object>> dailyCounts, String title) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
        
        for (java.util.Map<String, Object> day : dailyCounts) {
            java.sql.Date date = (java.sql.Date) day.get("date");
            Integer count = (Integer) day.get("count");
            dataset.addValue(count, "Shipments", sdf.format(date));
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
            title,
            "Date",
            "Number of Shipments",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        
        // Customize appearance
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, PRIMARY_COLOR);
        
        return chart;
    }
    
    /**
     * Create delivery performance pie chart (on-time vs delayed)
     */
    public static JFreeChart createDeliveryPerformancePieChart(java.util.Map<String, Object> performanceStats) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        int onTime = (Integer) performanceStats.getOrDefault("onTime", 0);
        int delayed = (Integer) performanceStats.getOrDefault("delayed", 0);
        int inProgress = (Integer) performanceStats.getOrDefault("inProgress", 0);
        int pending = (Integer) performanceStats.getOrDefault("pending", 0);
        
        dataset.setValue("On-Time (" + onTime + ")", onTime);
        dataset.setValue("Delayed (" + delayed + ")", delayed);
        dataset.setValue("In Progress (" + inProgress + ")", inProgress);
        dataset.setValue("Pending (" + pending + ")", pending);
        
        JFreeChart chart = ChartFactory.createPieChart(
            "Delivery Performance",
            dataset,
            true,
            true,
            false
        );
        
        // Customize colors
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("On-Time (" + onTime + ")", SUCCESS_COLOR);
        plot.setSectionPaint("Delayed (" + delayed + ")", DANGER_COLOR);
        plot.setSectionPaint("In Progress (" + inProgress + ")", INFO_COLOR);
        plot.setSectionPaint("Pending (" + pending + ")", WARNING_COLOR);
        plot.setBackgroundPaint(Color.WHITE);
        
        return chart;
    }
    
    /**
     * Create revenue bar chart by package type
     */
    public static JFreeChart createRevenueByTypeChart(java.util.Map<String, Object> revenueStats) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        Double expressRevenue = (Double) revenueStats.getOrDefault("expressRevenue", 0.0);
        Double standardRevenue = (Double) revenueStats.getOrDefault("standardRevenue", 0.0);
        Double fragileRevenue = (Double) revenueStats.getOrDefault("fragileRevenue", 0.0);
        
        dataset.addValue(expressRevenue, "Revenue", "Express");
        dataset.addValue(standardRevenue, "Revenue", "Standard");
        dataset.addValue(fragileRevenue, "Revenue", "Fragile");
        
        JFreeChart chart = ChartFactory.createBarChart(
            "Revenue by Package Type",
            "Package Type",
            "Revenue ($)",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            true,
            false
        );
        
        // Customize appearance
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, SECONDARY_COLOR);
        
        return chart;
    }
    
    /**
     * Create vehicle utilization bar chart
     */
    public static JFreeChart createVehicleUtilizationChart(java.util.List<java.util.Map<String, Object>> vehicleStats) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (java.util.Map<String, Object> vehicle : vehicleStats) {
            String vehicleNumber = (String) vehicle.get("vehicleNumber");
            Double utilization = (Double) vehicle.get("utilizationPercent");
            dataset.addValue(utilization, "Utilization %", vehicleNumber);
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
            "Vehicle Capacity Utilization",
            "Vehicle",
            "Utilization (%)",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            true,
            false
        );
        
        // Customize appearance
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        // Color bars based on utilization level
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        for (int i = 0; i < vehicleStats.size(); i++) {
            Double utilization = (Double) vehicleStats.get(i).get("utilizationPercent");
            if (utilization >= 95) {
                renderer.setSeriesPaint(0, DANGER_COLOR);
            } else if (utilization >= 70) {
                renderer.setSeriesPaint(0, WARNING_COLOR);
            } else if (utilization >= 30) {
                renderer.setSeriesPaint(0, INFO_COLOR);
            } else {
                renderer.setSeriesPaint(0, SUCCESS_COLOR);
            }
        }
        
        return chart;
    }
    
    /**
     * Create shipment status distribution pie chart
     */
    public static JFreeChart createShipmentStatusPieChart(java.util.Map<String, Object> shipmentStats) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        Integer delivered = (Integer) shipmentStats.getOrDefault("deliveredCount", 0);
        Integer inTransit = (Integer) shipmentStats.getOrDefault("inTransitCount", 0);
        Integer pending = (Integer) shipmentStats.getOrDefault("pendingCount", 0);
        Integer assigned = (Integer) shipmentStats.getOrDefault("assignedCount", 0);
        
        if (delivered > 0) dataset.setValue("Delivered (" + delivered + ")", delivered);
        if (inTransit > 0) dataset.setValue("In Transit (" + inTransit + ")", inTransit);
        if (pending > 0) dataset.setValue("Pending (" + pending + ")", pending);
        if (assigned > 0) dataset.setValue("Assigned (" + assigned + ")", assigned);
        
        JFreeChart chart = ChartFactory.createPieChart(
            "Shipment Status Distribution",
            dataset,
            true,
            true,
            false
        );
        
        // Customize colors
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("Delivered (" + delivered + ")", SUCCESS_COLOR);
        plot.setSectionPaint("In Transit (" + inTransit + ")", INFO_COLOR);
        plot.setSectionPaint("Pending (" + pending + ")", WARNING_COLOR);
        plot.setSectionPaint("Assigned (" + assigned + ")", SECONDARY_COLOR);
        plot.setBackgroundPaint(Color.WHITE);
        
        return chart;
    }
    
    /**
     * Create revenue by zone bar chart
     */
    public static JFreeChart createRevenueByZoneChart(java.util.Map<String, Object> revenueStats) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        Double zone1Revenue = (Double) revenueStats.getOrDefault("zone1Revenue", 0.0);
        Double zone2Revenue = (Double) revenueStats.getOrDefault("zone2Revenue", 0.0);
        
        dataset.addValue(zone1Revenue, "Revenue", "Zone 1");
        dataset.addValue(zone2Revenue, "Revenue", "Zone 2");
        
        JFreeChart chart = ChartFactory.createBarChart(
            "Revenue by Delivery Zone",
            "Zone",
            "Revenue ($)",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            true,
            false
        );
        
        // Customize appearance
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, PRIMARY_COLOR);
        
        return chart;
    }
    
    /**
     * Create package type distribution pie chart
     */
    public static JFreeChart createPackageTypePieChart(java.util.Map<String, Object> shipmentStats) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        Integer express = (Integer) shipmentStats.getOrDefault("expressCount", 0);
        Integer standard = (Integer) shipmentStats.getOrDefault("standardCount", 0);
        Integer fragile = (Integer) shipmentStats.getOrDefault("fragileCount", 0);
        
        if (express > 0) dataset.setValue("Express (" + express + ")", express);
        if (standard > 0) dataset.setValue("Standard (" + standard + ")", standard);
        if (fragile > 0) dataset.setValue("Fragile (" + fragile + ")", fragile);
        
        JFreeChart chart = ChartFactory.createPieChart(
            "Package Type Distribution",
            dataset,
            true,
            true,
            false
        );
        
        // Customize colors
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("Express (" + express + ")", DANGER_COLOR);
        plot.setSectionPaint("Standard (" + standard + ")", INFO_COLOR);
        plot.setSectionPaint("Fragile (" + fragile + ")", WARNING_COLOR);
        plot.setBackgroundPaint(Color.WHITE);
        
        return chart;
    }
}
package util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Professional PDF Report Generator for SmartShip
 * Creates comprehensive reports with charts, tables, and formatted text
 */
public class PDFReportGenerator {

    // Colors
    private static final BaseColor PRIMARY_COLOR = new BaseColor(163, 67, 53);
    private static final BaseColor SECONDARY_COLOR = new BaseColor(52, 73, 94);
    private static final BaseColor LIGHT_GRAY = new BaseColor(249, 249, 249);

    // Fonts
    private static Font TITLE_FONT;
    private static Font HEADING_FONT;
    private static Font SUBHEADING_FONT;
    private static Font NORMAL_FONT;
    private static Font BOLD_FONT;
    private static Font SMALL_FONT;

    static {
        try {
            TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, PRIMARY_COLOR);
            HEADING_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, SECONDARY_COLOR);
            SUBHEADING_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, SECONDARY_COLOR);
            NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);
            BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.BLACK);
            SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.GRAY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ================================================================
       SAFE NULL HANDLING HELPERS
       ============================================================== */

    private static double safeDouble(Object value) {
        return (value == null) ? 0.0 : ((Number) value).doubleValue();   // FIX
    }

    private static int safeInt(Object value) {
        return (value == null) ? 0 : ((Number) value).intValue();        // FIX
    }

    private static String safeCurrency(Object value) {
        return String.format("$%.2f", safeDouble(value));                // FIX
    }

    private static String safePercent(Object value) {
        return String.format("%.1f%%", safeDouble(value));               // FIX
    }

    private static String safeNumber(Object value) {
        return String.valueOf(safeInt(value));                           // FIX
    }


    /**
     * Generate comprehensive analytics report
     */
    public static void generateAnalyticsReport(
            String reportType,
            java.sql.Date startDate,
            java.sql.Date endDate,
            String managerName) {

        try {
            // Create filename with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = "SmartShip_" + reportType + "_Report_" + timestamp + ".pdf";

            // Let user choose save location
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Report");
            fileChooser.setSelectedFile(new File(filename));

            int userSelection = fileChooser.showSaveDialog(null);
            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return; // User cancelled
            }

            File fileToSave = fileChooser.getSelectedFile();

            // Create document
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileToSave));

            // Add header and footer
            HeaderFooter headerFooter = new HeaderFooter(managerName);
            writer.setPageEvent(headerFooter);

            document.open();

            // Add title page
            addTitlePage(document, reportType, startDate, endDate, managerName);
            document.newPage();

            // Collect data
            Map<String, Object> shipmentStats = database.DatabaseHelper.getShipmentStatsByDateRange(startDate, endDate);
            Map<String, Object> performanceStats = database.DatabaseHelper.getDeliveryPerformanceStats(startDate, endDate);
            Map<String, Object> revenueStats = database.DatabaseHelper.getRevenueStats(startDate, endDate);
            java.util.List<Map<String, Object>> vehicleStats = database.DatabaseHelper.getVehicleUtilizationStats();
            java.util.List<Map<String, Object>> dailyCounts = database.DatabaseHelper.getDailyShipmentCounts(startDate, endDate);

            // Sections
            addExecutiveSummary(document, shipmentStats, revenueStats, performanceStats);
            document.newPage();

            addShipmentAnalysis(document, shipmentStats, dailyCounts);
            document.newPage();

            addDeliveryPerformance(document, performanceStats);
            document.newPage();

            addRevenueAnalysis(document, revenueStats);
            document.newPage();

            addVehicleUtilization(document, vehicleStats);

            document.close();

            // Confirmation dialog
            int result = JOptionPane.showConfirmDialog(
                    null,
                    "Report generated successfully!\n\nFile: " + fileToSave.getName() +
                            "\n\nWould you like to open the report?",
                    "Report Generated",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
            );

            if (result == JOptionPane.YES_OPTION) {
                java.awt.Desktop.getDesktop().open(fileToSave);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Error generating report: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    /**
     * Add title page
     */
    private static void addTitlePage(Document document, String reportType,
                                     java.sql.Date startDate, java.sql.Date endDate,
                                     String managerName) throws DocumentException {

        Paragraph title = new Paragraph("SMARTSHIP", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        Paragraph subtitle = new Paragraph(reportType + " Report", HEADING_FONT);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(30);
        document.add(subtitle);

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
        Paragraph dateRange = new Paragraph(
                "Report Period: " + sdf.format(startDate) + " - " + sdf.format(endDate),
                NORMAL_FONT
        );
        dateRange.setAlignment(Element.ALIGN_CENTER);
        dateRange.setSpacingAfter(50);
        document.add(dateRange);

        // Info box
        PdfPTable infoTable = new PdfPTable(1);
        infoTable.setWidthPercentage(60);
        infoTable.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(LIGHT_GRAY);
        cell.setPadding(20);
        cell.setBorder(Rectangle.NO_BORDER);

        Paragraph info = new Paragraph();
        info.add(new Chunk("SmartShip Package Management System\n\n", BOLD_FONT));
        info.add(new Chunk("Generated by: " + managerName + "\n", NORMAL_FONT));
        info.add(new Chunk("Generated on: " + new SimpleDateFormat("MMMM dd, yyyy HH:mm").format(new Date()) + "\n", NORMAL_FONT));
        info.add(new Chunk("\nConfidential - Internal Use Only", SMALL_FONT));
        info.setAlignment(Element.ALIGN_CENTER);

        cell.addElement(info);
        infoTable.addCell(cell);
        document.add(infoTable);
    }

    /**
     * Add executive summary
     */
    private static void addExecutiveSummary(Document document,
                                            Map<String, Object> shipmentStats,
                                            Map<String, Object> revenueStats,
                                            Map<String, Object> performanceStats)
            throws DocumentException {

        addSectionHeading(document, "Executive Summary");

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);

        addSummaryRow(table, "Total Shipments", safeNumber(shipmentStats.get("totalShipments")));
        addSummaryRow(table, "Total Revenue", safeCurrency(revenueStats.get("totalRevenue")));
        addSummaryRow(table, "Delivered Shipments", safeNumber(performanceStats.get("delivered")));
        addSummaryRow(table, "On-Time Delivery Rate", safePercent(performanceStats.get("onTimePercent")));
        addSummaryRow(table, "Average Shipment Value", safeCurrency(revenueStats.get("avgShipmentCost")));
        addSummaryRow(table, "Total Weight Shipped",
                String.format("%.2f kg", safeDouble(shipmentStats.get("totalWeight"))));

        document.add(table);

        // Insights list
        Paragraph insights = new Paragraph("Key Insights", SUBHEADING_FONT);
        insights.setSpacingBefore(20);
        insights.setSpacingAfter(10);
        document.add(insights);

        com.itextpdf.text.List list = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
        list.setListSymbol(new Chunk("•  "));

        list.add(new ListItem(
                String.format("Processed %s shipments during this period",
                        safeNumber(shipmentStats.get("totalShipments"))),
                NORMAL_FONT));

        list.add(new ListItem(
                String.format("Generated %s in revenue",
                        safeCurrency(revenueStats.get("totalRevenue"))),
                NORMAL_FONT));

        // FIX: safe on-time percent
        double otp = safeDouble(performanceStats.get("onTimePercent"));
        String performanceText =
                otp >= 90 ? "Excellent" :
                        otp >= 80 ? "Good" :
                                otp >= 70 ? "Fair" : "Needs Improvement";

        list.add(new ListItem(
                String.format("%s on-time delivery performance (%.1f%%)", performanceText, otp),
                NORMAL_FONT));

        document.add(list);
    }

    /**
     * Shipment analysis
     */
    private static void addShipmentAnalysis(Document document,
                                            Map<String, Object> shipmentStats,
                                            java.util.List<Map<String, Object>> dailyCounts) throws Exception {

        addSectionHeading(document, "Shipment Analysis");

        JFreeChart volumeChart = ChartGenerator.createShipmentVolumeChart(dailyCounts, "Daily Shipment Volume");
        addChart(document, volumeChart, 500, 300);

        JFreeChart statusChart = ChartGenerator.createShipmentStatusPieChart(shipmentStats);
        addChart(document, statusChart, 400, 300);

        JFreeChart typeChart = ChartGenerator.createPackageTypePieChart(shipmentStats);
        addChart(document, typeChart, 400, 300);
    }

    /**
     * Delivery performance
     */
    private static void addDeliveryPerformance(Document document,
                                               Map<String, Object> performanceStats) throws Exception {

        addSectionHeading(document, "Delivery Performance");

        JFreeChart chart = ChartGenerator.createDeliveryPerformancePieChart(performanceStats);
        addChart(document, chart, 450, 350);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(80);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setSpacingBefore(20);

        addMetricRow(table, "Total Delivered", safeNumber(performanceStats.get("delivered")));
        addMetricRow(table, "On-Time Deliveries", safeNumber(performanceStats.get("onTime")));
        addMetricRow(table, "Delayed Deliveries", safeNumber(performanceStats.get("delayed")));
        addMetricRow(table, "On-Time Rate", safePercent(performanceStats.get("onTimePercent")));

        // FIX: avg days safe
        addMetricRow(table, "Average Delivery Time",
                String.format("%.1f days", safeDouble(performanceStats.get("avgDeliveryDays"))));

        document.add(table);
    }

    /**
     * Revenue section
     */
    private static void addRevenueAnalysis(Document document,
                                           Map<String, Object> revenueStats) throws Exception {

        addSectionHeading(document, "Revenue Analysis");

        addChart(document, ChartGenerator.createRevenueByTypeChart(revenueStats), 450, 300);
        addChart(document, ChartGenerator.createRevenueByZoneChart(revenueStats), 450, 300);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(80);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setSpacingBefore(20);

        addMetricRow(table, "Total Revenue", safeCurrency(revenueStats.get("totalRevenue")));
        addMetricRow(table, "Paid Revenue", safeCurrency(revenueStats.get("paidRevenue")));
        addMetricRow(table, "Unpaid Revenue", safeCurrency(revenueStats.get("unpaidRevenue")));
        addMetricRow(table, "Express Revenue", safeCurrency(revenueStats.get("expressRevenue")));
        addMetricRow(table, "Standard Revenue", safeCurrency(revenueStats.get("standardRevenue")));
        addMetricRow(table, "Fragile Revenue", safeCurrency(revenueStats.get("fragileRevenue")));

        document.add(table);
    }

    /**
     * Vehicle utilization
     */
    private static void addVehicleUtilization(Document document,
                                              java.util.List<Map<String, Object>> vehicleStats)
            throws Exception {

        addSectionHeading(document, "Fleet Utilization");

        addChart(document,
                ChartGenerator.createVehicleUtilizationChart(vehicleStats),
                500, 300);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(20);

        addTableHeader(table, "Vehicle");
        addTableHeader(table, "Driver");
        addTableHeader(table, "Utilization");
        addTableHeader(table, "Status");

        // FIX: null-safe utilizationPercent
        for (Map<String, Object> v : vehicleStats) {
            addTableCell(table, (String) v.getOrDefault("vehicleNumber", "N/A"));
            addTableCell(table, (String) v.getOrDefault("driverName", "N/A"));
            addTableCell(table, safePercent(v.get("utilizationPercent")));
            addTableCell(table, (String) v.getOrDefault("status", "N/A"));
        }

        document.add(table);
    }


    /* ================================================================
       Helper Methods
       ============================================================== */

    private static void addSectionHeading(Document document, String text)
            throws DocumentException {

        Paragraph heading = new Paragraph(text, HEADING_FONT);
        heading.setSpacingBefore(20);
        heading.setSpacingAfter(15);
        document.add(heading);
    }

    private static void addChart(Document document, JFreeChart chart, int width, int height)
            throws Exception {

        BufferedImage chartImage = chart.createBufferedImage(width, height);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(chartImage, "png", baos);
        Image image = Image.getInstance(baos.toByteArray());
        image.setAlignment(Element.ALIGN_CENTER);
        image.scaleToFit(width, height);
        image.setSpacingBefore(10);
        image.setSpacingAfter(20);
        document.add(image);
    }

    private static void addSummaryRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, BOLD_FONT));
        labelCell.setBackgroundColor(LIGHT_GRAY);
        labelCell.setPadding(8);
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setPadding(8);
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private static void addMetricRow(PdfPTable table, String label, String value) {
        table.addCell(new Phrase(label, NORMAL_FONT));

        PdfPCell cell = new PdfPCell(new Phrase(value, BOLD_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
    }

    private static void addTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, BOLD_FONT));
        cell.setBackgroundColor(SECONDARY_COLOR);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);

        Font white = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);
        cell.setPhrase(new Phrase(text, white));

        table.addCell(cell);
    }

    private static void addTableCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL_FONT));
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }


    /**
     * Header/Footer
     */
    static class HeaderFooter extends PdfPageEventHelper {
        private final String managerName;

        public HeaderFooter(String managerName) {
            this.managerName = managerName;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();

            Phrase footer = new Phrase(
                    "SmartShip Analytics Report | Generated by: " + managerName +
                            " | Page " + writer.getPageNumber(),
                    SMALL_FONT
            );

            ColumnText.showTextAligned(
                    cb, Element.ALIGN_CENTER,
                    footer,
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 10,
                    0
            );
        }
    }
}

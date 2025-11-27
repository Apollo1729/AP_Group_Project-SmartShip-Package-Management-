package util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;

/**
 * Test class to verify that JFreeChart and iText libraries are properly installed
 * Run this class to ensure all dependencies are working
 */
public class LibraryVerificationTest {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   Library Verification Test");
        System.out.println("========================================\n");
        
        boolean allPassed = true;
        
        // Test 1: JFreeChart
        System.out.print("Testing JFreeChart... ");
        try {
            DefaultPieDataset dataset = new DefaultPieDataset();
            dataset.setValue("Test", 100);
            
            JFreeChart chart = ChartFactory.createPieChart(
                "Test Chart",
                dataset,
                true,
                true,
                false
            );
            
            System.out.println("✅ PASSED");
            System.out.println("   JFreeChart is working correctly!");
        } catch (Exception e) {
            System.out.println("❌ FAILED");
            System.out.println("   Error: " + e.getMessage());
            allPassed = false;
        }
        
        System.out.println();
        
        // Test 2: iText PDF
        System.out.print("Testing iText PDF... ");
        try {
            Document document = new Document();
            // Don't actually create the file, just test the classes load
            document.close();
            
            System.out.println("✅ PASSED");
            System.out.println("   iText PDF is working correctly!");
        } catch (Exception e) {
            System.out.println("❌ FAILED");
            System.out.println("   Error: " + e.getMessage());
            allPassed = false;
        }
        
        System.out.println();
        
        // Test 3: Create a simple PDF to verify full functionality
        System.out.print("Testing PDF creation... ");
        try {
            String testFile = "test-report.pdf";
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(testFile));
            document.open();
            document.add(new Paragraph("SmartShip Test Report"));
            document.add(new Paragraph("If you can read this, PDF generation works!"));
            document.close();
            
            System.out.println("✅ PASSED");
            System.out.println("   Created test-report.pdf successfully!");
            System.out.println("   Check your project root folder for the file.");
        } catch (Exception e) {
            System.out.println("❌ FAILED");
            System.out.println("   Error: " + e.getMessage());
            allPassed = false;
        }
        
        System.out.println();
        System.out.println("========================================");
        
        if (allPassed) {
            System.out.println("✅ ALL TESTS PASSED!");
            System.out.println("You're ready to build the reporting system!");
        } else {
            System.out.println("❌ SOME TESTS FAILED");
            System.out.println("Please check that all JAR files are properly added.");
            System.out.println("\nRequired JARs:");
            System.out.println("  - jfreechart-1.5.4.jar");
            System.out.println("  - jcommon-1.0.24.jar");
            System.out.println("  - itextpdf-5.5.13.3.jar");
        }
        
        System.out.println("========================================");
    }
}
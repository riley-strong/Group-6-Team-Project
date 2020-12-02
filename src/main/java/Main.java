import org.jfree.ui.RefineryUtilities;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

import static java.time.temporal.ChronoUnit.MILLIS;

public class Main {

    private static LocalTime programStart;
    private static LocalTime programEnd;
    private final String inventory_file = "inventory_team6.csv";
    //private final String customer_orders_file = "10Ktesting.csv"; //file to stress test db.
    //private final String customer_orders_file = "100Ktesting.csv"; //file to stress test db.
    private final String customer_orders_file = "customer_orders_A_team6.csv";
    private final String customer_orders_file = "customer_orders_final_team6.csv";
    private final String dim_date_start = "1900-01-01";
    private final String dim_date_end = "3000-12-31";
    private final String top_ten_date = "2020-03-01";
    private final String analytics_start = "2020-01-01";
    private final String analytics_end = "2020-06-28";
    private final int resupply_quantity = 500;


    public Main() {

    }

    public static void main(String[] args) {
        System.out.println("Program Start.");
        programStart = LocalTime.now();

        final Credentials credentials = new Credentials();
    }

    public void invoke(Credentials credentials, QueryMaker qm) throws IOException, SQLException, ClassNotFoundException {
        LocalTime dbCreation, bLoading, bProcessing, dAssets, chartDemo, mailServiceDemo;

        //Database re-creation as well as batch (.csv) file loading and processing
        System.out.println("The Credentials and QueryMaker objects have been created.");

        qm.createDatabaseStructure(inventory_file);
        System.out.println("The basic database structure has been created and inventory has been loaded.");
        dbCreation = LocalTime.now();

        qm.batchLoading(customer_orders_file, dim_date_start, dim_date_end);
        System.out.println("Batch file loading complete.");
        bLoading = LocalTime.now();

        qm.batchProcessing(resupply_quantity, 1);
        System.out.println("Batch processing complete.");
        bProcessing = LocalTime.now();

        //Daily Asset Generation
        qm.generateDailyAssets(analytics_start, analytics_end);
        System.out.println("Daily assets loaded.");
        dAssets = LocalTime.now();

      
        //MailService Demonstration
        qm.createTable("temp_unprocessed_sales",
        "date DATE ,cust_email VARCHAR(320) ,cust_location VARCHAR(5) ,product_id VARCHAR(12) ,product_quantity INT");
//        MailService mail = new MailService();
//        System.out.println("\nReading emails");
//        mail.readEmail(credentials, qm);
//        System.out.println("Emails read");
//        qm.processEmails();
//        qm.batchProcessing(resupply_quantity, 2);
//        System.out.println("Mail Service demonstrations completed.\n");
        mailServiceDemo = LocalTime.now();


        //Top Ten Information
        qm.topTenCustomers(top_ten_date);
        qm.topTenProducts(top_ten_date);


        //Chart Demonstrations
        System.out.println("\nStarting charts");
        String title = "CUP'O JAVA ASSETS";
        TimeSeries_AWT demo = new TimeSeries_AWT(title, credentials, 1, analytics_start, analytics_end);
        demo.pack();
        RefineryUtilities.positionFrameRandomly(demo);
        System.out.println("Chart completed");
        demo.setVisible(true);
        System.out.println("Chart demonstrations completed.");

        String title2 = "CUP'O JAVA DAILY ORDERS";
        TimeSeries_AWT demo2 = new TimeSeries_AWT(title2, credentials, 2, analytics_start, analytics_end);
        demo2.pack();
        RefineryUtilities.positionFrameRandomly(demo2);
        System.out.println("Chart completed");
        demo2.setVisible(true);
        System.out.println("Chart demonstrations completed.");

        String title3 = "CUP'O JAVA DAILY PURCHASE TOTALS";
        TimeSeries_AWT demo3 = new TimeSeries_AWT(title3, credentials, 3, analytics_start, analytics_end);
        demo3.pack();
        RefineryUtilities.positionFrameRandomly(demo3);
        System.out.println("Chart completed");
        demo3.setVisible(true);
        System.out.println("Chart demonstrations completed.");
        chartDemo = LocalTime.now();
      

        programEnd = LocalTime.now();
        //System.out.println("\nDatabase authentication/connection took " + programStart.until(cqmCreation, SECONDS) + " seconds.");
        System.out.println("Database creation took " + (programStart.until(dbCreation, MILLIS) / 1000.0) + " seconds.");
        System.out.println("Batch (.csv) loading took " + (dbCreation.until(bLoading, MILLIS) / 1000.0) + " seconds.");
        System.out.println("Batch (.csv) processing took " + (bLoading.until(bProcessing, MILLIS) / 1000.0) + " seconds.");
        System.out.println("Daily assets took " + (bProcessing.until(dAssets, MILLIS) / 1000.0) + " seconds.");
        System.out.println("Chart demonstration took " + bProcessing.until(chartDemo, MILLIS) / 1000.0 + " seconds.");
        System.out.println("Mail Service demonstration took " + bProcessing.until(mailServiceDemo, MILLIS) / 1000.0 + " seconds.");
        System.out.println("Total program execution took " + (programStart.until(programEnd, MILLIS) / 1000.0) + " seconds.");
    }
}

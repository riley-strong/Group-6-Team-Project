import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalTime;

import static java.time.temporal.ChronoUnit.MILLIS;

public class Main {

        private static LocalTime programStart;
        private static LocalTime programEnd;
        private final String inventory_file = "inventory_team6.csv";
        //private final String customer_orders_file = "10Ktesting.csv"; //file to stress test db.
        private final String customer_orders_file = "customer_orders_A_team6.csv";
        private final String dim_date_start = "2020-01-01";
        private final String dim_date_end = "2020-12-31";
        private final int resupply_quantity = 500;


        public Main() {

        }

        public static void main(String[] args) {
                System.out.println("Program Start.");
                programStart = LocalTime.now();

                final Credentials credentials = new Credentials();
        }

        public void invoke(Credentials credentials,QueryMaker qm) throws IOException, SQLException, ClassNotFoundException {
                LocalTime dbCreation, bLoading, bProcessing, dAssets, chartDemo, mailServiceDemo;

                //Database re-creation as well as batch (.csv) file loading and processing
                System.out.println("The Credentials and QueryMaker objects have been created.");

                qm.createDatabaseStructure(inventory_file);
                System.out.println("The basic database structure has been created and inventory has been loaded.");
                dbCreation = LocalTime.now();

                qm.batchLoading(customer_orders_file, dim_date_start, dim_date_end);
                System.out.println("Batch file loading complete.");
                bLoading = LocalTime.now();

                qm.batchProcessing(resupply_quantity);
                System.out.println("Batch processing complete.");
                bProcessing = LocalTime.now();

                qm.generateDailyAssets("01/01/2020", "06/28/2020");
                System.out.println("Daily assets loaded.");
                dAssets = LocalTime.now();


                //Chart Demonstration

                System.out.println("\nStarting chart");
                String title = "CUP'O JAVA ASSETS";
                TimeSeries_AWT demo = new TimeSeries_AWT(title, credentials, 3);
//
//                demo.pack();
//                RefineryUtilities.positionFrameRandomly(demo);
//                System.out.println("Chart completed");
//                demo.setVisible(true);
//                System.out.println("Chart demonstrations completed.");
//                chartDemo = LocalTime.now();


                //MailService Demonstration
                qm.createTable("temp_unprocessed_sales",
                        "date DATE ,cust_email VARCHAR(320) ,cust_location VARCHAR(5) ,product_id VARCHAR(12) ,product_quantity INT");

//                MailService mail = new MailService();
//                System.out.println("\nReading emails");
//                mail.readEmail(credentials, qm);
//                System.out.println("Emails read");
                qm.processEmails();
                qm.batchProcessing(resupply_quantity);
//                System.out.println("Mail Service demonstrations completed.\n");
//                mailServiceDemo = LocalTime.now();

                programEnd = LocalTime.now();
                //System.out.println("\nDatabase authentication/connection took " + programStart.until(cqmCreation, SECONDS) + " seconds.");
                System.out.println("Database creation took " + (programStart.until(dbCreation, MILLIS) / 1000.0) + " seconds.");
                System.out.println("Batch (.csv) loading took " + (dbCreation.until(bLoading, MILLIS) / 1000.0) + " seconds.");
                System.out.println("Batch (.csv) processing took " + (bLoading.until(bProcessing, MILLIS) / 1000.0) + " seconds.");
              System.out.println("Daily assets took " + (bProcessing.until(dAssets, MILLIS) / 1000.0) + " seconds.");
//                System.out.println("Chart demonstration took " + bProcessing.until(chartDemo, SECONDS) + " seconds.");
                //System.out.println("Mail Service demonstration took " + bProcessing.until(mailServiceDemo, SECONDS) + " seconds.");
                System.out.println("Total program execution took " + (programStart.until(programEnd, MILLIS) / 1000.0) + " seconds.");
        }
}

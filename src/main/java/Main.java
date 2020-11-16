import org.jfree.ui.RefineryUtilities;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;

import static java.time.temporal.ChronoUnit.SECONDS;

public class Main {

        private static LocalTime programStart;
        private static LocalTime programEnd;
        private final String inventory_file = "inventory_team6.csv";
        //private final String customer_orders_file = "100Ktesting.csv"; //file to stress test db.
        //private final String customer_orders_file = "500Ktesting.csv"; //file to stress test db.
        //private final String customer_orders_file = "1Mtesting.csv"; //file to stress test db.
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
                LocalTime cqmCreation, dbCreation, bLoading, bProcessing, chartDemo, mailServiceDemo;

                //Database re-creation as well as batch (.csv) file loading and processing
                System.out.println("The Credentials and QueryMaker objects have been created.");
                cqmCreation = LocalTime.now();

                qm.createDatabaseStructure(inventory_file);
                System.out.println("The basic database structure has been created and inventory has been loaded.");
                dbCreation = LocalTime.now();

                qm.batchLoading(customer_orders_file, dim_date_start, dim_date_end);
                System.out.println("Batch file loading complete.");
                bLoading = LocalTime.now();

                qm.batchProcessing(resupply_quantity);
                System.out.println("Batch processing complete.");
                bProcessing = LocalTime.now();


                //Chart Demonstration

//                System.out.println("\nStarting chart");
//                String title = "CUP'O JAVA ASSETS";
//                TimeSeries_AWT demo = new TimeSeries_AWT(title, credentials, 3);
//
//                demo.pack();
//                RefineryUtilities.positionFrameRandomly(demo);
//                System.out.println("Chart completed");
//                demo.setVisible(true);
//                System.out.println("Chart demonstrations completed.");
//                chartDemo = LocalTime.now();


                //MailService Demonstration

                MailService mail = new MailService();
                System.out.println("\nReading emails");
                mail.readEmail(credentials, qm);
                System.out.println("Emails read");
                System.out.println("Mail Service demonstrations completed.\n");
                mailServiceDemo = LocalTime.now();


                //SQL-Java method demonstrations

                //used in illustrations below - not intended for production code
                qm.generateUpdate("INSERT INTO inventory VALUES (\"test1234test\", 10, 1.01, 2.02, \"testsupp\")");

//                System.out.println();

                //illustrates use of readValues
                ResultSet rs = qm.readValues("quantity", "inventory", "product_id = " + qm.valueQueryPrep("test1234test"));
                while (rs.next()) {
                        int testQuant1 = rs.getInt(1);
//                        System.out.println("The quantity of Product \"test1234test\" in 'inventory' is " + testQuant1);
                }

 //               System.out.println();





                programEnd = LocalTime.now();
                System.out.println("\nDatabase authentication/connection took " + programStart.until(cqmCreation, SECONDS) + " seconds.");
                System.out.println("Database creation took " + cqmCreation.until(dbCreation, SECONDS) + " seconds.");
                System.out.println("Batch (.csv) loading took " + dbCreation.until(bLoading, SECONDS) + " seconds.");
                System.out.println("Batch (.csv) processing took " + bLoading.until(bProcessing, SECONDS) + " seconds.");
//                System.out.println("Chart demonstration took " + bProcessing.until(chartDemo, SECONDS) + " seconds.");
                System.out.println("Mail Service demonstration took " + bProcessing.until(mailServiceDemo, SECONDS) + " seconds.");
                System.out.println("Total program execution took " + programStart.until(programEnd, SECONDS) + " seconds.");
        }
}

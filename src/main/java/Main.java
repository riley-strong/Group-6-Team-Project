import org.jfree.ui.RefineryUtilities;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;

import static java.time.temporal.ChronoUnit.SECONDS;

public class Main {

        private static LocalTime programStart;
        private static LocalTime programEnd;

        public Main() {

        }

        public static void main(String[] args) {
                System.out.println("Program Start.");
                programStart = LocalTime.now();

                final Credentials credentials = new Credentials();
        }

        public void invoke(Credentials credentials,QueryMaker qm) throws IOException, SQLException, ClassNotFoundException {
                LocalTime cqmCreation, dbCreation, hiCreation, bLoading, bProcessing, chartDemo, mailServiceDemo, sqlDemo;

                //Database re-creation as well as batch (.csv) file loading and processing
                System.out.println("The Credentials and QueryMaker objects have been created.");
                cqmCreation = LocalTime.now();

                qm.createDatabaseStructure();
                System.out.println("The basic database structure has been created and inventory has been loaded.");
                dbCreation = LocalTime.now();

//                qm.createHistInv();
                System.out.println("Historical Inventory table has been created.");
                hiCreation = LocalTime.now();

                qm.batchLoading();
                System.out.println("Batch file loading complete.");
                bLoading = LocalTime.now();

                qm.batchProcessing();
                System.out.println("Batch processing complete.");
                bProcessing = LocalTime.now();


                //Chart Demonstration
                /*
                System.out.println("\nStarting chart");
                String title = "CUP'O JAVA ASSETS";
                TimeSeries_AWT demo = new TimeSeries_AWT(title, credentials, 3);

                demo.pack();
                RefineryUtilities.positionFrameRandomly(demo);
                System.out.println("Chart completed");
                demo.setVisible(true);
                System.out.println("Chart demonstrations completed.");
                chartDemo = LocalTime.now();


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

                //illustrates use of readRecords with one record result
                ResultSet rs2 = qm.readRecords("inventory", "product_id", "test1234test");
                while (rs2.next()) {
                        String testProdID2 = rs2.getString(1);
                        int testQuant2 = rs2.getInt(2);
                        double testWholesale2 = rs2.getDouble(3);
                        double testSale2 = rs2.getDouble(4);
                        String testSuppID2 = rs2.getString(5);
 //                       System.out.println("Test Value of 'test1234test' search in inventory spits back:\n" +
 //                               testProdID2 + ", " + testQuant2 + ", " + testWholesale2 + ", " + testSale2 + ", " + testSuppID2);
                }

//                System.out.println();

                //illustrates use of readRecords with multiple records result
                ResultSet rs3 = qm.readRecords("inventory", "quantity", "10");
                while (rs3.next()) {
                        String testProdID2 = rs3.getString(1);
                        int testQuant2 = rs3.getInt(2);
                        double testWholesale2 = rs3.getDouble(3);
                        double testSale2 = rs3.getDouble(4);
                        String testSuppID2 = rs3.getString(5);
//                        System.out.println("Extract records with quantity of '10' from inventory:\n" +
//                                testProdID2 + ", " + testQuant2 + ", " + testWholesale2 + ", " + testSale2 + ", " + testSuppID2);
                }

//                System.out.println();

                //illustrates use of readRecords with no matching result in SQL database
                ResultSet rs4 = qm.readRecords("inventory", "product_id", "test5678test");
                while (rs4.next()) {
                        String testProdID2 = rs4.getString(1);
                        int testQuant2 = rs4.getInt(2);
                        double testWholesale2 = rs4.getDouble(3);
                        double testSale2 = rs4.getDouble(4);
                        String testSuppID2 = rs4.getString(5);
//                        System.out.println("Test Value of 'test1234test' search in inventory spits back:\n" +
//                                testProdID2 + ", " + testQuant2 + ", " + testWholesale2 + ", " + testSale2 + ", " + testSuppID2);
                }

                System.out.println("Sample Java-SQL demonstrations completed.");
                sqlDemo = LocalTime.now();
                */


                programEnd = LocalTime.now();
                System.out.println("\nDatabase authentication/connection took " + programStart.until(cqmCreation, SECONDS) + " seconds.");
                System.out.println("Database creation took " + cqmCreation.until(dbCreation, SECONDS) + " seconds.");
                System.out.println("Historical Inventory table creation and initial load took " + dbCreation.until(hiCreation, SECONDS) + " seconds.");
                System.out.println("Batch (.csv) loading took " + hiCreation.until(bLoading, SECONDS) + " seconds.");
                System.out.println("Batch (.csv) processing took " + bLoading.until(bProcessing, SECONDS) + " seconds.");
 //               System.out.println("Chart demonstration took " + bProcessing.until(chartDemo, SECONDS) + " seconds.");
 //               System.out.println("Mail Service demonstration took " + chartDemo.until(mailServiceDemo, SECONDS) + " seconds.");
 //               System.out.println("SQL in Java demonstration took " + mailServiceDemo.until(sqlDemo, SECONDS) + " seconds.");
                System.out.println("Total program execution took " + programStart.until(programEnd, SECONDS) + " seconds.");
        }
}

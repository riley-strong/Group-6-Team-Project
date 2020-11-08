import com.mysql.cj.xdevapi.Schema;

import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {

        public Main() {}

        public static void main(String[] args) throws SQLException, ClassNotFoundException, FileNotFoundException {
                System.out.println("This is the first line of main.");
                final Credentials credentials = new Credentials();
        }

        public void invoke(Credentials credentials,QueryMaker qm) throws FileNotFoundException, SQLException, ClassNotFoundException {
                System.out.println("The QueryMaker object has been created.");
                qm.createDatabaseStructure();
                System.out.println("The basic database structure has been created and inventory has been loaded.");
                qm.batchLoading();
                System.out.println("Batch file loading complete.");
                qm.batchProcessing(); //troubleshooting this right now ///////////////////////////////////////////////
                System.out.println();
                System.out.println();
                System.out.println("Batch processing complete.");
                System.out.println();


                //used in illustrations below - not intended for production code
                qm.generateUpdate("INSERT INTO inventory VALUES (\"test1234test\", 10, 1.01, 2.02, \"testsupp\")");

                System.out.println();

                //illustrates use of readValues
                ResultSet rs = qm.readValues("quantity", "inventory", "product_id = " + qm.valueQueryPrep("test1234test"));
                while (rs.next()) {
                        int testQuant1 = rs.getInt(1);
                        System.out.println("The quantity of Product \"test1234test\" in 'inventory' is " + testQuant1);
                }

                System.out.println();

                //illustrates use of readRecords with one record result
                ResultSet rs2 = qm.readRecords("inventory", "product_id", "test1234test");
                while (rs2.next()) {
                        String testProdID2 = rs2.getString(1);
                        int testQuant2 = rs2.getInt(2);
                        double testWholesale2 = rs2.getDouble(3);
                        double testSale2 = rs2.getDouble(4);
                        String testSuppID2 = rs2.getString(5);
                        System.out.println("Test Value of 'test1234test' search in inventory spits back:\n" +
                                testProdID2 + ", " + testQuant2 + ", " + testWholesale2 + ", " + testSale2 + ", " + testSuppID2);
                }

                System.out.println();

                //illustrates use of readRecords with multiple records result
                ResultSet rs3 = qm.readRecords("inventory", "quantity", "10");
                while (rs3.next()) {
                        String testProdID2 = rs3.getString(1);
                        int testQuant2 = rs3.getInt(2);
                        double testWholesale2 = rs3.getDouble(3);
                        double testSale2 = rs3.getDouble(4);
                        String testSuppID2 = rs3.getString(5);
                        System.out.println("Extract records with quantity of '10' from inventory:\n" +
                                testProdID2 + ", " + testQuant2 + ", " + testWholesale2 + ", " + testSale2 + ", " + testSuppID2);
                }

                System.out.println();

                //illustrates use of readRecords with no matching result in SQL database
                ResultSet rs4 = qm.readRecords("inventory", "product_id", "test5678test");
                while (rs4.next()) {
                        String testProdID2 = rs4.getString(1);
                        int testQuant2 = rs4.getInt(2);
                        double testWholesale2 = rs4.getDouble(3);
                        double testSale2 = rs4.getDouble(4);
                        String testSuppID2 = rs4.getString(5);
                        System.out.println("Test Value of 'test1234test' search in inventory spits back:\n" +
                                testProdID2 + ", " + testQuant2 + ", " + testWholesale2 + ", " + testSale2 + ", " + testSuppID2);
                }

                System.out.println();

        }


}

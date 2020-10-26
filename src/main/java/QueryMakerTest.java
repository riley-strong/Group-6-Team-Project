import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
class QueryMakerTest {
    QueryMaker qm = Credentials.databaseLogin();

    QueryMakerTest() throws SQLException, ClassNotFoundException {
    }
  //  @org.junit.jupiter.api.Test
//    void batchLoading() throws SQLException, FileNotFoundException, ClassNotFoundException {
//
//        //Todo
//        qm.batchLoading();
//
//        Object[][] rows = qm.getProduct("TestCustomertable", "product_id", "P768IEEII2NF");
//
//        assertEquals("2BTACVJEM9CB", rows[0][0].toString());
//
//
//    }
//    @org.junit.jupiter.api.Test
//   void batchProcessing() throws SQLException {
//      qm.batchProcessing();
//   }
  @org.junit.jupiter.api.Test
    void contains() throws SQLException {
        String productID = "GL6NZYQCJO9J";
        String badProductID = "GL6NZYQCJO9k";
        ResultSet rs = qm.generateQuery("SELECT * FROM inventory WHERE product_id = '" + productID + "'");
        assertNotEquals(badProductID, productID);
    }
    @org.junit.jupiter.api.Test
    void createDatabaseStructure() {
    }
    //TODO:
    @org.junit.jupiter.api.Test
    void csvToArray() {
    }
    //TODO:
    @org.junit.jupiter.api.Test
    void deleteRows() throws SQLException, FileNotFoundException, ClassNotFoundException {
        qm.generateUpdate("Drop table if exists Testtable;");
        qm.generateUpdate("CREATE TEMPORARY TABLE Testtable (product_id varchar(16), quantity int(16));");

        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{
                new Object[]{"2BTACVJEM9CB", 2925}
        };
        qm.setTableName("Testtable");
        qm.insertRows(columnHeader, columnValues);
        qm.deleteRows();

        Object[][] rows = qm.getProduct("Testtable", "product_id", "2BTACVJEM9CB");
        assertEquals( rows.length, 0);
    }
    @org.junit.jupiter.api.Test
    void extractResults() {
    }
    @org.junit.jupiter.api.Test
    void generateQuery() {
    }
    //TODO:
    @org.junit.jupiter.api.Test
    void generateUpdate() {
    }
    @org.junit.jupiter.api.Test
    void getColumnNames() throws SQLException {
        Set<String> staticArrayHeaders = Set.of(new String[]
                {"product_id", "quantity", "wholesale_cost", "sale_price", "supplier_id"});
        qm.setTableName("inventory");
        Set<String> queryHeaders = Set.of(qm.getColumnNames());
        assertEquals(staticArrayHeaders, queryHeaders);
    }
    @org.junit.jupiter.api.Test
    void getProduct() throws SQLException {
        qm.generateUpdate("Drop table if exists Testtable;");
        qm.generateUpdate("CREATE TEMPORARY TABLE Testtable (product_id varchar(16), quantity int(16));");


        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{
                new Object[]{"2BTACVJEM9CB", 2925}
        };
        qm.setTableName("Testtable");
        qm.insertRows(columnHeader, columnValues);
        Object[][] rows = qm.getProduct("Testtable", "product_id", "2BTACVJEM9CB");
        assertEquals("2BTACVJEM9CB", rows[0][0].toString());


    }
    @org.junit.jupiter.api.Test
    void insertRows() throws SQLException, FileNotFoundException {

        qm.generateUpdate("Drop table if exists Testtable;");
        qm.generateUpdate("CREATE TEMPORARY TABLE Testtable (product_id varchar(16), quantity int(16));");

        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{
                new Object[]{"2BTACVJEM9CB", 2925}
        };
        qm.setTableName("Testtable");
            qm.insertRows(columnHeader, columnValues);

        Object[][] rows = qm.getProduct("Testtable", "product_id", "2BTACVJEM9CB");

        assertEquals("2BTACVJEM9CB", rows[0][0].toString());


        qm.deleteRows();


    }
    //TODO:
    @org.junit.jupiter.api.Test
    void loadInventory() throws SQLException {
        qm.generateUpdate("Drop table if exists temp_table;");
        qm.generateUpdate("DROP TABLE IF EXISTS other_temp_table;");
        qm.generateUpdate("CREATE TEMPORARY TABLE temp_table (product_id varchar(16), quantity int(16));");
        qm.generateUpdate("CREATE TEMPORARY TABLE other_temp_table(product_id varchar(16), quantity int(16));");
        assertEquals(qm.rowCountResults(qm.generateQuery("SELECT * FROM temp_table")),
                qm.rowCountResults(qm.generateQuery("SELECT * FROM other_temp_table")));
        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{
                new Object[]{"2BTACVJEM9CB", 2925},
                new Object[]{"QNBY0A3WYARJ", 1258}
        };
        qm.setTableName("temp_table");
        qm.insertRows(columnHeader, columnValues);
        assertNotEquals(qm.generateQuery("SELECT * FROM other_temp_table"),
                qm.generateQuery("SELECT * FROM temp_table"));
    }
    @org.junit.jupiter.api.Test
    void rowCountResults() throws SQLException, FileNotFoundException, ClassNotFoundException {
        Scanner sc = new Scanner(new File("inventory_team6.csv"));
        int i = 0;
        while (sc.hasNextLine()) {
            i++;
            sc.nextLine();
        }
        assertEquals(qm.rowCountResults(qm.generateQuery("SELECT * FROM inventory")), i - 1);
    }
    @org.junit.jupiter.api.Test
    void setTableName() {
        String tableName = "inventory";
        qm.setTableName(tableName);
        assertNotEquals(tableName, "");
        assertEquals(tableName, qm.getTableName());
    }
}
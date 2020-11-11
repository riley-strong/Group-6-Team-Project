import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.Scanner;
import java.util.Set;


import static org.junit.jupiter.api.Assertions.*;

class QueryMakerTest {


    private QueryMaker qm;

    QueryMakerTest() throws SQLException, ClassNotFoundException, FileNotFoundException {

        File myObj = new File("sample");
        Scanner myReader = new Scanner(myObj);
        String userName = myReader.nextLine();
        String password = myReader.nextLine();
        String ipAddress = myReader.nextLine();
        String portNumber = myReader.nextLine();
        String databaseName = myReader.nextLine();

        qm = new QueryMaker(userName,password,ipAddress, portNumber, databaseName );
    }

    //todo
    @Test
    void batchLoading() {
    }

    @Test
    void batchProcessing() {
    }
    //todo
    @Test
    void contains() throws SQLException {
        String productID = "GL6NZYQCJO9J";
        String badProductID = "GL6NZYQCJO9k";
        ResultSet rs = qm.generateQuery("SELECT * FROM inventory WHERE product_id = '" + productID + "'");
        assertNotEquals(badProductID, productID);
    }

    @Test
    void createDatabaseStructure() throws FileNotFoundException, SQLException {
        qm.createDatabaseStructure();

    }

    @Test
    void createTable() throws SQLException {
        qm.createTable("abc", "PersonID int, LastName varchar(255), FirstName varchar(255), Address varchar(255), City varchar(255))");
        //boolean test =  qm.generateQuery("select * from abc");
        //assertTrue(test);
        qm.deleteTable("abc");


    }
    //todo
    @Test
    void csvToArray() {
    }

    @Test
    void deleteRecords() throws SQLException {

        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{
                new Object[]{"'2BTACVxEM9CB'", 2925}
        };
        qm.setTableName("inventory");
        qm.insertRows(columnHeader, columnValues);
        qm.deleteRecords("inventory", "product_id", "2BTACVxEM9CB");

        ResultSet resultSet = qm.generateQuery(("select * from inventory where product_id = 2BTACVxEM9CB"));

        assertTrue(!resultSet.next());


    }

    @Test
    void testDeleteRecords1() {
    }


    @Test
    void testDeleteRecords() throws SQLException {

        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{
                new Object[]{"'2BTACVxEM9CB'", 2925}
        };
        qm.setTableName("inventory");
        qm.insertRows(columnHeader, columnValues);
        qm.deleteRecords("inventory", "product_id", "2BTACVxEM9CB");

        ResultSet resultSet =  qm.generateQuery(("select * from inventory where product_id = 2BTACVxEM9CB"));

        assertTrue(!resultSet.next());
    }


    @Test
    void deleteRows() throws SQLException {
        qm.generateUpdate("Drop table if exists Testtable;");
        qm.generateUpdate("CREATE TEMPORARY TABLE Testtable (product_id varchar(16), quantity int(16));");

        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{
                new Object[]{"'2BTACVJEM9CB'", 2925}
        };
        qm.setTableName("Testtable");
        qm.insertRows(columnHeader, columnValues);
        qm.deleteRows();

        Object[][] rows = qm.getProduct("Testtable", "product_id", "2BTACVJEM9CB");
        assertEquals( rows.length, 0);



    }

    @Test
    void deleteTable() throws SQLException {
        qm.createTable("TestTable", "PersonID int, LastName varchar(255), FirstName varchar(255), Address varchar(255), City varchar(255))");

        ResultSet resultSet =  qm.generateQuery("select * from TestTable");
        qm.deleteTable("TestTable");

        assertTrue(!resultSet.next());
    }
    //todo
    @Test
    void deleteTableWithCond() {
    }
    //todo
    @Test
    void extractResults() {
    }
    //todo
    @Test
    void generateQuery() {
    }

    @Test
    void generateUpdate() {

    }

    @Test
    void getColumnNames() throws SQLException {
        Set<String> staticArrayHeaders = Set.of(new String[]
                {"product_id", "quantity", "wholesale_cost", "sale_price", "supplier_id"});
        qm.setTableName("inventory");
        Set<String> queryHeaders = Set.of(qm.getColumnNames());
        assertEquals(staticArrayHeaders, queryHeaders);

    }

    @Test
    void getProduct() throws SQLException {
        qm.generateUpdate("Drop table if exists Testtable;");
        qm.generateUpdate("CREATE TEMPORARY TABLE Testtable (product_id varchar(16), quantity int(16));");


        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{
                new Object[]{"'2BTACVJEM9CB'", 2925}
        };
        qm.setTableName("Testtable");
        qm.insertRows(columnHeader, columnValues);
        Object[][] rows = qm.getProduct("Testtable", "product_id", "2BTACVJEM9CB");
        assertEquals("2BTACVJEM9CB", rows[0][0].toString());


    }



    @Test
    void getTableName() {
        qm.setTableName("t2");

        String tableName = qm.getTableName();
        assertEquals(tableName, "t2");

    }

    @Test
    void insertRows() throws FileNotFoundException, SQLException {
        qm.generateUpdate("DELETE FROM inventory WHERE product_id='2BTCEM9CB'");

        Object[][] objArr = qm.csvToArray("inventory_team6.csv", new int[]{0,1, 2,2,0});

        Object[][] columnValues = new Object[][]{
                new Object[]{"'2BTCEM9CB'", 2925, 32.2, 365.2,"'supplier'"},
        };
        qm.setTableName("inventory");
        qm.insertRows(new String[]{"product_id","quantity","wholesale_cost","sale_price","supplier_id"},columnValues);

        Object[][] rows = qm.getProduct("inventory", "product_id", "2BTCEM9CB");

        assertEquals("2BTCEM9CB", rows[0][0].toString());
        qm.generateUpdate("DELETE FROM  inventory WHERE product_id='2BTCEM9CB'");

    }



    @Test
    void insertRecordIntoTable() {

    }

    @Test
    void insertValuesIntoTable() {
    }

    @Test
    void readRecords() throws SQLException {
        ResultSet rs =  qm.readRecords("inventory","product_id","GL6NZYQCJO9J");
        if(rs.next()){
            assertEquals("GL6NZYQCJO9J", rs.getString("product_id"));
        }
    }

    @Test
    void readTable() throws SQLException {

        qm.generateUpdate("Drop table if exists Testtable;");
        qm.generateUpdate("CREATE TEMPORARY TABLE Testtable (product_id varchar(16), quantity int(16));");


        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{
                new Object[]{"'2BTACVJEM9CB'", 2925},
                new Object[]{"'2BTKCVJEM9CB'", 2925},
                new Object[]{"'2BTACVJEJ9CB'", 2925}
        };
        qm.setTableName("Testtable");
        qm.insertRows(columnHeader, columnValues);
        ResultSet rs = qm.readTable("Testtable");
        int count = 0;
        while (rs.next()){

            count++;
        }
        assertEquals(3, count);
    }
    //todo
    @Test
    void readTableWithCond() {
    }
    //todo
    @Test
    void readValues() {
    }
    //todo
    @Test
    void rowCountResults() {
    }

    @Test
    void setTableName() {

        String tableName = "inventory";
        qm.setTableName(tableName);
        assertNotEquals(tableName, "");
        assertEquals(tableName, qm.getTableName());
    }

    //todo
    @Test
    void topTenCustomers() {
    }

    @Test
    void updateTableFromTable() throws SQLException {
        qm.generateUpdate("Drop table if exists Testtable1;");
        qm.generateUpdate("CREATE TEMPORARY TABLE Testtable1 (product_id varchar(16), quantity int(16));");
        qm.generateUpdate("Drop table if exists Testtable2;");
        qm.generateUpdate("CREATE TEMPORARY TABLE Testtable2 (product_id varchar(16), quantity int(16));");

        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{
                new Object[]{"'2BTACVJEM9CB'", 2925},
                new Object[]{"'2BTKCVJEM9CB'", 2925},
                new Object[]{"'2BTACVJEJ9CB'", 2925}
        };
        qm.setTableName("Testtable1");
        qm.insertRows(columnHeader, columnValues);

        qm.setTableName("Testtable2");
        qm.insertRows(columnHeader, columnValues);


        qm.updateTableFromTable("Testtable2", "Testtable1", "quantity",
                "quantity", "product_id", "product_id");

        ResultSet rs = qm.readTable("Testtable2");

        int count = 0;
        while (rs.next()){

            count++;
        }

    }
    //todo
    @Test
    void updateTableFromStatic() {
    }

    @Test
    void valueExists() throws SQLException {
        qm.generateUpdate("Drop table if exists Testtable;");
        qm.generateUpdate("CREATE TEMPORARY TABLE Testtable (product_id varchar(16), quantity int(16));");


        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{
                new Object[]{"'2BTACVJEM9CB'", 2925},
                new Object[]{"'2BTKCVJEM9CB'", 2925},
                new Object[]{"'2BTACVJEJ9CB'", 2925}
        };
        qm.setTableName("Testtable");
        qm.insertRows(columnHeader, columnValues);
        assertTrue(qm.valueExists("quantity","Testtable",2925));

    }

    @Test
    void testValueExists() {
    }

    @Test
    void testValueExists1() {
    }

    @Test
    void valueQueryPrep() {
    }

    @Test
    void testValueQueryPrep() {
    }

    @Test
    void testValueQueryPrep1() {
    }

    @Test
    void testValueQueryPrep2() {
    }

    @Test
    void testValueQueryPrep3() {
    }

    @Test
    void testValueQueryPrep4() {
    }

    @Test
    void main() {
    }
}


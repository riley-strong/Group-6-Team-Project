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
//reading the credentials  from text file
        File myObj = new File("sample");
        Scanner myReader = new Scanner(myObj);
        String userName = myReader.nextLine();
        String password = myReader.nextLine();
        String ipAddress = myReader.nextLine();
        String portNumber = myReader.nextLine();
        String databaseName = myReader.nextLine();

        qm = new QueryMaker(userName,password,ipAddress, portNumber, databaseName );
    }



    @Test
    void createDatabaseStructure() throws FileNotFoundException, SQLException {
      qm.createDatabaseStructure("inventory_team6.csv");
        ResultSet rs = qm.generateQuery("SELECT * FROM temp_inventory");
        assertTrue(rs.next());

    }
//creates a table and and check if the table existed
    @Test
    void createTable() throws SQLException {
        qm.createTable("temp", "date date, cust_email VARCHAR(320), product_id VARCHAR(12), Total_Purchase DECIMAL (64, 2)");
        try{
            ResultSet rs = qm.generateQuery("SELECT * FROM temp");

        }
        catch (Exception e){

            assertTrue(false);
        }
        qm.deleteTable("temp");


    }

    /**
     * read csv file
     * insert it in to array
     * check if the csv file matches what is in the array
     * @throws FileNotFoundException
     */
    @Test
    void csvToArray() throws FileNotFoundException {
        Scanner fileReader = new Scanner(new File("inventory_team6.csv"));
        fileReader.nextLine();
        Object[] objArr1 = fileReader.nextLine().split(",");

        Object[][] objArr = qm.csvToArray("inventory_team6.csv", new int[]{3, 0, 0, 0, 0});

        Object IdFromTestMothod = objArr[0][0];
        Object IdFromFile = "'" +objArr1[0]+"'";
        if(IdFromFile.equals(IdFromTestMothod)){
            assertTrue(true);
        }
        else {
            assertTrue(false);
        }
    }

    @Test
    void deleteRecords() throws SQLException {

        qm.generateUpdate("Drop table if exists Testtable;");
        qm.generateUpdate("CREATE TEMPORARY TABLE Testtable (product_id varchar(16), quantity int(16));");

        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{
                new Object[]{"'2BTACVJEM9CB'", 2925}
        };
        qm.setTableName("Testtable");
        qm.insertRows(columnHeader, columnValues);
        qm.deleteRecords("Testtable", "quantity", 2925);

        ResultSet resultSet = qm.generateQuery(("select * from Testtable where product_id = 2BTACVxEM9CB"));

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
    void extractResults() throws SQLException {

        qm.generateUpdate("Drop table if exists Testtable;");
        qm.generateUpdate("CREATE TEMPORARY TABLE Testtable (product_id varchar(16), quantity int(16));");

        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{
                new Object[]{"'2BTACVJEM9CB'", 2925}
        };
        qm.setTableName("Testtable");
        qm.insertRows(columnHeader, columnValues);
        ResultSet rs =  qm.generateQuery("select * from Testtable");

        Object[][] objArr = qm.extractResults(rs, false);


       if(objArr.length >0){
            assertTrue(true);
        }
        else {
            assertTrue(false);
        }

        qm.deleteTable("Testtable");
    }
    //todo
    @Test
    void generateQuery() throws SQLException {
        qm.generateUpdate("Drop table if exists Testtable;");
        qm.generateUpdate("CREATE TEMPORARY TABLE Testtable (product_id varchar(16), quantity int(16));");

        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{
                new Object[]{"'2BTACVJEM9CB'", 2925}
        };
        qm.setTableName("Testtable");
        qm.insertRows(columnHeader, columnValues);

        ResultSet resultSet =  qm.generateQuery("select * from Testtable");
        if(resultSet.next()){
            assertTrue(true);
        }
        else {
            assertTrue(false);
        }

        qm.deleteTable("Testtable");



    }

    @Test
    void generateUpdate() throws SQLException {

    }

    @Test
    void getColumnNames() throws SQLException {
        qm.generateUpdate("Drop table if exists Testtable;");
        qm.generateUpdate("CREATE TABLE Testtable (product_id varchar(16), quantity int(16));");

        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{
                new Object[]{"'2BTACVJEM9CB'", 2925}
        };
        qm.setTableName("temp_inventory");
        qm.insertRows(columnHeader, columnValues);

        String[] queryHeaders = qm.getColumnNames();
        assertEquals(columnHeader, queryHeaders);

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


    /**
     * set a table name t2
     * run getTableName and compare it with actual table name
     */
    @Test
    void getTableName() {
        qm.setTableName("t2");

        String tableName = qm.getTableName();
        assertEquals(tableName, "t2");

    }

    /**
     * create a tamp table
     * add product in to table
     * check if the product exist in the table
     * @throws FileNotFoundException
     * @throws SQLException
     */
    @Test
    void insertRows() throws FileNotFoundException, SQLException {
        qm.generateUpdate("Drop table if exists Testtable;");
        qm.generateUpdate("CREATE TEMPORARY TABLE Testtable (product_id varchar(16), quantity int(16));");


        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{
                new Object[]{"'2BTACVJEM9CB'", 2925}
        };
        qm.setTableName("Testtable");
        qm.insertRows(columnHeader, columnValues);

        ResultSet resultSet =  qm.generateQuery("select * from Testtable");
        if(resultSet.next()){
            assertTrue(true);
        }
        else {
            assertTrue(false);
        }



    }

    /**
     * create  a tamp table
     * add a product with id and quantity
     * check if the product exist in the table and matches expected product by id by running readRecords
     * @throws SQLException
     */


    @Test
    void readRecords() throws SQLException {

        qm.generateUpdate("Drop table if exists Testtable;");
        qm.generateUpdate("CREATE TEMPORARY TABLE Testtable (product_id varchar(16), quantity int(16));");


        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{
                new Object[]{"'2BTACVJEM9CB'", 2925}
        };
        qm.setTableName("Testtable");
        qm.insertRows(columnHeader, columnValues);
        ResultSet rs =  qm.readRecords("Testtable","product_id","2BTACVJEM9CB");
        if(rs.next()){
            assertEquals("2BTACVJEM9CB", rs.getString("product_id"));
        }
    }
/**
 * drop a table if it exist
 * creat a temp table Testtable
 * add product with id and quantity
 * check if rs matches the actual number od products add into the table
 */
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

/**
 * drop a table if it exist
 * creat a temp table Testtable
 * add product with id and quantity
 * check if rowCoutResult matches the actual number od products add into the table
 */
    @Test
    void rowCountResults() throws SQLException {
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
        ResultSet rs = qm.generateQuery("select * from Testtable");

       assertEquals(qm.rowCountResults(rs), 3);
    }

    /**
     * assign a table name
     * run setTableName, and check if the setTableName matches the assigned table
     */
    @Test
    void setTableName() {

        String tableName = "inventory";
        qm.setTableName(tableName);
        assertNotEquals(tableName, "");
        assertEquals(tableName, qm.getTableName());
    }

    /**
     * creat temporary tables Testabletable1, and 2
     * load a product into table1
     * run updateTableFromTable and check if that trsfared product existed in table2
     * @throws SQLException
     */

    @Test
    void updateTableFromTable() throws SQLException {
        qm.generateUpdate("Drop table if exists Testtable1;");
        qm.generateUpdate("CREATE TEMPORARY TABLE Testtable1 (product_id varchar(16), quantity int(16));");
        qm.generateUpdate("Drop table if exists Testtable2;");
        qm.generateUpdate("CREATE TEMPORARY TABLE Testtable2 (product_id varchar(16), quantity int(16));");

        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{
                new Object[]{"'2BTACVJEM9CB'", 2925},
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

    /**
     * creat a tamp table
     * add a product with id and quantity
     * run valueExist and check if the product existed in the new table
     * @throws SQLException
     */

    @Test
    void valueExists() throws SQLException {
        qm.generateUpdate("Drop table if exists Testtable;");
        qm.generateUpdate("CREATE TEMPORARY TABLE Testtable (product_id varchar(16), quantity int(16));");


        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{
                new Object[]{"'2BTACVJEM9CB'", 2925},
        };
        qm.setTableName("Testtable");
        qm.insertRows(columnHeader, columnValues);
        assertTrue(qm.valueExists("quantity","Testtable",2925));

    }
}


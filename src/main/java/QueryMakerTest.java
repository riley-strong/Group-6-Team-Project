import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class QueryMakerTest {


    private final QueryMaker qm;

    QueryMakerTest() throws SQLException, ClassNotFoundException, FileNotFoundException {
//reading the credentials  from text file
        File myObj = new File("sample");
        Scanner myReader = new Scanner(myObj);
        String userName = myReader.nextLine();
        String password = myReader.nextLine();
        String ipAddress = myReader.nextLine();
        String portNumber = myReader.nextLine();
        String databaseName = myReader.nextLine();

        qm = new QueryMaker(userName, password, ipAddress, portNumber, databaseName);
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
        try {
            ResultSet rs = qm.generateQuery("SELECT * FROM temp");
        } catch (Exception e) {
            assertTrue(false);
        }
        qm.deleteTable("temp");


    }

    /**
     * read csv file
     * insert it in to array
     * check if the csv file matches what is in the array
     *
     * @throws FileNotFoundException
     */
    @Test
    void csvToArray() throws FileNotFoundException {
        Scanner fileReader = new Scanner(new File("inventory_team6.csv"));
        fileReader.nextLine();
        Object[] objArr1 = fileReader.nextLine().split(",");

        Object[][] objArr = qm.csvToArray("inventory_team6.csv", new int[]{3, 0, 0, 0, 0});

        Object IdFromTestMothod = objArr[0][0];
        Object IdFromFile = "'" + objArr1[0] + "'";
        assertTrue(IdFromFile.equals(IdFromTestMothod));
    }

    @Test
    void deleteRecords() throws SQLException {

        qm.createTable("Testtable", "product_id varchar(16), quantity int");

        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{new Object[]{"'2BTACVJEM9CB'", 2925}};
        qm.setTableName("Testtable");
        qm.insertRows(columnHeader, columnValues);
        qm.deleteRecords("Testtable", "quantity", 2925);

        ResultSet resultSet = qm.generateQuery("select * from Testtable where product_id = '2BTACVJEM9CB'");

        assertTrue(resultSet.getFetchSize() == 0);
        qm.deleteTable("Testtable");

    }


    @Test
    void deleteRows() throws SQLException {
        qm.createTable("Testtable", "product_id varchar(16), quantity int");

        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{new Object[]{"'2BTACVJEM9CB'", 2925}};
        qm.setTableName("Testtable");
        qm.insertRows(columnHeader, columnValues);
        qm.deleteRows();

        Object[][] rows = qm.getProduct("Testtable", "product_id", "2BTACVJEM9CB");
        assertEquals(rows.length, 0);
        qm.deleteTable("Testtable");


    }

    @Test
    void deleteTable() throws SQLException {
        qm.createTable("TestTable", "PersonID int, LastName varchar(255), FirstName varchar(255), Address varchar(255), City varchar(255)");

        qm.deleteTable("TestTable");
        ResultSet resultSet = qm.generateQuery("show tables like 'Testtable'");


        assertTrue(resultSet.getFetchSize() == 0);
    }

    @Test
    void extractResults() throws SQLException {

        qm.generateUpdate("Drop table if exists Testtable;");
        qm.generateUpdate("CREATE TEMPORARY TABLE Testtable (product_id varchar(16), quantity int(16));");

        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{new Object[]{"'2BTACVJEM9CB'", 2925}};
        qm.setTableName("Testtable");
        qm.insertRows(columnHeader, columnValues);
        ResultSet rs = qm.generateQuery("select * from Testtable");

        Object[][] objArr = qm.extractResults(rs, false);


        assertTrue(objArr.length > 0);

        qm.deleteTable("Testtable");
    }

    @Test
    void generateQuery() throws SQLException {
        qm.generateUpdate("Drop table if exists Testtable;");
        qm.generateUpdate("CREATE TEMPORARY TABLE Testtable (product_id varchar(16), quantity int(16));");

        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{new Object[]{"'2BTACVJEM9CB'", 2925}};
        qm.setTableName("Testtable");
        qm.insertRows(columnHeader, columnValues);

        ResultSet resultSet = qm.generateQuery("select * from Testtable");
        assertTrue(resultSet.next());

        qm.deleteTable("Testtable");
    }

    @Test
    void generateUpdate() throws SQLException {

    }

    @Test
    void getColumnNames() throws SQLException {
        qm.createTable("Testtable", "product_id VARCHAR(12), quantity INT, wholesale_cost DECIMAL(12,2),sale_price DECIMAL(12,2),supplier_id VARCHAR(8)");

        String[] columnHeader = new String[]{"product_id", "quantity", "wholesale_cost", "sale_price", "supplier_id"};

        qm.setTableName("Testtable");
        String[] queryHeaders = qm.getColumnNames();
        assertTrue(Arrays.deepEquals(columnHeader, queryHeaders));
        qm.deleteTable("Testtable");

    }

    @Test
    void getProduct() throws SQLException {
        qm.createTable("Testtable", "product_id varchar(16), quantity int");


        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{
                new Object[]{"'2BTACVJEM9CB'", 2925}
        };
        qm.setTableName("Testtable");
        qm.insertRows(columnHeader, columnValues);
        Object[][] rows = qm.getProduct("Testtable", "product_id", "2BTACVJEM9CB");
        assertEquals("2BTACVJEM9CB", rows[0][0].toString());
        qm.deleteTable("Testtable");


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
     *
     * @throws FileNotFoundException
     * @throws SQLException
     */
    @Test
    void insertRows() throws FileNotFoundException, SQLException {
        qm.createTable("Testtable", "product_id varchar(16), quantity int");

        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{
                new Object[]{"'2BTACVJEM9CB'", 2925}
        };
        qm.setTableName("Testtable");
        qm.insertRows(columnHeader, columnValues);

        ResultSet resultSet = qm.generateQuery("select COUNT(*) from Testtable");
        resultSet.next();
        int size = resultSet.getInt(1);
        assertTrue(size == 1);
        qm.deleteTable("Testtable");


    }

    /**
     * create  a tamp table
     * add a product with id and quantity
     * check if the product exist in the table and matches expected product by id by running readRecords
     *
     * @throws SQLException
     */


    @Test
    void readRecords() throws SQLException {

        qm.createTable("Testtable", "product_id varchar(16), quantity int");

        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{
                new Object[]{"'2BTACVJEM9CB'", 2925}
        };
        qm.setTableName("Testtable");
        qm.insertRows(columnHeader, columnValues);
        ResultSet rs = qm.readRecords("Testtable", "product_id", "2BTACVJEM9CB");
        if (rs.next()) {
            assertEquals("2BTACVJEM9CB", rs.getString("product_id"));
        }
        qm.deleteTable("Testtable");
    }

    /**
     * drop a table if it exist
     * creat a temp table Testtable
     * add product with id and quantity
     * check if rs matches the actual number od products add into the table
     */
    @Test
    void readTable() throws SQLException {

        qm.createTable("Testtable", "product_id varchar(16), quantity int");

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
        while (rs.next()) {

            count++;
        }
        assertEquals(3, count);
        qm.deleteTable("Testtable");
    }

    /**
     * drop a table if it exist
     * creat a temp table Testtable
     * add product with id and quantity
     * check if rowCoutResult matches the actual number od products add into the table
     */
    @Test
    void rowCountResults() throws SQLException {
        qm.createTable("Testtable", "product_id varchar(16), quantity int");


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
        qm.deleteTable("Testtable");
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
     *
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
        while (rs.next()) {

            count++;
        }

    }

    /**
     * creat a tamp table
     * add a product with id and quantity
     * run valueExist and check if the product existed in the new table
     *
     * @throws SQLException
     */

    @Test
    void valueExists() throws SQLException {
        qm.createTable("Testtable", "product_id varchar(16), quantity int");


        String[] columnHeader = new String[]{"product_id", "quantity"};
        Object[][] columnValues = new Object[][]{
                new Object[]{"'2BTACVJEM9CB'", 2925},
        };
        qm.setTableName("Testtable");
        qm.insertRows(columnHeader, columnValues);
        assertTrue(qm.valueExists("quantity", "Testtable", 2925));
        qm.deleteTable("Testtable");

    }
}


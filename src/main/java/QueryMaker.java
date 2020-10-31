import javax.swing.text.DateFormatter;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;


public class QueryMaker {

    public static int DATE = 3;
    public static int DOUBLE = 2;
    public static int INT = 1;
    public static int STRING = 0;
    private static Connection connection;
    private static Statement statement;
    private PreparedStatement preparedStatement;
    private String tableName;


    /**
     * Creates QueryMaker object that is used to interact with MySQL Database
     *
     * @param userName     MySQL userName
     * @param password     MySQL password
     * @param ipAddress    MySQL IP Address
     * @param portNumber   SQL portNumber
     * @param databaseName MySQL database name
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public QueryMaker(String userName, String password, String ipAddress, String portNumber, String databaseName) throws ClassNotFoundException, SQLException {

        Class.forName("com.mysql.cj.jdbc.Driver");
        String getURL = "jdbc:mysql://" + ipAddress + ":" + portNumber + "/" + databaseName;
        connection = DriverManager.getConnection(getURL, userName, password);
        statement = connection.createStatement();
        //System.out.println("Connection Succesful");
    }

    public void batchLoading() throws SQLException, FileNotFoundException, ClassNotFoundException {
        // Step 1: Load SQL unprocessed_sales table with Java 2-D Array.
        Object[][] objArr = this.csvToArray("customer_orders_A_team6.csv", new int[]{DATE, STRING, STRING, STRING, INT});
        this.setTableName("unprocessed_sales");
        this.insertRows(new String[]{"date", "cust_email", "cust_location", "product_id", "product_quantity"}, objArr);

        // Step 2: Add column to unprocessed_sales table for hashed email.
        statement.executeUpdate("ALTER TABLE unprocessed_sales ADD COLUMN hashed_email VARBINARY(32)");

        // Step 3: Load hash table with emails from unprocessed orders and generate hashed emails.
        statement.executeUpdate("INSERT INTO hash_ref (hashed_email, unhashed_email) " +
                "SELECT MD5(cust_email), cust_email FROM unprocessed_sales");

        // Step 4: Fill with hashed emails from hash table.
        this.updateTableFromTable("unprocessed_sales", "hash_ref", "hashed_email",
                "hashed_email", "cust_email", "unhashed_email");

        // Step 5: Delete (unhashed) email column from unprocessed orders.
        statement.executeUpdate("ALTER TABLE unprocessed_sales DROP COLUMN cust_email");

    }

    public void batchProcessing() throws SQLException {
        // Step 1: Add tempoorary email column to Processed Sales table.
        statement.executeUpdate("ALTER TABLE processed_sales ADD COLUMN unhashed_email VARCHAR(320)");

        //Part a: Load in unprocessed sales into Java data structure.
        ResultSet us = statement.executeQuery("SELECT DISTINCT date, us.cust_location, us.product_id, us.product_quantity, us.hashed_email, hr.unhashed_email " +
                "FROM unprocessed_sales AS us " +
                "LEFT JOIN hash_ref AS hr ON hr.hashed_email = us.hashed_email " +
                "ORDER BY date, hr.unhashed_email");

        ResultSet inv;
        String us_date;
        String us_loc;
        String us_p_id;
        int us_quant;
        Blob us_he;
        String us_uhe;
        LocalDateTime dt;
        int inv_quant;
        String ps_values;


        //Part b: Iterate through unprocessed_sales, comparing with inventory & writing to processed_sales
        while (us.next()) {
            us_date = us.getString(1);
            us_loc = us.getString(2);
            us_p_id = us.getString(3);
            us_quant = us.getInt(4);
            us_he = us.getBlob(5);
            us_uhe = us.getString(6);

            dt = LocalDateTime.now();

            System.out.println(us_date + ", " + us_loc + ", " + us_p_id + ", " + "" + us_quant + ", " + us_he + ", " + us_uhe + ", " + valueQueryPrep(dt));

            inv = this.readValues("quantity", "inventory", "product_id = " + valueQueryPrep(us_p_id));
            inv.next();
            inv_quant = inv.getInt(1);

            if (inv_quant >= us_quant) {
                this.updateTableFromStatic("inventory", "unprocessed_sales", "quantity",
                        "" + (inv_quant - us_quant), "product_id", us_p_id);
                ps_values = "" + valueQueryPrep(us_date) + ", " + valueQueryPrep(dt) + ", " + valueQueryPrep(us_uhe) + ", " +
                        valueQueryPrep(us_loc) + ", " + valueQueryPrep(us_p_id) + ", " + valueQueryPrep("" + us_quant) + ", " + 1;
                System.out.println("Successful order for " + us_uhe);
            }
            else {
                ps_values = "" + valueQueryPrep(us_date) + ", " + valueQueryPrep(dt) + ", " + valueQueryPrep(us_uhe) + ", " +
                        valueQueryPrep(us_loc) + ", " + valueQueryPrep(us_p_id) + ", " + valueQueryPrep("" + us_quant) + ", " + 0;
                System.out.println("Unsuccessful order for " + us_uhe);
            }
            insertValuesIntoTable("processed_sales",
                    "date, processed_datetime, unhashed_email, cust_location, product_id, product_quantity, result",ps_values);
            System.out.println("processed_sales table in SQL database updated.");
        }

        this.updateTableFromTable("processed_sales", "hash_ref", "hashed_email",
                "hashed_email", "unhashed_email", "unhashed_email");

        statement.executeUpdate("ALTER TABLE processed_sales DROP COLUMN unhashed_email");
    }



    /**
     * @param productID
     * @return
     * @throws SQLException
     */
    public boolean contains(String productID) throws SQLException {
        ResultSet rs = generateQuery("SELECT * FROM inventory WHERE product_id = '" + productID + "'");
        return rs.next();
    }

    // This method takes in a .csv file and turns it into a 2D array with the specified column types.
    // Making the format compatible for SQL to read

    public void createDatabaseStructure() throws SQLException, FileNotFoundException {
        // Step 1: Create Inventory Table in SQL Database.
        createTable("inventory",
                "product_id VARCHAR(12),quantity INT,wholesale_cost DECIMAL(12,2),sale_price DECIMAL(12,2),supplier_id VARCHAR(8))");

        // Step 2: Load SQL Inventory table with Java 2-D Array.
        Object[][] objArr = this.csvToArray("inventory_team6.csv", new int[]{STRING, INT, DOUBLE, DOUBLE, STRING});
        this.setTableName("inventory");
        this.insertRows(new String[]{"product_id", "quantity", "wholesale_cost", "sale_price", "supplier_id"}, objArr);

        // Step 3: Create unprocessed_sales Table in SQL Database.
        createTable("unprocessed_sales",
                "date DATE,cust_email VARCHAR(320),cust_location VARCHAR(5),product_id VARCHAR(12),product_quantity int)");

        // Step 4: Create hash_ref Table in SQL Database.
        createTable("hash_ref",
                "hashed_email VARBINARY(32),unhashed_email VARCHAR(320))");

        // Step 5: Create processed_sales Table in SQL Database.
        createTable("processed_sales",
                "date DATE,processed_datetime DATETIME,hashed_email VARBINARY(32) NULL,cust_location VARCHAR(5),product_id VARCHAR(12),product_quantity int,result bit)");

    }

    public void createTable(String tableName, String columnSpecs) throws SQLException {
        this.deleteTable(tableName);
        statement.executeUpdate("CREATE TABLE " + tableName + " (" + columnSpecs);
    }

    /**
     * @param fileName
     * @param types
     * @return
     * @throws FileNotFoundException
     */
    public Object[][] csvToArray(String fileName, int[] types) throws FileNotFoundException {
        Scanner fileReader = new Scanner(new File(fileName));
        String[] headers = fileReader.nextLine().split(",");
        LinkedList<String[]> arrays = new LinkedList<>();

        for (int j = 0; fileReader.hasNextLine(); j++) {
            arrays.add(fileReader.nextLine().split(","));

        }
        Object[][] objects = new Object[arrays.size()][headers.length];
        for (int j = 0; j < objects.length; j++) {
            String[] element = arrays.remove();
            for (int i = 0; i < headers.length; i++) {

                objects[j][i] = types[i] == STRING || types[i] == DATE ?
                        "'" + element[i] + "'"
                        : types[i] == INT ?
                        Integer.parseInt(element[i])
                        : Double.parseDouble(element[i]);
            }
        }
        return objects;
    }

    public void deleteRecords(String tableName, String columnName, double value) throws SQLException {
        statement.executeUpdate("DELETE FROM " + tableName + "WHERE " + columnName + " = " + valueQueryPrep(value));
    }

    public void deleteRecords(String tableName, String columnName, int value) throws SQLException {
        statement.executeUpdate("DELETE FROM " + tableName + "WHERE " + columnName + " = " + valueQueryPrep(value));
    }

    public void deleteRecords(String tableName, String columnName, String value) throws SQLException {
        statement.executeUpdate("DELETE FROM " + tableName + "WHERE " + columnName + " = " + valueQueryPrep(value));
    }

    // deletes all rows in a table
    /**
     * @throws SQLException
     */
    public void deleteRows() throws SQLException {
        generateUpdate("DELETE FROM " + tableName);
    }

    public void deleteTable(String tableName) throws SQLException {
        statement.executeUpdate("DROP TABLE IF EXISTS " + tableName);
    }

    public void deleteTableWithCond(String tableName, String condition) throws SQLException {
        statement.executeUpdate("DROP TABLE IF EXISTS " + tableName + " " + condition);
    }

    // once setTableName() has been called then displayFile will print the table the to console

    /**
     * @throws SQLException
     */
    private void displayFile() throws SQLException {
        ResultSet rs = generateQuery("SELECT * FROM " + tableName);
        while (rs.next()) {
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                System.out.print(rs.getObject(i + 1));
                if (i < rs.getMetaData().getColumnCount() - 1) {
                    System.out.print(",");
                }
            }
            System.out.println();
        }
    }

    // helper method for getproduct() method

    /**
     * @param rs
     * @param isOneColumn
     * @return
     * @throws SQLException
     */
    public Object[][] extractResults(ResultSet rs, Boolean isOneColumn) throws SQLException {
        ArrayList<Object[]> temp = new ArrayList<>();
        int columnCount = rs.getMetaData().getColumnCount();
        for (int j = 0; rs.next(); j++) {
            Object[] objects = new Object[columnCount];
            for (int i = 0; i < columnCount; i++) {
                objects[i] = rs.getObject(i + 1);
            }
            temp.add(objects);
        }
        Object[][] data = new Object[temp.size()][columnCount];
        Iterator<Object[]> it = temp.iterator();
        for (int i = 0; i < data.length; i++) {
            data[i] = it.next();
        }
        return data;
    }

    /**
     * @param s
     * @return
     * @throws SQLException
     */
    public ResultSet generateQuery(String s) throws SQLException {
        Statement st = connection.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        return st.executeQuery(s);

    }

    /**
     * @param s
     * @throws SQLException
     */
    public void generateUpdate(String s) throws SQLException {
        Statement st = connection.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        st.execute(s);
    }

    /**
     * Gets an array of the column names of a specific table
     */
    /**
     * @return
     * @throws SQLException
     */
    public String[] getColumnNames() throws SQLException {
        ResultSet rs = generateQuery("SELECT `COLUMN_NAME` FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE `TABLE_SCHEMA`='TEAM_6' AND `TABLE_NAME`='" + this.tableName + "'");
        String[] columnNames = new String[rowCountResults(rs)];
        for (int j = 0; rs.next(); j++) {
            columnNames[j] = rs.getString(1);
        }
        return columnNames;
    }

    // returns all rows that match a specified column name to a specified column value

    /**
     * @param tableName
     * @param columnName
     * @param columnValue
     * @return
     * @throws SQLException
     */
    public Object[][] getProduct(final String tableName, final String columnName, Object columnValue) throws SQLException {
        ResultSet rs = generateQuery("SELECT * FROM " + tableName + " WHERE " + columnName + " = " + quoteWrap(columnValue));
        String s = "select count(*) from " + tableName + " where " + columnName + " = " + getString(columnValue);
        ResultSet resultCount = generateQuery(s);
        return extractResults(rs, true);
    }

    /**
     * @param columnValue
     * @return
     */
    private String getString(Object columnValue) {
        if (columnValue instanceof String || columnValue instanceof LocalDate) {
            return "'" + columnValue + "'";
        }
        return columnValue.toString();
    }


    public String getTableName() {
        return tableName;
    }

    // inserts rows into the specified table
    /**
     * @param columnNames
     * @param rows
     * @throws SQLException
     */
    public void insertRows(String[] columnNames, Object[][] rows) throws SQLException {

        StringBuilder builder = new StringBuilder();
        String s = Arrays.toString(columnNames);
        builder.append("INSERT INTO " + tableName + " (" + s.substring(1, s.length() - 1) + ")VALUES");
        for (int i = 0; i < rows.length; i++) {

            String s1 = Arrays.deepToString(rows[i]);
            builder.append(" (" + s1.substring(1, s1.length() - 1) + ")");
            if (i < rows.length - 1) {
                builder.append(",");
            }
        }
        generateUpdate(builder.toString());
    }

    public void insertRecordIntoTable(String tableName, String values) throws SQLException {
        statement.executeUpdate("INSERT INTO " + tableName + " VALUES ( " + values + " ) ");
    }

    public void insertValuesIntoTable(String tableName, String columnNames, String values) throws SQLException {
        statement.executeUpdate("INSERT INTO " + tableName + " ( " + columnNames + " ) VALUES ( " + values + " ) ");
    }
    /**
     * @param columnValue
     * @return
     */
    private String quoteWrap(Object columnValue) {
        if (columnValue instanceof String || columnValue instanceof LocalDate) {
            return "'" + columnValue + "'";
        } else {
            return columnValue + "";
        }
    }

    //use when needing (up to) all columns from result
    public ResultSet readRecords(String tableName, String whereClause, String value) throws SQLException {
        value = this.valueQueryPrep(value);
        String query = "SELECT * FROM " + tableName + " WHERE " + whereClause + " = " + value;
        preparedStatement = connection.prepareStatement(query);
        ResultSet rs = preparedStatement.executeQuery();
        return rs;
    }

    public ResultSet readTable(String tableName) throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName);
        return rs;

    }

    public ResultSet readTableWithCond(String tableName, String condition) throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName + " " + condition);
        return rs;
    }

    //use when assuming needing all matches for one column
    public ResultSet readValues(String columnName, String tableName, String whereClause) throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT " + columnName + " FROM " + tableName + " WHERE " + whereClause);
        return rs;
    }

    /**
     * retrieve the number of rows of in a ResultSet.
     */
    /**
     * @param rs
     * @return
     * @throws SQLException
     */
    int rowCountResults(ResultSet rs) throws SQLException {
        rs.last();
        int countRows = rs.getRow();
        rs.beforeFirst();
        return countRows;
    }




    //ALWAYS declare the table name before proceeding with anything else!!!

    /**
     * @param tableName
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void updateTableFromTable(String tableName1, String tableName2, String setColumnNameT1, String setColumnNameT2, String whereColumnNameT1, String whereColumnNameT2) throws SQLException {
        statement.executeUpdate("UPDATE " + tableName1 + ", " + tableName2 + " " +
                "SET " + tableName1 + "." + setColumnNameT1 + " = " + tableName2 + "." + setColumnNameT2 + " " +
                "WHERE " + tableName1 + "." + whereColumnNameT1 + " = " + tableName2 + "." + whereColumnNameT2);
    }

    public void updateTableFromStatic(String tableName1, String tableName2, String setColumnNameT1, String value, String whereColumnNameT1, String condition) throws SQLException {
        statement.executeUpdate("UPDATE " + tableName1 + ", " + tableName2 + " " +
                "SET " + tableName1 + "." + setColumnNameT1 + " = " + valueQueryPrep(value) + " " +
                "WHERE " + tableName1 + "." + whereColumnNameT1 + " = " + valueQueryPrep(condition));
    }


    public Boolean valueExists(String columnName, String tableName, double value) throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT COUNT( " + columnName + " FROM " + tableName + " WHERE " + columnName + " = " + valueQueryPrep(value));
        int temp = 0;
        while (rs.next()) {
            temp ++;
        }
        if (temp > 0)
            return true;
        else
            return false;
    }

    public Boolean valueExists(String columnName, String tableName, int value) throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT COUNT( " + columnName + " FROM " + tableName + " WHERE " + columnName + " = " + valueQueryPrep(value));
        int temp = 0;
        while (rs.next()) {
            temp ++;
        }
        if (temp > 0)
            return true;
        else
            return false;
    }

    public Boolean valueExists(String columnName, String tableName, String value) throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT COUNT( " + columnName + " FROM " + tableName + " WHERE " + columnName + " = " + valueQueryPrep(value));
        int temp = 0;
        while (rs.next()) {
            temp ++;
        }
        if (temp > 0)
            return true;
        else
            return false;
    }

    public String valueQueryPrep (LocalDateTime value) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
        String staging = value.format(formatter);
        String result = "'" + staging + "'";
        return result;
    }

    public String valueQueryPrep (Date value) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String staging = dateFormat.format(value);
        String result = "'" + staging + "'";
        return result;
    }

    public String valueQueryPrep (double value) {
        String result = "'" + value + "'";
        return result;
    }

    public String valueQueryPrep (int value) {
        String result = "'" + value + "'";
        return result;
    }

    public static String valueQueryPrep(String value) {
        value = "'" + value + "'";
        return value;
    }

    // takes a table from the database and creates a file with the pathname of your choosing

    public static void main(String[] args) throws SQLException, ClassNotFoundException, FileNotFoundException {

        System.out.println("This is the first line of main.");
        QueryMaker qm = Credentials.databaseLogin();
        System.out.println("The QueryMaker object has been created.");


        qm.createDatabaseStructure();
        System.out.println("The basic database structure has been created and inventory has been loaded.");
        qm.batchLoading();
        System.out.println("Batch file loading complete.");

        /*
        qm.batchProcessing(); troubleshooting this right now ///////////////////////////////////////////////
        System.out.println();
        System.out.println();
        System.out.println("Batch processing complete.");
        System.out.println();
         */

        //used in illustrations below - not intended for production code
        statement.executeUpdate("INSERT INTO inventory VALUES (\"test1234test\", 10, 1.01, 2.02, \"testsupp\")");

        System.out.println();

        //illustrates use of readValues
        ResultSet rs = qm.readValues("quantity", "inventory", "product_id = " + valueQueryPrep("test1234test"));
        while (rs.next()) {
            int testQuant1 = rs.getInt(1);
            System.out.println("The quantity of Product \"test1234test\" in \'inventory\' is " + testQuant1);
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
            System.out.println("Test Value of \'test1234test\' search in inventory spits back:\n" +
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
            System.out.println("Extract records with quantity of \'10\' from inventory:\n" +
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
            System.out.println("Test Value of \'test1234test\' search in inventory spits back:\n" +
                    testProdID2 + ", " + testQuant2 + ", " + testWholesale2 + ", " + testSale2 + ", " + testSuppID2);
        }

        System.out.println();


    }
}
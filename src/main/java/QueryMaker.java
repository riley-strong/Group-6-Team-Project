import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class QueryMaker {

    public static int DATE = 3;
    public static int DOUBLE = 2;
    public static int INT = 1;
    public static int STRING = 0;
    private static Connection connection;
    private static Statement statement;
    private static Object JDBCTutorialUtilities;
    private PreparedStatement preparedStatement;
    private ResultSet tempRS;

    public String getTableName() {
        return tableName;
    }

    private String tableName;



    //public static final String PRODUCT_ID_STRING = "product_id";
    //public static final String QUANTITY_STRING = "quantity";
   // public static final String WHOLESALE_ID_STRING = "wholesale_cost";
   // public static final String SALE_PRICE_ID_STRING = "sale_price";
   // public static final String SUPPLIER_ID_STRING = "supplier_id";


    // rs.getString(PRODUCT_ID_STRING);
    // rs.getString(QUANTITY_STRING);
    // rs.getString(WHOLESALE_ID_STRING);
    // rs.getString(SALE_PRICE_ID_STRING);
    // rs.getString(SUPPLIER_ID_STRING);


    /** Creates QueryMaker object that is used to interact with MySQL Database
     *
     * @param userName      MySQL userName
     * @param password      MySQL password
     * @param ipAddress     MySQL IP Address
     * @param portNumber    SQL portNumber
     * @param databaseName  MySQL database name
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
        statement.execute("ALTER TABLE unprocessed_sales ADD COLUMN hashed_email VARBINARY(32)");

        // Step 3: Load hash table with emails from unprocessed orders and generate hashed emails.
        statement.execute("INSERT INTO hash_ref (hashed_email, unhashed_email) " +
                "SELECT MD5(cust_email), cust_email FROM unprocessed_sales");

        // Step 4: Fill with hashed emails from hash table.
        statement.execute("UPDATE unprocessed_sales us ,hash_ref hr " +
                "SET us.hashed_email = hr.hashed_email " +
                "WHERE us.cust_email = hr.unhashed_email");

        // Step 5: Delete (unhashed) email column from unprocessed orders.
        statement.execute("ALTER TABLE unprocessed_sales DROP COLUMN cust_email");

    }

    public void batchProcessing() throws SQLException {
        // Step 1: Process orders sequentially (by date) & load into processed orders (with result bit column added).
            //Part a: Load in inventory into Java data structure.
            ResultSet inv = statement.executeQuery("SELECT product_id, quantity FROM inventory");

            //Part b: Load in unprocessed sales into Java data structure.
            ResultSet us = statement.executeQuery("SELECT date, us.product_id, us.product_quantity, hr.unhashed_email" +
                    "FROM unprocessed_sales AS us" +
                    "LEFT JOIN hash_ref AS hr ON hr.hashed_email = us.hashed_email" +
                    "ORDER BY date, hr.unhashed_email");

            //Part c: Load in processed sales into Java data structure.
            ResultSet ps = statement.executeQuery("SELECT ps.date, ps.processed_date, hr.unhashed_email, ps.cust_location, ps.product_id, ps.product_quantity, ps.result " +
                    "FROM processed_sales AS ps " +
                    "LEFT JOIN hash_ref AS hr ON hr.hashed_email = ps.hashed_email " +
                    "ORDER BY date, hr.unhashed_email");

            //Pard d: Iterate through unprocessed_sales, comparing with inventory & writing to processed_sales
            while (us.next()) {
                String us_p_id = us.getString(1);
                int us_quant = us.getInt(2);

                String invQuery = "SELECT quantity FROM inventory WHERE product_id = " + us_p_id;
                preparedStatement = connection.prepareStatement(invQuery);
                ResultSet tempRS = preparedStatement.executeQuery();
                int inv_quant = tempRS.getInt(1);

                if (inv_quant >= us_quant) {
                    String updateInv = "UPDATE inventory inv, unprocessed_sales us " +
                            "SET inv.quantity = inv.quantity - us.quantity " +
                            "WHERE inv.product_id = ";
                    preparedStatement = connection.prepareStatement(updateInv);
                    preparedStatement.executeQuery();

                //    ps.updateArray(3); //pick up here with writing into processed sales ResultSet from unprocessed_sales ResultSet
            }
        }
    }




    /**
     *
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

    public void createDatabaseStructure() throws SQLException, FileNotFoundException, ClassNotFoundException {
        // Step 1: Create Inventory Table in SQL Database.
        statement.execute("DROP TABLE IF EXISTS inventory");
        statement.execute("CREATE TABLE inventory (" +
                                                    "product_id VARCHAR(12)" +
                                                    ",quantity INT" +
                                                    ",wholesale_cost DECIMAL(12,2)" +
                                                    ",sale_price DECIMAL(12,2)" +
                                                    ",supplier_id VARCHAR(8)" +
                                                    ", PRIMARY KEY (product_id))");

        // Step 2: Load SQL Inventory table with Java 2-D Array.
        Object[][] objArr = this.csvToArray("inventory_team6.csv", new int[]{STRING, INT, DOUBLE, DOUBLE, STRING});
        this.setTableName("inventory");
        this.insertRows(new String[]{"product_id","quantity","wholesale_cost","sale_price","supplier_id"},objArr);

        // Step 3: Create unprocessed_sales Table in SQL Database.
        statement.execute("DROP TABLE IF EXISTS unprocessed_sales");
        statement.execute("CREATE TABLE unprocessed_sales (" +
                                                    "date DATE" +
                                                    ",cust_email VARCHAR(320)" +
                                                    ",cust_location VARCHAR(5)" +
                                                    ",product_id VARCHAR(12)" +
                                                    ",product_quantity int);");

        // Step 4: Create hash_ref Table in SQL Database.
        statement.execute("DROP TABLE IF EXISTS hash_ref");
        statement.execute("CREATE TABLE hash_ref (" +
                                                    "hashed_email VARBINARY(32)" +
                                                    ",unhashed_email VARCHAR(320))");

        // Step 5: Create processed_sales Table in SQL Database.
        statement.execute("DROP TABLE IF EXISTS processed_sales");
        statement.execute("CREATE TABLE processed_sales (" +
                                                    "date DATE" +
                                                    ",processed_date DATE" +
                                                    ",hashed_email VARBINARY(32) NULL" +
                                                    ",cust_location VARCHAR(5)" +
                                                    ",product_id VARCHAR(12)" +
                                                    ",product_quantity int" +
                                                    ",result bit);");
    }

    /**
     *
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

    // deletes all rows in a table

    /**
     *
     * @throws SQLException
     */
    public void deleteRows() throws SQLException {
        generateUpdate("DELETE FROM " + tableName);
    }

    // once setTableName() has been called then displayFile will print the table the to console

    /**
     *
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

    // displays all tables in our database

    /**
     *
     * @throws SQLException
     */
    private void displayTableNames() throws SQLException {
        ResultSet tables = generateQuery("SHOW TABLES FROM TEAM_6");
        while (tables.next()) {
            String tblName = tables.getString(1);
            System.out.println(tblName);
        }
    }
    // helper method for getproduct() method

    /**
     *
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
     *
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
     *
     * @param s
     * @return
     * @throws SQLException
     */
    public int generateUpdate(String s) throws SQLException {
        Statement st = connection.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        return st.executeUpdate(s);
    }

    /**
     * Gets an array of the column names of a specific table
     */
    /**
     *
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
     *
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
     *
     * @param columnValue
     * @return
     */
    private String getString(Object columnValue) {
        if (columnValue instanceof String || columnValue instanceof LocalDate) {
            return "'" + columnValue + "'";
        }
        return columnValue.toString();
    }

    // inserts rows into the specified table

    /**
     *
     * @param columnNames
     * @param rows
     * @throws SQLException
     */
    public void insertRows(String[] columnNames, Object[][] rows) throws SQLException {
        StringBuilder builder = new StringBuilder();
        String s = Arrays.toString(columnNames);
        builder.append("INSERT INTO " + tableName + " (" + s.substring(1, s.length() - 1) + ")VALUES");
        for (int i = 0; i < rows.length; i++) {
            builder.append(" (");
            for (int j = 0; j < rows[i].length; j++) {
                builder.append(quoteWrap(rows[i][j]));
                builder.append(j < rows[i].length - 1 ? "," : ")");
            }
            builder.append(i < rows.length - 1 ? "," : "");
        }
        generateUpdate(builder.toString());
    }

    /**
     *
     * @param fileName
     * @param qm
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     */
    public void loadInventory(String fileName, QueryMaker qm) throws SQLException, ClassNotFoundException, FileNotFoundException  {

        qm.generateUpdate("CREATE TABLE inventory (idx int(16)  NOT NULL AUTO_INCREMENT, product_id " +
            "VARCHAR(16),quantity int(16),wholesale_cost decimal(13,2),sale_price decimal(13,2),supplier_id VARCHAR(32), PRIMARY KEY (idx));");
        qm.setTableName("inventory");
        qm.insertRows(new String[]{"product_id","quantity","wholesale_cost","sale_price","supplier_id"},
        qm.csvToArray("" + fileName + "", new int[]{STRING, INT, DOUBLE, DOUBLE, STRING}));
    }

    /**
     *
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

    /**
     * Removes the (BOM byte-order mark) from the beginning of the string.
     */
    /**
     *
     * @param s
     * @return
     */
    private static String removeUTF8BOM(String s) {
        if (s.startsWith("\uFEFF")) {
            s = s.substring(1);
        }
        return s;
    }

    /**
     * retrieve the number of rows of in a ResultSet.
     */
    /**
     *
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
     *
     * @param tableName
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    // takes a table from the database and creates a file with the pathname of your choosing

    /**
     *
     * @param pathname
     * @throws FileNotFoundException
     * @throws SQLException
     */
    private void tableToFile(String pathname) throws FileNotFoundException, SQLException {
        ResultSet rs = generateQuery("SELECT * FROM " + tableName);
        PrintWriter writer = new PrintWriter(new File(pathname));
        int columnCount = getColumnNames().length;
        for (int i = 0; i < columnCount; i++) {
            writer.print(rs.getMetaData().getColumnName(i + 1));
            writer.print(i == columnCount - 1 ? "" : ",");
        }
        while (rs.next()) {
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                writer.print(removeUTF8BOM(rs.getObject(i + 1) + ""));
                if (i < rs.getMetaData().getColumnCount() - 1) {
                    writer.print(",");
                }
            }
            writer.print("");
        }
        writer.close();
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException, FileNotFoundException {
        System.out.println("This is the first line of main.");
        QueryMaker qm = Credentials.databaseLogin();
        System.out.println("The QueryMaker object has been created.");
        qm.createDatabaseStructure();
        System.out.println("The basic database structure has been created and inventory has been loaded.");
        qm.batchLoading();
        System.out.println("Batch file loading complete.");


        //qm.generateUpdate("CREATE TABLE unprocessed_sales (idx int(16)  NOT NULL AUTO_INCREMENT, " +
        // "date DATETIME,cust_email VARCHAR(100),cust_location VARCHAR(100),product_id VARCHAR(100),product_quantity INT(100), PRIMARY KEY (idx));");

        //qm.insertRows(new String[]{"date","cust_email","cust_location","product_id","product_quantity"},
        // qm.csvToArray("customer_orders_A_team6.csv", new int[]{DATE, STRING, INT, STRING, INT}));

        // qm.displayTableNames();
        //qm.setTableName("inventory");
        //qm.generateUpdate("DROP TABLE IF EXISTS " + qm.tableName);
        //qm.displayTableNames();


        //qm.displayTableNames();
        // qm.setTableName("unprocessed_sales");
        //qm.displayFile();
        // qm.tableToFile("TESTFILE.csv");
        //System.out.println(Arrays.deepToString(qm.getColumnNames()));
        //System.out.println(Arrays.deepToString(qm.getProduct("unprocessed_sales","cust_email","bin@msn.com")));

        //qm.displayTableNames();
        //qm.setTableName("inventory");
        //qm.displayFile();
        //qm.tableToFile("LOOKOVERHERE.csv");
        //System.out.println(Arrays.deepToString(qm.getColumnNames()));
        //System.out.println(Arrays.deepToString(qm.getProduct("inventory", "supplier_id", "TKLCRQAQ")));
        //System.out.println(Arrays.deepToString(qm.getProduct("inventory", "quantity", 1100)));

    }

}

//add to cheat sheet
//qm.generateUpdate("CREATE TABLE inventory (idx int(16)  NOT NULL AUTO_INCREMENT, product_id " +
//"VARCHAR(16),quantity int(16),wholesale_cost decimal(13,2),sale_price decimal(13,2),supplier_id VARCHAR(32), PRIMARY KEY (idx));");
//qm.setTableName("inventory");
//qm.insertRows(new String[]{"product_id","quantity","wholesale_cost","sale_price","supplier_id"},
// qm.csvToArray("inventory_team6.csv", new int[]{STRING, INT, DOUBLE, DOUBLE, STRING}));

//qm.generateUpdate("CREATE TABLE unprocessed_sales (idx int(16)  NOT NULL AUTO_INCREMENT, " +
// "date DATETIME,cust_email VARCHAR(100),cust_location VARCHAR(100),product_id VARCHAR(100),product_quantity INT(100), PRIMARY KEY (idx));");

//qm.insertRows(new String[]{"date","cust_email","cust_location","product_id","product_quantity"},
// qm.csvToArray("customer_orders_A_team6.csv", new int[]{DATE, STRING, INT, STRING, INT}));


// qm.generateUpdate("DROP TABLE IF EXISTS " + qm.tableName);


//qm.displayTableNames();
//qm.setTableName("unprocessed_sales");
//qm.displayFile();
//qm.tableToFile("TESTFILE.csv");
//System.out.println(Arrays.deepToString(qm.getColumnNames()));
//System.out.println(Arrays.deepToString(qm.getProduct("unprocessed_sales","cust_email","bin@msn.com")));

//qm.displayTableNames();
//qm.setTableName("inventory");
//qm.displayFile();
//qm.tableToFile("LOOKOVERHERE.csv");
//System.out.println(Arrays.deepToString(qm.getColumnNames()));
//System.out.println(Arrays.deepToString(qm.getProduct("inventory", "supplier_id", "TKLCRQAQ")));
//System.out.println(Arrays.deepToString(qm.getProduct("inventory", "quantity", 1100)));


//(Stream.of(getColumnNames()).collect(Collectors.joining(","))) + ""

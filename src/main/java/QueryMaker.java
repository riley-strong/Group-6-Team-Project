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
    public static int DATETIME = 4;
    public static int DOUBLE = 2;
    public static int INT = 1;
    public static int STRING = 0;
    private static Connection connection;
    private PreparedStatement preparedStatement;
    public static Statement statement;
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

    /**
     * transfers unprocessed customer_orders csv file into the unprocessed_sales SQL table
     * add an additional column to unprocessed_sales table for hashing emails
     * load and hash customer emails in hash_ref table from unprocessed_sales table
     * add newly hashed emails into the unprocessed_sales table from hash_ref.
     * DELETE un-hashed emails column from unprocessed_sales
     * We now have a SQL table with unprocessed sales and hashed customer emails
     *
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     */

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
        //statement.executeUpdate("ALTER TABLE unprocessed_sales DROP COLUMN cust_email");

    }

    /**
     * convert inventory table/unprocessed_sales into java readable format and load a hash map
     * update product quantity in inventory based on unprocessed_sales order amount
     * if the unprocessed_sales quantity is > inventory quantity then sale cannot be processed. (negative inventory)
     * Discard any untouched product quantities. (Eliminates any duplicates)
     * Load the inventory SQL table with new changes to quantity.
     * Create a new date column and add to the processed_sales table
     * load processed_sales with the information below
     * date, processed_datetime, un-hashed_email, cust_location, product_id, product_quantity, result
     * We now have a table with ALL the information we need for analytics.
     *
     * @throws SQLException
     */

    public void batchProcessing() throws SQLException {
        // Step 1: Pull inventory table into Java data structure.
        ResultSet inv = this.readTable("inventory");
        HashMap<String, Integer> invHashMap = new HashMap<>();
        String inv_p_id;
        int inv_quant;

        while (inv.next()) {
            inv_p_id = inv.getString(1);
            inv_quant = inv.getInt(2);
            invHashMap.put(inv_p_id, inv_quant);
        }
        HashMap<String, Integer> invHashMap_original = new HashMap<>();
        invHashMap_original.putAll(invHashMap);

        // Step 2: Pull unprocessed sales into Java data structure
        ResultSet us = statement.executeQuery("SELECT DISTINCT date, us.cust_email, us.cust_location, us.product_id, us.product_quantity " +
                "FROM unprocessed_sales AS us " +
                "ORDER BY date, us.cust_email");
        java.sql.Date us_date;
        String us_email;
        String us_loc;
        String us_p_id;
        int us_quant;
        List<Transaction> usList = new ArrayList<>();

        while (us.next()) {
            us_date = us.getDate(1);
            us_email = us.getString(2);
            us_loc = us.getString(3);
            us_p_id = us.getString(4);
            us_quant = us.getInt(5);

            Transaction transaction = new Transaction(us_date, us_email, us_loc, us_p_id, us_quant);
            usList.add(transaction);
        }

        // Step 3: Iteratively compare batch orders to inventory, updating inventory & processed sales Java structures
        int inv_q;
        int us_q;
        List<String[]> psList = new ArrayList<>();

        for (Transaction t : usList) {
            us_q = t.getProduct_quantity();
            inv_q = invHashMap.get(t.getProduct_id());

            if (us_q <= inv_q) {
                invHashMap.put(t.getProduct_id(), inv_q - us_q);
                psList.add(t.processTransaction(1));
            } else
                psList.add(t.processTransaction(0));
        }

        //Step 3.5: Remove duplicates (unchanged quantities) from hash map to go into inventory SQL table.
        Iterator iter = invHashMap_original.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry p = (Map.Entry) iter.next();
            if (invHashMap.get(p.getKey()) == p.getValue())
                invHashMap.remove(p.getKey());
        }

        // Step 4: Alter SQL inventory with updated inventory values from Java data structure.
        Iterator it = invHashMap.entrySet().iterator();
        String p;
        int q;
        int x = 0;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            p = (String) pair.getKey();
            q = (int) pair.getValue();

            statement.executeUpdate("UPDATE inventory SET quantity = " + valueQueryPrep(q) + " " +
                    " WHERE product_id = " + valueQueryPrep(p));
        }

        //Step 5: Construct second parameter of InsertRows method (2-D Object Array)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LinkedList<Object[]> tempArray = new LinkedList<>();

        for (String[] s : psList) {
            Object[] strArr = new String[7];

            strArr[0] = valueQueryPrep(java.sql.Date.valueOf(s[0]));
            strArr[1] = valueQueryPrep(LocalDateTime.parse(s[1], formatter));
            strArr[2] = valueQueryPrep(s[2]);
            strArr[3] = valueQueryPrep(s[3]);
            strArr[4] = valueQueryPrep(s[4]);
            strArr[5] = valueQueryPrep(Integer.parseInt(s[5]));
            strArr[6] = valueQueryPrep(Integer.parseInt(s[6]));
            tempArray.add(strArr);
        }

        String[] headers = {"date", "processed_datetime", "unhashed_email", "cust_location", "product_id", "product_quantity", "result"};
        Object[][] objects = new Object[tempArray.size()][headers.length];
        for (int j = 0; j < objects.length; j++) {
            Object[] element = tempArray.pop();
            for (int i = 0; i < headers.length; i++) {
                objects[j][i] = element[i];
            }
        }

        //Step 6: Insert Rows into processed_sales SQL table
        this.setTableName("processed_sales");
        this.insertRows(headers, objects);

        //Step 7: Truncate unprocessed_sales table.
        statement.executeUpdate("TRUNCATE unprocessed_sales");

        //Step 8: Add in hashed emails to processed_sales table
        statement.executeUpdate("UPDATE processed_sales, hash_ref " +
                " SET processed_sales.hashed_email = hash_ref.hashed_email " +
                " WHERE processed_sales.unhashed_email = hash_ref.unhashed_email");

        //Step 9: Truncate unhashed_emails values from processed_sales table
        statement.executeUpdate("UPDATE processed_sales SET unhashed_email = null ");
    }

    //TODO: Once riley changes the MailService.java from using this method we can delete it.

    /**
     * @param productID
     * @return
     * @throws SQLException
     */

    public boolean contains(String productID) throws SQLException {
        ResultSet rs = generateQuery("SELECT * FROM inventory WHERE product_id = '" + productID + "'");
        return rs.next();
    }

    /**
     * Takes in a inventory csv file and formats the file information to be recognized by SQL
     * creates four tables: inventory, unprocessed_sales, hash_ref and processed_sales
     * inventory table is loaded with csv file information
     * all other tables have specified columns created, are empty and ready to use
     *
     * @throws SQLException
     * @throws FileNotFoundException
     */

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
                "date DATE,processed_datetime DATETIME,unhashed_email VARCHAR(320), hashed_email VARBINARY(32) NULL," +
                        "cust_location VARCHAR(5),product_id VARCHAR(12),product_quantity int,result int)");

    }

    /**
     * creates a new table with the following two arguments:
     * NOTE - method checks and deletes if the table already exists first
     *
     * @param tableName   - name of table name
     * @param columnSpecs - name the columns and a variable type (int, DATE, VARCHAR etc..)
     * @throws SQLException
     */

    public void createTable(String tableName, String columnSpecs) throws SQLException {
        this.deleteTable(tableName);
        statement.executeUpdate("CREATE TABLE " + tableName + " (" + columnSpecs);
    }

    /**
     * takes in a csv file and formats it to be SQL recognizable (quote wrapping) using the following two arguments:
     *
     * @param fileName - csv filename
     * @param types    - integer array of types (STRING, DATE, INT etc..)
     * @return - 2D array of SQL format readable information from the csv file
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

    /**
     * deletes any values from a table based on three of the following arguments:
     *
     * @param tableName  - name of table to delete from
     * @param columnName - name of column within the table
     * @param value      - deletes all rows with the DECIMAL value matching that of the search
     * @throws SQLException
     */

    public void deleteRecords(String tableName, String columnName, double value) throws SQLException {
        statement.executeUpdate("DELETE FROM " + tableName + "WHERE " + columnName + " = " + valueQueryPrep(value));
    }

    /**
     * deletes any values from a table based on three of the following arguments:
     *
     * @param tableName  - name of table to delete from
     * @param columnName - name of column within the table
     * @param value      - deletes all rows with the INTEGER value matching that of the search
     * @throws SQLException
     */

    public void deleteRecords(String tableName, String columnName, int value) throws SQLException {
        statement.executeUpdate("DELETE FROM " + tableName + "WHERE " + columnName + " = " + valueQueryPrep(value));
    }

    /**
     * deletes any values from a table based on three of the following arguments:
     *
     * @param tableName  - name of table to delete from
     * @param columnName - name of column within the table
     * @param value      - deletes all rows with the STRING value matching that of the search
     * @throws SQLException
     */

    public void deleteRecords(String tableName, String columnName, String value) throws SQLException {
        statement.executeUpdate("DELETE FROM " + tableName + "WHERE " + columnName + " = " + valueQueryPrep(value));
    }

    /**
     * deletes all rows given the table name but preserves the table
     *
     * @throws SQLException
     */

    public void deleteRows() throws SQLException {
        generateUpdate("DELETE FROM " + tableName);
    }

    /**
     * deletes the entire table given the table name
     *
     * @param tableName
     * @throws SQLException
     */

    public void deleteTable(String tableName) throws SQLException {
        statement.executeUpdate("DROP TABLE IF EXISTS " + tableName);
    }

    /**
     * deletes the table based on the following arguments:
     *
     * @param tableName - name of table
     * @param condition - this condition must be met
     * @throws SQLException
     */

    public void deleteTableWithCond(String tableName, String condition) throws SQLException {
        statement.executeUpdate("DROP TABLE IF EXISTS " + tableName + " " + condition);
    }

    /**
     * call setTableName() to proceed to use this method
     * displays the information in the IDE console from the SQL table
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

    /**
     * helper method for getProduct() method
     * returns a 2D object array of information taking the following arguments
     *
     * @param rs          - must provide a result set
     * @param isOneColumn - boolean value if the extracted information is one column or greater
     * @return 2D object results of data
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
     * given an argument (any SQL statement) it will return a table of useful data
     *
     * @param s - any SQL syntax commands
     * @return returns a table of data that is scrollable
     * @throws SQLException
     */

    public ResultSet generateQuery(String s) throws SQLException {
        Statement st = connection.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        return st.executeQuery(s);

    }

    /**
     * given an argument (any SQL statement) it will update any changes to a specified target
     *
     * @param s - any SQL syntax commands
     * @throws SQLException
     */

    public void generateUpdate(String s) throws SQLException {
        Statement st = connection.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        st.execute(s);
    }

    /**
     * call setTableName() to proceed to use this method
     * based on the table name we can return the column names of that table
     *
     * @return tables column names
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

    /**
     * uses the extractResults() method to help return the information searched for
     * returns the information searched for given the following arguments below
     *
     * @param tableName   - specify the name of the table
     * @param columnName  - specify the name of the column within the table
     * @param columnValue - specify any object type searched for (int, String etc)
     * @return calls on a helper method to return a nice table of information
     * @throws SQLException
     */

    public Object[][] getProduct(final String tableName, final String columnName, Object columnValue) throws SQLException {
        ResultSet rs = generateQuery("SELECT * FROM " + tableName + " WHERE " + columnName + " = " + quoteWrap(columnValue));
        String s = "select count(*) from " + tableName + " where " + columnName + " = " + getString(columnValue);
        ResultSet resultCount = generateQuery(s);
        return extractResults(rs, true);
    }

    /**
     * quote wraps a specified String or Date based on the argument below
     *
     * @param columnValue - any column containing a String or Date will be wrapped in quotes for SQL syntax
     * @return
     */

    private String getString(Object columnValue) {
        if (columnValue instanceof String || columnValue instanceof LocalDate) {
            return "'" + columnValue + "'";
        }
        return columnValue.toString();
    }

    /**
     * getter method for returning the table name
     *
     * @return - any table name found in the database
     */

    public String getTableName() {
        return tableName;
    }

    /**
     * call setTableName() to proceed to use this method
     * loads specified rows of information in a table given the following arguments
     *
     * @param columnNames - names of all the columns
     * @param rows        - 2D array of all the information to add
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

    /**
     * add new information into a table with unspecified column given the two following arguments
     *
     * @param tableName - name of the table
     * @param values    - the values to be added
     * @throws SQLException
     */

    public void insertRecordIntoTable(String tableName, String values) throws SQLException {
        statement.executeUpdate("INSERT INTO " + tableName + " VALUES ( " + values + " ) ");
    }

    /**
     * add new information into a table with the specified column name given the three following arguments
     *
     * @param tableName   - name of table
     * @param columnNames - name of column in the table
     * @param values      - the values to be added
     * @throws SQLException
     */

    public void insertValuesIntoTable(String tableName, String columnNames, String values) throws SQLException {
        statement.executeUpdate("INSERT INTO " + tableName + " ( " + columnNames + " ) VALUES ( " + values + " ) ");
    }

    /**
     * If the information is of type String or LocalDate then it will be formatted for SQL readable syntax
     *
     * @param columnValue - value must be of type String or LocalDate
     * @return returns a string of SQL friendly syntax
     */
    private String quoteWrap(Object columnValue) {
        if (columnValue instanceof String || columnValue instanceof LocalDate) {
            return "'" + columnValue + "'";
        } else {
            return columnValue + "";
        }
    }

    /**
     * searches a table for specific information and returns any results given the following arguments
     *
     * @param tableName   - name of table
     * @param whereClause - what user is searching for
     * @param value       - what user search is checked against
     * @return return a table of data
     * @throws SQLException
     */

    //use when needing (up to) all columns from result
    public ResultSet readRecords(String tableName, String whereClause, String value) throws SQLException {
        value = valueQueryPrep(value);
        String query = "SELECT * FROM " + tableName + " WHERE " + whereClause + " = " + value;
        preparedStatement = connection.prepareStatement(query);
        ResultSet rs = preparedStatement.executeQuery();
        return rs;
    }

    /**
     * reads a table based on the argument below:
     *
     * @param tableName - name of table
     * @return return a table of data - result set
     * @throws SQLException
     */

    public ResultSet readTable(String tableName) throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName);
        return rs;

    }

    /**
     * reads a table based on a given condition
     *
     * @param tableName - name of table
     * @param condition - a condition can be weather something is true for false or (0 or 1)
     * @return a table of data - result set
     * @throws SQLException
     */

    public ResultSet readTableWithCond(String tableName, String condition) throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName + " " + condition);
        return rs;
    }

    /**
     * searches a table for a value based on column name
     *
     * @param columnName  - name of column from table
     * @param tableName   - name of table
     * @param whereClause - what the user is searching for in the column
     * @return returns a column of data
     * @throws SQLException
     */

    //use when assuming needing all matches for one column
    public ResultSet readValues(String columnName, String tableName, String whereClause) throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT " + columnName + " FROM " + tableName + " WHERE " + whereClause);
        return rs;
    }

    /**
     * returns the number of rows in the result set
     *
     * @param rs - specify a result set
     * @return return the number or rows in that result set
     * @throws SQLException
     */
    int rowCountResults(ResultSet rs) throws SQLException {
        rs.last();
        int countRows = rs.getRow();
        rs.beforeFirst();
        return countRows;
    }

    /**
     * setter method for table name
     * ALWAYS SET THE TABLE NAME BEFORE PROCEEDING WITH ANYTHING ELSE
     *
     * @param tableName - String name given from user
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * creates a new temporary table that will fill information on the top ten customers
     * takes on the customer information: date , customer email, product ID, total purchased.
     *
     * @throws SQLException
     */

    public void topTenCustomers() throws SQLException {
        statement.execute("DROP TABLE IF EXISTS temp");
        this.createTable("temp", "date date, cust_email VARCHAR(320), product_id VARCHAR(12), Total_Purchase DECIMAL (64, 2)");
        statement.executeUpdate("INSERT INTO temp ");

    }

    /**
     * populate one tables information into another with the original tables information
     *
     * @param tableName1        - original table name
     * @param tableName2        - new table name
     * @param setColumnNameT1   - first column name
     * @param setColumnNameT2   - second column name
     * @param whereColumnNameT1 - old column name 1
     * @param whereColumnNameT2 - old column name 2
     * @throws SQLException
     */

    public void updateTableFromTable(String tableName1, String tableName2, String setColumnNameT1, String setColumnNameT2, String whereColumnNameT1, String whereColumnNameT2) throws SQLException {
        statement.executeUpdate("UPDATE " + tableName1 + ", " + tableName2 + " " +
                "SET " + tableName1 + "." + setColumnNameT1 + " = " + tableName2 + "." + setColumnNameT2 + " " +
                "WHERE " + tableName1 + "." + whereColumnNameT1 + " = " + tableName2 + "." + whereColumnNameT2);
    }

    /**
     * populate one tables information into another with the original tables information such that a condition is met
     *
     * @param tableName1        - original table name
     * @param tableName2        - new table name
     * @param setColumnNameT1   - new column name
     * @param value             - values to be added
     * @param whereColumnNameT1 - old column name
     * @param condition         - may be true false condition
     * @throws SQLException
     */

    public void updateTableFromStatic(String tableName1, String tableName2, String setColumnNameT1, String value, String whereColumnNameT1, String condition) throws SQLException {
        statement.executeUpdate("UPDATE " + tableName1 + ", " + tableName2 + " " +
                "SET " + tableName1 + "." + setColumnNameT1 + " = " + valueQueryPrep(value) + " " +
                "WHERE " + tableName1 + "." + whereColumnNameT1 + " = " + valueQueryPrep(condition));
    }

    /**
     * searches a table for a DECIMAL value and returns true/false based on the given arguments below:
     *
     * @param columnName - name of column
     * @param tableName  - name of table
     * @param value      - the value being searched by the user
     * @return
     * @throws SQLException
     */


    public Boolean valueExists(String columnName, String tableName, double value) throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT COUNT( " + columnName + " FROM " + tableName + " WHERE " + columnName + " = " + valueQueryPrep(value));
        int temp = 0;
        while (rs.next()) {
            temp++;
        }
        return temp > 0;
    }

    /**
     * searches a table for an INTEGER value and returns true/false based on the given arguments below:
     *
     * @param columnName - name of column
     * @param tableName  - name of table
     * @param value      - the value being searched by the user
     * @return
     * @throws SQLException
     */

    public Boolean valueExists(String columnName, String tableName, int value) throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT COUNT( " + columnName + " FROM " + tableName + " WHERE " + columnName + " = " + valueQueryPrep(value));
        int temp = 0;
        while (rs.next()) {
            temp++;
        }
        return temp > 0;
    }

    /**
     * searches a table for a STRING value and returns true/false based on the given arguments below:
     *
     * @param columnName - name of column
     * @param tableName  - name of table
     * @param value      - the value being searched by the user
     * @return
     * @throws SQLException
     */

    public Boolean valueExists(String columnName, String tableName, String value) throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT COUNT( " + columnName + " FROM " + tableName + " WHERE " + columnName + " = " + valueQueryPrep(value));
        int temp = 0;
        while (rs.next()) {
            temp++;
        }
        return temp > 0;
    }

    /**
     * formats and parses the date and time based on the format year-month-day hour:minute:second
     *
     * @param value - the date time value given from the user
     * @return return the parsed time/date format
     */

    public String valueQueryPrep(LocalDateTime value) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
        String staging = value.format(formatter);
        String result = "'" + staging + "'";
        return result;
    }

    /**
     * formats and parses the date based on the format year-month-day
     *
     * @param value - user provided value
     * @return return the parsed date format
     */

    public String valueQueryPrep(Date value) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String staging = dateFormat.format(value);
        String result = "'" + staging + "'";
        return result;
    }

    /**
     * wrapper class for a millisecond value that is recognized as SQL format
     *
     * @param value - SQL provided value
     * @return returns the proper year-month-day format
     */

    public String valueQueryPrep(java.sql.Date value) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String staging = dateFormat.format(value);
        String result = "'" + staging + "'";
        return result;
    }

    /**
     * quote wraps a decimal value
     *
     * @param value - user provided decimal value
     * @return return a quote wrapped string
     */

    public String valueQueryPrep(double value) {
        String result = "'" + value + "'";
        return result;
    }

    /**
     * quote wraps a integer value
     *
     * @param value - user provided integer value
     * @return return a quote wrapped string
     */

    public String valueQueryPrep(int value) {
        String result = "'" + value + "'";
        return result;
    }

    /**
     * quote wraps a integer value
     *
     * @param value - user provided String value
     * @return return a quote wrapped string
     */

    public static String valueQueryPrep(String value) {
        value = "'" + value + "'";
        return value;
    }

    // takes a table from the database and creates a file with the pathname of your choosing

    public static void main(String[] args) throws SQLException, ClassNotFoundException, FileNotFoundException {

        System.out.println("This is the first line of main.");
        final Credentials credentials = new Credentials();

    }
//    public Main(){
//        QueryMaker qm = credentials.getQueryMaker();
//
//
//        System.out.println("The QueryMaker object has been created.");
//
//
//        qm.createDatabaseStructure();
//        System.out.println("The basic database structure has been created and inventory has been loaded.");
//        qm.batchLoading();
//        System.out.println("Batch file loading complete.");
//
//
//        qm.batchProcessing(); //troubleshooting this right now ///////////////////////////////////////////////
//        System.out.println();
//        System.out.println();
//        System.out.println("Batch processing complete.");
//        System.out.println();
//
//
//        //used in illustrations below - not intended for production code
//        statement.executeUpdate("INSERT INTO inventory VALUES (\"test1234test\", 10, 1.01, 2.02, \"testsupp\")");
//
//        System.out.println();
//
//        //illustrates use of readValues
//        ResultSet rs = qm.readValues("quantity", "inventory", "product_id = " + valueQueryPrep("test1234test"));
//        while (rs.next()) {
//            int testQuant1 = rs.getInt(1);
//            System.out.println("The quantity of Product \"test1234test\" in 'inventory' is " + testQuant1);
//        }
//
//        System.out.println();
//
//        //illustrates use of readRecords with one record result
//        ResultSet rs2 = qm.readRecords("inventory", "product_id", "test1234test");
//        while (rs2.next()) {
//            String testProdID2 = rs2.getString(1);
//            int testQuant2 = rs2.getInt(2);
//            double testWholesale2 = rs2.getDouble(3);
//            double testSale2 = rs2.getDouble(4);
//            String testSuppID2 = rs2.getString(5);
//            System.out.println("Test Value of 'test1234test' search in inventory spits back:\n" +
//                    testProdID2 + ", " + testQuant2 + ", " + testWholesale2 + ", " + testSale2 + ", " + testSuppID2);
//        }
//
//        System.out.println();
//
//        //illustrates use of readRecords with multiple records result
//        ResultSet rs3 = qm.readRecords("inventory", "quantity", "10");
//        while (rs3.next()) {
//            String testProdID2 = rs3.getString(1);
//            int testQuant2 = rs3.getInt(2);
//            double testWholesale2 = rs3.getDouble(3);
//            double testSale2 = rs3.getDouble(4);
//            String testSuppID2 = rs3.getString(5);
//            System.out.println("Extract records with quantity of '10' from inventory:\n" +
//                    testProdID2 + ", " + testQuant2 + ", " + testWholesale2 + ", " + testSale2 + ", " + testSuppID2);
//        }
//
//        System.out.println();
//
//        //illustrates use of readRecords with no matching result in SQL database
//        ResultSet rs4 = qm.readRecords("inventory", "product_id", "test5678test");
//        while (rs4.next()) {
//            String testProdID2 = rs4.getString(1);
//            int testQuant2 = rs4.getInt(2);
//            double testWholesale2 = rs4.getDouble(3);
//            double testSale2 = rs4.getDouble(4);
//            String testSuppID2 = rs4.getString(5);
//            System.out.println("Test Value of 'test1234test' search in inventory spits back:\n" +
//                    testProdID2 + ", " + testQuant2 + ", " + testWholesale2 + ", " + testSale2 + ", " + testSuppID2);
//        }
//
//        System.out.println();
//
//    }
}

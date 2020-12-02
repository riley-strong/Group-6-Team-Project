import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.*;
import java.util.stream.Collectors;
import static java.time.temporal.ChronoUnit.DAYS;


public class QueryMaker {
    public static int DATE = 3;
    public static int DOUBLE = 2;
    public static int INT = 1;
    public static int STRING = 0;
    public static Statement statement;
    private static Connection connection;
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

    /**
     * quote wraps a integer value.
     *
     * @param value - user provided String value
     * @return return a quote wrapped string
     */

    public static String valueQueryPrep(String value) {
        value = "'" + value + "'";
        return value;
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

    public void batchLoading(String customer_orders_file, String dim_date_start, String dim_date_end) throws SQLException, FileNotFoundException, ClassNotFoundException {
        // Step 1: Load SQL unprocessed_sales table with Java 2-D Array using .csv file for data source.
        Object[][] objArr = this.csvToArray(customer_orders_file, new int[]{DATE, STRING, STRING, STRING, INT});
        this.setTableName("temp_unprocessed_sales");
        this.insertRows(new String[]{"date", "cust_email", "cust_location", "product_id", "product_quantity"}, objArr);

        statement.execute("CALL TEAM_6_DB.batchLoading");

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

    public void batchProcessing(int resupply_quantity, int platform) throws SQLException {
        // Step 1: Pull inventory table into Java data structure.
        ResultSet inv = statement.executeQuery("SELECT product_tid, quantity FROM inventory ");
        HashMap<Integer, Integer> invHashMap = new HashMap<>();
        Integer inv_p_tid;
        Integer inv_quant;

        while (inv.next()) {
            inv_p_tid = inv.getInt(1);
            inv_quant = inv.getInt(2);
            invHashMap.put(inv_p_tid, inv_quant);
        }
        HashMap<Integer, Integer> invHashMap_original = new HashMap<>();
        invHashMap_original.putAll(invHashMap);

        HashMap<Integer, Integer> invHashMap2 = new HashMap<>();
        invHashMap2.putAll(invHashMap);


        // Step 2: Pull unprocessed sales into Java data structure
        ResultSet us = statement.executeQuery("SELECT * FROM unprocessed_sales ORDER BY date, hashed_email");
        java.sql.Date us_date;
        String us_loc;
        int us_p_tid;
        int us_quant;
        String us_email;
        Set<Transaction> usList = new LinkedHashSet<>();

        while (us.next()) {
            us_date = us.getDate(1);
            us_loc = us.getString(2);
            us_p_tid = us.getInt(3);
            us_quant = us.getInt(4);
            us_email = us.getString(5);

            Transaction transaction = new Transaction(us_date, us_loc, us_p_tid, us_quant, us_email);
            usList.add(transaction);
        }

        ResultSet us2 = statement.executeQuery("SELECT DISTINCT product_tid, quantity FROM unprocessed_sales");
        HashMap<Integer, Integer> usalesHashMap = new HashMap<>();
        Integer usales_p_tid;
        Integer usales_quant;
        while (us2.next()) {
            usales_p_tid = us2.getInt(1);
            usales_quant = us2.getInt(2);
            usalesHashMap.put(usales_p_tid, usales_quant);
        }


        Iterator usales_iter = invHashMap2.entrySet().iterator();
        while (usales_iter.hasNext()) {
            Map.Entry a = (Map.Entry) usales_iter.next();
            if (!usalesHashMap.containsKey(a.getKey())) {
                invHashMap.remove(a.getKey());
            }
        }


        // Step 3: Iteratively compare batch orders to inventory, updating inventory & unprocessed sales Java structures
        int inv_q;
        int us_q;
        List<String[]> psList = new ArrayList<>();

        Iterator us_iter = usList.iterator();
        while (us_iter.hasNext()) {
            Transaction t = (Transaction) us_iter.next();
            us_q = t.getQuantity();
            inv_q = invHashMap.get(t.getProduct_TID());

            if (us_q <= inv_q) {
                invHashMap.put(t.getProduct_TID(), inv_q - us_q); //enough inventory in stock; process sale
                psList.add(t.processTransaction(1));
            } else {
                psList.add(t.processTransaction(0)); //not enough inventory in stock; order more from supplier
                invHashMap.put(t.getProduct_TID(), inv_q + resupply_quantity);
                ResultSet s_tid_rs = statement.executeQuery("SELECT supplier_tid FROM dim_product " +
                        "WHERE product_tid = " + t.getProduct_TID());
                int s_tid = 0;
                s_tid_rs.next();
                s_tid = s_tid_rs.getInt(1);
                statement.executeUpdate("INSERT INTO supplier_orders " +
                        "VALUES( " + valueQueryPrep(t.getDate()) + " , " + s_tid + " , " + t.getProduct_TID() + " , " + resupply_quantity + " )");
            }
        }

        //Step 4: Remove duplicates (unchanged quantities) from hash map to go into inventory SQL table.
        Iterator iter = invHashMap_original.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry p = (Map.Entry) iter.next();
            if (invHashMap.get(p.getKey()) == p.getValue())
                invHashMap.remove(p.getKey());
        }

        //Step 5: Create indexed tables to house new inventory and historic inventory values on SQL server side
        createTable("temp_inventory",
                "product_tid INT, quantity INT, INDEX temp_product_id_index (product_tid)");

        // Step 6: Prepare SQL statement with updated inventory and historic inventory values from Java data structure.
        Iterator it = invHashMap.entrySet().iterator();
        LinkedList<Object[]> inv_Array = new LinkedList<>();

        while (it.hasNext()) {
            Object[] inv_Arr = new String[2];
            Map.Entry pair = (Map.Entry) it.next();
            inv_Arr[0] = pair.getKey().toString();
            inv_Arr[1] = pair.getValue().toString();
            inv_Array.add(inv_Arr);
        }

        String[] inv_headers = {"product_tid", "quantity"};
        Object[][] inv_objects = new Object[inv_Array.size()][inv_headers.length];
        for (int j = 0; j < inv_objects.length; j++) {
            Object[] element = inv_Array.pop();
            for (int i = 0; i < inv_headers.length; i++) {
                inv_objects[j][i] = element[i];
            }
        }

        //Step 7: Insert Rows into temp_inventory SQL tables
        this.setTableName("temp_inventory");
        this.insertRows(inv_headers, inv_objects);

        //Step 8: Optimize batch processing by indexing the temporary table on SQL server side.
//        statement.executeUpdate("ALTER TABLE temp_inventory ADD INDEX `temp_product_id_index` (`product_id`) ");

        //Step 9: Alter inventory & historic inventory with new temporary values
        updateTableFromTable("inventory", "temp_inventory",
                "quantity", "quantity",
                "product_tid", "product_tid");

        //Step 10: Construct second parameter of InsertRows method (2-D Object Array)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LinkedList<Object[]> tempArray = new LinkedList<>();

        for (String[] s : psList) {
            Object[] strArr = new String[7];

            strArr[0] = valueQueryPrep(s[0]);
            strArr[1] = valueQueryPrep(LocalDateTime.parse(s[1], formatter));
            strArr[2] = valueQueryPrep(s[2]);
            strArr[3] = valueQueryPrep(Integer.parseInt(s[3]));
            strArr[4] = valueQueryPrep(Integer.parseInt(s[4]));
            strArr[5] = valueQueryPrep(Integer.parseInt(s[5]));
            strArr[6] = valueQueryPrep(s[6]);
            tempArray.add(strArr);
        }

        String[] headers = {"date", "processed_dt", "cust_location", "product_tid", "quantity", "result", "hashed_email"};
        Object[][] objects = new Object[tempArray.size()][headers.length];
        for (int j = 0; j < objects.length; j++) {
            Object[] element = tempArray.pop();
            for (int i = 0; i < headers.length; i++) {
                objects[j][i] = element[i];
            }
        }

        //Step 11: Insert Rows into processed_sales SQL table
        this.setTableName("processed_sales");
        this.insertRows(headers, objects);

        //Step 11.5: If email orders, add to daily assets file:
        if (platform == 2)
            statement.execute("CALL TEAM_6_DB.emailAssetAddition()");

        //Step 12: Truncate unprocessed_sales table.
        statement.executeUpdate("TRUNCATE unprocessed_sales");

        //Step 13: Delete the temporary inventory and temporary historic inventory tables
        deleteTable("temp_inventory");

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
    public void createDatabaseStructure(String inventory_file) throws SQLException, FileNotFoundException {
        // Step 1:
        statement.execute("CALL TEAM_6_DB.createDB_Structure");

        // Step 10: Load temp_inventory table with Java 2-D Array using .csv file for data source.
        Object[][] objArr = this.csvToArray(inventory_file, new int[]{STRING, INT, DOUBLE, DOUBLE, STRING});
        this.setTableName("temp_inventory");
        this.insertRows(new String[]{"product_id", "quantity", "wholesale_cost", "sale_price", "supplier_id"}, objArr);
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
        statement.executeUpdate("CREATE TABLE " + tableName + " ( " + columnSpecs + " )");
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

        while (fileReader.hasNextLine()) {
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
        statement.executeUpdate("DELETE FROM " + tableName + " WHERE " + columnName + " = " + valueQueryPrep(value));
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
     * deletes the entire table given the table name.
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
     * call setTableName() to proceed to use this method.
     * displays the information in the IDE console from the SQL table.
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
     * helper method for getProduct() method.
     * returns a 2D object array of information taking the following arguments.
     *
     * @param rs          - must provide a result set.
     * @param isOneColumn - boolean value if the extracted information is one column or greater.
     * @return 2D object results of data.
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

    public void generateDailyAssets(String start, String end) throws SQLException {
        LocalDate start_ld = LocalDate.parse(start);
        LocalDate end_ld = LocalDate.parse(end);
        int stopPoint = (int) DAYS.between(start_ld, end_ld) + 1;
        for (int i = 0; i < stopPoint; i++) {
            statement.executeUpdate("CALL TEAM_6_DB.generateDailyAssets( '" + start_ld.toString() + "')");
            start_ld = start_ld.plusDays(1);
        }
    }

    /**
     * given an argument (any SQL statement) it will return a table of useful data.
     *
     * @param s - any SQL syntax commands.
     * @return returns a table of data that is scrollable.
     * @throws SQLException
     */

    public ResultSet generateQuery(String s) throws SQLException {
        Statement st = connection.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        return st.executeQuery(s);

    }

    /**
     * given an argument (any SQL statement) it will update any changes to a specified target.
     *
     * @param s - any SQL syntax commands.
     * @throws SQLException
     */

    public void generateUpdate(String s) throws SQLException {
        Statement st = connection.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        st.execute(s);
    }

    /**
     * call setTableName() to proceed to use this method.
     * based on the table name we can return the column names of that table.
     *
     * @return tables column names
     * @throws SQLException
     */

    public String[] getColumnNames() throws SQLException {
        ResultSet rs = generateQuery("SELECT `COLUMN_NAME` FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE `TABLE_SCHEMA`='TEAM_6_DB' AND `TABLE_NAME`='" + this.tableName + "'");
        String[] columnNames = new String[rowCountResults(rs)];
        for (int j = 0; rs.next(); j++) {
            columnNames[j] = rs.getString(1);
        }
        return columnNames;
    }

    public ArrayList<Object[]> getAnalyticsData(String start, String end, int choice) throws SQLException {
        LocalDate start_ld = LocalDate.parse(start);
        LocalDate end_ld = LocalDate.parse(end);
        ArrayList<Object[]> al = new ArrayList<>();

        statement.executeUpdate("SET @startDate = '" + start_ld.toString() + "'");
        statement.executeUpdate("SET @endDate = '" + end_ld.toString() + "'");
        Object[] arr = new Object[6];
        if (choice == 1) {
            ResultSet rs = statement.executeQuery("call TEAM_6_DB.specificDailyAssets(@startDate, @endDate)");

            while (rs.next()) {
                arr = new Object[6];
                for (int i = 0; i < arr.length; i++) {
                    if (i < arr.length - 1)
                        arr[i] = rs.getInt(i + 1);
                    else
                        arr[i] = rs.getDouble(i + 1);
                }
                al.add(arr);
            }
        } else if (choice == 2) {
            ResultSet rs = statement.executeQuery("call TEAM_6_DB.daily_orders(@startDate, @endDate)");

            while (rs.next()) {
                arr = new Object[6];
                for (int i = 0; i < arr.length; i++) {
                    arr[i] = rs.getInt(i + 1);
                }
                al.add(arr);
            }
        } else if (choice == 3) {
            ResultSet rs = statement.executeQuery("call TEAM_6_DB.daily_purchases(@startDate, @endDate)");

            while (rs.next()) {
                arr = new Object[6];
                for (int i = 0; i < arr.length; i++) {
                    if (i < arr.length - 1)
                        arr[i] = rs.getInt(i + 1);
                    else
                        arr[i] = rs.getDouble(i + 1);
                }
                al.add(arr);
            }
        }
        return al;
    }

    /**
     * uses the extractResults() method to help return the information searched for
     * returns the information searched for given the following arguments below:
     *
     * @param tableName   - specify the name of the table.
     * @param columnName  - specify the name of the column within the table.
     * @param columnValue - specify any object type searched for (int, String etc).
     * @return calls on a helper method to return a nice table of information.
     * @throws SQLException
     */

    public Object[][] getProduct(final String tableName, final String columnName, Object columnValue) throws SQLException {
        ResultSet rs = generateQuery("SELECT * FROM " + tableName + " WHERE " + columnName + " = " + quoteWrap(columnValue));
        String s = "select count(*) from " + tableName + " where " + columnName + " = " + getString(columnValue);
        ResultSet resultCount = generateQuery(s);
        return extractResults(rs, true);
    }

    /**
     * quote wraps a specified String or Date based on the argument below.
     *
     * @param columnValue - any column containing a String or Date will be wrapped in quotes for SQL syntax.
     * @return
     */

    private String getString(Object columnValue) {
        if (columnValue instanceof String || columnValue instanceof LocalDate) {
            return "'" + columnValue + "'";
        }
        return columnValue.toString();
    }

    /**
     * getter method for returning the table name.
     *
     * @return - any table name found in the database.
     */

    public String getTableName() {
        return tableName;
    }

    /**
     * setter method for table name.
     * ALWAYS SET THE TABLE NAME BEFORE PROCEEDING WITH ANYTHING ELSE.
     *
     * @param tableName - String name given from user.
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * call setTableName() to proceed to use this method.
     * loads specified rows of information in a table given the following arguments.
     *
     * @param columnNames - names of all the columns.
     * @param rows        - 2D array of all the information to add.
     * @throws SQLException
     */

    public void insertRows(String[] columnNames, Object[][] rows) throws SQLException {

        StringBuilder builder = new StringBuilder();
        String s = Arrays.toString(columnNames);
        builder.append(" INSERT INTO " + tableName + " ( " + s.substring(1, s.length() - 1) + " ) VALUES ");
        for (int i = 0; i < rows.length; i++) {

            if (i % 2000 != 0 || i == 0) {
                String s1 = Arrays.deepToString(rows[i]);
                builder.append(" ( " + s1.substring(1, s1.length() - 1) + " )");
                if (i < rows.length - 1) {
                    builder.append(",");
                }
            } else {
                builder.setLength(builder.length() - 1);
                String s2 = builder.toString();
                generateUpdate(s2);

                builder.setLength(0);
                builder.append(" INSERT INTO " + tableName + " ( " + s.substring(1, s.length() - 1) + " ) VALUES ");
            }
        }
        if (builder.length() > 0) {
            builder.setLength(builder.length());
            String s2 = builder.toString();
            generateUpdate(s2);
        }
    }

    /**
     * add new information into a table with unspecified column given the two following arguments:
     *
     * @param tableName - name of the table.
     * @param values    - the values to be added.
     * @throws SQLException
     */

    public void insertRecordIntoTable(String tableName, String values) throws SQLException {
        statement.executeUpdate("INSERT INTO " + tableName + " VALUES ( " + values + " ) ");
    }

    /**
     * add new information into a table with the specified column name given the three following arguments:
     *
     * @param tableName   - name of table.
     * @param columnNames - name of column in the table.
     * @param values      - the values to be added.
     * @throws SQLException
     */

    public void insertValuesIntoTable(String tableName, String columnNames, String values) throws SQLException {
        statement.executeUpdate("INSERT INTO " + tableName + " ( " + columnNames + " ) VALUES ( " + values + " ) ");
    }

    public void processEmails() throws SQLException {
        statement.execute("CALL TEAM_6_DB.emailLoading();");
    }

    /**
     * If the information is of type String or LocalDate then it will be formatted for SQL readable syntax.
     *
     * @param columnValue - value must be of type String or LocalDate.
     * @return returns a string of SQL friendly syntax.
     */
    private String quoteWrap(Object columnValue) {
        if (columnValue instanceof String || columnValue instanceof LocalDate) {
            return "'" + columnValue + "'";
        } else {
            return columnValue + "";
        }
    }

    /**
     * searches a table for specific information and returns any results given the following arguments:
     *
     * @param tableName   - name of table.
     * @param whereClause - what user is searching for.
     * @param value       - what user search is checked against.
     * @return return a table of data.
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
     * @param tableName - name of table.
     * @return return a table of data - result set.
     * @throws SQLException
     */

    public ResultSet readTable(String tableName) throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName);
        return rs;

    }

    /**
     * reads a table based on a given condition.
     *
     * @param tableName - name of table.
     * @param condition - a condition can be weather something is true for false or (0 or 1).
     * @return a table of data - result set.
     * @throws SQLException
     */

    public ResultSet readTableWithCond(String tableName, String condition) throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName + " " + condition);
        return rs;
    }

    /**
     * searches a table for a value based on column name.
     *
     * @param columnName  - name of column from table.
     * @param tableName   - name of table.
     * @param whereClause - what the user is searching for in the column.
     * @return returns a column of data.
     * @throws SQLException
     */

    //use when assuming needing all matches for one column
    public ResultSet readValues(String columnName, String tableName, String whereClause) throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT " + columnName + " FROM " + tableName + " WHERE " + whereClause);
        return rs;
    }

    /**
     * returns the number of rows in the result set.
     *
     * @param rs - specify a result set.
     * @return return the number or rows in that result set.
     * @throws SQLException
     */
    int rowCountResults(ResultSet rs) throws SQLException {
        rs.last();
        int countRows = rs.getRow();
        rs.beforeFirst();
        return countRows;
    }

    /**
     * creates a new temporary table that will fill information on the top ten customers.
     * takes on the customer information: date , customer email, total purchased.
     *
     * @throws SQLException
     */

    public void topTenCustomers(String theDate) throws SQLException {
        LocalDate theDate_ld = LocalDate.parse(theDate);

        statement.executeUpdate("SET @theDate = '" + theDate_ld.toString() + "'");

        ResultSet rs = statement.executeQuery("CALL TEAM_6_DB.topTenCustomers(@theDate) ");

        System.out.println("\nThe top ten customers for " + theDate + " are:");
        System.out.printf("%-25s %-25s %-25s",
                "Date", "Customer Email", "Total Purchased");
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        while (rs.next()) {
            System.out.printf("\n%-25s %-25s %-25s",
                    rs.getDate(1).toString(), rs.getString(2), formatter.format(rs.getDouble(3)));
        }
        System.out.println();
    }

    /**
     * creates a new temporary table that will fill information on the top ten products.
     * takes on the customer information: date , product, total sold.
     *
     * @throws SQLException
     */

    public void topTenProducts(String theDate) throws SQLException {
        LocalDate theDate_ld = LocalDate.parse(theDate);

        statement.executeUpdate("SET @theDate = '" + theDate_ld.toString() + "'");

        ResultSet rs = statement.executeQuery("CALL TEAM_6_DB.topTenProducts(@theDate) ");

        System.out.println("\nThe top ten products for " + theDate + " are:");
        System.out.printf("%-25s %-25s %-25s",
                "Date", "Product", "Total Sold");
        while (rs.next()) {
            System.out.printf("\n%-25s %-25s %-25s",
                    rs.getDate(1).toString(), rs.getString(2), rs.getString(3));
        }
        System.out.println();
    }

    /**
     * populate one tables information into another with the original tables information.
     *
     * @param tableName1        - original table name.
     * @param tableName2        - new table name.
     * @param setColumnNameT1   - first column name.
     * @param setColumnNameT2   - second column name.
     * @param whereColumnNameT1 - old column name 1.
     * @param whereColumnNameT2 - old column name 2.
     * @throws SQLException
     */

    public void updateTableFromTable(String tableName1, String tableName2, String setColumnNameT1, String setColumnNameT2, String whereColumnNameT1, String whereColumnNameT2) throws SQLException {
        statement.executeUpdate("UPDATE " + tableName1 + ", " + tableName2 + " " +
                "SET " + tableName1 + "." + setColumnNameT1 + " = " + tableName2 + "." + setColumnNameT2 + " " +
                "WHERE " + tableName1 + "." + whereColumnNameT1 + " = " + tableName2 + "." + whereColumnNameT2);
    }

    /**
     * populate one tables information into another with the original tables information such that a condition is met.
     *
     * @param tableName1        - original table name.
     * @param setColumnNameT1   - new column name.
     * @param value             - values to be added.
     * @param whereColumnNameT1 - old column name.
     * @param condition         - may be true false condition.
     * @throws SQLException
     */

    public void updateTableFromStatic(String tableName1, String setColumnNameT1, String value, String whereColumnNameT1, String condition) throws SQLException {
        statement.executeUpdate("UPDATE " + tableName1 +
                " SET " + tableName1 + "." + setColumnNameT1 + " = " + valueQueryPrep(value) + " " +
                " WHERE " + tableName1 + "." + whereColumnNameT1 + " = " + valueQueryPrep(condition));
    }

    /**
     * searches a table for a DECIMAL value and returns true/false based on the given arguments below:
     *
     * @param columnName - name of column.
     * @param tableName  - name of table.
     * @param value      - the value being searched by the user.
     * @return
     * @throws SQLException
     */


    public Boolean valueExists(String columnName, String tableName, double value) throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT COUNT( " + columnName + " ) FROM " + tableName + " WHERE " + columnName + " = " + valueQueryPrep(value));
        int temp = 0;
        while (rs.next()) {
            temp++;
        }
        return temp > 0;
    }

    /**
     * searches a table for an INTEGER value and returns true/false based on the given arguments below:
     *
     * @param columnName - name of column.
     * @param tableName  - name of table.
     * @param value      - the value being searched by the user.
     * @return boolean value.
     * @throws SQLException
     */

    public Boolean valueExists(String columnName, String tableName, int value) throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT COUNT( " + columnName + " ) FROM " + tableName + " WHERE " + columnName + " = " + valueQueryPrep(value));
        int temp = 0;
        while (rs.next()) {
            temp++;
        }
        return temp > 0;
    }

    /**
     * searches a table for a STRING value and returns true/false based on the given arguments below:
     *
     * @param columnName - name of column.
     * @param tableName  - name of table.
     * @param value      - the value being searched by the user.
     * @return boolean value.
     * @throws SQLException
     */

    public Boolean valueExists(String columnName, String tableName, String value) throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT COUNT( " + columnName + " ) FROM " + tableName + " WHERE " + columnName + " = " + valueQueryPrep(value));
        int temp = 0;
        while (rs.next()) {
            temp++;
        }
        return temp > 0;
    }

    /**
     * formats and parses the date and time based on the format year-month-day hour:minute:second.
     *
     * @param value - the date time value given from the user.
     * @return return the parsed time/date format.
     */

    public String valueQueryPrep(LocalDateTime value) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
        String staging = value.format(formatter);
        String result = "'" + staging + "'";
        return result;
    }

    /**
     * formats and parses the date based on the format year-month-day.
     *
     * @param value - user provided value.
     * @return return the parsed date format.
     */

    public String valueQueryPrep(Date value) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String staging = dateFormat.format(value);
        String result = "'" + staging + "'";
        return result;
    }

    /**
     * wrapper class for a millisecond value that is recognized as SQL format.
     *
     * @param value - SQL provided value.
     * @return returns the proper year-month-day format.
     */

    public String valueQueryPrep(java.sql.Date value) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String staging = dateFormat.format(value);
        String result = "'" + staging + "'";
        return result;
    }

    /**
     * quote wraps a decimal value.
     *
     * @param value - user provided decimal value.
     * @return return a quote wrapped string.
     */

    public String valueQueryPrep(double value) {
        String result = "'" + value + "'";
        return result;
    }

    /**
     * quote wraps a integer value.
     *
     * @param value - user provided integer value.
     * @return return a quote wrapped string.
     */

    public String valueQueryPrep(int value) {
        String result = "'" + value + "'";
        return result;
    }
}

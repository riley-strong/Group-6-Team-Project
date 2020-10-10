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
    private final Connection connection;
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



    public QueryMaker(String userName, String password, String ipAddress, String portNumber, String databaseName) throws ClassNotFoundException, SQLException {

        Class.forName("com.mysql.cj.jdbc.Driver");
        String getURL = "jdbc:mysql://" + ipAddress + ":" + portNumber + "/" + databaseName;
        connection = DriverManager.getConnection(getURL, userName, password);
        //System.out.println("Connection Succesful");
    }

    public boolean contains(String productID) throws SQLException {
        ResultSet rs = generateQuery("SELECT * FROM inventory WHERE product_id = '" + productID + "'");
        return rs.next();
    }

    // This method takes in a .csv file and turns it into a 2D array with the specified column types.
    // Making the format compatible for SQL to read
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
    public void deleteRows() throws SQLException {
        generateUpdate("DELETE FROM " + tableName);
    }

    // once setTableName() has been called then displayFile will print the table the to console
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
    private void displayTableNames() throws SQLException {
        ResultSet tables = generateQuery("SHOW TABLES FROM TEAM_6");
        while (tables.next()) {
            String tblName = tables.getString(1);
            System.out.println(tblName);
        }
    }
    // helper method for getproduct() method
    private Object[][] extractResults(ResultSet rs, Boolean isOneColumn) throws SQLException {
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

    public ResultSet generateQuery(String s) throws SQLException {
        Statement st = connection.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        return st.executeQuery(s);

    }

    public int generateUpdate(String s) throws SQLException {
        Statement st = connection.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        return st.executeUpdate(s);
    }

    /**
     * Gets an array of the column names of a specific table
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
    public Object[][] getProduct(final String tableName, final String columnName, Object columnValue) throws SQLException {
        ResultSet rs = generateQuery("SELECT * FROM " + tableName + " WHERE " + columnName + " = " + quoteWrap(columnValue));
        String s = "select count(*) from " + tableName + " where " + columnName + " = " + getString(columnValue);
        ResultSet resultCount = generateQuery(s);
        return extractResults(rs, true);
    }

    private String getString(Object columnValue) {
        if (columnValue instanceof String || columnValue instanceof LocalDate) {
            return "'" + columnValue + "'";
        }
        return columnValue.toString();
    }

    // inserts rows into the specified table
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

    public static void main(String[] args) throws SQLException, ClassNotFoundException, FileNotFoundException {
        QueryMaker qm = Credentials.databaseLogin();


      //qm.generateUpdate("CREATE TABLE inventory (idx int(16)  NOT NULL AUTO_INCREMENT, product_id " +
                //"VARCHAR(16),quantity int(16),wholesale_cost decimal(13,2),sale_price decimal(13,2),supplier_id VARCHAR(32), PRIMARY KEY (idx));");
        //qm.setTableName("inventory");
        //qm.insertRows(new String[]{"product_id","quantity","wholesale_cost","sale_price","supplier_id"},
              // qm.csvToArray("inventory_team6.csv", new int[]{STRING, INT, DOUBLE, DOUBLE, STRING}));

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
    private static String removeUTF8BOM(String s) {
        if (s.startsWith("\uFEFF")) {
            s = s.substring(1);
        }
        return s;
    }

    /**
     * retrieve the number of rows of in a ResultSet.
     */
    int rowCountResults(ResultSet rs) throws SQLException {
        rs.last();
        int countRows = rs.getRow();
        rs.beforeFirst();
        return countRows;
    }

    //ALWAYS declare the table name before proceeding with anything else!!!
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    // takes a table from the database and creates a file with the pathname of your choosing
    private void tableToFile(String pathname) throws FileNotFoundException, SQLException {
        ResultSet rs = generateQuery("SELECT * FROM " + tableName);
        PrintWriter writer = new PrintWriter(new File(pathname));
        int columnCount = getColumnNames().length;
        for (int i = 0; i < columnCount; i++) {
            writer.print(rs.getMetaData().getColumnName(i + 1));
            writer.print(i == columnCount - 1 ? "\n" : ",");
        }
        while (rs.next()) {
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                writer.print(removeUTF8BOM(rs.getObject(i + 1) + ""));
                if (i < rs.getMetaData().getColumnCount() - 1) {
                    writer.print(",");
                }
            }
            writer.print("\n");
        }
        writer.close();
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


//(Stream.of(getColumnNames()).collect(Collectors.joining(","))) + "\n"
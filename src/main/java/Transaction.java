import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {

    private java.sql.Date date_id;
    private String cust_location;
    private int product_tid;
    private String product_id;
    private int quantity;
    private String hashed_email;

    /**
     * Constructor with parameters (assuming all params provided)
     *
     * @param date_id       - java.sql.Date
     * @param cust_location - customer location; String type
     * @param product_tid   - id representing product in SQL database
     * @param quantity      - product quantity; initial value must be integer >= 0
     * @param hashed_email  - Hashed String representing customer email in SQL database
     */

    public Transaction(java.sql.Date date_id, String cust_location, int product_tid, int quantity, String hashed_email) {
        setDate(date_id);
        setCustLocation(cust_location);
        setProductID(product_tid);
        setQuantity(quantity);
        setCustEmail(hashed_email);
    }

    /**
     * Constructor with parameters (assuming all params provided)
     *
     * @param date_id       - java.sql.Date
     * @param cust_location - customer location; String type
     * @param product_id    - id representing product in SQL database
     * @param quantity      - product quantity; initial value must be integer >= 0
     * @param hashed_email  - Hashed String representing customer email in SQL database
     */

    public Transaction(java.sql.Date date_id, String cust_location, String product_id, int quantity, String hashed_email) {
        setDate(date_id);
        setCustLocation(cust_location);
        setProductID(product_id);
        setQuantity(quantity);
        setCustEmail(hashed_email);
    }

    /**
     * @return - String representation of a hashed customer email
     */

    public String getCustEmail() {
        return this.hashed_email;
    }

    /**
     * @param hashed_email sets the customer email with the argument of a hashed email address of integer type.
     *                     integer types defined in QueryMaker.java
     */

    public void setCustEmail(String hashed_email) {
        this.hashed_email = hashed_email;
    }

    /**
     * @return customer location in a string format
     */

    public String getCustLocation() {
        return this.cust_location;
    }

    /**
     * @param cust_location sets the customer location with the argument of customer location of integer type.
     *                      integer types defined in QueryMaker.java
     */

    public void setCustLocation(String cust_location) {
        this.cust_location = cust_location;
    }

    /**
     * @return java.sql.Date
     */

    public java.sql.Date getDate() {
        return this.date_id;
    }

    /**
     * @param date_id sets the representing date in SQL table with the sql date argument using an integer type.
     *                integer types defined in QueryMaker.java
     */

    public void setDate(java.sql.Date date_id) {
        this.date_id = date_id;
    }

    /**
     * @return Product temporary ID
     */

    public int getProduct_TID() {
        return this.product_tid;
    }

    /**
     * @return Product ID
     */

    public String getProduct_ID() {
        return this.product_id;
    }

    /**
     * @return integer value of product quantity
     */

    public int getQuantity() {
        return this.quantity;
    }

    /**
     * @param quantity Sets the product quantity using the quantity argument of type integer.
     */

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * @param product_tid sets the temporary product Id taking an integer value argument.
     */

    public void setProductID(int product_tid) {
        this.product_tid = product_tid;
    }

    /**
     * @param product_id Sets the the product Id taking the string argument of the product id using an integer type.
     *                   integer types defined in QueryMaker.java
     */

    public void setProductID(String product_id) {
        this.product_id = product_id;
    }

    /**
     * @param x - 1 value means transaction process 0 value means transaction never processed
     * @return a string array of specific formatted date, result, customer location, product Id, quantity and email
     */

    public String[] processTransaction(int x) {
        LocalDateTime dt = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
        String dt_staging = dt.format(formatter);
        String dt_result = dt_staging;

        return new String[]{String.valueOf(getDate()), dt_result, getCustLocation(), String.valueOf(getProduct_TID()),
                String.valueOf(getQuantity()), Integer.toString(x), getCustEmail()};
    }
}
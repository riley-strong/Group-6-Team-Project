import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.*;

public class Transaction {

    //variable initialization for headers needed in transaction
    private java.sql.Date date;
    private String cust_email;
    private String cust_location;
    private String product_id;
    private int product_quantity;

    public static final String[] headers = new String[]{"date","cust_email","cust_location","product_id","product_quantity"};

    /**
     * Constructor with parameters (assuming all params provided)
     *  @param date  LocalDate
     * @param custEmail  String
     * @param custLocation  customer location; String type
     * @param productID  8-character alphanumeric that will assist in representing this object in memory
     * @param productQuantity  product quantity; initial value must be integer >= 0
     */
    public Transaction(java.sql.Date date, String custEmail, String custLocation, String productID, int productQuantity){
        this.date = date;
        this.cust_email = custEmail;
        this.cust_location = custLocation;
        this.product_id = productID;
        this.product_quantity = productQuantity;
    }

    /**
     *
     * @return  customer e-mail
     */
    public String getCust_email() {
        return cust_email;
    }

    /**
     *
     * @param cust_email  customer e-mail; int type
     */
    public void setCust_email(String cust_email) {
        this.cust_email = cust_email;
    }

    /**
     *
     * @return customer location
     */
    public String getCust_location() {
        return cust_location;
    }

    /**
     *
     * @param cust_location customer location; int type
     */
    public void setCust_location(String cust_location) {
        this.cust_location = cust_location;
    }

    /**
     *
     * @return Product ID
     */
    public String getProduct_id() {
        return product_id;
    }

    /**
     *
     * @param product_id Product ID; String type
     */
    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    /**
     *
     * @return Product Quantity
     */
    public int getProduct_quantity() {
        return product_quantity;
    }

    /**
     *
     * @param product_quantity Product Quantity; int type
     */
    public void setProduct_quantity(int product_quantity) {
        this.product_quantity = product_quantity;
    }

    /**
     *
     * @return  Output is an array version of the headers used in transaction
     */
    public static String[] getHeaders() {
        return headers;
    }

    /**
     *
     * @return LocalDate
     */
    public java.sql.Date getDate() { return date; }

    /**
     *
     * @param date Localdate; date type
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     *
     * @return  Output is an array version of a transaction object
     */
    public Object[] toArray(){
        return new Object[]{date,cust_email,cust_location,product_id,product_quantity};
    }

    public String[] processTransaction(int x){
        LocalDateTime dt = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
        String dt_staging = dt.format(formatter);
        String dt_result = dt_staging;

        LocalDate local_date = this.date.toLocalDate();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd");
        String date_staging = local_date.format(dateFormat);
        String date_result =  date_staging;

        String result_bit = Integer.toString(x);

        return new String[]{date_result, dt_result, this.cust_email,this.cust_location,this.product_id,
                String.valueOf(this.product_quantity), result_bit};
    }
}//end of Transaction Class

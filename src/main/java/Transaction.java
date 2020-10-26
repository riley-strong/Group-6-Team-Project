import java.time.LocalDate;

public class Transaction {

    //variable initialization for headers needed in transaction
    private LocalDate date;
    private String cust_email;
    private int cust_location;
    private String product_id;
    private int product_quantity;

    public static final String[] headers = new String[]{"date","cust_email","cust_location","product_id","product_quantity"};

    /**
     * Constructor with parameters (assuming all params provided)
     *
     * @param date  LocalDate
     * @param custEmail  String
     * @param custLocation  customer location; int type
     * @param productID  8-character alphanumeric that will assist in representing this object in memory
     * @param productQuantity  product quantity; initial value must be integer >= 0
     */
    public Transaction(LocalDate date, String custEmail, int custLocation, String productID, int productQuantity){
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
    public int getCust_location() {
        return cust_location;
    }

    /**
     *
     * @param cust_location customer location; int type
     */
    public void setCust_location(int cust_location) {
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
    public LocalDate getDate() {
        return date;
    }

    /**
     *
     * @param date Localdate; date type
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
     *
     * @return  Output is an array version of a transaction object
     */
    public Object[] toArray(){
        return new Object[]{date,cust_email,cust_location,product_id,product_quantity};
    }
}//end of Transaction Class

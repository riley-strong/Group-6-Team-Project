import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {

    //variable initialization for headers needed in transaction
    private java.sql.Date date;
    private String cust_email;
    private String cust_location;
    private String product_id;
    private int product_quantity;

    /**
     * Constructor with parameters (assuming all params provided)
     *  @param date  java.sql.date
     * @param custEmail  String
     * @param custLocation  customer location; String type
     * @param productID  8-character alphanumeric that will assist in representing this object in memory
     * @param productQuantity  product quantity; initial value must be integer >= 0
     */
    public Transaction(java.sql.Date date, String custEmail, String custLocation, String productID, int productQuantity){
        setDate(date);
        setCust_email(custEmail);
        setCust_location(custLocation); ;
        setProduct_id(productID);
        setProduct_quantity(productQuantity);
    }

    /**
     *
     * @return  customer e-mail
     */
    public String getCust_email() {
        return this.cust_email;
    }

    /**
     *
     * @return customer location
     */
    public String getCust_location() {
        return this.cust_location;
    }

    /**
     *
     * @return LocalDate
     */
    public java.sql.Date getDate() { return this.date; }

    /**
     *
     * @return Product ID
     */
    public String getProduct_id() {
        return this.product_id;
    }

    /**
     *
     * @return Product Quantity
     */
    public int getProduct_quantity() {
        return this.product_quantity;
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
     * @param cust_location customer location; int type
     */
    public void setCust_location(String cust_location) {
        this.cust_location = cust_location;
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
     * @param product_quantity Product Quantity; int type
     */
    public void setProduct_quantity(int product_quantity) {
        this.product_quantity = product_quantity;
    }

    /**
     *
     * @param date Localdate; date type
     */
    public void setDate(java.sql.Date date) {
        this.date = date;
    }


    public String[] processTransaction(int x){
        LocalDateTime dt = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
        String dt_staging = dt.format(formatter);
        String dt_result = dt_staging;

        LocalDate local_date = getDate().toLocalDate();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd");
        String date_staging = local_date.format(dateFormat);
        String date_result =  date_staging;

        String result_bit = Integer.toString(x);

        return new String[]{date_result, dt_result, getCust_email(),getCust_location(),getProduct_id(),
                String.valueOf(getProduct_quantity()), result_bit};
    }
}

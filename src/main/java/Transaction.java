import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {

    //variable initialization for headers needed in transaction
    private java.sql.Date date_id;
    private String cust_location;
    private int product_tid;
    private String product_id;
    private int quantity;
    private String hashed_email;

    /**
     * Constructor with parameters (assuming all params provided)
     * @param cust_location  customer location; String type
     * @param product_tid  id representing product in SQL database
     * @param quantity  product quantity; initial value must be integer >= 0
     * @param hashed_email  Hashed String representing customer email in SQL database
     */
    public Transaction(java.sql.Date date_id, String cust_location, int product_tid, int quantity, String hashed_email){
        setDate(date_id);
        setCustLocation(cust_location); ;
        setProductID(product_tid);
        setQuantity(quantity);
        setCustEmail(hashed_email);
    }

    public Transaction(java.sql.Date date_id, String cust_location, String product_id, int quantity, String hashed_email){
        setDate(date_id);
        setCustLocation(cust_location); ;
        setProductID(product_id);
        setQuantity(quantity);
        setCustEmail(hashed_email);
    }

    /**
     *
     * @return  hashed String representing customer e-mail
     */
    public String getCustEmail() {
        return this.hashed_email;
    }

    /**
     *
     * @return customer location
     */
    public String getCustLocation() {
        return this.cust_location;
    }

    /**
     *
     * @return LocalDate
     */
    public java.sql.Date getDate() { return this.date_id; }

    /**
     *
     * @return Product ID
     */
    public int getProduct_TID() {
        return this.product_tid;
    }

    public String getProduct_ID(){
        return this.product_id;
    }
    /**
     *
     * @return Product Quantity
     */
    public int getQuantity() {
        return this.quantity;
    }


    /**
     *
     * @param hashed_email  customer e-mail; int type
     */
    public void setCustEmail(String hashed_email) {
        this.hashed_email = hashed_email;
    }

    /**
     *
     * @param cust_location customer location; int type
     */
    public void setCustLocation(String cust_location) {
        this.cust_location = cust_location;
    }

    /**
     *
     * @param date_id id representing date in SQL table; int type
     */
    public void setDate(java.sql.Date date_id) {
        this.date_id = date_id;
    }

    /**
     *
     * @param product_tid Product ID; int type
     */
    public void setProductID(int product_tid) {
        this.product_tid = product_tid;
    }

    public void setProductID(String product_id){
        this.product_id = product_id;
    }

    /**
     *
     * @param quantity Product Quantity; int type
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    public String[] processTransaction(int x){
        LocalDateTime dt = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
        String dt_staging = dt.format(formatter);
        String dt_result = dt_staging;

        return new String[]{String.valueOf(getDate()), dt_result, getCustLocation(), String.valueOf(getProduct_TID()),
                String.valueOf(getQuantity()), Integer.toString(x), getCustEmail()};
    }
}

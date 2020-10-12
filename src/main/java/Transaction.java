import java.time.LocalDate;

public class Transaction {

    private LocalDate date;
    private String cust_email;
    private int cust_location;
    private String product_id;
    private int product_quantity;

public static final String[] headers = new String[]{"date","cust_email","cust_location","product_id","product_quantity"};

public Transaction(LocalDate date, String custEmail, int custLocation, String productID, int productQuantity){
    this.date = date;
    this.cust_email = custEmail;
    this.cust_location = custLocation;
    this.product_id = productID;
    this.product_quantity = productQuantity;
}

    public String getCust_email() {
        return cust_email;
    }

    public void setCust_email(String cust_email) {
        this.cust_email = cust_email;
    }

    public int getCust_location() {
        return cust_location;
    }

    public void setCust_location(int cust_location) {
        this.cust_location = cust_location;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public int getProduct_quantity() {
        return product_quantity;
    }

    public void setProduct_quantity(int product_quantity) {
        this.product_quantity = product_quantity;
    }

    public static String[] getHeaders() {
        return headers;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
    public Object[] toArray(){
    return new Object[]{date,cust_email,cust_location,product_id,product_quantity};
    }
}

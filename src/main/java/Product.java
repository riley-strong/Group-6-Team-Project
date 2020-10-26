/*
 * For the purpose of MSU Denver, Fall 2020, CS 3250-52681 course with Dr. Geinitz
 * Contributors include Hector Cruz; Riley Strong; Firew Handiso; Busra Ozdemir; Adam Wojdyla; Dakota Miller
 */

/**
 * Sets the functions and scope of the Product object class.
 */
public class Product {

    //variable initialization
    private int quantity;
    private double wholesale;
    private double salePrice;
    private String supplierID;
    private String productID;

    public static final String[] headers = new String[]{"product_id","quantity","wholesale_cost",
            "sale_price","supplier_id"};

    /**
     * No-arg constructor for Product object.
     */
    public Product() {
        setProductID("NA12345678NA");
        setQuantity(0);
        setWholesale(0.00);
        setSalePrice(0.00);
        setSupplierID("NA1234NA");
    }
    /**
     * Constructor for product object (assuming original product type & all params provided).
     *
     * @param initProductID     12-character unique alphanumeric that will represent this object in memory
     * @param initQuantity      product quantity; initial value must be integer >= 0
     * @param initWholesale     product wholesale (price purchased from supplier); initial value must be double >= 0.00
     * @param initSalePrice     product sale price (price sold to customers); initial value must be double >= 0.00
     * @param initSupplierID    8-character alphanumeric that will assist in representing this object in memory
     */
    public Product(String initProductID, int initQuantity, double initWholesale, double initSalePrice, String initSupplierID) {
        setProductID(initProductID);
        setQuantity(initQuantity);
        setWholesale(initWholesale);
        setSalePrice(initSalePrice);
        setSupplierID(initSupplierID);
    }
    /**
     * Creates a Product object array for use in Inventory.java to create a 2D-array
     *
     * @return      Output is an array version of an individual Product object
     */
    public Object[] toArray() {
        return new Object[]{productID, quantity, wholesale, salePrice, supplierID};
    }

    /**
     * Returns object-specific product ID (12-digit alphanumeric).
     *
     * @return  Returns product object's Product ID instance variable
     */
    public String getProductID() { return productID; }
    /**
     * Assumes valid input passed in, sets productID instance variable for a Product object
     *
     * @param productID     a valid product ID (as of Oct 2020, 12-digit alphanumeric)
     */
    public void setProductID(String productID) {
        this.productID = productID.toUpperCase();
    }
    /**
     * Returns object-specific quantity (should be integer >= 0).
     *
     * @return      Returns product object's quantity instance variable
     */
    public int getQuantity() {
        return quantity;
    }
    /**
     * Assumes valid input passed in, sets quantity instance variable for a Product object
     *
     * @param newQuantity     a valid quantity (as of Oct 2020, integer >= 0)
     */
    public void setQuantity(int newQuantity) {
        this.quantity = newQuantity;
    }
    /**
     * Returns object-specific wholesale price (should be double >= 0.00).
     *
     * @return      Returns product object's wholesale price instance variable
     */
    public double getWholesale() {
        return wholesale;
    }
    /**
     * Assumes valid input passed in, sets wholesale price instance variable for a Product object
     *
     * @param newWhole     a valid wholesale price (as of Oct 2020, double >= 0.00)
     */
    public void setWholesale(double newWhole) {
        this.wholesale = newWhole;
    }
    /**
     * Returns object-specific sale price (should be double >= 0.00).
     *
     * @return      Returns product object's sale price instance variable
     */
    public double getSalePrice() {
        return salePrice;
    }
    /**
     * Assumes valid input passed in, sets sale price instance variable for a Product object
     *
     * @param newSale     a valid sale price (as of Oct 2020, double >= 0.00)
     */
    public void setSalePrice(double newSale) {
        this.salePrice = newSale;
    }
    /**
     * Returns object-specific supplier ID (should be 8-digit alphanumeric).
     *
     * @return      Returns product object's supplier ID instance variable
     */
    public String getSupplierID() {
        return supplierID;
    }
    /**
     * Assumes valid input passed in, sets supplierID instance variable for a Product object
     *
     * @param supplierID     a valid supplier ID (as of Oct 2020, 8-digit alphanumeric)
     */
    public void setSupplierID(String supplierID) {
        this.supplierID = supplierID.toUpperCase();
    }

    /**
     * Used to validate product ID is a valid Product ID; assumes String input
     *
     * @param productID     desired input is length 12 alphanumeric
     */
    public boolean validProductCheck(String productID)
    {
        if (productID.length() != 12) { return false; }
        else if (productID == null) { return false; }

        int digits = 0;
        int letters = 0;
        for (int i = 0; i < productID.length(); i++)
        {
            if (Character.isLetter(productID.charAt(i)))
            {
                letters++;
            }
            if (Character.isDigit(productID.charAt(i)))
            {
                digits++;
            }
        }

        if (digits == 0 && letters == 0)
            return false;

        return true;
    }
    /**
     * Used to validate quantity is a valid quantity; assumes int input
     *
     * @param quantity     desired input is positive integer
     */
    public boolean validQuantityCheck(int quantity)
    {
        if (quantity < 0)
            return false;
        return true;
    }
    /**
     * Used to validate wholesale price is a valid wholesale price; assumes double input
     *
     * @param wholePrice     desired input is double formatted 0.00
     */
    public boolean validWholesaleCheck(double wholePrice)
    {
        if (wholePrice < 0.00)
            return false;
        return true;
    }

    /**
     * Used to validate sale price is a valid sale price; assumes double input
     *
     * @param salePrice     desired input is double formatted 0.00
     */
    public boolean validSaleCheck(double salePrice)
    {
        if (salePrice < 0.00)
            return false;
        return true;
    }

    /**
     * Used to validate supplier ID is a valid Supplier ID; assumes String input
     *
     * @param supplierID     desired input is length 8 alphanumeric
     */
    public boolean validSupplierCheck(String supplierID)
    {
        if (supplierID.length() != 8) { return false; }
        else if (supplierID == null) { return false; }

        int digits = 0;
        int letters = 0;
        for (int i = 0; i < supplierID.length(); i++)
        {
            if (Character.isLetter(supplierID.charAt(i)))
            {
                letters++;
            }
            if (Character.isDigit(supplierID.charAt(i)))
            {
                digits++;
            }
        }

        if (digits == 0 && letters == 0)
            return false;

        return true;
    }
    /**
     * Returns String representation of a Product object in a human-readable form.
     *
     * @return Returns formatted instance variables of a Product object.
     */
    @Override public String toString() {
        return getProductID() + ","
                + getQuantity() + ","
                + getWholesale() + ","
                + getSalePrice() + ","
                + getSupplierID();
    }
}

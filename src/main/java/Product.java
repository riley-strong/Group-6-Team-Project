/*
 * For the purpose of MSU Denver, Fall 2020, CS 3250-52681 course with Dr. Geinitz
 * Contributors include Hector Cruz; Riley Strong; Firew Handiso; Busra Ozdemir; Adam Wojdyla; Dakota Miller
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


    public Product() {
        setProductID("NA12345678NA");
        setQuantity(0);
        setWholesale(0.00);
        setSalePrice(0.00);
        setSupplierID("NA1234NA");
    }

    public Product(String initProductID, int initQuantity, double initWholesale, double initSalePrice, String initSupplierID) {
        setProductID(initProductID);
        setQuantity(initQuantity);
        setWholesale(initWholesale);
        setSalePrice(initSalePrice);
        setSupplierID(initSupplierID);
    }

    public Object[] toArray() {
        return new Object[]{productID, quantity, wholesale, salePrice, supplierID};
    }

    public String getProductID() { return productID; }

    public void setProductID(String productID) {
        this.productID = productID.toUpperCase();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int newQuantity) {
        this.quantity = newQuantity;
    }

    public double getWholesale() {
        return wholesale;
    }

    public void setWholesale(double newWhole) {
        this.wholesale = newWhole;
    }

    public double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(double newSale) {
        this.salePrice = newSale;
    }

    public String getSupplierID() {
        return supplierID;
    }

    public void setSupplierID(String supplierID) {
        this.supplierID = supplierID.toUpperCase();
    }

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

    public boolean validQuantityCheck(int quantity)
    {
        if (quantity < 0)
            return false;
        return true;
    }

    public boolean validWholesaleCheck(double wholePrice)
    {
        if (wholePrice < 0.00)
            return false;
        return true;
    }

    public boolean validSaleCheck(double salePrice)
    {
        if (salePrice < 0.00)
            return false;
        return true;
    }


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

    @Override public String toString() {
        return getProductID() + ","
                + getQuantity() + ","
                + getWholesale() + ","
                + getSalePrice() + ","
                + getSupplierID();
    }
}

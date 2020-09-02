public class Product {

    private int quantity;
    private double wholesale; private double salePrice;
    private String supplierID; private String productID;

    public Product(){
        productID = "NA";
        quantity = 0;
        wholesale = 0;
        salePrice = 0;
        supplierID = "NA";
    }

    public Product(String initProductID, int initQuantity, double initWholesale, double initSalePrice, String initSupplierID){
        productID = initProductID;
        quantity = initQuantity;
        wholesale = initWholesale;
        salePrice = initSalePrice;
        supplierID = initSupplierID;
    }
    // Getters
    public String getProductID(){
        return productID;
    }
    public String getSupplierID(){
        return supplierID;
    }
    public int getQuantity(){
        return quantity;
    }
    public double getWholesale(){
        return wholesale;
    }

    public double getSalePrice() {
        return salePrice;
    }
    // Setters
    public void setSupplierId(String newName){
        supplierID = newName;
    }
    public void setQuantity(int newQuant){
        quantity = newQuant;
    }
    public void setWholesale(double newWhole){
        wholesale = newWhole;
    }
    public void setSalePrice(double newSale){
        salePrice = newSale;
    }
}

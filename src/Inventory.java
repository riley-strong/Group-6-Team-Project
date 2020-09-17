import java.util.*;
import java.io.*;

public class Inventory {
    // Set up reader
    private static final String fileName = "inventory_team6.csv";
    public static Scanner sc = new Scanner(System.in);
    private HashMap<String, Product> inventoryMap;
    private final int prodIDIndex = 0;
    private final int quantityIndex = 1;
    private final int wholesaleIndex = 2;
    private final int salePriceIndex = 3;
    private final int supplierIndex = 4;

    public Inventory() throws FileNotFoundException {
        loadInventory();
    }

    /*public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException {
        DataBaseSimulator test = new DataBaseSimulator();
        Inventory test1 = new Inventory();

        test1.incrementQuantity("TYGT2J2Y4MD2", 1000);
        test.updateWholeSaleCost("RJAXQ1N1J20O");
        test.updateSalePrice("RJAXQ1N1J20O");
        

    }*/

    public void loadInventory() throws FileNotFoundException {
        Scanner sc = new Scanner(new File(fileName));
        inventoryMap = new HashMap<>();

        // Place holder for titles
        sc.nextLine();
        while (sc.hasNext()) {
            String[] categories = sc.nextLine().split(",");
            String prodID = categories[prodIDIndex];
            int quantity = Integer.parseInt(categories[quantityIndex]);
            double wholesale = Double.parseDouble(categories[wholesaleIndex]);
            double salePrice = Double.parseDouble(categories[salePriceIndex]);
            String supplierID = categories[supplierIndex];

            Product newProduct = new Product(prodID, quantity, wholesale, salePrice, supplierID);
            inventoryMap.put(prodID, newProduct);
        }
        //System.out.println(inventory.get(1).toString());
        sc.close();
    }// End loadInventory

    public void update() throws FileNotFoundException {
        File file = new File(fileName);
        PrintWriter writer = new PrintWriter(file);
        writer.println("product_id,quantity,wholesale_cost,sale_price,supplier_id");
        Iterator<Product> itr = iterator();
        while (itr.hasNext()) {
            writer.println(itr.next());
        }
        writer.close();
    }

    public boolean contains(String id){
        return inventoryMap.containsKey(id);
    }
    public boolean isEmpty(){
        return inventoryMap.isEmpty();
    }

    public Product addProduct(Product product) {
        return inventoryMap.put(product.getProductID(), product);
    }

    public Product deleteProduct(Product product) {
        return inventoryMap.remove(product.getProductID());
    }

    public Product deleteProduct(String id) {
        return deleteProduct(inventoryMap.get(id));
    }

    public String searchProduct(String productID) {
        return inventoryMap.get(productID).toString();
    }

    public boolean quantityValidation(String id, int quantity){
        if ((inventoryMap.get(id).getQuantity() - quantity) <= 0)
            return false;
        return true;

    }

    public void incrementQuantity(String productID, int amount) {
        Product product = inventoryMap.get(productID);
        product.setQuantity(product.getQuantity() + amount);
    }

    public void decrementQuantity(String productID, int amount) {
        Product product = inventoryMap.get(productID);
        product.setQuantity(product.getQuantity() - amount);
    }



    public int getQuantity(String id) {
        if (inventoryMap.get(id) == null) {
            throw new NullPointerException();
        }
        return inventoryMap.get(id).getQuantity();
    }// End getQuantity

    public double getSalePrice(String id) {
        if (inventoryMap.get(id) == null) {
            throw new NullPointerException();
        }
        return inventoryMap.get(id).getSalePrice();
    }// End getSalePrice

    public double getWholeSale(String id) {
        if (inventoryMap.get(id) == null) {
            throw new NullPointerException();
        }
        return inventoryMap.get(id).getWholesale();
    }// End getQuantity


    public void setSalePrice(String id, double price) {
        inventoryMap.get(id).setSalePrice(price);

    }// End setSalePrice

    public void setWholeSalePrice(String id, double price) {
        inventoryMap.get(id).setWholesale(price);
    }// End setWholeSalePrice

    public void setQuantity(String id, int quantity) {
        inventoryMap.get(id).setQuantity(quantity);

    }// End setQuantity

    public Iterator<Product> iterator() {
        return inventoryMap.values().iterator();
    }


}// End Class

// test

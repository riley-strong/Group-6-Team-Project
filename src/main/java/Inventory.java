/*
 * For the purpose of MSU Denver, Fall 2020, CS 3250-52681 course with Dr. Geinitz
 * Contributors include Hector Cruz; Riley Strong; Firew Handiso; Busra Ozdemir; Adam Wojdyla; Dakota Miller
 */

import java.util.*;
import java.io.*;

public class Inventory {

    private static final String fileName = "inventory_team6.csv";

    private HashMap<String, Product> inventoryMap;

    private final int prodIDIndex = 0;
    private final int quantityIndex = 1;
    private final int wholesaleIndex = 2;
    private final int salePriceIndex = 3;
    private final int supplierIndex = 4;


    public Inventory() throws FileNotFoundException {
        loadInventory();
    }

    public void loadInventory() throws FileNotFoundException {
        Scanner input = new Scanner(new File(fileName));
        inventoryMap = new HashMap<>();

        input.nextLine();
        while (input.hasNext()) {
            String[] categories = input.nextLine().split(",");
            String prodID = categories[prodIDIndex];
            int quantity = Integer.parseInt(categories[quantityIndex]);
            double wholesale = Double.parseDouble(categories[wholesaleIndex]);
            double salePrice = Double.parseDouble(categories[salePriceIndex]);
            String supplierID = categories[supplierIndex];

            Product newProduct = new Product(prodID, quantity, wholesale, salePrice, supplierID);
            inventoryMap.put(prodID, newProduct);
        }
        input.close();
    }

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

    public Object[][] toArray() {
        Object[][] data = new Object[inventoryMap.size()][5];
        Iterator<Product> product = inventoryMap.values().iterator();
        for (int i = 0; i < inventoryMap.size(); i++) {
            data[i] = product.next().toArray();
        }
        return data;
    }

    public boolean contains(String productID) {
        return inventoryMap.containsKey(productID);
    }

    public boolean isEmpty() {
        return inventoryMap.isEmpty();
    }

    public void addProduct(Product product) {
        inventoryMap.put(product.getProductID(), product);
    }

    public void deleteProduct(String productID) {
        inventoryMap.remove(productID);
    }

    public String searchProduct(String productID) {
        return inventoryMap.get(productID).toString();
    }


    public boolean quantityValidation(String productID, int desiredQuantity) {
        return (inventoryMap.get(productID).getQuantity() - desiredQuantity) >= 0;
    }


    public void incrementQuantity(String productID, int amount) {
        Product product = inventoryMap.get(productID);
        product.setQuantity(product.getQuantity() + amount);
    }


    public void decrementQuantity(String productID, int amount) {
        Product product = inventoryMap.get(productID);
        product.setQuantity(product.getQuantity() - amount);
    }


    public int getQuantity(String productID) {
        return inventoryMap.get(productID).getQuantity();
    }

    public double getWholeSale(String productID) {
        return inventoryMap.get(productID).getWholesale();
    }

    public double getSalePrice(String productID) {
        return inventoryMap.get(productID).getSalePrice();
    }

    public void setSalePrice(String productID, double price) {
        inventoryMap.get(productID).setSalePrice(price);
    }

    public void setWholeSalePrice(String productID, double price) {
        inventoryMap.get(productID).setWholesale(price);
    }

    public void setQuantity(String productID, int quantity) {
        inventoryMap.get(productID).setQuantity(quantity);
    }

    public Iterator<Product> iterator() {
        return inventoryMap.values().iterator();
    }
}

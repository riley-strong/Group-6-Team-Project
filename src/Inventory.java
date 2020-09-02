import java.util.*;
import java.io.*;

public class Inventory {
    private LinkedList<Product> inventory;
    private int prodIDIndex = 0;
    private int quantityIndex = 1;
    private int wholesaleIndex = 2;
    private int salePriceIndex = 3;
    private int supplierIndex = 4;

    // Set up reader
    private static final String fileName = "inventory_team6.csv";
    public static Scanner sc = new Scanner(System.in);

    public Inventory() throws FileNotFoundException {
        loadInventory();
    }

    public void loadInventory() throws FileNotFoundException{
        Scanner sc = new Scanner(new File(fileName));
        inventory = new LinkedList<>();

        // Place holder for titles
        sc.nextLine();
        while(sc.hasNext()){
            String[] categories = sc.nextLine().split(",");
            String prodID = categories[prodIDIndex];
            int quantity = Integer.parseInt(categories[quantityIndex]);
            double wholesale = Double.parseDouble(categories[wholesaleIndex]);
            double salePrice = Double.parseDouble(categories[salePriceIndex]);
            String supplierID = categories[supplierIndex];

            Product newProduct = new Product(prodID, quantity, wholesale, salePrice, supplierID);
            inventory.add(newProduct);
        }
        //System.out.println(inventory.get(1).toString());
        sc.close();
    }


    public static void main(String[] args) throws FileNotFoundException {
        Inventory newInv = new Inventory();
        newInv.loadInventory();
    }









}

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;


public class DataBaseSimulator {

    private Inventory ourInventory;
    private static Scanner sc;

    public DataBaseSimulator() throws FileNotFoundException {
        ourInventory = new Inventory();
    }

    public Inventory getOurInventory() {
        return ourInventory;
    }

// IGNORE THESE METHODS, MOST ARE IN INVENTORY.JAVA NOW!!

/*
    public void updateWholeSaleCost(String productID) throws FileNotFoundException {
        ourInventory = new Inventory();
        double random = new Random().nextDouble();
        random = random < 0 ? random * -1 : random;
        System.out.println("Initial wholesale price: " + ourInventory.getWholeSale(productID) + "\n" + "random double is: " + random);
        ourInventory.setWholeSalePrice(productID, random);
        System.out.println("Final wholesale price: " + ourInventory.getWholeSale(productID) + "\n");

        update();
    }



    public void updateSalePrice(String productID) throws FileNotFoundException {
        ourInventory = new Inventory();
        double random = new Random().nextDouble();
        random = random < 0 ? random * -1 : random;
        System.out.println("Initial sale price: " + ourInventory.getSalePrice(productID) + "\n" + "random double is: " + random);
        ourInventory.setSalePrice(productID, random);
        System.out.println("Final sale price: " + ourInventory.getSalePrice(productID));

        update();
    }


    public void update() throws FileNotFoundException {
        File file = new File("inventory_test.csv");
        PrintWriter writer = new PrintWriter(file);
        writer.println("product_id,quantity,wholesale_cost,sale_price,supplier_id");
        Iterator<Product> itr = ourInventory.iterator();
        while (itr.hasNext()) {
            writer.println(itr.next());
        }
        writer.close();
    }*/
}

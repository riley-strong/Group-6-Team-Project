import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Random;


public class DataBaseSimulator {

    private static Inventory ourInventory;


    public DataBaseSimulator(String productID) throws FileNotFoundException {
        ourInventory = new Inventory();
        int random = new Random().nextInt();
        random = random < 0 ? random * -1 : random;
        System.out.println("Initial inventory quantity: " + ourInventory.getQuantity(productID) + "\n" + "random integer is: " + random);
        ourInventory.setQuantity(productID, random);
        System.out.println("final quantity: " + ourInventory.getQuantity(productID));

        update();

    }


    private static void update() throws FileNotFoundException {
        File file = new File("inventory_team6.csv");
        PrintWriter writer = new PrintWriter(file);
        writer.println("product_id,quantity,wholesale_cost,sale_price,supplier_id");
        Iterator<Product> itr = ourInventory.iterator();
        while (itr.hasNext()) {
            writer.println(itr.next());
        }
        writer.close();

    }


}

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Random;


public class DataBaseSimulator {

    private static Inventory ourInventory;

    /**
     * Finds the quantity and updates given the product ID
     * @param productID
     * @throws FileNotFoundException
     */
    public void updateQuantity(String productID) throws FileNotFoundException {
        ourInventory = new Inventory();
        int random = new Random().nextInt();
        random = random < 0 ? random * -1 : random;
        System.out.println("Initial quantity: " + ourInventory.getQuantity(productID) + "\n" + "random integer is: " + random);
        ourInventory.setQuantity(productID, random);
        System.out.println("Final quantity: " + ourInventory.getQuantity(productID) + "\n");

        update();
    }

    /**
     * Finds the wholesale price and updates given the Product ID.
     * @param productID
     * @throws FileNotFoundException
     */
    public void updateWholeSaleCost(String productID) throws FileNotFoundException{
        ourInventory = new Inventory();
        double random = new Random().nextDouble();
        random = random < 0 ? random * -1 : random;
        System.out.println("Initial wholesale price: " + ourInventory.getWholePrice(productID) + "\n" + "random double is: " + random);
        ourInventory.setWholeSalePrice(productID , random);
        System.out.println("Final wholesale price: " + ourInventory.getWholePrice(productID) + "\n");

        update();
    }

    /**
     * Finds the sale price and updates given the product ID
     * @param productID
     * @throws FileNotFoundException
     */
    public void updateSalePrice(String productID) throws FileNotFoundException{
        ourInventory = new Inventory();
        double random = new Random().nextDouble();
        random = random < 0 ? random * -1 : random;
        System.out.println("Initial sale price: " + ourInventory.getSalePrice(productID) + "\n" + "random double is: " + random);
        ourInventory.setSalePrice(productID , random);
        System.out.println("Final sale price: " + ourInventory.getSalePrice(productID));

        update();
    }

    /*public void listProductsFromSupplier(String supplierID, String productID) throws FileNotFoundException{
        ourInventory = new Inventory();
        supplierID = ourInventory.getSupplierID(productID);
        Iterator<Product> itr = ourInventory.iterator();
        while(itr.hasNext()){
            if(supplierID.equals(ourInventory.getSupplierID(productID))){

            }
        }
    }*/

    /**
     * Updates changes made to any product in the inventory csv file.
     * @throws FileNotFoundException
     */
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

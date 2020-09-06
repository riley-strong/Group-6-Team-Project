import java.io.FileNotFoundException;

public class DataBaseSimulator {

    private static Inventory ourInventory;


    public DataBaseSimulator(String productID, int reqQuantity) throws FileNotFoundException {
        ourInventory = new Inventory();
        //double wholeSalePrice = ourInventory.getWholePrice(productID);
        int currentQuantity = ourInventory.getQuantity(productID);
        System.out.println("current quantity: " + currentQuantity);
        int quantityToBuy = 400;

        update(productID, currentQuantity + quantityToBuy);
        System.out.println("current quantity: " + currentQuantity);


    }

    private static void update(String productID, int quantity) {

        ourInventory.setQuantity(productID, quantity);
    }


}

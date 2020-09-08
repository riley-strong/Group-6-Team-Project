import java.util.*;
import java.io.*;
import java.util.function.Consumer;

public class Inventory {
    private LinkedList<Product> inventory;
    private int prodIDIndex = 0;
    private int quantityIndex = 1;
    private int wholesaleIndex = 2;
    private int salePriceIndex = 3;
    private int supplierIndex = 4;
    // Category Columns for CSVwriter method
    private int quantityCol = 1;
    private int wholeSaleCol = 2;
    private int salePriceCol = 3;

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
    }// End loadInventory

    public int getQuantity(String id){
        for(int i = 0; i < inventory.size(); i++){
            if(inventory.get(i).getProductID().equals(id))
                return inventory.get(i).getQuantity();
        }
        return 0;
    }// End getQuantity

    public double getSalePrice(String id){
        for(int i = 0; i < inventory.size(); i++){
            if(inventory.get(i).getProductID().equals(id))
                return inventory.get(i).getSalePrice();
        }
        return 0;
    }// End getQuantity

    public String getSupplierID(String id){
        for(int i = 0; i < inventory.size(); i++){
            if(inventory.get(i).getProductID().equals(id))
                return inventory.get(i).getSupplierID();
        }
        return "";
    }// End getSupplierID

    public double getWholePrice(String id){
        for(int i = 0; i < inventory.size(); i++){
            if(inventory.get(i).getProductID().equals(id))
                return inventory.get(i).getWholesale();
        }
        return 0;
    }//End getWholePrice

    public void setSalePrice(String id, double price){
        for(int i = 0; i < inventory.size(); i++){
            if(inventory.get(i).getProductID().equals(id))
                inventory.get(i).setSalePrice(price);
        }
        updateCSV(price, i + 1, salePriceCol);
    }// End setSalePrice

    public void setWholeSalePrice(String id, double price){
        for(int i = 0; i < inventory.size(); i++){
            if(inventory.get(i).getProductID().equals(id))
                inventory.get(i).setWholesale(price);
        }
        updateCSV(price, i + 1, wholeSaleCol);
    }// End setWholeSalePrice

    public void setQuantity(String id, int quantity){
        for(int i = 0; i < inventory.size(); i++){
            if(inventory.get(i).getProductID().equals(id))
                inventory.get(i).setQuantity(quantity);
        }
        updateCSV(quantity, i + 1, quantityCol);
    }// End setQuantity

    public Iterator<Product> iterator() {
        return inventory.iterator();
    }

    public static void main(String[] args) throws FileNotFoundException {
        DataBaseSimulator test = new DataBaseSimulator();

        test.updateQuantity("RJAXQ1N1J200");
        test.updateWholeSaleCost("RJAXQ1N1J200");
        test.updateSalePrice("RJAXQ1N1J200");

    }

    public static void updateCSV(double replace, int row, int col) throws IOException {

        File inputFile = new File(fileName);

// Read existing file
        CSVReader reader = new CSVReader(new FileReader(inputFile), ',');
        List<String[]> csvBody = reader.readAll();
// Check column to determine double or int
// get CSV row column  and replace with by using row and column
        if(col == quantityCol) {
            int newReplace = (int)replace
            csvBody.get(row)[col] = newReplace;
        }
        else
            csvBody.get(row)[col] = replace;
        reader.close();


// Write to CSV file which is open
        CSVWriter writer = new CSVWriter(new FileWriter(inputFile), ',');
        writer.writeAll(csvBody);
        writer.flush();
        writer.close();
    }

}// End Class



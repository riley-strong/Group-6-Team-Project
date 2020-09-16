import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

public class InventoryApp {
    private JPanel PanelMain;
    private JButton searchButton;
    private JButton sellButton;
    private JButton addButton;
    private JButton deleteButton;
    private JTextField headerField;
    private JButton buyButton;
    private final Inventory inventory;

    private String obtainProductID(){
        String id = JOptionPane.showInputDialog("Enter product ID");
        return id.toUpperCase();
    }

    private int obtainQuantity(){
        int quantity = Integer.parseInt(JOptionPane.showInputDialog(null, "Enter Quantity"));
        return quantity;
    }

    private double obtainWholeCost(){
        double wholeCost = Double.parseDouble(JOptionPane.showInputDialog("Enter product whole sale cost"));
        return wholeCost;
    }

    private Double obtainCost(){
        double saleCost = Double.parseDouble(JOptionPane.showInputDialog("Enter product sale cost"));
        return saleCost;
    }

    private String obtainSellerID(){
        String sellerID = JOptionPane.showInputDialog("Enter seller id");
        return sellerID.toUpperCase();
    }


    public InventoryApp() throws FileNotFoundException, ClassNotFoundException {
        inventory = new Inventory();
        inventory.loadInventory();

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String productID = obtainProductID();
                JOptionPane.showMessageDialog(null, "Product info:\n" + inventory.searchProduct(productID));
            }
        });

        sellButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String productID = obtainProductID();
                int quantity = obtainQuantity();
                inventory.decrementQuantity(productID, quantity);
                try {
                    inventory.update();
                } catch (FileNotFoundException fileNotFoundException) {
                    fileNotFoundException.printStackTrace();
                }
                JOptionPane.showMessageDialog(null, "Quantity has changed for product: " + productID);

            }
        });

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String productID = obtainProductID();
                int quantity = obtainQuantity();
                double wholeCost = obtainWholeCost();
                double saleCost = obtainCost();
                String sellerID = obtainSellerID();

                Product newProduct = new Product(productID, quantity, wholeCost, saleCost, sellerID);

                inventory.addProduct(newProduct);
                try {
                    inventory.update();
                } catch (FileNotFoundException fileNotFoundException) {
                    fileNotFoundException.printStackTrace();
                }

                JOptionPane.showMessageDialog(null, "New Product " + productID +" has been Added!");
            }
        });
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String productID = obtainProductID();
                inventory.deleteProduct(productID);
                try {
                    inventory.update();
                } catch (FileNotFoundException fileNotFoundException) {
                    fileNotFoundException.printStackTrace();
                }
                JOptionPane.showMessageDialog(null, "Product " + productID + " has been deleted");

            }
        });
        buyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String productID = obtainProductID();
                int add = obtainQuantity();
                inventory.incrementQuantity(productID, add);
                try {
                    inventory.update();
                } catch (FileNotFoundException fileNotFoundException) {
                    fileNotFoundException.printStackTrace();
                }
                JOptionPane.showMessageDialog(null, "Quantity has been changed for product: " + productID);
            }
        });
    }

    public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException {

        JFrame frame = new JFrame("Database APP");
        frame.setMinimumSize(new Dimension(700, 350));
        frame.setContentPane(new InventoryApp().PanelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        //Inventory finalizeUpdate = new Inventory();
        //finalizeUpdate.update();

    }

}

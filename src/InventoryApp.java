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


    public InventoryApp() throws FileNotFoundException, ClassNotFoundException {
        inventory = new Inventory();
        inventory.loadInventory();



        searchButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String id = JOptionPane.showInputDialog("Enter product ID");

                JOptionPane.showMessageDialog(null, "product info:\n" + inventory.searchProduct(id.toUpperCase()));


            }
        });
        sellButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String productID = JOptionPane.showInputDialog(null, "Enter Product ID");
                int add = Integer.parseInt(JOptionPane.showInputDialog(null, "Enter Quantity to sell"));
                inventory.decrementQuantity(productID.toUpperCase(), add);
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
                String productId = JOptionPane.showInputDialog("Enter product ID");
                String quantity = JOptionPane.showInputDialog("Enter product quantity");
                String wholeCost = JOptionPane.showInputDialog("Enter product whole sale cost");
                String saleCost = JOptionPane.showInputDialog("Enter product sale cost");
                String sellerId = JOptionPane.showInputDialog("Enter seller id");

                Product newProduct = new Product();
                newProduct.setProductID(productId.toUpperCase());
                newProduct.setQuantity(Integer.parseInt(quantity));
                newProduct.setWholesale(Double.parseDouble(wholeCost));
                newProduct.setSalePrice(Double.parseDouble(saleCost));
                newProduct.setSupplierID(sellerId.toUpperCase());

                inventory.addProduct(newProduct);
                try {
                    inventory.update();
                } catch (FileNotFoundException fileNotFoundException) {
                    fileNotFoundException.printStackTrace();
                }

                JOptionPane.showMessageDialog(null, "New Product has been Added!");
            }
        });
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String id = JOptionPane.showInputDialog(null, "Enter product ID for deletion");

                inventory.deleteProduct(id.toUpperCase());

                try {
                    inventory.update();
                } catch (FileNotFoundException fileNotFoundException) {
                    fileNotFoundException.printStackTrace();
                }
                JOptionPane.showMessageDialog(null, "Product has been deleted");

            }
        });
        buyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String productID = JOptionPane.showInputDialog(null, "Enter Product ID");
                int add = Integer.parseInt(JOptionPane.showInputDialog(null, "Enter Quantity to buy"));
                inventory.incrementQuantity(productID.toUpperCase(), add);
                try {
                    inventory.update();
                } catch (FileNotFoundException fileNotFoundException) {
                    fileNotFoundException.printStackTrace();
                }

                JOptionPane.showMessageDialog(null, "quantity has been changed for product: " + productID);


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

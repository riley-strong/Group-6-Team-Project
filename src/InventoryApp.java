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
    private JButton buyButton;
    private final Inventory inventory;

    private void notFound() {
        JOptionPane.showMessageDialog(null, "Error: Product Not Found");

    }


    private String obtainProductID() {
        String s = null;
        try {
            s = JOptionPane.showInputDialog("Enter product ID").toUpperCase();
        } catch (NullPointerException ignore) {
        } finally {
            return s;
        }
    }

    private int obtainQuantity() {
        Integer i = null;
        try {
            i = Integer.parseInt(JOptionPane.showInputDialog(null, "Enter quantity"));
        } catch (NullPointerException ignore) {
        } finally {
            return i;
        }
    }

    private double obtainWholeCost() {
        Double d = null;
        try {
            d = Double.parseDouble(JOptionPane.showInputDialog("Enter product whole sale cost"));
        } catch (NullPointerException ignore) {
        } finally {
            return d;
        }
    }

    private double obtainSalePrice() {
        Double d = null;
        try {
            d = Double.parseDouble(JOptionPane.showInputDialog("Enter product sale price"));
        } catch (NullPointerException ignore) {
        } finally {
            return d;
        }
    }

    private String obtainSupplierID() {
        String s = null;
        try {
            s = JOptionPane.showInputDialog("Enter supplier id").toUpperCase();
        } catch (NullPointerException ignore) {
        } finally {
            return s;
        }
    }


    public InventoryApp() throws FileNotFoundException, ClassNotFoundException {
        inventory = new Inventory();
        inventory.loadInventory();

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String productID = obtainProductID();

                if (!inventory.contains(productID)) {
                    notFound();
                    return;
                }
                JOptionPane.showMessageDialog(null, "Product info:\n" + inventory.searchProduct(productID));

            }
        });

        sellButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String productID = obtainProductID();
                if ((!inventory.contains(productID)) || inventory.isEmpty()) {
                    notFound();
                    return;
                }
                int quantity = obtainQuantity();
                if (!inventory.quantityValidation(productID, quantity))
                    JOptionPane.showMessageDialog(null, "Not enough inventory in stock");
                else {
                    inventory.decrementQuantity(productID, quantity);
                    try {
                        inventory.update();
                    } catch (FileNotFoundException fileNotFoundException) {
                        fileNotFoundException.printStackTrace();
                    }
                    JOptionPane.showMessageDialog(null, "Quantity has changed for product: " + productID);
                }
            }
        });

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String productID = null;
                productID = obtainProductID();

                if (productID == null) {
                    JOptionPane.showMessageDialog(null, "Error Product does not exist!");
                    return;
                }

                if (productID.length() != 12) {
                    JOptionPane.showMessageDialog(null, "Invalid product ID");
                    return;
                }
                if (inventory.contains(productID)) {
                    JOptionPane.showMessageDialog(null, "Product already exists");
                    return;
                }

                int quantity = obtainQuantity();
                double wholeCost = obtainWholeCost();
                double salePrice = obtainSalePrice();
                if (wholeCost < 0 || salePrice < 0 || quantity < 0) {
                    JOptionPane.showMessageDialog(null, "Cannot get parameters below 0");
                    return;
                }

                String supplierID = obtainSupplierID();
                if (supplierID.length() != 8) {
                    JOptionPane.showMessageDialog(null, "Invalid supplier ID");
                    return;
                }
                Product newProduct = new Product(productID, quantity, wholeCost, salePrice, supplierID);
                inventory.addProduct(newProduct);
                try {
                    inventory.update();
                } catch (FileNotFoundException fileNotFoundException) {
                    fileNotFoundException.printStackTrace();
                }

                JOptionPane.showMessageDialog(null, "New product " + productID + " has been added");
            }
        });
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String productID = obtainProductID();
                if (!inventory.contains(productID)) {
                    notFound();
                    return;
                }
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
                if (!inventory.contains(productID)) {
                    notFound();
                    return;
                }
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

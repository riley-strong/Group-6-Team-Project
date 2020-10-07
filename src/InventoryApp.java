/*
 * For the purpose of MSU Denver, Fall 2020, CS 3250-52681 course with Dr. Geinitz
 * Contributors include Hector Cruz; Riley Strong; Firew Handiso; Busra Ozdemir; Adam Wojdyla; Dakota Miller
 */

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.sql.SQLException;

/**
 * InventoryApp class has the main method which is the entry point of the program, and it provides interaction with the user
 */
public class InventoryApp {
    //
    private JPanel PanelMain;
    private JButton searchButton;
    private JButton sellButton;
    private JButton addButton;
    private JButton deleteButton;
    private JButton buyButton;
    private JButton displayButton;
    private final Inventory inventory;
    public  JTable tv;
    private JFrame tableFrame;
    private JLabel tableLable;


    /**
     * Displays not found message
     */
    private void notFound() {
        JOptionPane.showMessageDialog(null, "Error: Product Not Found");

    }

    /**
     * Displays invalid input message
     */
    private void invalid() {
        JOptionPane.showMessageDialog(null, "Error: Invalid Input");

    }

    /**
     * Used to obtain user input for product id
     *
     * @return      String input
     */
    private String obtainProductID() {
        String s = null;
        try {
            s = JOptionPane.showInputDialog("Enter Product ID").toUpperCase();
        } catch (NullPointerException ignore) {
        } finally {
            return s;
        }
    }

    /**
     * Used to obtain user input for product quantity if provided input does not contain letter or character
     *
     * @throws NullPointerException        Used to capture any attempts to use an object reference that has the null value
     *
     * @return      int input
     */
    private int obtainQuantity() {
        String input = JOptionPane.showInputDialog("Enter Quantity");
        Integer quantity = null;

        for(int i=0; i<input.length(); i++){
            char ch = input.charAt(i);
            if(Character.isLetter(ch) || ch == ' ') {
                JOptionPane.showMessageDialog(null, "Invalid Quantity!");
            } break;
        }
        try {
            //parses the input from String to Integer
            quantity = Integer.parseInt(input);
        } catch (NullPointerException ignore) {

        } finally {
            return quantity;
        }
    }

    /**
     * Used to obtain user input for product wholesale cost
     *
     * @throws NullPointerException        Used to capture any attempts to use an object reference that has the null value
     *
     * @return      double input
     */
    private double obtainWholeCost() {
        Double d = null;
        try {
            //parses the input from String to Double
            d = Double.parseDouble(JOptionPane.showInputDialog("Enter product whole sale cost"));
        } catch (NullPointerException ignore) {
        } finally {
            return d;
        }
    }

    /**
     * Used to obtain user input for product sale price
     *
     * @throws NullPointerException        Used to capture any attempts to use an object reference that has the null value
     *
     * @return      double input
     */
    private double obtainSalePrice() {
        Double d = null;
        try {
            //parses the input from String to Double
            d = Double.parseDouble(JOptionPane.showInputDialog("Enter product sale price"));
        } catch (NullPointerException ignore) {
        } finally {
            return d;
        }
    }

    /**
     * Used to obtain user input for supplier id
     *
     * @throws NullPointerException        Used to capture any attempts to use an object reference that has the null value
     *
     * @return      String input
     */
    private String obtainSupplierID() {
        String s = null;
        try {
            s = JOptionPane.showInputDialog("Enter supplier id").toUpperCase();
        } catch (NullPointerException ignore) {
        } finally {
            return s;
        }
    }

    /**
     * No-arg constructor, creates a new inventory and loads an inventory object with collection of Product objects,
     * sets the formats of the buttons and implements action listeners if inventory is found
     *
     * @throws FileNotFoundException        Used to capture bad execution due to poor file path
     *
     * @throws ClassNotFoundException       Used to indicate that the specified class cannot be found in the classpath
     */
    public InventoryApp() throws FileNotFoundException, ClassNotFoundException {
        inventory = new Inventory();
        inventory.loadInventory();

        setButtonFlavor(searchButton);
        searchButton.addActionListener(e -> {
            String productID = obtainProductID();

            //calls to display the not found message dialog if inventory does not contain the provided product id
            if (!inventory.contains(productID)) {
                notFound();
                return;
            }
            JOptionPane.showMessageDialog(null, "        Product info:\n" + inventory.searchProduct(productID));

        });

        setButtonFlavor(sellButton);
        sellButton.addActionListener(e -> {
            String productID = obtainProductID();
            //calls to display the not found message dialog if inventory does not contain the provided product id or is empty
            if ((!inventory.contains(productID)) || inventory.isEmpty()) {
                notFound();
                return;
            }
            int quantity = obtainQuantity();
            //calls to display the invalid message dialog if quantity is negative or zero
            if (quantity <=0){
                invalid();
                return;
            }
            //displays a message dialog if quantity is not validated
            if (!inventory.quantityValidation(productID, quantity))
                JOptionPane.showMessageDialog(null, "Not enough inventory in stock");
            else {
                inventory.decrementQuantity(productID, quantity);
                try {
                    inventory.update();
                } catch (FileNotFoundException fileNotFoundException) {
                    fileNotFoundException.printStackTrace();
                }
                JOptionPane.showMessageDialog(null, "Product sold: " + productID);
            }
        });

        setButtonFlavor(addButton);
        addButton.addActionListener(e -> {

            String productID = null;
            productID = obtainProductID();

            //displays a message dialog if product id is null
            if (productID == null) {
                JOptionPane.showMessageDialog(null, "Error Product does not exist!");
                return;
            }

            //displays a message dialog if product id is not 12-character alphanumeric String
            if (productID.length() != 12) {
                JOptionPane.showMessageDialog(null, "Invalid product ID");
                return;
            }

            //displays a message dialog if the inventory contains the provided product id
            if (inventory.contains(productID)) {
                JOptionPane.showMessageDialog(null, "Product already exists");
                return;
            }

            int quantity = obtainQuantity();
            double wholeCost = obtainWholeCost();
            double salePrice = obtainSalePrice();
            //displays a message dialog if whole cost or sale price or quantity is negative
            if (wholeCost < 0 || salePrice < 0 || quantity < 0) {
                JOptionPane.showMessageDialog(null, "Cannot get parameters below 0");
                return;
            }

            String supplierID = obtainSupplierID();
            //displays a message dialog if supplier id is not 8-character alphanumeric String
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
        });

        setButtonFlavor(deleteButton);
        deleteButton.addActionListener(e -> {
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

        });

        setButtonFlavor(buyButton);
        buyButton.addActionListener(e -> {
            String productID = obtainProductID();
            //calls to display the not found message dialog if inventory does not contain the provided product id
            if (!inventory.contains(productID)) {
                notFound();
                return;
            }
            int add = obtainQuantity();
            //calls to display the invalid message dialog if quantity is negative or zero
            if (add <=0){
                invalid();
                        return;
            }
            inventory.incrementQuantity(productID, add);
            try {
                inventory.update();
            } catch (FileNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            }
            JOptionPane.showMessageDialog(null, "Quantity has been changed for product: " + productID);
        });

        setButtonFlavor(displayButton);
        displayButton.addActionListener(e -> showTable(inventory.toArray(), Product.headers));
    }

    /**
     * Sets the button format
     * */
    private void setButtonFlavor(JButton button) {
        button.setBackground(Color.BLACK);
        button.setForeground(Color.yellow);
        button.setMaximumSize(new Dimension(10, 60 ));
        button.setMinimumSize(new Dimension(10, 60 ));
    }

    /**
     * Creates a table for inventory with product headers
     *
     * @param dataVector 2-D Array of inventory
     * @param headers    String Array of product headers
     *
     * */
    public void showTable(Object[][] dataVector, String[] headers) {
        tableFrame = new JFrame("Table Viewer");
        tableFrame.setMinimumSize(new Dimension(700, 350));
        tv = new JTable();
        DefaultTableModel dtm = new DefaultTableModel();

        dtm.setDataVector(dataVector, headers);
        tv.setModel(dtm);
        JScrollPane scrollPane = new JScrollPane(tv);
        tableLable = new JLabel();
        JPanel panel = new JPanel();
        panel.add(scrollPane);
        panel.add(tableLable);
        tableFrame.add(panel);
        tableFrame.pack();
        tableFrame.setLocationRelativeTo(null);
        tableFrame.setVisible(true);
    }

    /**
     * Main method is used as the entry point of the program
     *
     * @throws FileNotFoundException        Used to capture bad execution due to poor file path
     *
     * @throws ClassNotFoundException       Used to indicate that the specified class cannot be found in the classpath
     */
    public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException {
        JFrame frame = new JFrame("Database APP");
        frame.setMinimumSize(new Dimension(350, 350));
        frame.setContentPane(new InventoryApp().PanelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(Color.BLUE);
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
    }
}

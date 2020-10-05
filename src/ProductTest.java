/*
 * For the purpose of MSU Denver, Fall 2020, CS 3250-52681 course with Dr. Geinitz
 * Contributors include Hector Cruz; Riley Strong; Firew Handiso; Busra Ozdemir; Adam Wojdyla; Dakota Miller
 */

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProductTest class is used to test the functions and scopes for Product test objects.
 */

class ProductTest {

    //Product test objects created and initialized
    Product test1 = new Product("RJAXQ1N1J20O",7063,448.62,729.01,"SLVUJJVM");
    Product test2 = new Product("TYGT2J2Y4MD2",3091,436.09,489.29,"RRHKWHUG");
    Product test2Duplicate = new Product("TYGT2J2Y4MD2",3091,436.09,489.29,"RRHKWHUG");

    /**
     * Checks if the Product test object's product id is equal to instance variable
     */
    @org.junit.jupiter.api.Test
    void getProduct_id() {
        assertEquals(test1.getProductID(), "RJAXQ1N1J20O");

    }
    /**
     * Sets product id instance variable for the Product test object,
     * Checks if the Product test object's product id is equal to instance variable & product id is 12-character alphanumeric
     */
    @org.junit.jupiter.api.Test
    void setProduct_id() {
        test1.setProductID("RJAXQ1N1J211");
        assertEquals(test1.getProductID(), "RJAXQ1N1J211");
        assertEquals(test1.getProductID().length() , 12 );
    }

    /**
     * Checks if the Product test object's quantity is equal to instance variable
     */
    @org.junit.jupiter.api.Test
    void getQuantity() {
        assertEquals(test1.getQuantity(), 7063);
    }

    /**
     * Sets Quantity instance variable for the Product test object,
     * Checks if the Product test object's Quantity is equal to instance variable
     */
    @org.junit.jupiter.api.Test
    void setQuantity() {
        test1.setQuantity(7064);
        assertEquals(test1.getQuantity(), 7064);
    }

    /**
     * Checks if the Product test object's wholesale price is equal to instance variable
     */
    @org.junit.jupiter.api.Test
    void getWholesale_cost() {
        assertEquals(test1.getWholesale(), 448.62);
    }

    /**
     * Sets wholesale price instance variable for the Product test object,
     * Checks if the Product test object's wholesale price is equal to instance variable
     */
    @org.junit.jupiter.api.Test
    void setWholesale_cost() {
        test1.setWholesale(444.62);
        assertEquals(test1.getWholesale(), 444.62);
    }

    /**
     * Checks if the Product test object's sale price is equal to instance variable
     */
    @org.junit.jupiter.api.Test
    void getSale_price() {
        assertEquals(test1.getSalePrice(),729.01);
    }

    /**
     * Sets sale price instance variable for the Product test object,
     * Checks if the Product test object's sale price is equal to instance variable
     */
    @org.junit.jupiter.api.Test
    void setSale_price() {
        test1.setSalePrice(728.01);
        assertEquals(test1.getSalePrice(),728.01);
    }

    /**
     * Checks if the Product test object's supplier id is equal to instance variable
     */
    @org.junit.jupiter.api.Test
    void getSupplier_id() {
        assertEquals(test1.getSupplierID(), "SLVUJJVM");
    }}

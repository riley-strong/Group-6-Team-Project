


import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    Product test1 = new Product("RJAXQ1N1J200", 7063, 448.62, 729.01, "SLVUJJVM");
    Product test2 = new Product("TYGT2J2Y4MD2", 3091, 436.09, 489.29, "RRHKWHUG");
    Product test2Duplicate = new Product("TYGT2J2Y4MD2", 3091, 436.09, 489.29, "RRHKWHUG");

    ProductTest() throws FileNotFoundException {
    }

    @org.junit.jupiter.api.Test
    void getProductID() {
        assertEquals(test1.getProductID(), "RJAXQ1N1J200");

    }

    @org.junit.jupiter.api.Test
    void setProductID() {
        assertEquals(test1.getProductID(), "RJAXQ1N1J200");
        assertEquals(test1.getProductID().length(), 12);
    }

    @org.junit.jupiter.api.Test
    void getQuantity() {
        assertEquals(test1.getQuantity(), 7063);
    }

    @org.junit.jupiter.api.Test
    void setQuantity() {
        test1.setQuantity(7063);
        assertFalse(test1.setQuantity(-1));
    }

    @org.junit.jupiter.api.Test
    void getWholesale_cost() {
        assertEquals(test1.getWholesale(), 448.62);
    }

    @org.junit.jupiter.api.Test
    void setWholesale_cost() {
        test1.setWholesale(448.62);
        assertFalse(test1.setWholesale(-1));
    }

    @org.junit.jupiter.api.Test
    void getSale_price() {
        assertEquals(test1.getSalePrice(), 729.01);
    }

    @org.junit.jupiter.api.Test
    void setSale_price() {
        test1.setSalePrice(729.01);
        assertFalse(test1.setSalePrice(-1));
    }

    @org.junit.jupiter.api.Test
    void getSupplier_id() {
        assertEquals(test1.getSupplierID(), "SLVUJJVM");
    }
}




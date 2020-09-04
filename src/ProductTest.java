import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    Product test1 = new Product("RJAXQ1N1J20O",7063,448.62,729.01,"SLVUJJVM");
    Product test2 = new Product("TYGT2J2Y4MD2",3091,436.09,489.29,"RRHKWHUG");
    Product test2Duplicate = new Product("TYGT2J2Y4MD2",3091,436.09,489.29,"RRHKWHUG");

    @org.junit.jupiter.api.Test
    void getProduct_id() {
        assertEquals(test1.getProduct_id(), "RJAXQ1N1J20O");

    }

    @org.junit.jupiter.api.Test
    void setProduct_id() {
        test1.setProduct_id("RJAXQ1N1J211");
        assertEquals(test1.getProduct_id(), "RJAXQ1N1J211");
        assertEquals(test1.getProduct_id().length() , 12 );
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
        assertEquals(test1.getWholesale_cost(), 448.62);
    }

    @org.junit.jupiter.api.Test
    void setWholesale_cost() {
        test1.setWholesale_cost(448.62);
        assertFalse(test1.setWholesale_cost(-1));
    }

    @org.junit.jupiter.api.Test
    void getSale_price() {
        assertEquals(test1.getSale_price(),729.01);
    }

    @org.junit.jupiter.api.Test
    void setSale_price() {
        test1.setSale_price(729.01);
        assertFalse(test1.setSale_price(-1));
    }

    @org.junit.jupiter.api.Test
    void getSupplier_id() {
        assertEquals(test1.getSupplier_id(), "SLVUJJVM");
    }

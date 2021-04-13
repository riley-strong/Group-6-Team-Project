DELIMITER $$
CREATE DEFINER=`wojack5555`@`localhost` PROCEDURE `batchLoading`()
BEGIN
	
	INSERT INTO dim_supplier (supplier_id)
    SELECT DISTINCT supplier_id FROM temp_inventory;
    
    
    INSERT INTO dim_product (product_id, supplier_id)
    SELECT DISTINCT product_id, supplier_id FROM temp_inventory;
    
    
    UPDATE dim_product dp, dim_supplier ds
    SET dp.supplier_tid = ds.supplier_tid
    WHERE dp.supplier_id = ds.supplier_id;
    
    
    ALTER TABLE dim_product DROP COLUMN supplier_id;
    
    
    INSERT INTO inventory 
    SELECT dp.product_tid, ti.quantity, ti.wholesale_cost, ti.sale_price, ds.supplier_tid 
    FROM temp_inventory ti 
    INNER JOIN dim_product dp ON ti.product_id = dp.product_id 
    INNER JOIN dim_supplier ds ON ti.supplier_id = ds.supplier_id;
    
    
    DROP TABLE IF EXISTS temp_inventory;
    
    
    ALTER TABLE temp_unprocessed_sales ADD COLUMN hashed_email VARBINARY(32);
    
    
    INSERT INTO hash_ref (hashed_email, unhashed_email) 
    SELECT DISTINCT MD5(cust_email), cust_email FROM temp_unprocessed_sales;
    
    
    UPDATE temp_unprocessed_sales, hash_ref
    SET temp_unprocessed_sales.hashed_email = hash_ref.hashed_email
    WHERE temp_unprocessed_sales.cust_email = hash_ref.unhashed_email;
    
    
    ALTER TABLE temp_unprocessed_sales DROP COLUMN cust_email;
    
    
    INSERT INTO unprocessed_sales 
    SELECT tus.date, tus.cust_location, dp.product_tid, tus.product_quantity, tus.hashed_email
    FROM temp_unprocessed_sales tus 
    INNER JOIN dim_product dp ON tus.product_id = dp.product_id;
    
    
    DROP TABLE IF EXISTS temp_unprocessed_sales;
    
    
    TRUNCATE daily_assets;
END $$
DELIMITER ;
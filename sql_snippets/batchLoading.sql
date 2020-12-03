CREATE DEFINER=`wojack5555`@`%` PROCEDURE `batchLoading`()
BEGIN
	#Purpose: Shifts Batch Loading processes to SQL (since commands were sent to SQL from Java JDBC anyway)
	#Author: dmill166
    #Date Modified: December 1, 2020
    
	#Load dim_supplier with supplier_id from temp_inventory.
	INSERT INTO dim_supplier (supplier_id)
    SELECT DISTINCT supplier_id FROM temp_inventory;
    
    #Load dim_product with product_id and supplier_id from temp_inventory.
    INSERT INTO dim_product (product_id, supplier_id)
    SELECT DISTINCT product_id, supplier_id FROM temp_inventory;
    
    #Load dim_product with supplier_tid
    UPDATE dim_product dp, dim_supplier ds
    SET dp.supplier_tid = ds.supplier_tid
    WHERE dp.supplier_id = ds.supplier_id;
    
    #Remove supplier_id column from dim_product table.
    ALTER TABLE dim_product DROP COLUMN supplier_id;
    
    #Load inventory from dim_product, dim_supplier, and temp_inventory
    INSERT INTO inventory 
    SELECT dp.product_tid, ti.quantity, ti.wholesale_cost, ti.sale_price, ds.supplier_tid 
    FROM temp_inventory ti 
    INNER JOIN dim_product dp ON ti.product_id = dp.product_id 
    INNER JOIN dim_supplier ds ON ti.supplier_id = ds.supplier_id;
    
    #Drop temp_inventory table (no longer needed).
    DROP TABLE IF EXISTS temp_inventory;
    
    #Add column to temp_unprocessed_sales table for hashed email.
    ALTER TABLE temp_unprocessed_sales ADD COLUMN hashed_email VARBINARY(32);
    
    #Load hash table with emails from unprocessed orders and generate hashed emails.
    INSERT INTO hash_ref (hashed_email, unhashed_email) 
    SELECT DISTINCT MD5(cust_email), cust_email FROM temp_unprocessed_sales;
    
    #Fill with hashed emails from hash table.
    UPDATE temp_unprocessed_sales, hash_ref
    SET temp_unprocessed_sales.hashed_email = hash_ref.hashed_email
    WHERE temp_unprocessed_sales.cust_email = hash_ref.unhashed_email;
    
    #Delete (unhashed) email column from unprocessed orders.
    ALTER TABLE temp_unprocessed_sales DROP COLUMN cust_email;
    
    #Load temp_unprocessed_sales into unprocessed_sales table
    INSERT INTO unprocessed_sales 
    SELECT tus.date, tus.cust_location, dp.product_tid, tus.product_quantity, tus.hashed_email
    FROM temp_unprocessed_sales tus 
    INNER JOIN dim_product dp ON tus.product_id = dp.product_id;
    
    #Drop temp_unprocessed_sales table.
    DROP TABLE IF EXISTS temp_unprocessed_sales;
    
    #Truncate Daily Assets table (in case any data persisted through the drop).
    TRUNCATE daily_assets;
END
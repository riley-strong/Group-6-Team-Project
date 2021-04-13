DELIMITER $$
CREATE DEFINER=`wojack5555`@`localhost` PROCEDURE `createDB_Structure`()
BEGIN
	

	DROP TABLE IF EXISTS temp_inventory;
    DROP TABLE IF EXISTS inventory;
    DROP TABLE IF EXISTS dim_supplier;
    DROP TABLE IF EXISTS dim_product;
    DROP TABLE IF EXISTS temp_unprocessed_sales;
    DROP TABLE IF EXISTS unprocessed_sales;
    DROP TABLE IF EXISTS hash_ref;
    DROP TABLE IF EXISTS processed_sales;
    DROP TABLE IF EXISTS supplier_orders;
	DROP TABLE IF EXISTS daily_assets;
    
    
    CREATE TABLE temp_inventory (
		product_id VARCHAR(12)
        ,quantity INT
        ,wholesale_cost DECIMAL(12,2)
        ,sale_price DECIMAL(12,2)
        ,supplier_id VARCHAR(8));
        
	
	CREATE TABLE inventory (
		product_tid INT
        ,quantity INT
        ,wholesale_cost DECIMAL(12,2)
        ,sale_price DECIMAL(12,2)
        ,supplier_tid INT);
	    
	
	CREATE TABLE dim_supplier (
		supplier_tid INT AUTO_INCREMENT
        ,supplier_id VARCHAR(8)
        ,CONSTRAINT ds_pk PRIMARY KEY (supplier_tid));
	    
	
	CREATE TABLE dim_product (
		product_tid INT AUTO_INCREMENT 
        ,supplier_tid INT
        ,supplier_id VARCHAR(8)
        ,product_id VARCHAR(12)
        ,CONSTRAINT dp_pk PRIMARY KEY (product_tid));
	   
	
	CREATE TABLE temp_unprocessed_sales (
		date DATE
        ,cust_email VARCHAR(320)
        ,cust_location VARCHAR(5)
        ,product_id VARCHAR(12)
        ,product_quantity INT);
	    
	
	CREATE TABLE unprocessed_sales (
		date DATE
        ,cust_location VARCHAR(5)
        ,product_tid INT
        ,quantity INT
        ,hashed_email VARBINARY(32));
	   
	
	CREATE TABLE hash_ref (
		hashed_email VARBINARY(32)
        ,unhashed_email VARCHAR(320)
        ,INDEX hr_index (hashed_email));
	    
	
	CREATE TABLE processed_sales (
		date DATE
        ,processed_dt DATETIME
        ,cust_location VARCHAR(5)
        ,product_tid INT
        ,quantity INT
        ,result TINYINT
        ,hashed_email VARBINARY(32));
	    
	
	CREATE TABLE supplier_orders (
		date DATE
        ,supplier_tid INT
        ,product_tid INT
        ,quantity INT);
        
	
	CREATE TABLE daily_assets (
		date DATE
        ,assets DECIMAL (64, 2));
	    	
END $$
DELIMITER ;
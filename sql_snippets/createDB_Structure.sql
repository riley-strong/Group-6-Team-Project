CREATE DEFINER=`wojack5555`@`%` PROCEDURE `createDB_Structure`()
BEGIN
	#Purpose: To allow anyone to recreate the database from scratch
	#Author: dmill166
    #Date Modified: December 1, 2020
    
    #Drop tables from database if they exist
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
    
    #Create temp_inventory table to house initial .csv contents
    CREATE TABLE temp_inventory (
		product_id VARCHAR(12)
        ,quantity INT
        ,wholesale_cost DECIMAL(12,2)
        ,sale_price DECIMAL(12,2)
        ,supplier_id VARCHAR(8));
        
	#Create inventory table for long term use in database
	CREATE TABLE inventory (
		product_tid INT
        ,quantity INT
        ,wholesale_cost DECIMAL(12,2)
        ,sale_price DECIMAL(12,2)
        ,supplier_tid INT);
	    
	#Create supplier dimension table
	CREATE TABLE dim_supplier (
		supplier_tid INT AUTO_INCREMENT
        ,supplier_id VARCHAR(8)
        ,CONSTRAINT ds_pk PRIMARY KEY (supplier_tid));
	    
	#Create product dimension table
	CREATE TABLE dim_product (
		product_tid INT AUTO_INCREMENT 
        ,supplier_tid INT
        ,supplier_id VARCHAR(8)
        ,product_id VARCHAR(12)
        ,CONSTRAINT dp_pk PRIMARY KEY (product_tid));
	   
	#Create temporary location to house customer_orders .csv contents
	CREATE TABLE temp_unprocessed_sales (
		date DATE
        ,cust_email VARCHAR(320)
        ,cust_location VARCHAR(5)
        ,product_id VARCHAR(12)
        ,product_quantity INT);
	    
	#Create long-term location to be used for housing orders that have yet to be processed
	CREATE TABLE unprocessed_sales (
		date DATE
        ,cust_location VARCHAR(5)
        ,product_tid INT
        ,quantity INT
        ,hashed_email VARBINARY(32));
	   
	#Create table to store hashed customer emails and the unhashed variants
	CREATE TABLE hash_ref (
		hashed_email VARBINARY(32)
        ,unhashed_email VARCHAR(320)
        ,INDEX hr_index (hashed_email));
	    
	#Create table to store orders as they are processed
	CREATE TABLE processed_sales (
		date DATE
        ,processed_dt DATETIME
        ,cust_location VARCHAR(5)
        ,product_tid INT
        ,quantity INT
        ,result TINYINT
        ,hashed_email VARBINARY(32));
	    
	#Create table to store supplier orders as resupply events occur
	CREATE TABLE supplier_orders (
		date DATE
        ,supplier_tid INT
        ,product_tid INT
        ,quantity INT);
        
	#Create table to store daily assets for later analytics purposes
	CREATE TABLE daily_assets (
		date DATE
        ,assets DECIMAL (64, 2));
	    	
END
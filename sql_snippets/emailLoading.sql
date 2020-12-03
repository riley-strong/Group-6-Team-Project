CREATE DEFINER=`wojack5555`@`%` PROCEDURE `emailLoading`()
BEGIN
	#Purpose: Used to load emails into unprocessed_sales table
	#Author: dmill166
    #Date Modified: December 2, 2020

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
END
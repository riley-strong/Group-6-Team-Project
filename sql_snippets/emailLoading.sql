DELIMITER $$
CREATE DEFINER=`wojack5555`@`localhost` PROCEDURE `emailLoading`()
BEGIN
	
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
END $$
DELIMITER ;
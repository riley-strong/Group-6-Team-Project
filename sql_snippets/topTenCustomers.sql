CREATE DEFINER=`wojack5555`@`%` PROCEDURE `topTenCustomers`(IN theDate DATE)
BEGIN
	#Purpose: Used to select top ten customers for a specific date (via Java JDBC)
	#Author: dmill166
    #Date Modified: December 2, 2020

	#Assign input date into local variable
	SET @theDate = theDate;

	#Select a table of results for a specific date, unhashed emails, and total spend by customer
	SELECT DISTINCT ps.date, hr.unhashed_email, SUM(ps.quantity * inv.sale_price)
    FROM processed_sales ps
    INNER JOIN hash_ref hr ON ps.hashed_email = hr.hashed_email
    INNER JOIN inventory inv ON ps.product_tid = inv.product_tid
    WHERE date = @theDate
    AND ps.result = 1
    GROUP BY ps.date, hr.unhashed_email
    ORDER BY SUM(ps.quantity * inv.sale_price) DESC
    LIMIT 10;
END
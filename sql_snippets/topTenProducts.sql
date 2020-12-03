CREATE DEFINER=`wojack5555`@`%` PROCEDURE `topTenProducts`(IN theDate DATE)
BEGIN
	#Purpose: Used to select top ten products for a specific date (via Java JDBC)
	#Author: dmill166
    #Date Modified: December 2, 2020

	#Assign input date into local variable
	SET @theDate = theDate;

	#Select a table of results for a specific date, product IDs, and total products sold
	SELECT DISTINCT ps.date, dp.product_id, SUM(ps.quantity)
    FROM processed_sales ps
    INNER JOIN dim_product dp ON ps.product_tid = dp.product_tid
    WHERE date = @theDate
    AND ps.result = 1
    GROUP BY ps.date, dp.product_id
    ORDER BY SUM(ps.quantity) DESC
    LIMIT 10;
END
CREATE DEFINER=`wojack5555`@`%` PROCEDURE `daily_orders`(IN startDate DATE, IN endDate DATE)
BEGIN
	#Purpose: Used to output daily orders to user based on input date range table
	#Author: dmill166
    #Date Modified: December 2, 2020
    
	#Establish dates to gather ending inventory quantities
	SET @startDate = startDate;
    SET @endDate = endDate;

	#1. Select table of date, products, and ending inventory quantities for specified date range
    SELECT '30', '12', DAY(date), MONTH(date), YEAR(date), count(product_tid)
    FROM processed_sales
    WHERE date BETWEEN @startDate AND @endDate
    AND result = 1
    GROUP BY '30', '12', DAY(date), MONTH(date), YEAR(date)
    ORDER BY '30', '12', MONTH(date), DAY(date), YEAR(date);
END
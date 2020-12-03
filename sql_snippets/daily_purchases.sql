CREATE DEFINER=`wojack5555`@`%` PROCEDURE `daily_purchases`(IN startDate DATE, IN endDate DATE)
BEGIN
	#Purpose: Used to output daily assets reporting for a specified date range
	#Author: dmill166
    #Date Modified: December 2, 2020

	#Design intended date range for reporting
	SET @startDate = startDate;
	SET @endDate = endDate;

	#Select a table of date and purchase totals for the specified date range
	SELECT DISTINCT '30', '12', DAY(ps.date), MONTH(ps.date), YEAR(ps.date), sum(CAST(ps.quantity * i.sale_price AS DECIMAL (64, 2)))
    FROM processed_sales ps
    INNER JOIN inventory i on ps.product_tid = i.product_tid
    WHERE ps.date BETWEEN @startDate AND @endDate
    and ps.result = 1
    GROUP BY '30', '12', DAY(ps.date), MONTH(ps.date), YEAR(ps.date)
    ORDER BY '30', '12', MONTH(date), DAY(date), YEAR(date);
END
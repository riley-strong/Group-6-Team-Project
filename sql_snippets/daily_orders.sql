DELIMITER $$
CREATE DEFINER=`wojack5555`@`localhost` PROCEDURE `daily_orders`(IN startDate DATE, IN endDate DATE)
BEGIN
	
	SET @startDate = startDate;
    SET @endDate = endDate;
	
    SELECT '30', '12', DAY(date), MONTH(date), YEAR(date), count(product_tid)
    FROM processed_sales
    WHERE date BETWEEN @startDate AND @endDate
    AND result = 1
    GROUP BY '30', '12', DAY(date), MONTH(date), YEAR(date)
    ORDER BY '30', '12', MONTH(date), DAY(date), YEAR(date);
END $$
DELIMITER ;
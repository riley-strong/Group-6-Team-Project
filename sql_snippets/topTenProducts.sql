DELIMITER $$
CREATE DEFINER=`wojack5555`@`localhost` PROCEDURE `topTenProducts`(IN theDate DATE)
BEGIN
	
	SET @theDate = theDate;

	SELECT DISTINCT ps.date, dp.product_id, SUM(ps.quantity)
    FROM processed_sales ps
    INNER JOIN dim_product dp ON ps.product_tid = dp.product_tid
    WHERE date = @theDate
    AND ps.result = 1
    GROUP BY ps.date, dp.product_id
    ORDER BY SUM(ps.quantity) DESC
    LIMIT 10;
END $$
DELIMITER ;
DELIMITER $$
CREATE DEFINER=`wojack5555`@`localhost` PROCEDURE `topTenCustomers`(IN theDate DATE)
BEGIN
	
	
	SET @theDate = theDate;
	
	SELECT DISTINCT ps.date, hr.unhashed_email, SUM(ps.quantity * inv.sale_price)
    FROM processed_sales ps
    INNER JOIN hash_ref hr ON ps.hashed_email = hr.hashed_email
    INNER JOIN inventory inv ON ps.product_tid = inv.product_tid
    WHERE date = @theDate
    AND ps.result = 1
    GROUP BY ps.date, hr.unhashed_email
    ORDER BY SUM(ps.quantity * inv.sale_price) DESC
    LIMIT 10;
END $$
DELIMITER ;
DELIMITER $$
CREATE DEFINER=`wojack5555`@`localhost` PROCEDURE `specificDailyAssets`(IN startDate DATE, IN endDate DATE)
BEGIN
	
	SET @startDate = startDate;
	SET @endDate = endDate;
	
	SELECT DISTINCT '30', '12', DAY(date), MONTH(date), YEAR(date), assets
    FROM daily_assets
    WHERE date BETWEEN @startDate AND @endDate
    ORDER BY '30', '12', MONTH(date), DAY(date), YEAR(date);
END $$
DELIMITER ;
CREATE DEFINER=`wojack5555`@`%` PROCEDURE `specificDailyAssets`(IN startDate DATE, IN endDate DATE)
BEGIN
	#Purpose: Used to output a specific range of daily_assets in Java (via JDBC)
	#Author: dmill166
    #Date Modified: December 2, 2020

	#Assign parameters to local variables
	SET @startDate = startDate;
	SET @endDate = endDate;

	#Select a table of results including assets by day
	SELECT DISTINCT '30', '12', DAY(date), MONTH(date), YEAR(date), assets
    FROM daily_assets
    WHERE date BETWEEN @startDate AND @endDate
    ORDER BY '30', '12', MONTH(date), DAY(date), YEAR(date);
END
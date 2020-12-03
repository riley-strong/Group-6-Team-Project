CREATE DEFINER=`wojack5555`@`%` PROCEDURE `daily_assets`(IN startDate DATE, IN endDate DATE)
BEGIN
	#Purpose: Used to fill in daily_assets table
	#Author: dmill166
    #Date Modified: December 1, 2020
    
    #Creates Staging table to store aggregated assets for a single day
	DROP TABLE IF EXISTS assets_staging;
	CREATE TEMPORARY TABLE assets_staging (
	date DATE,  assetWorth DECIMAL(64,2));

	#Inserts all products and wholesale cost for a particular day
	INSERT INTO assets_staging
	SELECT ih.date, (ih.quantity * i.wholesale_cost)
	FROM inv_hist ih
	INNER JOIN inventory i on ih.product_id = i.product_id
	WHERE ih.date BETWEEN startDate AND endDate;

	#Returns a table with aggregated daily assets (all products) for a range of dates
	SELECT ast.date, SUM(ast.assetWorth) 'Daily Assets'
	FROM assets_staging ast
    GROUP BY ast.date
    ORDER BY ast.date;
END
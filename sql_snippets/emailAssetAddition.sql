CREATE DEFINER=`wojack5555`@`%` PROCEDURE `emailAssetAddition`()
BEGIN
	#Purpose: Used to supplement daily_assets table with assets generated through email orders
	#Author: dmill166
    #Date Modified: December 2, 2020

	#Create a table to store assets from email orders
	CREATE TEMPORARY TABLE emailAssetUpdate (
		date DATE, emailAssets DECIMAL (64, 2));
		
	#Insert the processed email orders into the table in addition to their asset worth
	INSERT INTO emailAssetUpdate
	SELECT us.date , SUM(us.product_tid * inv.product_tid)
	FROM unprocessed_sales us
	INNER JOIN inventory inv on us.product_tid = inv.product_tid
	INNER JOIN processed_sales ps ON us.product_tid = ps.product_tid and us.date = ps.date
	WHERE ps.result = 1
	GROUP BY us.date;

	#Create a date variable for email orders
	SELECT @assetDate:=date
    FROM emailAssetUpdate;
    
    #Checks for existing record of specific date in daily_assets table
	IF EXISTS (SELECT date FROM daily_assets WHERE date = @assetDate) THEN
		#Supplement existing date record in daily_assets with email order asset worth
        UPDATE daily_assets da, emailAssetUpdate eau
		SET assets = assets + emailAssets
		where da.date = eau.date;
	#Date not found in daily_assets table
    ELSE
		#Store email assets in a variable
		SELECT @emailAssets:=emailAssets
		FROM emailAssetUpdate;
		
        #Insert a new date record into daily_assets table
		INSERT INTO daily_assets VALUES
		(@assetDate, assets + @emailAssets);
	END IF;      
END
DELIMITER $$
CREATE DEFINER=`wojack5555`@`localhost` PROCEDURE `emailAssetAddition`()
BEGIN
	
	CREATE TEMPORARY TABLE emailAssetUpdate (
		date DATE, emailAssets DECIMAL (64, 2));
		
	
	INSERT INTO emailAssetUpdate
	SELECT us.date , SUM(us.product_tid * inv.product_tid)
	FROM unprocessed_sales us
	INNER JOIN inventory inv on us.product_tid = inv.product_tid
	INNER JOIN processed_sales ps ON us.product_tid = ps.product_tid and us.date = ps.date
	WHERE ps.result = 1
	GROUP BY us.date;

	
	SELECT @assetDate:=date
    FROM emailAssetUpdate;
    
    
	IF EXISTS (SELECT date FROM daily_assets WHERE date = @assetDate) THEN
		
        UPDATE daily_assets da, emailAssetUpdate eau
		SET assets = assets + emailAssets
		where da.date = eau.date;
	
    ELSE
		
		SELECT @emailAssets:=emailAssets
		FROM emailAssetUpdate;
		
        
		INSERT INTO daily_assets VALUES
		(@assetDate, assets + @emailAssets);
	END IF;      
END $$
DELIMITER ;
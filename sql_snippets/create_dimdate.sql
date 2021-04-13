DELIMITER $$
CREATE DEFINER=`wojack5555`@`localhost` PROCEDURE `create_dimdate`(IN startDate DATE, IN endDate DATE)
BEGIN
	
	SET @startDate = startDate;
    SET @endDate = endDate;
    SET @n = -1; 					 
    
	
	CREATE OR REPLACE VIEW v3 AS SELECT 1 n UNION ALL SELECT 1 UNION ALL SELECT 1; 
	CREATE OR REPLACE VIEW v AS SELECT 1 n FROM v3 a, v3 b UNION ALL SELECT 1; 
    
	DROP TABLE IF EXISTS dim_date; 
	CREATE TABLE dim_date(date DATE,  INDEX date_index (date)); 

	
	SET @insertSTMT = CONCAT('INSERT INTO dim_date (date) SELECT CAST(@startDate + INTERVAL @n:=@n+1 day AS DATE) AS dt FROM v a, v b, v c, v d, v e, v LIMIT ', (DATEDIFF(endDate, startDate) + 1));
	PREPARE STMT FROM @insertSTMT;
	EXECUTE STMT;
	DEALLOCATE PREPARE STMT; 
    
    DROP VIEW v;
    DROP VIEW v3;
END $$
DELIMITER ;
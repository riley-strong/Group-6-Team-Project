CREATE DEFINER=`wojack5555`@`%` PROCEDURE `create_dimdate`(IN startDate DATE, IN endDate DATE)
BEGIN
	#Purpose: To allow anyone to create a date dimension table
	#Author: dmill166
    #Date Created: November 15, 2020
    
	#Modify requirements for the historical date reference table here.
		#NOTE: Minimize range between @startDate and @endDate as much as possible (for efficiency sake).alter
	SET @startDate = startDate;
    SET @endDate = endDate;
    SET @n = -1; 					 #Used in generated dim_date table. -1 = @startDate as first date record.
    
	#Step 1: Rapidly create views (of ints) that will then generate a date dimension table.
	CREATE OR REPLACE VIEW v3 AS SELECT 1 n UNION ALL SELECT 1 UNION ALL SELECT 1; 
	CREATE OR REPLACE VIEW v AS SELECT 1 n FROM v3 a, v3 b UNION ALL SELECT 1; 
    
	#Step 2: Delete dim_date table if it exists. Create an indexed dimension table for dates.
	DROP TABLE IF EXISTS dim_date; 
	CREATE TABLE dim_date(date DATE,  INDEX date_index (date)); 

	#Step 3: Builds and executes prepared statement to insert records into dim_date based on @startDate, @n, and @datesCreated variables established at top of code.
	SET @insertSTMT = CONCAT('INSERT INTO dim_date (date) SELECT CAST(@startDate + INTERVAL @n:=@n+1 day AS DATE) AS dt FROM v a, v b, v c, v d, v e, v LIMIT ', (DATEDIFF(endDate, startDate) + 1));
	PREPARE STMT FROM @insertSTMT;
	EXECUTE STMT;
	DEALLOCATE PREPARE STMT; 
	
    
    #Step 5: Load dim_date with relevant dates.
    #DROP TABLE IF EXISTS dim_date;
    #CREATE TABLE dim_date (date DATE);
    #INSERT INTO dim_date (date)
    #SELECT date FROM temp_dim_date;
    
    #Step 6: Delete temp_dim_date table.alter
    #DROP TABLE temp_dim_date;
    
    #Step 7: Drop views used to create dim_date table.
    DROP VIEW v;
    DROP VIEW v3;
END
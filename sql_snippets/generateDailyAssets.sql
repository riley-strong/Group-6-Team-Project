DELIMITER $$
CREATE DEFINER=`wojack5555`@`localhost` PROCEDURE `generateDailyAssets`(IN theDate DATE)
BEGIN
	
	SET @theDate = theDate;

	
	DROP TABLE IF EXISTS sales;
	CREATE TABLE sales (
	date DATE, product_tid INT, inv_quantity INT, sold_quantity INT, restock_quantity INT);

	
	INSERT INTO sales
	SELECT @theDate, product_tid, quantity, 0, 0
	FROM inventory;


	
	ALTER TABLE processed_sales ADD INDEX ps_p_index (product_tid), ADD INDEX ps_d_index (date);

	
	DROP TABLE IF EXISTS temp_ps;
	CREATE TABLE temp_ps (product_tid INT, quantity INT);
	INSERT INTO temp_ps
	SELECT product_tid, SUM(quantity)
	FROM processed_sales
	WHERE date >= @theDate
    AND result = 1
	GROUP BY product_tid;

	
	DROP INDEX ps_p_index ON processed_sales;
	DROP INDEX ps_d_index ON processed_sales;
	ALTER TABLE supplier_orders ADD INDEX so_p_index (product_tid), ADD INDEX so_d_index (date);

	
	DROP TABLE IF EXISTS temp_so;
	CREATE TABLE temp_so (product_tid INT, quantity INT);
	INSERT INTO temp_so
	SELECT product_tid, SUM(quantity)
	FROM supplier_orders
	WHERE date >= @theDate
	GROUP BY product_tid;

	DROP INDEX so_p_index ON supplier_orders;
	DROP INDEX so_d_index ON supplier_orders;
	ALTER TABLE sales ADD INDEX sales_p_index (product_tid), ADD INDEX sales_d_index (date);
	ALTER TABLE temp_ps ADD INDEX tps_p_index (product_tid);
	ALTER TABLE temp_so ADD INDEX tso_p_index (product_tid);

	
	update sales s, temp_ps tps
	SET s.sold_quantity = tps.quantity
	WHERE s.product_tid = tps.product_tid;

	
	update sales s, supplier_orders so
	SET s.restock_quantity = so.quantity
	WHERE s.product_tid = so.product_tid;

	INSERT INTO daily_assets
	SELECT date, SUM((inv_quantity + sold_quantity + restock_quantity) * i.wholesale_cost)
	FROM sales s
	INNER JOIN inventory i ON s.product_tid = i.product_tid
	GROUP BY s.date;

    DROP TABLE temp_ps;
    DROP TABLE temp_so;
	DROP TABLE sales;
END $$
DELIMITER ;
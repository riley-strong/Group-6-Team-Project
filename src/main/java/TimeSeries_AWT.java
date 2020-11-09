import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import java.sql.*;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.LinkedList;

public class TimeSeries_AWT extends ApplicationFrame {


        //private static Connection connection;
        private static Statement statement;
        private final int assets = 1;
        private final int dailyOrders = 2;
        private final int dailyPurchase = 3;
//        private static Object JDBCTutorialUtilities;
//        private PreparedStatement preparedStatement;
//        private ResultSet tempRS;


    public TimeSeries_AWT(String title, Credentials credentials, int op) throws IOException, SQLException, ClassNotFoundException {
        super(title);
        QueryMaker qm = credentials.getQueryMaker();
        statement = qm.statement;
        XYDataset dataset = createDataset(op);
        JFreeChart chart = createChart(dataset, op);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 370 ) );
        chartPanel.setMouseZoomable(true ,false);
        setContentPane( chartPanel );
        ChartUtilities.saveChartAsPNG(new File("line_chart.png"), chart, 450, 400);
    }

    private XYDataset createDataset(int op) throws SQLException {
        TimeSeries series = null;
        // SQL Step 1: Company Assets (1 cell value)
        if(op == 1) {

            ResultSet rs = statement.executeQuery("SELECT SUM(CAST(wholesale_cost * quantity AS DECIMAL (64,2))) FROM inventory");

            while (rs.next()) {
                double totalAssets = rs.getDouble(1);
            }
            series = new TimeSeries( "Assets" );
            //Add bar chart instead of Timeseries


        }

        // SQL Step 2: Daily Orders
        else if(op == 2) {
            ResultSet rs2 = statement.executeQuery("SELECT '30', '12', DAY(date), MONTH(date), YEAR(date), count(product_id) " +
                    "FROM processed_sales " +
                    "GROUP BY '30', '12', DAY(date), MONTH(date), YEAR(date) " +
                    "ORDER BY '30', '12', MONTH(date), DAY(date), YEAR(date)");


            LinkedList<int[]> dailyOrder = new LinkedList<>();

            while (rs2.next()) {
                int[] arr = new int[6];
                for (int i = 0; i < 6; i++) {
                    arr[i] = rs2.getInt(i + 1);
                }

                dailyOrder.add(arr);
            }// End while
            series = new TimeSeries("Daily Orders");
            for(int[] i : dailyOrder){
                series.add(new Minute( i[0], i[1], i[2], i[3], i[4]), i[5]);
            }

        }// End Daily Orders

            // SQL Step 3: Daily Purchase Totals
        else if(op == 3){
            ResultSet rs3 = statement.executeQuery("SELECT '30', '12', DAY(date), MONTH(date), YEAR(date), sum(CAST(ps.product_quantity * i.sale_price AS DECIMAL (64, 2))) " +
                    "FROM processed_sales ps " +
                    "INNER JOIN inventory i on ps.product_id = i.product_id " +
                    "GROUP BY '30', '12', DAY(date), MONTH(date), YEAR(date) " +
                    "ORDER BY '30', '12', MONTH(date), DAY(date), YEAR(date)");

            LinkedList<double[]> dailyPurchase = new LinkedList<double[]>();

            while (rs3.next()) {
                double[] arr = new double[6];
                for (int i = 0; i < 6; i++) {
                    arr[i] = rs3.getDouble(i + 1);
                }

                dailyPurchase.add(arr);

            }// End while
            series = new TimeSeries( "Daily Purchase" );

            for(double[] i : dailyPurchase){
                series.add(new Minute((int) i[0],(int) i[1],(int) i[2],(int) i[3],(int) i[4]), i[5]);
            }
        }//End else


        return new TimeSeriesCollection(series);
    }

    private JFreeChart createChart(XYDataset dataset, int op) {
        if (op == 1) {
            return ChartFactory.createTimeSeriesChart(
                    "Assets",
                    "Months",
                    "Quantity",
                    dataset,
                    false,
                    false,
                    false);
        }
        else if(op == 2){
            return ChartFactory.createTimeSeriesChart(
                    "Daily Orders",
                    "Months",
                    "Quantity",
                    dataset,
                    false,
                    false,
                    false);
        }
        else if(op == 3){
            return ChartFactory.createTimeSeriesChart(
                    "Daily Purchases",
                    "Months",
                    "Quantity",
                    dataset,
                    false,
                    false,
                    false);
        }
        return null;
    }

}   
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import java.sql.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class TimeSeries_AWT extends ApplicationFrame {

    //private static Connection connection;
    private static Statement statement;
    private final int assets = 1;
    private final int dailyOrders = 2;
    private final int dailyPurchase = 3;
    private final QueryMaker qm;
//        private static Object JDBCTutorialUtilities;
//        private PreparedStatement preparedStatement;
//        private ResultSet tempRS;


    public TimeSeries_AWT(String title, Credentials credentials, int op, String start, String end) throws IOException, SQLException, ClassNotFoundException {
        super(title);
        qm = credentials.getQueryMaker();
        statement = QueryMaker.statement;
        XYDataset dataset = createDataset(op, start, end);
        JFreeChart chart = createChart(dataset, op);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 370));
        chartPanel.setMouseZoomable(true, false);
        setContentPane(chartPanel);
        ChartUtilities.saveChartAsPNG(new File("line_chart.png"), chart, 450, 400);
    }

    private XYDataset createDataset(int op, String start, String end) throws SQLException {
        TimeSeries series = null;
        ArrayList<Object[]> al = new ArrayList<>();

        // SQL Step 1: Company Assets (1 cell value)
        if (op == 1) {

            //TODOd: Iterate through ArrayList to get elements & add to TimeSeries
            al = qm.getAnalyticsData(start, end, assets);


            series = new TimeSeries("Assets");
            //Add bar chart instead of Timeseries
            for (Object[] i : al) {
                series.add(new Minute((int) i[0], (int) i[1], (int) i[2], (int) i[3], (int) i[4]), (Double) i[5]);
            }

        }

        // SQL Step 2: Daily Orders
        else if (op == 2) {
            //TODOd: Iterate through ArrayList to get elements & add to TimeSeries
            al = qm.getAnalyticsData(start, end, dailyOrders);

            series = new TimeSeries("Daily Orders");
            for (Object[] i : al) {
                series.add(new Minute((int) i[0], (int) i[1], (int) i[2], (int) i[3], (int) i[4]), (int) i[5]);
            }

        }// End Daily Orders

        // SQL Step 3: Daily Purchase Totals
        else if (op == 3) {
            //TODOd: Iterate through ArrayList to get elements & add to TimeSeries
            al = qm.getAnalyticsData(start, end, dailyPurchase);

            series = new TimeSeries("Daily Purchase");

            for (Object[] i : al) {
                series.add(new Minute((int) i[0], (int) i[1], (int) i[2], (int) i[3], (int) i[4]), (Double) i[5]);
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
        } else if (op == 2) {
            return ChartFactory.createTimeSeriesChart(
                    "Daily Orders",
                    "Months",
                    "Quantity",
                    dataset,
                    false,
                    false,
                    false);
        } else if (op == 3) {
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
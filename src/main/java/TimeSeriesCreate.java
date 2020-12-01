import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class TimeSeriesCreate extends ApplicationFrame {

    String dateAxis = "Month";

    public TimeSeriesCreate(final String title, String chartTitle) {
        super(title);
        String valueAxisLabel;
        if (chartTitle.equals("Assets")) {
            valueAxisLabel = "Assets";
        } else if (chartTitle.equals("Customer Orders")) {
            valueAxisLabel = "Order #";
        } else
            valueAxisLabel = "Total";

        final XYDataset dataset = createDataset();
        final JFreeChart chart = createChart(dataset, chartTitle, dateAxis, valueAxisLabel);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 370));
        chartPanel.setMouseZoomable(true, false);
        setContentPane(chartPanel);
    }

   /* public void createAssetsTimeSeries (String title, String chartTitle, String xAxisLabel, String yAxisLabel ){
         title = "CUP'O JAVA ASSETS";
         chartTitle = "Quantity Count";
         xAxisLabel = "Months";
         yAxisLabel = "Quantity";
         new TimeSeriesCreate(title, chartTitle, xAxisLabel, yAxisLabel);
    }*/

    public static void main(final String[] args) {
        String title = "CUP'O JAVA ASSETS";
        String chartTitle = "Assets";
        TimeSeriesCreate demo = new TimeSeriesCreate(title, chartTitle);
        demo.pack();
        RefineryUtilities.positionFrameRandomly(demo);
        demo.setVisible(true);
    }

    private XYDataset createDataset() {
        final TimeSeries series = new TimeSeries("TimeSeries");

        series.add(new Minute(30, 12, 01, 1, 2020), 10000.0);
        series.add(new Minute(30, 12, 01, 2, 2020), 4561.0);
        series.add(new Minute(30, 12, 01, 3, 2020), 1564.0);
        series.add(new Minute(30, 12, 01, 4, 2020), 7456.0);
        series.add(new Minute(30, 12, 01, 5, 2020), 1245.0);
        series.add(new Minute(30, 12, 01, 6, 2020), 6587.0);
        series.add(new Minute(30, 12, 01, 7, 2020), 8645.0);
        series.add(new Minute(30, 12, 01, 8, 2020), 1259.0);
        series.add(new Minute(30, 12, 01, 9, 2020), 1259.0);


        return new TimeSeriesCollection(series);
    }

    private JFreeChart createChart(final XYDataset dataset, String title, String timeAxisLabel, String valueAxisLabel) {
        return ChartFactory.createTimeSeriesChart(
                title,
                timeAxisLabel,
                valueAxisLabel,
                dataset,
                false,
                false,
                false);
    }
}   
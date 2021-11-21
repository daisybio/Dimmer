package dk.sdu.imada.gui.plots;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.IntervalXYDataset;

/**
 * A demo of the {@link HistogramDataset} class.
 */
public class HistogramPvalueDistribution {

    /**
     * Creates a new demo.
     *
     * @param title  the frame title.
     */
	
	JFreeChart chart;
	ChartPanel panel;
	double domainMin, domainMax;	
    public HistogramPvalueDistribution(String title, double values[], String xlabel, String ylabel, int binningSize, Color awtColot, double min, double max, double domainMin, double domainMax) {
    	this.domainMin = domainMin;
    	this.domainMax = domainMax;
        createDemoPanel(values, title, xlabel, ylabel, binningSize, awtColot, min, max);
	}
    
    public HistogramPvalueDistribution(String title, double values[], String xlabel, String ylabel, int binningSize, Color awtColot,double domainMin, double domainMax) {
    	this.domainMax = domainMax;
    	this.domainMin = domainMin;
        createDemoPanel(values, title, xlabel, ylabel, binningSize, awtColot);
	}

	/**
     * Creates a sample {@link HistogramDataset}.
     *
     * @return the dataset.
     */
    private IntervalXYDataset createDataset(double []values, int binningSize) {
        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries("p0", values, binningSize);
        return dataset;
    }
    
    private IntervalXYDataset createDataset(double []values, int binningSize, double min, double max) {
    	
        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries("p0", values, binningSize, min, max);
        return dataset;
    }

    /**
     * Creates a chart.
     *
     * @param dataset  a dataset.
     *
     * @return The chart.
     */
    private JFreeChart createChart(IntervalXYDataset dataset, String title, String xlabel, String ylabel, Color awtColot) {
        JFreeChart chart = ChartFactory.createHistogram(
            title,
            xlabel,
            ylabel,
            dataset,
            PlotOrientation.VERTICAL,
            false,
            true,
            false);
        XYPlot plot = (XYPlot) chart.getPlot();
        NumberAxis domain = (NumberAxis) plot.getDomainAxis();
        domain.setRange(domainMin, domainMax);
        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        plot.setForegroundAlpha(0.85f);
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);
        renderer.setSeriesPaint(0, awtColot);
        // flat bars look best...
        renderer.setBarPainter(new StandardXYBarPainter());
        renderer.setShadowVisible(false);
        
        return chart;
    }

    /**
     * Creates a panel for the demo (used by SuperDemo.java).
     *
     * @return A panel.
     */
    private void createDemoPanel(double values[], String title, String xlabel, String ylabel, int binningSize,	Color awtColot) {
        chart = createChart(createDataset(values, binningSize), title, xlabel, ylabel, awtColot);
        panel = new ChartPanel(chart);
        //panel.setMouseWheelEnabled(false);
    }
    
    private void createDemoPanel(double values[], String title, String xlabel, String ylabel, int binningSize,	Color awtColot, double min, double max) {
        chart = createChart(createDataset(values, binningSize, min, max), title, xlabel, ylabel, awtColot);
        panel = new ChartPanel(chart);
        //panel.setMouseWheelEnabled(false);
    }
    
    public JFreeChart getChart() {
		return chart;
	}
    
    public ChartPanel getPanel() {
		return panel;
	}
}

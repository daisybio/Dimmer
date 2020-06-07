package dk.sdu.imada.gui.plots;

import java.awt.Color;
import java.util.Arrays;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.IntervalXYDataset;

/**
 * A demo of the {@link HistogramDataset} class.
 */
public class HistogramScoreDistribution {

    /**
     * Creates a new demo.
     *
     * @param title  the frame title.
     */
	
	JFreeChart chart;
	ChartPanel panel;
	double reference[];
	double max;
	
	
    public HistogramScoreDistribution(String title, double values[], double [] reference, String xlabel, String ylabel, int binningSize, Color awtColot) {
    	this.reference = reference;
    	setMax(values, reference);
        createDemoPanel(values, reference, title, xlabel, ylabel, binningSize, awtColot);
	}
    
    
    private void setMax(double values[], double [] reference) {
    	Arrays.sort(values);
    	Arrays.sort(reference);
    	this.max = reference[reference.length-1];
    	
    	if (this.max <= values[values.length - 1]) this.max = values[values.length - 1]; 
	}

	/**
     * Creates a sample {@link HistogramDataset}.
     *
     * @return the dataset.
     */
    private IntervalXYDataset createDataset(double []values, int binningSize) {
        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries("DMR's permuted data scores: #CpGs/length(bp)", values, binningSize, 0.0, max);
        return dataset;
    }
    
    /**
     * Creates a chart.
     *
     * @param dataset  a dataset.
     *
     * @return The chart.
     */
    private JFreeChart createChart(IntervalXYDataset dataset, String title, String xlabel, String ylabel, Color awtColot, double[] reference) {
        JFreeChart chart = ChartFactory.createHistogram(
            title,
            xlabel,
            ylabel,
            dataset,
            PlotOrientation.VERTICAL,
            true,
            false,
            false);
        
        chart.setBackgroundPaint(Color.white);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDomainGridlinePaint(Color.white);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);
        plot.setRangeGridlinePaint(Color.white);
        plot.setBackgroundPaint(Color.white);
        
        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        plot.setForegroundAlpha(0.85f);
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);
        renderer.setSeriesPaint(0, awtColot);
        
        // flat bars look best...
        Arrays.sort(reference);
        
        ValueMarker m = new ValueMarker(reference[reference.length - 1]);
        m.setPaint(Color.BLACK);
    	plot.addDomainMarker(m);
    	//plot.marker
    	
        renderer.setBarPainter(new StandardXYBarPainter());
        renderer.setShadowVisible(false);
        
        return chart;
    }

    /**
     * Creates a panel for the demo (used by SuperDemo.java).
     * @return A panel.
     */
    private void createDemoPanel(double values[], double [] reference, String title, String xlabel, String ylabel, int binningSize,	Color awtColot) {
        chart = createChart(createDataset(values, binningSize), title, xlabel, ylabel, awtColot, reference);
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

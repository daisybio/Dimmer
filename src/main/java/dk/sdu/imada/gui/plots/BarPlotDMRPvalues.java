package dk.sdu.imada.gui.plots;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;

/**
 * A demo of the {@link HistogramDataset} class.
 */
public class BarPlotDMRPvalues {

	/**
	 * Creates a new demo.
	 *
	 * @param title  the frame title.
	 */

	JFreeChart chart;
	ChartPanel panel;
	double reference;
	double max;


	public BarPlotDMRPvalues(String title, double values[], double reference, String xlabel, String ylabel, int binningSize, Color awtColot) {
		this.reference = reference;
		createDemoPanel(values, reference, title, xlabel, ylabel, binningSize, awtColot);
	}



	/**
	 * Creates a sample {@link HistogramDataset}.
	 *
	 * @return the dataset.
	 */
	/*private IntervalXYDataset createDataset(double []values, int binningSize) {
        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries("DMR's permuted data scores: #CpGs/length(bp)", values, binningSize, 0.0, max);
        return dataset;
    }*/

	private CategoryDataset createDataset(double []values) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		int i = 1;
		for (double d : values) {
			dataset.addValue(d, "", i+"");
			i++;
		}

		//dataset.addSeries("DMR's permuted data scores: #CpGs/length(bp)", values, binningSize, 0.0, max);
		return dataset;
	}

	/**
	 * Creates a chart.
	 *
	 * @param dataset  a dataset.
	 *
	 * @return The chart.
	 */
	private JFreeChart createChart(CategoryDataset dataset, String title, String xlabel, String ylabel, Color awtColot, double reference) {
		JFreeChart chart = ChartFactory.createBarChart(
				title,         // chart title
				xlabel,               // domain axis label
				ylabel,                  // range axis label
				dataset,                  // data
				PlotOrientation.VERTICAL, // orientation
				false,                     // include legend
				false,                     // tooltips?
				false                     // URLs?
				);

		CategoryPlot plot = (CategoryPlot) chart.getPlot();

		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setMaximumCategoryLabelWidthRatio(0.8f);
		domainAxis.setLowerMargin(0.02);
		domainAxis.setUpperMargin(0.02);
		domainAxis.setTickLabelsVisible(false);
		domainAxis.setCategoryMargin(0.0);

		
		// set the range axis to display integers only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setRange(0.0, 3 * reference);

		// disable bar outlines...
		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setDrawBarOutline(false);
		renderer.setBarPainter(new StandardBarPainter());

		// set up gradient paints for series...
		renderer.setSeriesPaint(0, awtColot);
		
		plot.setBackgroundPaint(Color.white);

		ValueMarker m = new ValueMarker(reference);
		m.setPaint(Color.BLACK);
		plot.addRangeMarker(m);
		
		return chart;
	}

	/**
	 * Creates a panel for the demo (used by SuperDemo.java).
	 * @return A panel.
	 */
	private void createDemoPanel(double values[], double reference, String title, String xlabel, String ylabel, int binningSize,	Color awtColot) {
		chart = createChart(createDataset(values), title, xlabel, ylabel, awtColot, reference);
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

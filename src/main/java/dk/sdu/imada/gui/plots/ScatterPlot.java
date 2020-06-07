package dk.sdu.imada.gui.plots;

import java.awt.BasicStroke;
import java.awt.Color;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.function.Function2D;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYDataset;


public class ScatterPlot {

	/**
	 * A demonstration application showing a scatter plot.
	 *
	 * @param title  the frame title.
	 */

	JPanel panel;

	JFreeChart chart;
	 boolean log10;

	public JPanel getPanel() {
		return panel;
	}

	public JFreeChart getChart() {
		return chart;
	}

	public ScatterPlot(String title, XYData dataset, String xaxis, String yaxis, Color awtColot, boolean log10) {
		this.log10 = log10;
		createDemoPanel(dataset, title, xaxis, yaxis, awtColot);
	}

	private JFreeChart createChart(XYData dataset, String title, String xaxis, String yaxis, Color awtColot) {

		XYDotRenderer xyRender = new XYDotRenderer();
		xyRender.setDotWidth(2);
		xyRender.setDotHeight(2);
		xyRender.setSeriesPaint(0, awtColot);

		NumberAxis xAxis = new NumberAxis(xaxis);
		xAxis.setAutoRangeIncludesZero(true);
		NumberAxis yAxis = new NumberAxis(yaxis);
		yAxis.setAutoRangeIncludesZero(true);

		//XYPlot plot = new XYPlot(dataset, xAxis, yAxis, xyRender);
		XYPlot plot = new XYPlot();
		
		if (!log10) {
			xAxis.setRange(0.00, 1.00);
			yAxis.setRange(0.00, 1.00);
		}
		
		plot.setDomainAxis(xAxis);
		plot.setRangeAxis(yAxis);

		plot.setDomainCrosshairVisible(true);
		plot.setDomainCrosshairLockedOnData(true);
		plot.setRangeCrosshairVisible(true);
		plot.setRangeCrosshairLockedOnData(true);
		plot.setDomainZeroBaselineVisible(false);
		plot.setRangeZeroBaselineVisible(false);

		NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setAutoRangeIncludesZero(false);

		Function2D function2d = new LineFunction2D(0, 1);
		XYDataset line = DatasetUtilities.sampleFunction2D(function2d, 0, dataset.maxX, 2, "");
		XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer(true, false);
		
		lineRenderer.setSeriesPaint(0, Color.black);
		lineRenderer.setSeriesStroke(0, new BasicStroke(2.f));
//		lineRenderer.setUseFillPaint(true);
//		lineRenderer.setSeriesShapesFilled(0, true	);
		plot.setDataset(0, line);
		plot.setRenderer(0, lineRenderer);
		
		plot.setDataset(1, dataset);
		plot.setRenderer(1, xyRender);

		//plot.setBackgroundPaint(Color.lightGray);
		JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
		
		return chart;
	}

	/**
	 * Creates a panel for the demo (used by SuperDemo.java).
	 *
	 * @return A panel.
	 */
	public void createDemoPanel(XYData dataset, String title, String xaxis, String yaxis, Color awtColot) {
		this.chart = createChart(dataset, title, xaxis, yaxis, awtColot);
		this.panel = new ChartPanel(chart);
	}

	/*public static void main(String[] args) {
		ScatterPlotDemo2 demo = new ScatterPlotDemo2("JFreeChart: ScatterPlotDemo2.java");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);
	}*/
}

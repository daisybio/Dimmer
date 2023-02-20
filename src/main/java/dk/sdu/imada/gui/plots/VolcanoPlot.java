package dk.sdu.imada.gui.plots;

import java.awt.Color;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;


public class VolcanoPlot {

	/**
	 * A demonstration application showing a scatter plot.
	 *
	 * @param title  the frame title.
	 */

	JPanel panel;

	JFreeChart chart;

	public JPanel getPanel() {
		return panel;
	}

	public JFreeChart getChart() {
		return chart;
	}

	public VolcanoPlot(String title, XYLogData dataset, String xaxis, String yaxis) {
		createDemoPanel(dataset, title, xaxis, yaxis);
	}

	private JFreeChart createChart(XYLogData dataset, String title, String xaxis, String yaxis) {

		XYDotRenderer xyRender = new XYDotRenderer();
		xyRender.setDotWidth(3);
		xyRender.setDotHeight(2);
		xyRender.setSeriesPaint(0, Color.BLUE);
		xyRender.setSeriesPaint(1, Color.RED);
		xyRender.setSeriesPaint(2, Color.GREEN);

		NumberAxis xAxis = new NumberAxis(xaxis);
		xAxis.setAutoRangeIncludesZero(true);
		NumberAxis yAxis = new NumberAxis(yaxis);
		yAxis.setAutoRangeIncludesZero(true);

		//XYPlot plot = new XYPlot(dataset, xAxis, yAxis, xyRender);
		XYPlot plot = new XYPlot();
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

		plot.setDataset(1, dataset);
		plot.setRenderer(1, xyRender);

		JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
		
		return chart;
	}

	/**
	 * Creates a panel for the demo (used by SuperDemo.java).
	 *
	 * @return A panel.
	 */
	public void createDemoPanel(XYLogData dataset, String title, String xaxis, String yaxis) {
		this.chart = createChart(dataset, title, xaxis, yaxis);
		this.panel = new ChartPanel(chart);
	}

	/*public static void main(String[] args) {
		ScatterPlotDemo2 demo = new ScatterPlotDemo2("JFreeChart: ScatterPlotDemo2.java");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);
	}*/
}

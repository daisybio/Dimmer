package dk.sdu.imada.gui.controllers.util;

import java.util.ArrayList;

import org.jfree.chart.JFreeChart;

import dk.sdu.imada.gui.controllers.MainController;
import dk.sdu.imada.gui.plots.HistogramDistanceDistribution;
import dk.sdu.imada.jlumina.core.io.ReadManifest;
import dk.sdu.imada.jlumina.core.primitives.CpG;

public class DMRParametersUtil {
	MainController mainController;
	
	public DMRParametersUtil(MainController mainController) {
		this.mainController = mainController;
	}
	
	public void setScreen() {
		JFreeChart cpgDistanceChart = getDistanceDistributionChart(getCpGDistribution(10000));
		mainController.setCpgDistanceChart(cpgDistanceChart);
		mainController.setDMRParametersScreen();
	}
	
	private JFreeChart getDistanceDistributionChart(double distance[]) {
		HistogramDistanceDistribution his = new HistogramDistanceDistribution("CpG distance distribution", distance, 
				"distance difference (bp)", "Frequency", 100, java.awt.Color.BLUE);
		JFreeChart chart = his.getChart();
		return chart;
	}
	
	private double[] getCpGDistribution(double treshold) {

		ReadManifest m = mainController.getManifest();

		double distance[] = new double[m.getCpgList().length];
		String chr[] = new String[m.getCpgList().length];

		int i = 0;
		for (CpG cpg : m.getCpgList()) {
			distance[i] = cpg.getMapInfo();
			chr[i] = cpg.getChromosome();
			i++;
		}

		ArrayList<Double> distribution = new ArrayList<Double>();
		for (i = 1; i < distance.length; i++) {

			double d = distance[i] - distance[i-1];

			if (chr[i].equals(chr[i-1]) && d<= treshold) {
				distribution.add(d);
			}
		}
		distance=null;
		chr=null;

		double[] list = new double[distribution.size()];
		i = 0;
		for (Double d : distribution) {
			list[i] = d;
			i++;
		}

		distribution = null;

		return list;
	}
}

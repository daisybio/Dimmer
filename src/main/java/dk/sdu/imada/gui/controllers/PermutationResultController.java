package dk.sdu.imada.gui.controllers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import dk.sdu.imada.gui.controllers.util.DMRParametersUtil;
import dk.sdu.imada.gui.controllers.util.SaveAll;
import dk.sdu.imada.gui.monitors.SaveAllMonitor;
import dk.sdu.imada.gui.plots.VolcanoPlot;
import dk.sdu.imada.gui.plots.XYLogData;
import dk.sdu.imada.jlumina.core.util.MatrixUtil;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class PermutationResultController {

	@FXML CheckBox useLog10;
	@FXML CheckBox useLog10glob;
	@FXML CheckBox useLog10sdm;
	@FXML CheckBox useLog10fdr;
	@FXML CheckBox useLogOrig;


	@FXML public ImageView empiricalHist;
	@FXML public ImageView empiricalScatter;
	@FXML public ImageView empiricalVulcano;

	@FXML public ImageView fwerHist;
	@FXML public ImageView fwerScatter;
	@FXML public ImageView fwerVulcano;

	@FXML public ImageView fdrHist;
	@FXML public ImageView fdrScatter;
	@FXML public ImageView fdrVulcano;

	@FXML public ImageView sdmHist;
	@FXML public ImageView sdmScatter;
	@FXML public ImageView sdmVulcano;

	@FXML public ImageView maxFdrHist;
	@FXML public ImageView maxFdrScatter;
	@FXML public ImageView maxFdrVulcano;

	@FXML public ImageView origHist;
	@FXML public ImageView origVulcano;

	@FXML public VBox scrollVboxOrig;
	@FXML public VBox scrollVboxFwer;
	@FXML public VBox scrollVboxFdr;
	@FXML public VBox scrollVboxEmp;
	@FXML public VBox scrollVboxMax;
	@FXML public VBox scrollVboxSdc;

	@FXML public Pane paneOrig;
	@FXML public Pane paneFwer;
	@FXML public Pane paneFdr;
	@FXML public Pane paneEmp;
	@FXML public Pane peaneMax;
	@FXML public Pane peaneSdc;

	@FXML public TextField vulcanoOrigField;
	@FXML public TextField vulcanoEmpField;
	@FXML public TextField vulcanoFdrField;
	@FXML public TextField vulcanoFwerField;
	@FXML public TextField vulcanoMinPField;


	@FXML public ScrollPane scrollPane;

	MainController mainController;

	@FXML public void pushContinue(ActionEvent actionEvent) {
		DMRParametersUtil dmrParametersUtil = new DMRParametersUtil(mainController);
		dmrParametersUtil.setScreen();
		mainController.loadScreen("dmrParameters");
	}

	@FXML public void pushBack(ActionEvent actionEvent) {
		mainController.loadScreen("summary");
	}

	// empirical pvalues ........................................................
	JPanel panel;
	JFrame frame = new JFrame("");


	private void setFrameContent(JFreeChart chart) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JPanel p = (JPanel) frame.getContentPane();
				if (p!=null) {
					frame.setVisible(false);
					frame = null; 
					frame = new JFrame("");
				}

				panel = new ChartPanel(chart);
				frame.setContentPane(panel);
				frame.setSize(600, 400);
				frame.setLocationRelativeTo(null);
				frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				frame.setVisible(true);
				frame.repaint();
			}
		});
	}

	boolean frameOpen = false;
	@FXML public void showEmpiricalScatterPlot(ActionEvent actionEvent) {

		try {
			JFreeChart chart;
			if (useLog10.isSelected()) {
				chart = mainController.getEmpiricalPvaluesScatterPlotLogChart();
			}else {
				chart = mainController.getEmpiricalPvaluesScatterPlotChart();
			}

			setFrameContent(chart);

		}catch (NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}

	@FXML public void showEmpiricalHistogram(ActionEvent actionEvent) {
		try {
			JFreeChart chart = mainController.getEmpiricalPvaluesDistributionChart();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JPanel panel = new ChartPanel(chart);
					JFrame frame = new JFrame("");
					frame.setSize(600, 400);
					frame.setLocationRelativeTo(null);
					frame.add(panel);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				}
			});
		}catch(NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}

	@FXML public void showEmpiricalVulcanoPlot(ActionEvent actionEvent) {
		double treshold = Double.parseDouble(vulcanoEmpField.getText());
		mainController.setEmpiricalPvaluesVulcanoPlotChart(vulcanoPlot(mainController.getEmpiricalPvalues(), mainController.getMethylationDifference(),
				"Methylation difference Vs. Emp. p-values", "Methylation difference", "Emp. p-values (-log10)", treshold));
		try {
			JFreeChart chart = mainController.getEmpiricalPvaluesVulcanoPlotChart();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JPanel panel = new ChartPanel(chart);
					JFrame frame = new JFrame("");
					frame.setSize(600, 400);
					frame.setLocationRelativeTo(null);
					frame.add(panel);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				}
			});

			setThumbnail(this.empiricalVulcano, chart);
		}catch(NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}

	@FXML public void saveEmpiricalHistogram(ActionEvent e ) {
		JFreeChart chart1 = mainController.getEmpiricalPvaluesDistributionChart();
		BufferedImage img = chart1.createBufferedImage(1200, 800);
		exportChart(img);
	}

	@FXML public void saveEmpiricalScatterPlot(ActionEvent e) {
		JFreeChart chart;
		if (useLog10.isSelected()) {
			chart = mainController.getEmpiricalPvaluesScatterPlotLogChart();
		}else {
			chart = mainController.getEmpiricalPvaluesScatterPlotChart();
		}
		BufferedImage img = chart.createBufferedImage(800, 800);
		exportChart(img);
	}

	@FXML public void saveEmpiricalVulcanoPlot(ActionEvent e) {
		double treshold = Double.parseDouble(vulcanoEmpField.getText());
		mainController.setEmpiricalPvaluesVulcanoPlotChart(vulcanoPlot(mainController.getEmpiricalPvalues(), mainController.getMethylationDifference(),
				"Methylation difference Vs. Emp. p-values", "Methylation difference", "Emp. p-values (-log10)", treshold));


		JFreeChart chart = mainController.getEmpiricalPvaluesVulcanoPlotChart();
		BufferedImage img = chart.createBufferedImage(1200, 800);
		exportChart(img);
	}


	@FXML public void saveAll(ActionEvent e) {

		Date d = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("yyMMddhhmmssMs");
		String datetime = ft.format(d)+"_permutationTest/";
		ProgressForm pf = new ProgressForm("Saving plots in " + datetime);

		SaveAll all = new  SaveAll(this.mainController, this, this.mainController.getOutputDirectory() + datetime);
		Thread t = new Thread(all);
		SaveAllMonitor monitor = new SaveAllMonitor(all, pf);
		Thread tm = new Thread(monitor);

		ArrayList<Thread> threads = new ArrayList<>();

		threads.add(t);
		threads.add(tm);
		pf.setThreads(threads);


		Platform.runLater(pf);
		t.start();
		tm.start();
	}

	// fwer pvalues ........................................................
	@FXML public void showFwerScatterPlot(ActionEvent actionEvent) {

		try {
			JFreeChart chart;
			if (useLog10glob.isSelected()) {
				chart = mainController.getFwerScatterPlotLogChart();
			}else {
				chart = mainController.getFwerScatterPlotChart();
			}
			setFrameContent(chart);
		}catch (NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}

	@FXML public void showFwerHistogramPlot(ActionEvent actionEvent) {
		try {
			JFreeChart chart = mainController.getFwerDistributionChart();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JPanel panel = new ChartPanel(chart);
					JFrame frame = new JFrame("");
					frame.setSize(600, 400);
					frame.setLocationRelativeTo(null);
					frame.add(panel);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				}
			});
		}catch(NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}

	@FXML public void showFwerVulcanoPlot(ActionEvent actionEvent) {
		double treshold = Double.parseDouble(vulcanoFwerField.getText());
		mainController.setFwerVulcanoPlotChart(vulcanoPlot(mainController.getFwerPvalues(), mainController.getMethylationDifference(),
				"Methylation difference Vs. FWER p-values", "Methylation difference", "FWER p-values (-log10)", treshold));
		try {
			JFreeChart chart = mainController.getFwerVulcanoPlotChart();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JPanel panel = new ChartPanel(chart);
					JFrame frame = new JFrame("");
					frame.setSize(600, 400);
					frame.setLocationRelativeTo(null);
					frame.add(panel);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				}
			});
			setThumbnail(this.fwerVulcano, chart);
		}catch(NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}

	@FXML public void saveFwerHistogram(ActionEvent e ) {
		JFreeChart chart1 = mainController.getFwerDistributionChart();
		BufferedImage img = chart1.createBufferedImage(1200, 800);
		exportChart(img);
	}

	@FXML public void saveFwerScatterPlot(ActionEvent e) {
		JFreeChart chart;
		if (useLog10glob.isSelected()) {
			chart = mainController.getFwerScatterPlotLogChart();
		}else {
			chart = mainController.getFwerScatterPlotChart();
		}
		BufferedImage img = chart.createBufferedImage(800, 800);
		exportChart(img);
	}

	@FXML public void saveFwerVulcanoPlot(ActionEvent e) {

		double treshold = Double.parseDouble(vulcanoFwerField.getText());
		mainController.setFwerVulcanoPlotChart(vulcanoPlot(mainController.getFwerPvalues(), mainController.getMethylationDifference(),
				"Methylation difference Vs. FWER p-values", "Methylation difference", "FWER", treshold));

		JFreeChart chart = mainController.getFwerVulcanoPlotChart();
		BufferedImage img = chart.createBufferedImage(1200, 800);
		exportChart(img);
	}

	// FDR pvalues ........................................................
	@FXML public void showSdcScatterPlot(ActionEvent actionEvent) {

		try {
			JFreeChart chart;
			if (useLog10sdm.isSelected()) {
				chart = mainController.getStepDownLogScatterPlotChart();
			}else {
				chart = mainController.getStepDownScatterPlotChart();
			}

			setFrameContent(chart);
		}catch (NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}

	@FXML public void showSdcHistogramPlot(ActionEvent actionEvent) {
		try {
			JFreeChart chart = mainController.getSteDownDistributionChart();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JPanel panel = new ChartPanel(chart);
					JFrame frame = new JFrame("");
					frame.setSize(600, 400);
					frame.setLocationRelativeTo(null);
					frame.add(panel);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				}
			});
		}catch(NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}

	@FXML public void showSdcVulcanoPlot(ActionEvent actionEvent) {
		double treshold = Double.parseDouble(vulcanoMinPField.getText());
		mainController.setStepDownVulcanoPlotChart(vulcanoPlot(mainController.getStepDownMinPvalues(), mainController.getMethylationDifference(),
				"Methylation difference Vs. Step-down minP. p-values", "Methylation difference", "Step-down minP (-log10)", treshold));
		try {
			JFreeChart chart = mainController.getStepDownVulcanoPlotChart();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JPanel panel = new ChartPanel(chart);
					JFrame frame = new JFrame("");
					frame.setSize(600, 400);
					frame.setLocationRelativeTo(null);
					frame.add(panel);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				}
			});
			setThumbnail(this.sdmVulcano, chart);
		}catch(NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}

	@FXML public void saveSdcHistogram(ActionEvent e ) {
		JFreeChart chart1 = mainController.getSteDownDistributionChart();
		BufferedImage img = chart1.createBufferedImage(1200, 800);
		exportChart(img);
	}

	@FXML public void saveSdcScatterPlot(ActionEvent e) {
		JFreeChart chart;
		if (useLog10sdm.isSelected()) {
			chart = mainController.getStepDownLogScatterPlotChart();
		}else {
			chart = mainController.getStepDownScatterPlotChart();
		}
		BufferedImage img = chart.createBufferedImage(800, 800);
		exportChart(img);
	}

	@FXML public void saveSdcVulcanoPlot(ActionEvent e) {
		double treshold = Double.parseDouble(vulcanoMinPField.getText());
		mainController.setStepDownVulcanoPlotChart(vulcanoPlot(mainController.getStepDownMinPvalues(), mainController.getMethylationDifference(),
				"Methylation difference Vs. Step-down minP. p-values", "Methylation difference", "Step-down minP (-log10)", treshold));
		JFreeChart chart = mainController.getStepDownVulcanoPlotChart();
		BufferedImage img = chart.createBufferedImage(1200, 800);
		exportChart(img);
	}

	//FDR pvalues ........................................................
	@FXML public void showFdrScatterPlot(ActionEvent actionEvent) {

		try {
			JFreeChart chart;
			if (useLog10fdr.isSelected()) {
				chart = mainController.getFdrLogScatterPlotChart();
			}else {
				chart = mainController.getFdrScatterPlotChart();
			}
			setFrameContent(chart);
		}catch (NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}

	@FXML public void showFdrHistogramPlot(ActionEvent actionEvent) {
		try {
			JFreeChart chart = mainController.getFdrDistributionChart();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JPanel panel = new ChartPanel(chart);
					JFrame frame = new JFrame("");
					frame.setSize(600, 400);
					frame.setLocationRelativeTo(null);
					frame.add(panel);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				}
			});
		}catch(NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}

	@FXML public void showFdrVulcanoPlot(ActionEvent actionEvent) {

		double treshold = Double.parseDouble(vulcanoFdrField.getText());
		mainController.setFdrVulcanoPlotChart(vulcanoPlot(mainController.getFdrPvalues(), mainController.getMethylationDifference(),
				"Methylation difference Vs. FDR p-values", "Methylation difference", "FDR p-values (-log10)", treshold));

		try {
			JFreeChart chart = mainController.getFdrVulcanoPlotChart();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JPanel panel = new ChartPanel(chart);
					JFrame frame = new JFrame("");
					frame.setSize(600, 400);
					frame.setLocationRelativeTo(null);
					frame.add(panel);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				}
			});
			setThumbnail(this.fdrVulcano, chart);
		}catch(NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}

	@FXML public void saveFdrHistogram(ActionEvent e ) {

		JFreeChart chart1 = mainController.getFdrDistributionChart();
		BufferedImage img = chart1.createBufferedImage(1200, 800);
		exportChart(img);
	}

	@FXML public void saveFdrScatterPlot(ActionEvent e) {
		JFreeChart chart;
		if (useLog10fdr.isSelected()) {
			chart = mainController.getFdrLogScatterPlotChart();
		}else {
			chart = mainController.getFdrScatterPlotChart();
		}
		BufferedImage img = chart.createBufferedImage(800, 800);
		exportChart(img);
	}

	@FXML public void saveFdrVulcanoPlot(ActionEvent e) {
		double treshold = Double.parseDouble(vulcanoFdrField.getText());
		mainController.setFdrVulcanoPlotChart(vulcanoPlot(mainController.getFdrPvalues(), mainController.getMethylationDifference(),
				"Methylation difference Vs. FDR p-values", "Methylation difference", "FDR p-values (-log10)", treshold));
		JFreeChart chart = mainController.getFdrVulcanoPlotChart();
		BufferedImage img = chart.createBufferedImage(1200, 800);
		exportChart(img);
	}	

	// Orig pvalues ........................................................
	@FXML public void showOrigHistogramPlot(ActionEvent actionEvent) {
		try {
			JFreeChart chart = mainController.getOrigDistributionChart();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JPanel panel = new ChartPanel(chart);
					JFrame frame = new JFrame("");
					frame.setSize(600, 400);
					frame.setLocationRelativeTo(null);
					frame.add(panel);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				}
			});
		}catch(NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}


	@FXML public void showOrigVulcanoPlot(ActionEvent actionEvent) {

		double treshold = Double.parseDouble(vulcanoOrigField.getText());

		mainController.setOrigVulcanoPlotChart(vulcanoPlot(mainController.getOriginalPvalues(), mainController.getMethylationDifference(),
				"Methylation difference Vs. Orig. p-values", "Methylation difference", "Orig. p-values (-log10)", treshold));

		try {
			JFreeChart chart = mainController.getOrigVulcanoPlotChart();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JPanel panel = new ChartPanel(chart);
					JFrame frame = new JFrame("");
					frame.setSize(600, 400);
					frame.setLocationRelativeTo(null);
					frame.add(panel);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				}
			});

			setThumbnail(this.origVulcano, chart);
		}catch(NumberFormatException e) {
			FXPopOutMsg.showWarning("You should use a numerical format");
		}
	}

	@FXML public void saveOrigHistogram(ActionEvent e ) {
		JFreeChart chart1 = mainController.getOrigDistributionChart();
		BufferedImage img = chart1.createBufferedImage(1200, 800);
		exportChart(img);
	}

	@FXML public void saveOrigVulcanoPlot(ActionEvent e) {
		double treshold = Double.parseDouble(vulcanoOrigField.getText());
		mainController.setOrigVulcanoPlotChart(vulcanoPlot(mainController.getOriginalPvalues(), mainController.getMethylationDifference(),
				"Methylation difference Vs. Orig. p-values", "Methylation difference", "Orig. p-values (-log10)", treshold));
		JFreeChart chart = mainController.getOrigVulcanoPlotChart();
		BufferedImage img = chart.createBufferedImage(1200, 800);
		exportChart(img);
	}

	// ........................... THUMBNAILS IMAGES

	private void updateFrame(JFreeChart chart) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				frame.setContentPane(null);
				frame.repaint();
				frame.revalidate();
				panel = new ChartPanel(chart);
				panel.updateUI();
				panel.revalidate();
				frame.setContentPane(panel);
				frame.add(panel);
				frame.revalidate();
				frame.repaint();
			}
		});	
	}

	@FXML public void empiricalThumbnail(ActionEvent actionEvent) {
		if (this.useLog10.isSelected()) {
			JFreeChart chart = mainController.getEmpiricalPvaluesScatterPlotLogChart();
			setThumbnail(this.empiricalScatter, chart);
			if(frameOpen) {
				updateFrame(chart);
			}
		}else {
			JFreeChart chart = mainController.getEmpiricalPvaluesScatterPlotChart();
			setThumbnail(this.empiricalScatter, chart);
			if(frameOpen) {
				updateFrame(chart);
			}
		}
	}

	@FXML public void fdrThumbnail(ActionEvent actionEvent) {
		if (this.useLog10fdr.isSelected()) {
			JFreeChart chart = mainController.getFdrLogScatterPlotChart();
			setThumbnail(this.fdrScatter, chart);
			if(frameOpen) {
				updateFrame(chart);
			}
		}else {
			JFreeChart chart = mainController.getFdrScatterPlotChart();
			setThumbnail(this.fdrScatter, chart);
			if(frameOpen) {
				updateFrame(chart);
			}
		}
	}

	@FXML public void fwerThumbnail(ActionEvent actionEvent) {
		if (this.useLog10glob.isSelected()) {
			JFreeChart chart = mainController.getFwerScatterPlotLogChart();
			setThumbnail(this.fwerScatter, chart);
			if(frameOpen) {
				updateFrame(chart);
			}
		}else {
			JFreeChart chart = mainController.getFwerScatterPlotChart();
			setThumbnail(this.fwerScatter, chart);
			if(frameOpen) {
				updateFrame(chart);
			}
		}
	}

	@FXML public void minpThumbnail(ActionEvent actionEvent) {
		if (this.useLog10sdm.isSelected()) {
			JFreeChart chart = mainController.getStepDownLogScatterPlotChart();
			setThumbnail(this.sdmScatter, chart);
			if(frameOpen) {
				updateFrame(chart);
			}
		}else {
			JFreeChart chart = mainController.getStepDownScatterPlotChart();
			setThumbnail(this.sdmScatter, chart);
			if(frameOpen) {
				updateFrame(chart);
			}
		}
	}

	private void setThumbnail(ImageView iv, JFreeChart c) {
		Image thumb = SwingFXUtils.toFXImage(c.createBufferedImage(600, 400), null);
		iv.setImage(thumb);
	}


	public void setCanvasController(MainController canvasController) {
		this.mainController = canvasController;
	}

	public void exportChart(BufferedImage bufferedImage) {
		try {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setInitialDirectory(new File((mainController.inputController.getOutputPath())));

			File outPutFile = fileChooser.showSaveDialog(null);

			if (outPutFile!=null) {
				ImageIO.write(bufferedImage, "png", outPutFile);
			}

		}catch(IOException e) {
			FXPopOutMsg.showWarning("Can't save file");
		}
	}

	public void exportChart(BufferedImage bufferedImage, String name) {
		try {
			File outPutFile = new File(name);
			outPutFile.getParentFile().mkdirs();
			if (outPutFile!=null) {
				ImageIO.write(bufferedImage, "png", outPutFile);
			}
		}catch(IOException e) {
			FXPopOutMsg.showWarning("Can't save file");
		}
	}

	public JFreeChart vulcanoPlot(float correctedPvalues[], float meanDiff[], String title, String xlab, String ylab, double treshold) {

		XYLogData xyLogData = new XYLogData(MatrixUtil.toDouble(meanDiff), 
				MatrixUtil.toDouble(correctedPvalues), treshold, 
				-Math.log10(1.0/(mainController.getNumPermutations() * 10.0)));

		VolcanoPlot vp = new VolcanoPlot(title, xyLogData, xlab, ylab);
		JFreeChart chart = vp.getChart();
		return chart;
	}
}

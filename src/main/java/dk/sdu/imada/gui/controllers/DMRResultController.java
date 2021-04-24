package dk.sdu.imada.gui.controllers;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import dk.sdu.imada.gui.controllers.util.Browser;
import dk.sdu.imada.gui.controllers.util.SaveAll;
import dk.sdu.imada.gui.monitors.SaveAllMonitor;
import dk.sdu.imada.jlumina.search.primitives.DMRDescription;
import dk.sdu.imada.jlumina.search.primitives.DMRPermutationSummary;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class DMRResultController {

	int cpgIDs[];

	@FXML ComboBox<String> cpgList;
	@FXML ComboBox<DMRDescription> Links;


	@FXML public ImageView scoreDistribution;
	//@FXML public ImageView methylationDifference;
	@FXML public ImageView dmrPvalues;
	@FXML public ImageView table;

	@FXML ImageView view1;
	@FXML ImageView view2;
	@FXML ImageView view3;
	@FXML ImageView view4;
	@FXML ImageView view5;
	@FXML ImageView view6;

	MainController mainController;

	TableView<FXDMRPermutationSummary> tablePermutationSummary;
	TableView<FXDMRFullSummary> tableViewFullSummary;
	TableView<FXDMRSummary> tableViewDMRSumary;	


	final WebView browser = new WebView();
	final WebEngine webEngine = browser.getEngine();
	
	public TableView<FXDMRFullSummary> getTableViewFullSummary() {
		return tableViewFullSummary;
	}

	@FXML public void setThumbail() {

		try {
			int selectedKey = Integer.parseInt(cpgList.getSelectionModel().getSelectedItem());
			JFreeChart c = mainController.getPvaluesChart().get(selectedKey);
			Image thumb = SwingFXUtils.toFXImage(c.createBufferedImage(600, 400), null);
			view1.setImage(thumb);
		}catch(NumberFormatException e) {
			//FXPopOutMsg.showWarning("Select the number of CpGs");
		}
	}

	@FXML public void openLink(ActionEvent e) {

		System.setProperty("jsse.enableSNIExtension", "false");

		DMRDescription dmr = Links.getSelectionModel().getSelectedItem();
		String []captions = new String[mainController.getDmrDescriptions().size()];

		for (int i = 0; i < captions.length; i++) {
			captions[i] = mainController.getDmrDescriptions().get(i).getLink();
		}

		//System.out.println(dmr.getLink());

		Stage stage = new Stage();
		stage.setTitle("Web View");

		Browser b = new Browser(dmr.getLink());
		
		Scene scene = new Scene(b, 900, 600, Color.web("#666970"));
		stage.setScene(scene);
		//scene.getStylesheets().add("webviewsample/BrowserToolbar.css");
		stage.show();
	}

	public ComboBox<String> getCpgList() {
		return cpgList;
	}

	public ComboBox<DMRDescription> getLinks() {
		return Links;
	}

	public void setLinks(ComboBox<DMRDescription> links) {
		Links = links;
	}

	@FXML public void pushBack(ActionEvent actionEvent) {
		mainController.loadScreen("executeDMR");
	}

	public ImageView getView1() {
		return view1;
	}

	public ImageView getView2() {
		return view2;
	}
	
	public ImageView getView3(){
		return this.view3;
	}

	public ImageView getView4() {
		return view4;
	}

	public ImageView getView5() {
		return view5;
	}

	public ImageView getView6() {
		return view6;
	}

	@SuppressWarnings("unchecked")
	public void setPermutationSummaryPreview() {

		tablePermutationSummary = new TableView<FXDMRPermutationSummary>();
		ArrayList<FXDMRPermutationSummary> arrayList = new ArrayList<FXDMRPermutationSummary>();

		TreeMap<Integer, DMRPermutationSummary> map = mainController.getDMRPermutationMap();

		for (int i : map.keySet()) {
			arrayList.add(new FXDMRPermutationSummary(map.get(i)));
		}

		ObservableList<FXDMRPermutationSummary> data = FXCollections.observableList(arrayList);

		tablePermutationSummary.setItems(data);
		tablePermutationSummary.setPrefSize(600, 200);

		Label col1 = new Label("#CpG");
		col1.setTooltip(new Tooltip("Number of CpGs in the DMR"));
		Label col2 = new Label("#DMRs");
		col2.setTooltip(new Tooltip("Number of DMRs of at least that size found in the original (non-permuted) data"));
		Label col3 = new Label("Avg.DMRs");
		col3.setTooltip(new Tooltip("Average number of DMRs across all permutations of at least that size"));
		Label col4 = new Label("FDR");
		col4.setTooltip(new Tooltip("Fraction of DMRs of a specific size that is expected to be noise "));
		Label col5 = new Label("log-ratio");
		col5.setTooltip(new Tooltip("-log10(#DMRs/Avg.DMRs)"));

		TableColumn<FXDMRPermutationSummary, String> c1 = new TableColumn<FXDMRPermutationSummary, String>("");
		c1.setCellValueFactory(new PropertyValueFactory<>("cpgID"));
		c1.setPrefWidth(90.0);
		c1.setGraphic(col1);

		TableColumn<FXDMRPermutationSummary, String> c2 = new TableColumn<FXDMRPermutationSummary, String>("");
		c2.setCellValueFactory(new PropertyValueFactory<>("numberOfIslands"));
		c2.setPrefWidth(90.0);
		c2.setGraphic(col2);


		TableColumn<FXDMRPermutationSummary, String> c3 = new TableColumn<FXDMRPermutationSummary, String>("");
		c3.setCellValueFactory(new PropertyValueFactory<>("averageOfIslands"));
		c3.setPrefWidth(90.0);
		c3.setGraphic(col3);


		TableColumn<FXDMRPermutationSummary, String> c4 = new TableColumn<FXDMRPermutationSummary, String>("");
		c4.setCellValueFactory(new PropertyValueFactory<>("FDR"));
		c4.setPrefWidth(90.0);
		c4.setGraphic(col4);


		TableColumn<FXDMRPermutationSummary, String> c5 = new TableColumn<FXDMRPermutationSummary, String>("");
		c5.setCellValueFactory(new PropertyValueFactory<>("logRatio"));
		c5.setPrefWidth(200.0);
		c5.setGraphic(col5);

		tablePermutationSummary.getColumns().addAll(c1, c2, c3, c4, c5);

		StackPane pane = new StackPane();
		pane.getChildren().add(tablePermutationSummary);
		pane.setPrefSize(300, 200);
		Stage stage = new Stage();
		stage.setScene(new Scene(pane));

		WritableImage image = new WritableImage(300, 200);
		WritableImage snap = pane.snapshot(new SnapshotParameters(), image);
		view4.setImage(snap);
	}

	@FXML public void pushPermutationSummary(ActionEvent actionEvent) {
		StackPane pane = new StackPane();
		pane.getChildren().add(tablePermutationSummary);
		Stage stage = new Stage();
		stage.setScene(new Scene(pane));
		stage.show();
	}

	@SuppressWarnings("unchecked")
	public void setFullPermutationSummaryPreview() {

		tableViewFullSummary = new TableView<FXDMRFullSummary>();
		tableViewFullSummary.setEditable(true);

		ArrayList<FXDMRFullSummary> arrayList = new ArrayList<FXDMRFullSummary>();

		TreeMap<Integer, DMRPermutationSummary> map = mainController.getDMRPermutationMap();
		ArrayList<DMRDescription> dmrs = mainController.getDmrDescriptions();

		for (DMRDescription d : dmrs ) {
			int id = d.getIsland().totalCpgs;

			for (int i : map.keySet()) {
				if (id  == map.get(i).getCpgID()) {
					arrayList.add(new FXDMRFullSummary(map.get(i), d));
				}
			}
		}

		ObservableList<FXDMRFullSummary> data = FXCollections.observableList(arrayList);

		tableViewFullSummary.setItems(data);
		tableViewFullSummary.setPrefSize(600, 200);	

		Label colLink = new Label("hyperlink");

		Label col1 = new Label("#CpG");
		col1.setTooltip(new Tooltip("Number of CpGs in the DMR"));
		Label col2 = new Label("#DMRs");
		col2.setTooltip(new Tooltip("Number of DMRs of at least that size found in the original (non-permuted) data"));
		Label col3 = new Label("Avg.DMRs");
		col3.setTooltip(new Tooltip("Average number of DMRs across all permutations of at least that size"));
		Label col4 = new Label("p-value");
		col4.setTooltip(new Tooltip("Probability to find at least the same number of DMRs of at least that size by chance across the given number of permutations"));
		Label col5 = new Label("log-ratio");
		col5.setTooltip(new Tooltip("-log10(#DMRs/Avg.DMRs)"));

		Label col6 = new Label("Chr");
		col6.setTooltip(new Tooltip("Chromosome"));
		Label col7 = new Label("Begin");
		col7.setTooltip(new Tooltip("Start genomic positionthe of this CpG at the genome"));
		Label col8 = new Label("End");
		col8.setTooltip(new Tooltip("End genomic positionthe of this CpG at the genome"));
		Label col9 = new Label("CpG.begin");
		col9.setTooltip(new Tooltip("Start CpG ID"));
		Label col10 = new Label("CpG.end");
		col10.setTooltip(new Tooltip("Final CpG ID"));
		Label col11 = new Label("Score");
		col11.setTooltip(new Tooltip("#CpGs/lenght(bp)"));
		Label col12 = new Label("Length (bp)");
		col12.setTooltip(new Tooltip("Length in base pairs of the DMR"));


		TableColumn<FXDMRFullSummary, Button> cl = new TableColumn<FXDMRFullSummary, Button>("");
		cl.setEditable(true);
		cl.setCellValueFactory(new PropertyValueFactory<>("hyperlink"));
		cl.setPrefWidth(90.0);
		cl.setGraphic(colLink);

		TableColumn<FXDMRFullSummary, String> c1 = new TableColumn<FXDMRFullSummary, String>("");
		c1.setCellValueFactory(new PropertyValueFactory<>("cpgID"));
		c1.setPrefWidth(90.0);
		c1.setGraphic(col1);

		TableColumn<FXDMRFullSummary, String> c2 = new TableColumn<FXDMRFullSummary, String>("");
		c2.setCellValueFactory(new PropertyValueFactory<>("numberOfIslands"));
		c2.setPrefWidth(90.0);
		c2.setGraphic(col2);

		TableColumn<FXDMRFullSummary, String> c3 = new TableColumn<FXDMRFullSummary, String>("");
		c3.setCellValueFactory(new PropertyValueFactory<>("averageOfIslands"));
		c3.setPrefWidth(90.0);
		c3.setGraphic(col3);

		TableColumn<FXDMRFullSummary, String> c4 = new TableColumn<FXDMRFullSummary, String>("");
		c4.setCellValueFactory(new PropertyValueFactory<>("pvalue"));
		c4.setPrefWidth(90.0);
		c4.setGraphic(col4);

		TableColumn<FXDMRFullSummary, String> c5 = new TableColumn<FXDMRFullSummary, String>("");
		c5.setCellValueFactory(new PropertyValueFactory<>("logRatio"));
		c5.setPrefWidth(200.0);
		c5.setGraphic(col5);

		TableColumn<FXDMRFullSummary, String> c6 = new TableColumn<FXDMRFullSummary, String>("");
		c6.setCellValueFactory(new PropertyValueFactory<>("chromosome"));
		c6.setPrefWidth(100.0);
		c6.setGraphic(col6);

		TableColumn<FXDMRFullSummary, String> c7 = new TableColumn<FXDMRFullSummary, String>("");
		c7.setCellValueFactory(new PropertyValueFactory<>("beginPosition"));
		c7.setPrefWidth(100.0);
		c7.setGraphic(col7);

		TableColumn<FXDMRFullSummary, String> c8 = new TableColumn<FXDMRFullSummary, String>("");
		c8.setCellValueFactory(new PropertyValueFactory<>("endPosition"));
		c8.setPrefWidth(100.0);
		c8.setGraphic(col8);

		TableColumn<FXDMRFullSummary, String> c9 = new TableColumn<FXDMRFullSummary, String>("");
		c9.setCellValueFactory(new PropertyValueFactory<>("beginCPG"));
		c9.setPrefWidth(100.0);
		c9.setGraphic(col9);

		TableColumn<FXDMRFullSummary, String> c10 = new TableColumn<FXDMRFullSummary, String>("");
		c10.setCellValueFactory(new PropertyValueFactory<>("endCPG"));
		c10.setPrefWidth(100.0);
		c10.setGraphic(col10);

		TableColumn<FXDMRFullSummary, String> c11 = new TableColumn<FXDMRFullSummary, String>("");
		c11.setCellValueFactory(new PropertyValueFactory<>("score"));
		c11.setPrefWidth(200.0);
		c11.setGraphic(col11);

		TableColumn<FXDMRFullSummary, String> c12 = new TableColumn<FXDMRFullSummary, String>("");
		c12.setCellValueFactory(new PropertyValueFactory<>("size"));
		c12.setPrefWidth(100.0);
		c12.setGraphic(col12);

		tableViewFullSummary.getColumns().addAll(cl, c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12);

		StackPane pane = new StackPane();
		pane.getChildren().add(tableViewFullSummary);
		Stage stage = new Stage();
		stage.setScene(new Scene(pane));

		WritableImage image = new WritableImage(300, 200);
		WritableImage snap = pane.snapshot(new SnapshotParameters(), image);
		view6.setImage(snap);
	}

	@FXML public void pushPermutationFullSummary(ActionEvent actionEvent) {

		StackPane pane = new StackPane();
		pane.getChildren().add(tableViewFullSummary);
		Stage stage = new Stage();
		stage.setScene(new Scene(pane));
		stage.show();
	}

	@SuppressWarnings("unchecked")
	public void setDMRSummaryPreview() {

		tableViewDMRSumary = new TableView<FXDMRSummary>();
		ArrayList<FXDMRSummary> arrayList = new ArrayList<FXDMRSummary>();

		for (DMRDescription s : mainController.getDmrDescriptions()) {
			arrayList.add(new FXDMRSummary(s));
		}

		ObservableList<FXDMRSummary> data = FXCollections.observableList(arrayList);

		tableViewDMRSumary.setItems(data);
		tableViewDMRSumary.setPrefSize(600, 200);

		Label col1 = new Label("Chr");
		col1.setTooltip(new Tooltip("Chromosome"));
		Label col2 = new Label("Begin");
		col2.setTooltip(new Tooltip("Start position of the CpG in the genome"));
		Label col3 = new Label("End");
		col3.setTooltip(new Tooltip("Final position of the CpG in the genome"));
		Label col4 = new Label("CpG.begin");
		col4.setTooltip(new Tooltip("Start CpG ID"));
		Label col5 = new Label("CpG.end");
		col5.setTooltip(new Tooltip("Final CpG ID"));
		Label col6 = new Label("Score");
		col6.setTooltip(new Tooltip(" #CpGs/DMR lenght(bp)"));
		Label col7 = new Label("Length (bp)");
		col7.setTooltip(new Tooltip("Length in base pairs of the DMR"));
		Label col8 = new Label("p-value");
		col8.setTooltip(new Tooltip("Chance that a random region with the same size has an equally good or better CpG-score."));


		TableColumn<FXDMRSummary, String> c1 = new TableColumn<FXDMRSummary, String>("");
		c1.setCellValueFactory(new PropertyValueFactory<>("chromosome"));
		c1.setPrefWidth(100.0);
		c1.setGraphic(col1);

		TableColumn<FXDMRSummary, String> c2 = new TableColumn<FXDMRSummary, String>("");
		c2.setCellValueFactory(new PropertyValueFactory<>("beginPosition"));
		c2.setPrefWidth(100.0);
		c2.setGraphic(col2);

		TableColumn<FXDMRSummary, String> c3 = new TableColumn<FXDMRSummary, String>("");
		c3.setCellValueFactory(new PropertyValueFactory<>("endPosition"));
		c3.setPrefWidth(100.0);
		c3.setGraphic(col3);

		TableColumn<FXDMRSummary, String> c4 = new TableColumn<FXDMRSummary, String>("");
		c4.setCellValueFactory(new PropertyValueFactory<>("beginCPG"));
		c4.setPrefWidth(100.0);
		c4.setGraphic(col4);

		TableColumn<FXDMRSummary, String> c5 = new TableColumn<FXDMRSummary, String>("");
		c5.setCellValueFactory(new PropertyValueFactory<>("endCPG"));
		c5.setPrefWidth(100.0);
		c5.setGraphic(col5);

		TableColumn<FXDMRSummary, String> c6 = new TableColumn<FXDMRSummary, String>("");
		c6.setCellValueFactory(new PropertyValueFactory<>("score"));
		c6.setPrefWidth(100.0);
		c6.setGraphic(col6);

		TableColumn<FXDMRSummary, String> c7= new TableColumn<FXDMRSummary, String>("");
		c7.setCellValueFactory(new PropertyValueFactory<>("size"));
		c7.setPrefWidth(100.0);
		c7.setGraphic(col7);
		
		TableColumn<FXDMRSummary, String> c8 = new TableColumn<FXDMRSummary, String>("");
		c8.setCellValueFactory(new PropertyValueFactory<>("pvalue"));
		c8.setPrefWidth(100.0);
		c8.setGraphic(col8);

		tableViewDMRSumary.getColumns().addAll(c1, c2, c3, c4, c5, c7, c6, c8);

		StackPane pane = new StackPane();
		pane.getChildren().add(tableViewDMRSumary);
		pane.setPrefSize(600, 200);
		Stage stage = new Stage();
		stage.setScene(new Scene(pane, 600, 200));

		WritableImage image = new WritableImage(300, 200);
		WritableImage snap = pane.snapshot(new SnapshotParameters(), image);
		view5.setImage(snap);
	}

	@FXML public void pushDMRSummary(ActionEvent actionEvent) {
		StackPane pane = new StackPane();
		pane.getChildren().add(tableViewDMRSumary);
		pane.setPrefSize(600, 200);
		Stage stage = new Stage();
		stage.setScene(new Scene(pane, 600, 200));
		stage.show();
	}


	@FXML public void plotScoreDistribution(ActionEvent actionEvent) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JPanel panel = new ChartPanel(mainController.dmrScoresDistributionChart);
				JFrame frame = new JFrame("");
				frame.setSize(600, 400);
				frame.setLocationRelativeTo(null);
				frame.add(panel);
				frame.setVisible(true);
				frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);	
			}
		});
	}
	
	@FXML public void plotPValueDistribution(ActionEvent actionEvent) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JPanel panel = new ChartPanel(mainController.getDmrPValueDistributionChart());
				JFrame frame = new JFrame("");
				frame.setSize(600, 400);
				frame.setLocationRelativeTo(null);
				frame.add(panel);
				frame.setVisible(true);
				frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);	
			}
		});
	}


	@FXML public void plotNumberIslandsByPermutation(ActionEvent actionEvent) {

		try {
			int selectedKey = Integer.parseInt(cpgList.getSelectionModel().getSelectedItem());
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JPanel panel = new ChartPanel(mainController.getPvaluesChart().get(selectedKey));
					JFrame frame = new JFrame("");
					frame.setSize(600, 400);
					frame.setLocationRelativeTo(null);
					frame.add(panel);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);				
				}
			});

		}catch(NumberFormatException e) {
			FXPopOutMsg.showWarning("Select the number of CpGs");
		}catch (Exception e) {
			FXPopOutMsg.showWarning("Something went wrong, sorry!");
		}
	}

	@FXML public void plotMethylationDifference(ActionEvent e) {
	}

	@FXML public void help(ActionEvent actionEvent) {
		String msg = "testing ";
		FXPopOutMsg.showHelp(msg);
	}

	@FXML public void restart(ActionEvent actionEvent) {
		mainController.reset();
		mainController.restart();
	}

	public void setCanvasController(MainController canvasController) {
		this.mainController = canvasController;
	}

	public void setSummary(String outputMsg) {
		//summary.setText(outputMsg);
	}

	public void setCpgIDS(int[] cpgIDs) {
		this.cpgIDs = cpgIDs;
		ArrayList<String>items = new ArrayList<String>(cpgIDs.length);
		for (int i : cpgIDs) {
			items.add(i+"");
		}
		ObservableList<String> observableList = FXCollections.observableArrayList(items); 
		cpgList.setItems(observableList);
	}

	@FXML public void pushSaveScoreDistribution(ActionEvent e) {

		JFreeChart chart = mainController.getDmrScoresDistributionChart();
		BufferedImage img = chart.createBufferedImage(1200, 600);
		exportChart(img);
	}
	
	@FXML public void pushSavePValueDistribution(ActionEvent e) {

		JFreeChart chart = mainController.getDmrPValueDistributionChart();
		BufferedImage img = chart.createBufferedImage(1200, 600);
		exportChart(img);
	}

	@FXML public void pushSavePvalue(ActionEvent e) {
		try {
			int selectedKey = Integer.parseInt(cpgList.getSelectionModel().getSelectedItem());
			JFreeChart chart = mainController.getPvaluesChart().get(selectedKey);
			BufferedImage img = chart.createBufferedImage(1200, 800);
			exportChart(img);
		}catch(NumberFormatException e1) {
			FXPopOutMsg.showWarning("Select the number of CpGs");
		}
	}

	@FXML public void pushSaveVulcanoPlot(ActionEvent e) {
	}

	@FXML public void pushSaveTableDMR(ActionEvent e) {

		try {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setInitialDirectory(new File((mainController.inputController.getOutputPath())));
			File file = fileChooser.showSaveDialog(null);
			if (file != null) {

				if (!file.exists()) {
					file.createNewFile();
				}

				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write("Chr, Begin, End, begin.CpG, end.CpG, score, p-value\n");
				for (DMRDescription d : mainController.getDmrDescriptions()) {
					bw.write(d.getChromosome() + ", ");
					bw.write(d.getBeginPosition() + ", ");
					bw.write(d.getEndPosition() + ", ");
					bw.write(d.getBeginCPG()+ ", ");
					bw.write(d.getEndCPG() + ", ");
					bw.write(d.getIsland().score + ", ");
					bw.write(d.getIsland().getPValue() + "\n");
				}

				bw.close();
			}

		} catch (IOException e1) {
			FXPopOutMsg.showWarning("File not saved");
		}
	}

	@FXML public void pushSaveTablePermutation(ActionEvent e) {
		try {

			FileChooser fileChooser = new FileChooser();
			fileChooser.setInitialDirectory(new File((mainController.inputController.getOutputPath())));
			File file = fileChooser.showSaveDialog(null);

			if (file != null) {
				if (!file.exists()) {
					file.createNewFile();
				}

				if (!file.exists()) {
					file.createNewFile();
				}

				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write("#CpG, Num.Islands, Average.Islands, FDR, log.ratio\n");
				for (int key : mainController.getDMRPermutationMap().keySet()) {
					DMRPermutationSummary summary = mainController.getDMRPermutationMap().get(key);
					bw.write(key + "," + summary.getNumberOfIslands() + "," + summary.getAverageOfIslands() + "," + summary.getFDR() + "," + summary.getLogRatio() + "\n");
				}
				bw.close();
			}
		} catch (IOException e1) {
			FXPopOutMsg.showWarning("File not saved");
		}
	}

	@FXML public void pushSaveWholeTable(ActionEvent e) {

		try {

			FileChooser fileChooser = new FileChooser();
			fileChooser.setInitialDirectory(new File((mainController.inputController.getOutputPath())));
			File file = fileChooser.showSaveDialog(null);

			if (file != null) {
				if (!file.exists()) {
					file.createNewFile();
				}

				if (!file.exists()) {
					file.createNewFile();
				}

				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);

				bw.write("Chr, Begin, End, begin.CpG, end.CpG, score #CpG, Num.Islands, Average.Islands, p-value, log.ratio\n");

				ObservableList<FXDMRFullSummary> list = tableViewFullSummary.getItems();

				for (FXDMRFullSummary l : list) {
					bw.write( l.getHyperlink() + ","
							+ l.getChromosome() + ","
							+ l.getBeginPosition() + ","
							+ l.getEndPosition() + ","
							+ l.getBeginCPG() + ","
							+ l.getEndCPG() + ","
							+ l.getScore() + ","
							+ l.getCpgID() + ","
							+ l.getNumberOfIslands() + ","
							+ l.getAverageOfIslands() + ","
							+ l.getPvalue() + ","
							+l.getLogRatio() + "\n");
				}

				bw.close();
			}
		}catch(IOException e1) {
			FXPopOutMsg.showWarning("File not saved");
		}
	}

	public void exportChart(BufferedImage bufferedImage) {
		try {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setInitialDirectory(new File((mainController.inputController.getOutputPath())));

			File outPutFile = fileChooser.showSaveDialog(null);
			outPutFile.getParentFile().mkdirs();
			if (outPutFile!=null) {
				ImageIO.write(bufferedImage, "png", outPutFile);
			}

		}catch(IOException e) {
			FXPopOutMsg.showWarning("File not saved");
		}
	}

	public void exportChart2(BufferedImage bufferedImage, String name) {
		try {
			File outPutFile = new File(name + ".png");
			outPutFile.getParentFile().mkdirs();
			if (outPutFile!=null) {
				ImageIO.write(bufferedImage, "png", outPutFile);
			}
		}catch(IOException e) {
			FXPopOutMsg.showWarning("Can't save file");
		}
	}

	@FXML public void saveAll(ActionEvent event) {
		Date d = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("yyMMddhhmmssMs");
		String datetime = ft.format(d)+"_DMRSearch/";
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
}

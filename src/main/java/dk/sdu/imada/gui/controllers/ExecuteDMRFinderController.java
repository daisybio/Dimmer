package dk.sdu.imada.gui.controllers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import dk.sdu.imada.console.Util;
import dk.sdu.imada.gui.monitors.DMRPermutationMonitor;
import dk.sdu.imada.jlumina.core.io.ReadManifest;
import dk.sdu.imada.jlumina.search.algorithms.DMRAlgorithm;
import dk.sdu.imada.jlumina.search.algorithms.DMRPermutation;
import dk.sdu.imada.jlumina.search.primitives.DMRDescription;
import dk.sdu.imada.jlumina.search.primitives.DMR;
import dk.sdu.imada.jlumina.search.util.DMRPermutationExecutor;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ExecuteDMRFinderController {

	@FXML Label summary;
	@FXML Button start;

	ArrayList<DMR> dmrs;

	MainController mainController;

	@FXML public void pushBack(ActionEvent actionEvent) {
		mainController.loadScreen("dmrParameters");
	}

	private int[] getBinaryArray(float[] p0, float treshold) {
		int []binaryArray = new int[p0.length];
		int index = 0;
		for (float v : p0) {
			if (v <= treshold) {
				binaryArray[index] = 1;
			}else {
				binaryArray[index] = 0; 
			}
			index++;
		}
		return binaryArray;
	}

	@FXML public void startDMRFinder() {
		
		ReadManifest manifest = mainController.getManifest();

		int numThreads = mainController.getNumThreads();
		int k = mainController.dmrParametersController.getNumException();
		int w = mainController.dmrParametersController.getWindowSize() - 1;
		int l = mainController.dmrParametersController.getCpgDistance();
		int np = mainController.dmrParametersController.getNumPermutations();
		
		if(np < numThreads){
			numThreads = np;
		}
		
		
		int[] binaryArray = getBinaryArray(mainController.getSearchPvalues(), mainController.getP0Cutoff());
		int MAX = binaryArray.length;

		int [] positions  = new int[MAX];
		String [] chrs = new String[MAX];
		String [] cpg = new String[MAX];
		
		
		for (int i = 0; i < manifest.getCpgList().length; i++) {
			cpg[i] = manifest.getCpgList()[i].getCpgName();
			chrs[i] = manifest.getCpgList()[i].getChromosome();
			positions[i] = manifest.getCpgList()[i].getMapInfo();
		}
		
		//Test Code Johannes
//		System.out.println("Writing test data...");
//		try{
//			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("test.txt")));
//			bw.write("name\tpositions\tchrs\tbinary\t");
//			bw.newLine();
//			for(int i = 0; i < MAX; i++){
//				bw.write(cpg[i]+"\t"+positions[i]+"\t"+chrs[i]+"\t"+binaryArray[i]);
//				bw.newLine();
//			}
//			bw.close();
//		}
//		catch(IOException e){
//			e.printStackTrace();
//		}
		
		
		DMRAlgorithm dmrAlgorithm = new DMRAlgorithm(k, w, l, 1, positions, chrs);
		dmrs = dmrAlgorithm.islandSearch(binaryArray);
		this.mainController.setDMRs(dmrs);
		
		ArrayList<DMRDescription> dmrDescriptions = new ArrayList<>();

		for (DMR island : dmrs) {
			DMRDescription d = new DMRDescription(island, cpg, chrs, positions);
			d.setLink();
			dmrDescriptions.add(d);
		}
		
		this.mainController.setDmrDescriptions(dmrDescriptions);
		
		ProgressForm progressForm = new ProgressForm();
		Platform.runLater(progressForm);
		ArrayList<Thread> threads = new ArrayList<>();

		DMRPermutationExecutor [] executors = new DMRPermutationExecutor[numThreads];
		DMRPermutation dmrPermutation[] = new DMRPermutation[numThreads];
		Thread eThread [] = new Thread[numThreads];
		int[] dmrPermuDist = Util.distributePermutations(numThreads, np);

		for (int i = 0; i < numThreads; i++) {
			dmrPermutation[i] = new DMRPermutation(new DMRAlgorithm(k, w, l, 1, positions, chrs), dmrs, binaryArray, dmrPermuDist[i]);
			executors[i] = new DMRPermutationExecutor(dmrPermutation[i]);
			eThread[i] = new Thread(executors[i], "permutation_" + i);
			threads.add(eThread[i]);
		}

		DMRPermutationMonitor dmrPermutationMonitor = new DMRPermutationMonitor(dmrPermutation, dmrs, progressForm, mainController, np);
		Thread monitorThread = new Thread(dmrPermutationMonitor, "dmr_monitor");

		threads.add(monitorThread);
		progressForm.setThreads(threads);
		
		for (Thread e : eThread) {
			e.start();
		}

		monitorThread.start();
	}

	public void setCanvasController(MainController canvasController) {
		this.mainController = canvasController;
	}

	public void setSummaryText(String value) {
		this.summary.setText(value);
	}

}

package dk.sdu.imada.gui.monitors;

import dk.sdu.imada.gui.controllers.FXPopOutMsg;
import dk.sdu.imada.gui.controllers.MainController;
import dk.sdu.imada.gui.controllers.ProgressForm;
import dk.sdu.imada.jlumina.core.util.MatrixUtil;
import dk.sdu.imada.jlumina.core.util.RawDataLoader;
import javafx.application.Platform;

public class InputFilesMonitor implements Runnable {

	boolean done = false;
	String msg;
	ProgressForm progressForm;
	MainController mainController;
	RawDataLoader rawDataLoader;


	private void setProgress() {
		this.progressForm.getProgressBar().setProgress(rawDataLoader.getProgress());
		this.progressForm.getText().setText(rawDataLoader.getMsg());
	}

	public InputFilesMonitor(RawDataLoader rawDataLoader, MainController mainController, ProgressForm progressForm) {
		this.rawDataLoader = rawDataLoader;
		this.mainController = mainController;
		this.progressForm = progressForm;
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis();

		synchronized (rawDataLoader) {
			while(!rawDataLoader.isDone()) {
				setProgress();
				try {
					rawDataLoader.wait();

					if(rawDataLoader.isOveflow()) {
						Platform.runLater(() -> progressForm.getDialogStage().close());
						Platform.runLater(() -> FXPopOutMsg.showWarning("A memory exception was detected. Use the java command line with -Xms2024M -Xmx3024M. "	+ 
								"If the problem persists try to increase the cited values... good luck"));
						this.progressForm.cancelThreads();
					}

				}catch(InterruptedException e) {
				}
			}
		}

		this.progressForm.getText().setText("Almost done....");
		System.out.println("Almost done...");
		float[][] beta = null;

		try {
			beta = MatrixUtil.getBetaAsMatrix(rawDataLoader.getuSet().getData(), rawDataLoader.getmSet().getData(), rawDataLoader.getManifest(), 0.f);
			mainController.setBeta(beta);
			float cellComposition[][] = null;
			if (rawDataLoader.getCellCompositionCorrection()!=null) {
				cellComposition = rawDataLoader.getCellCompositionCorrection().getCellCompositoin();
			}
			mainController.setCellComposition(cellComposition);
			
			rawDataLoader.setuSet(null); rawDataLoader.setmSet(null); System.gc();
			
			rawDataLoader.setNormalization(null); System.gc();
			
			mainController.setManifest(rawDataLoader.getManifest());
			
			Platform.runLater(() -> progressForm.getDialogStage().close());
			Platform.runLater(() -> mainController.loadScreen("permutationParameters"));
			
			long endTime   = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			System.out.println("Processing raw data in " + (((double)totalTime/1000.0)/60.0) + " minutes");
			done = true;
		}catch(OutOfMemoryError e) {
			Platform.runLater(() -> progressForm.getDialogStage().close());
			Platform.runLater(() -> FXPopOutMsg.showWarning("A memory exception was detected. Use the java command line with -Xms2024M -Xmx3024M. "	+ 
					"If the problem persists try to increase the previous values... good luck"));
			this.progressForm.cancelThreads();
		}
	}

	/*@Override
	public void run() {
		total = 6;
		if (cellCompositionCorrection!= null) total = 11;
		progressForm.getText().setText("Loading IDAT files... \n");
		progress = 0;
		checkRGSetProgress();
		progressBar.setProgress((double)progress/total);
		progressForm.getText().setText("Loading manifest file...");
		checkManifestProgress();
		progressBar.setProgress((double)progress/total);

		progressForm.getText().setText("Processing probes....");
		checkMethylationProgress(mSet);
		checkMethylationProgress(uSet);
		progressBar.setProgress((double)progress/total);

		if (cellCompositionCorrection!= null) {
			progressForm.getText().setText("Estimating cell composition... \n");
			checkMethylationProgress(refMSet);
			checkMethylationProgress(refUSet);
			progressBar.setProgress((double)progress/total);

			progressForm.getText().setText("Normalizing reference data... \n");
			checkNormalizationProgress(normalization.get(0));
			checkNormalizationProgress(normalization.get(1));
			//progressForm.getText().setText("Normalizing reference data... done \n");
			progressBar.setProgress((double)progress/total);

			System.gc();
			progressForm.getText().setText("Calculating cell composition \n");
			checkCellCompositionProgress();
			progressBar.setProgress((double)progress/total);
		}

		progressForm.getText().setText("Normalizing user data... \n");
		checkNormalizationProgress(normalization.get(2));
		checkNormalizationProgress(normalization.get(3));
		progressBar.setProgress((double)progress/total);

		progressForm.getText().setText("Finishing data pre-processing... \n");
		float[][] beta = MatrixUtil.getBetaAsMatrix(uSet.getData(), mSet.getData(), manifest, 0.f);
		mainController.setBeta(beta);
		float cellComposition[][] = null;
		if (cellCompositionCorrection!=null) {
			cellComposition = cellCompositionCorrection.getCellCompositoin();
		}
		mainController.setCellComposition(cellComposition);

		uSet = null; mSet = null; System.gc();

		normalization = null; System.gc();

		mainController.setManifest(manifest);
		Platform.runLater(() -> progressForm.getDialogStage().close());
		Platform.runLater(() -> mainController.loadScreen("permutationParameters"));
		//Platform.runLater(() -> mainController.loadScreen("summary"));
		done = true;
	}*/

}

package dk.sdu.imada.gui.monitors;

import dk.sdu.imada.console.Variables;
import dk.sdu.imada.gui.controllers.FXPopOutMsg;
import dk.sdu.imada.gui.controllers.MainController;
import dk.sdu.imada.gui.controllers.ProgressForm;
import dk.sdu.imada.jlumina.core.io.ReadBetaMatrix;
import dk.sdu.imada.jlumina.core.util.MatrixUtil;
import dk.sdu.imada.jlumina.core.util.RawDataLoader;
import javafx.application.Platform;

public class BetaMonitor implements Runnable{
	boolean done = false;
	String msg;
	ProgressForm progressForm;
	MainController mainController;
	ReadBetaMatrix betaReader;


	private void setProgress() {
		this.progressForm.getProgressBar().setProgress(betaReader.getProgress());
		this.progressForm.getText().setText(betaReader.getMsg());
	}

	public BetaMonitor(ReadBetaMatrix betaReader, MainController mainController, ProgressForm progressForm) {
		this.betaReader = betaReader;
		this.mainController = mainController;
		this.progressForm = progressForm;
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis();

		synchronized (betaReader) {
			while(!betaReader.isDone()) {
				setProgress();
				try {
					betaReader.wait();

					if(betaReader.isOveflow()) {
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

		try {
			if(!betaReader.check()){
				Platform.runLater(() -> FXPopOutMsg.showWarning(betaReader.errorLog()));
				Platform.runLater(() -> progressForm.getDialogStage().close());
			}
			else{
				mainController.setBeta(betaReader.getBeta());
				mainController.setManifest(betaReader.getManifest());
				Platform.runLater(() -> progressForm.getDialogStage().close());
				Platform.runLater(() -> mainController.loadScreen("permutationParameters"));
			}
			if(betaReader.hasWarnings()){
				Platform.runLater(() -> FXPopOutMsg.showWarning(betaReader.warningLog()));
			}

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
}

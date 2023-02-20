package dk.sdu.imada.gui.monitors;

import dk.sdu.imada.console.Variables;
import dk.sdu.imada.gui.controllers.FXPopOutMsg;
import dk.sdu.imada.gui.controllers.MainController;
import dk.sdu.imada.gui.controllers.ProgressForm;
import dk.sdu.imada.jlumina.core.io.ReadBetaMatrix;
import dk.sdu.imada.jlumina.core.io.WriteBetaMatrix;
import dk.sdu.imada.jlumina.core.util.MatrixUtil;
import dk.sdu.imada.jlumina.core.util.RawDataLoader;
import javafx.application.Platform;

public class WriteBetaMonitor implements Runnable{
	boolean done = false;
	String msg;
	ProgressForm progressForm;
	WriteBetaMatrix betaWriter;


	private void setProgress() {
		this.progressForm.getProgressBar().setProgress(betaWriter.getProgress());
		this.progressForm.getText().setText(betaWriter.getMsg());
	}

	public WriteBetaMonitor(WriteBetaMatrix betaWriter, ProgressForm progressForm) {
		this.betaWriter = betaWriter;
		this.progressForm = progressForm;
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis();

		synchronized (betaWriter) {
			while(!betaWriter.isDone()) {
				setProgress();
				try {
					betaWriter.wait();

					if(betaWriter.isOveflow()) {
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

			long endTime   = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			System.out.println("Writing data in " + (((double)totalTime/1000.0)/60.0) + " minutes");
			done = true;
			Platform.runLater(() -> progressForm.getDialogStage().close());
			
		}catch(OutOfMemoryError e) {
			Platform.runLater(() -> progressForm.getDialogStage().close());
			Platform.runLater(() -> FXPopOutMsg.showWarning("A memory exception was detected. Use the java command line with -Xms2024M -Xmx3024M. "	+ 
					"If the problem persists try to increase the previous values... good luck"));
			this.progressForm.cancelThreads();
		}
	}
}


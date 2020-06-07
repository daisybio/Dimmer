package dk.sdu.imada.gui.monitors;

import dk.sdu.imada.gui.controllers.ProgressForm;
import dk.sdu.imada.gui.controllers.util.SaveAll;
import javafx.application.Platform;

public class SaveAllMonitor implements Runnable{
	
	SaveAll s;
	ProgressForm pf;
	
	
	public SaveAllMonitor(SaveAll s,ProgressForm pf) {
		this.s = s;
		this.pf = pf;
	}
	
	public void checkProgress () {
		while(!s.isDone()) {
			pf.getProgressBar().setProgress(s.getProgress());
		}
	}

	@Override
	public void run() {
		checkProgress();
		Platform.runLater(()-> this.pf.getDialogStage().close());
	}
		
}

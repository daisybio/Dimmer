package dk.sdu.imada.gui.monitors;

import dk.sdu.imada.gui.controllers.MainController;
import dk.sdu.imada.gui.controllers.ProgressForm;
import dk.sdu.imada.gui.controllers.util.ReadDimmerProjectUtil;
import javafx.application.Platform;

public class DimmerProjectMonitor implements Runnable {

	MainController mainController;
	ReadDimmerProjectUtil projectUtil;
	ProgressForm progressForm;

	public DimmerProjectMonitor(MainController mainController, ReadDimmerProjectUtil projectUtil, ProgressForm progressForm) {
		super();
		this.projectUtil = projectUtil;
		this.progressForm = progressForm;
		this.mainController = mainController;
	}

	public void checkProgress() {
		while(!projectUtil.isDone()) {
			this.progressForm.getProgressBar().setProgress(projectUtil.getProgress());
			//this.progressForm.getText().setText("Done loading " + (int)(projectUtil.getProgress() * 100) + "% of the CpGs");
		}
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		
		this.progressForm.getText().setText("Wait...");
		checkProgress();
		this.progressForm.getText().setText("Setting methylation data");

		if (projectUtil.isCheck()) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					mainController.getDmrParametersController().setCpgAnnotation(projectUtil.getData()); 
					mainController.getDmrParametersController().setDiffDistribution(projectUtil.getDiff()); 
					mainController.setEmpiricalPvalues(projectUtil.getEmpirical());
					mainController.setOriginalPvalues(projectUtil.getOriginal());
					mainController.setFwerPvalues(projectUtil.getFwer());
					mainController.setStepDownMinPvalues(projectUtil.getSdc());
					mainController.setFdrPvalues(projectUtil.getFdr());
					mainController.setMethylationDifference(projectUtil.getDiff());
					
					mainController.setSkiptToDMRParameters(true);
					mainController.uncheckStep(mainController.getStepModel_2());
					mainController.uncheckStep(mainController.getStepPermutationParameters_3());
					mainController.uncheckStep(mainController.getInput_4());
					mainController.uncheckStep(mainController.getSummary_5());
					mainController.uncheckStep(mainController.getPermutationResult_6());
					mainController.uncheckStep(mainController.getDmrParameters_7());
					mainController.uncheckStep(mainController.getDmrExecute_8());
					mainController.uncheckStep(mainController.getDmrResult_9());
					mainController.getDmrParametersController().getBackButton().setDisable(true);
					mainController.loadScreen("dmrParameters");
				}
			});
		}
		
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Reading project file in " + ((double)totalTime/1000.0) + " seconds");
		Platform.runLater(()-> this.progressForm.getDialogStage().close());
	}
}

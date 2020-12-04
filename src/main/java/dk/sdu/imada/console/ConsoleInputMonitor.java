package dk.sdu.imada.console;

import dk.sdu.imada.jlumina.core.util.MatrixUtil;
import dk.sdu.imada.jlumina.core.util.RawDataLoader;


public class ConsoleInputMonitor implements Runnable {
	
	boolean done = false;
	String msg;
	ConsoleMainController mainController;
	RawDataLoader rawDataLoader;

	public ConsoleInputMonitor(RawDataLoader rawDataLoader, ConsoleMainController mainController) {
		this.rawDataLoader = rawDataLoader;
		this.mainController = mainController;
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis();

		synchronized (rawDataLoader) {
			while(!rawDataLoader.isDone()) {
				try {
					rawDataLoader.wait();
					if(rawDataLoader.isOveflow()) {
						System.out.println("A memory exception was detected. Use the java command line with -Xms2024M -Xmx3024M. "	+ 
								"If the problem persists try to increase the cited values... good luck");
						System.exit(0);
					}

				}catch(InterruptedException e) {
					
				}
			}
		}

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
			
			//Platform.runLater(() -> mainController.loadScreen("permutationParameters"));
			
			long endTime   = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			System.out.println("Processing raw data in " + (((double)totalTime/1000.0)/60.0) + " minutes\n");
			done = true;
		}catch(OutOfMemoryError e) {
			System.out.println("A memory exception was detected. Use the java command line with -Xms2024M -Xmx3024M. "	+ 
					"If the problem persists try to increase the cited values... good luck");
			System.exit(0);
		}
	}

}

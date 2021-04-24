package dk.sdu.imada.console;

import java.util.ArrayList;




import dk.sdu.imada.jlumina.core.io.ReadManifest;
import dk.sdu.imada.jlumina.search.algorithms.DMRAlgorithm;
import dk.sdu.imada.jlumina.search.algorithms.DMRPermutation;
import dk.sdu.imada.jlumina.search.algorithms.DMRScoring;
import dk.sdu.imada.jlumina.search.primitives.DMR;
import dk.sdu.imada.jlumina.search.primitives.DMRDescription;
import dk.sdu.imada.jlumina.search.util.DMRPermutationExecutor;



public class ConsoleDMRFinderController {
	
	ArrayList<DMR> dmrs;

	ConsoleMainController mainController;
	Config config;
	
	public ConsoleDMRFinderController(Config config, ConsoleMainController mainController){
		this.mainController = mainController;
		this.config = config;
	}
	
	public void start(){
		
		
		ReadManifest manifest = mainController.getManifest();

		int numThreads = config.getThreads();
		int k = config.getNExceptions();
		int w = config.getWSize() - 1;
		int l = config.getMaxCpgDist();
		int np = config.getNPermutationsDmr();
		
		if(np < numThreads){
			numThreads = np;
		}
		
		
		int[] binaryArray = getBinaryArray(mainController.getSearchPvalues(), config.getPValueCutoff());
		int MAX = binaryArray.length;

		int [] positions  = new int[MAX];
		String [] chrs = new String[MAX];
		String [] cpg = new String[MAX];
		
		
		for (int i = 0; i < manifest.getCpgList().length; i++) {
			cpg[i] = manifest.getCpgList()[i].getCpgName();
			chrs[i] = manifest.getCpgList()[i].getChromosome();
			positions[i] = manifest.getCpgList()[i].getMapInfo();
		}
		
		DMRAlgorithm dmrAlgorithm = new DMRAlgorithm(k, w, l, 1, positions, chrs, mainController.getSearchPvalues());
		dmrs = dmrAlgorithm.islandSearch(binaryArray);
		this.mainController.setDMRs(dmrs);
		
		DMRScoring scoring = new DMRScoring(dmrs);
		scoring.calcPValues(mainController.getSearchPvalues(), dmrAlgorithm.getBreakingPoints(positions, chrs, l));
		
		ArrayList<DMRDescription> dmrDescriptions = new ArrayList<>();

		for (DMR island : dmrs) {
			DMRDescription d = new DMRDescription(island, cpg, chrs, positions);
			d.setLink();
			dmrDescriptions.add(d);
		}
		
		this.mainController.setDmrDescriptions(dmrDescriptions);
		
		DMRPermutationExecutor [] executors = new DMRPermutationExecutor[numThreads];
		DMRPermutation dmrPermutation[] = new DMRPermutation[numThreads];
		Thread eThread [] = new Thread[numThreads];
		int[] dmrPermuDist = Util.distributePermutations(numThreads, np);

		for (int i = 0; i < numThreads; i++) {
			dmrPermutation[i] = new DMRPermutation(new DMRAlgorithm(k, w, l, 1, positions, chrs, mainController.getSearchPvalues()), dmrs, binaryArray, dmrPermuDist[i]);
			executors[i] = new DMRPermutationExecutor(dmrPermutation[i]);
			eThread[i] = new Thread(executors[i], "permutation_" + i);
		}

		ConsoleDMRPermutationMonitor dmrPermutationMonitor = new ConsoleDMRPermutationMonitor(dmrPermutation, dmrs, mainController,np);
	
		for (Thread e : eThread) {
			e.start();
		}

		dmrPermutationMonitor.run();
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

}

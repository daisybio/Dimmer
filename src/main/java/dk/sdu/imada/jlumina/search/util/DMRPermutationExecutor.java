package dk.sdu.imada.jlumina.search.util;

import dk.sdu.imada.jlumina.search.algorithms.DMRPermutation;

public class DMRPermutationExecutor implements Runnable {
	
	DMRPermutation dmrPermutation;
	
	public DMRPermutationExecutor(DMRPermutation dmrPermutation) {
		this.dmrPermutation = dmrPermutation;
	}

	@Override
	public void run() {
		dmrPermutation.computePermutation();
	}

}

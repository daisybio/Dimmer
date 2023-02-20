package dk.sdu.imada.jlumina.search.util;

import dk.sdu.imada.jlumina.search.algorithms.DMRPermutation;

public class DMRMonitor implements Runnable {

	DMRPermutation dmrSearch;
	int progress = 0;

	public DMRMonitor(DMRPermutation dmrSearch) {
		this.dmrSearch = dmrSearch;
	}

	public int getProgress() {
		return progress;
	}

	public void run() {
		checkProgress();
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public void checkProgress() {
		synchronized (dmrSearch) {
			while(!dmrSearch.isDone()) {
				try {
					dmrSearch.wait();
					setProgress(getProgress() + 1); 
				}catch(InterruptedException exception) {
				}
			}
		}
	}
}

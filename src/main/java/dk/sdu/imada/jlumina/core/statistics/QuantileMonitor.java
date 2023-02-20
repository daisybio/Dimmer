package dk.sdu.imada.jlumina.core.statistics;

public class QuantileMonitor implements Runnable {

	FastQuantileUtil fq[];
	boolean done;

	public QuantileMonitor(FastQuantileUtil fq[]) {
		this.fq = fq;
		done = false;
	}
	
	public boolean isDone() {
		return done;
	}

	public synchronized void synchronizeRowMean() {
		for (FastQuantileUtil q : fq) {
			synchronized (q) {
				while(!q.isDoneRowMean()) {
					try {
						q.wait();
					}catch(InterruptedException e) { 

					}
				}
			}
		}
		
		float [] rowMean = new float[fq[0].getMean().length];
		
		for (int row = 0; row < rowMean.length; row++) {
			for (FastQuantileUtil q : fq) {
				rowMean[row] += ((float) q.getMean()[row]);
			}
		}
		
		for (int i = 0; i < fq.length; i++) {
			fq[i].setRowMean(rowMean);
		}
		
		done = true;
		notify();
	}
	
	/*public synchronized void synchronizeReplace() {
		for (FastQuantileUtil q : fq) {
			synchronized (q) {
				while(!q.isDoneReplace()) {
					try {
						q.wait();
					}catch(InterruptedException e) { 

					}
				}
			}
		}
		notify();
	}*/

	@Override
	public void run() {
		synchronizeRowMean();
		System.out.println("Synchronizing means...");
	}
}

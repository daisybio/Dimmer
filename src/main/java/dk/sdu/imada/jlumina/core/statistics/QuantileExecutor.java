package dk.sdu.imada.jlumina.core.statistics;

public class QuantileExecutor implements Runnable{

	FastQuantileUtil fq;
	QuantileMonitor monitor;

	public QuantileExecutor(FastQuantileUtil fq) {
		this.fq = fq;
	}
	
	public void setMonitor(QuantileMonitor monitor) {
		this.monitor = monitor;
	}

	@Override
	public void run() {
		
		fq.setRowMeans();
		
		synchronized (monitor) {
			while(!this.monitor.isDone()) {
				try {
					monitor.wait();
				}catch(InterruptedException e) {

				}
			}	
		}
		System.out.println("Computing rankings");
		fq.setMeanReplaceMAtrix();
	}
}

package dk.sdu.imada.jlumina.search.algorithms;

public class PermutationProgress {
	
	int maxIterations;
	
	boolean done;
	
	double progress;
	double progressAux;

	public PermutationProgress() {
		progress = 0.0;
		progressAux = 0.0;
	}

	public boolean isDone() {
		return done;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}
	
	public synchronized void setProgress(int progress, double diff) {

		this.progress = (double) progress/(double)maxIterations;

		if (this.progress - this.progressAux >= diff) {
			this.progressAux = this.progress;
			notify();
		}
	}
	
	public synchronized void setDone(boolean done) {
		this.done = done;
		notifyAll();
	}

	public double getProgress() {
		return progress;
	}
	
	public int getNumPermutations() {
		return maxIterations;
	}
	
	public void setNumPermutations(int numPermutations) {
		this.maxIterations = numPermutations;
	}
	
	public void setProgressAux(double progressAux) {
		this.progressAux = progressAux;
	}
}

package dk.sdu.imada.jlumina.core.util;

public class DataProgress {

	boolean done;
	int maxSteps;
	boolean oveflow;
	double progress;
	String msg;
	
	public DataProgress(){
		done = false;
		oveflow = false;
		maxSteps = 0;
	}
	
	public synchronized void setDone(boolean done) {
		this.done = done;
		notify();
	}
	
	public synchronized void setOveflow(boolean oveflow) {
		this.oveflow = oveflow;
		this.done = true;
		notify();
	}
	
	public boolean isOveflow() {
		return oveflow;
	}
	
	public synchronized void setProgress(int stepdDone) {
		progress = (double)stepdDone/(double)maxSteps;
		notify();
	}
	
	public double getProgress() {
		return progress;
	}
	
	public boolean isDone() {
		return done;
	}
	
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	public String getMsg() {
		return msg;
	}
	
	public void setMaxSteps(int maxSteps) {
		this.maxSteps = maxSteps;
	}
	
	public int getMaxSteps() {
		return maxSteps;
	}
	
}

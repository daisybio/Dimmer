package dk.sdu.imada.jlumina.core.util;

import java.util.ArrayList;

public class DataProgress {

	boolean done;
	int maxSteps;
	boolean oveflow;
	double progress;
	String msg;
	protected ArrayList<String> warnings;
	protected ArrayList<String> errors;
	
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
	
	public boolean hasWarnings(){
		if(this.warnings == null){
			return false;
		}
		else if(this.warnings.size() != 0){
			return true;
		}
		else{
			return false;
		}
	}
	
	public boolean hasErrors(){
		if(this.errors == null){
			return false;
		}
		else if(this.errors.size() != 0){
			return true;
		}
		else{
			return false;
		}
	}
	
	public ArrayList<String> getWarnings(){
		return this.warnings;
	}
	
	public ArrayList<String> getErrors(){
		return this.errors;
	}
	
}

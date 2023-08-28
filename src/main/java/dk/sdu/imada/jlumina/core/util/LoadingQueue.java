package dk.sdu.imada.jlumina.core.util;

import java.util.concurrent.ConcurrentLinkedQueue;

public class LoadingQueue<T extends Runnable> extends ConcurrentLinkedQueue<T>{
	
	private boolean overflow = false;
	
	public void setOverflow(boolean overflow){
		this.overflow = overflow;
	}
	
	public boolean isOverflow(){
		return this.overflow;
	}
	

}

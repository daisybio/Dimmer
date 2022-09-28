package dk.sdu.imada.jlumina.core.util;

import java.util.Queue;

import dk.sdu.imada.console.Util;


public class QueueThread <T extends Runnable> extends Thread{
	
	private Queue<T> queue;
	private LoadingQueue<T> loaded;
	private int id;
	
/**
 * A thread that can work with other threads on a combined task queue (tasks need to implement runnable)
 * 
 * @param queue the task queue
 * @param loaded stores finished tasks
 * @param id a thread id for status prints
 */
	public QueueThread(Queue<T> queue, LoadingQueue<T> loaded, int id, Boolean overflow){
		this.loaded = loaded;
		this.queue = queue;
		this.id = id;
	}
	
	/**
	 * runs tasks from the queue, until empty
	 * adds them two loaded, once finished
	 */
	public void run(){
		try{
		
			T task = null;
			while((task = getTask())!=null){
				System.out.println("Thread " + id + " starts " + task);
				task.run();
				System.out.println("Thread " + id + " ends " + task);
				synchronized(loaded){
					if(loaded.isOverflow()){ 	//end thread if an overflow occurred somewhere
						return;
					}
					loaded.add(task);
					loaded.notify();
				}
			}	
		}catch(OutOfMemoryError e){
			synchronized(loaded){
				loaded.setOverflow(true);
				loaded.notify();
			}
		}

	}
	
	/**
	 * 
	 * @return returns a new task from the task queue and removes it
	 */
	
	private T getTask() throws OutOfMemoryError{
		synchronized(this.queue){
			queue.notify();
			return queue.poll();
		}
	}
	

	
}

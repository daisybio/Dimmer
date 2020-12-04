package dk.sdu.imada.jlumina.core.util;

public class DataExecutor implements Runnable{
	
	RawDataLoader rawDataLoader;
	
	public DataExecutor(RawDataLoader rawDataLoader) {
		this.rawDataLoader = rawDataLoader;
	}

	@Override
	public void run() {
		try{
			rawDataLoader.loadData();
		}
		catch(OutOfMemoryError e){
			rawDataLoader.setOveflow(true);
			System.out.println("Memory ram problem. "
					+ "Increase your java heap space with the parameters -Xmx and Xms");
		}

	}

}

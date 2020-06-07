package dk.sdu.imada.jlumina.core.util;

public class DataExecutor implements Runnable{
	
	RawDataLoader rawDataLoader;
	
	public DataExecutor(RawDataLoader rawDataLoader) {
		this.rawDataLoader = rawDataLoader;
	}

	@Override
	public void run() {
		rawDataLoader.loadData();
	}

}

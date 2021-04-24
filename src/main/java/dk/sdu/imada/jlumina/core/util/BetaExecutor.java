package dk.sdu.imada.jlumina.core.util;

import dk.sdu.imada.jlumina.core.io.ReadBetaMatrix;

public class BetaExecutor implements Runnable{
	
	private ReadBetaMatrix betaReader;
	private String[] sentrixIds;
	private String[] sentrixPositions;
	private String chipType;
	
	public BetaExecutor(ReadBetaMatrix betaReader, String[] sentrixIds, String[] sentrixPositions, String chipType) {
		this.betaReader = betaReader;
		this.sentrixIds = sentrixIds;
		this.sentrixPositions = sentrixPositions;
		this.chipType = chipType;
	}

	@Override
	public void run() {
		try{
			betaReader.initBetaMatrix(sentrixIds,sentrixPositions,chipType);
		}
		catch(OutOfMemoryError e){
			betaReader.setOveflow(true);
			System.out.println("Memory ram problem. "
					+ "Increase your java heap space with the parameters -Xmx and Xms");
		}

	}

}

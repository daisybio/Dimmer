package dk.sdu.imada.jlumina.core.primitives;

import java.util.HashMap;

import dk.sdu.imada.jlumina.core.io.ReadManifest;

public class USet extends MethylationData {
	
	int numSamples;
	RGSet rgSet; 
	ReadManifest manifest;
	
	public USet(){
		super();
	}
	
	public USet(RGSet rgSet, ReadManifest manifest ) {
		super();
		this.rgSet = rgSet;
		this.manifest = manifest;
	}
	
	
	public synchronized void loadData() throws OutOfMemoryError {
		
		done = false;
		progress = 0;
		
		numSamples = rgSet.getSampleIDs().size();
		data = new HashMap<String, float[]>();
		int counter = 0;
		
		for (CpG cpg : manifest.getCpgList()) {

			if (cpg.getInifniumType().equals("II")) { 
				float[] values = rgSet.getRedSet().get(cpg.getAddressA());
				data.put(cpg.getCpgName(), values);
				if(values == null){
					counter++;
				}
			}
			
			if (cpg.getInifniumType().equals("I")) {
				float values [];
				if (cpg.colorChannel.equals("Red")) {
					values = rgSet.getRedSet().get(cpg.getAddressA());
				}else {
					values = rgSet.getGreenSet().get(cpg.getAddressA());
				}
				if(values == null){
					counter++;
				}
				data.put(cpg.getCpgName(), values);
			}
			progress++;
			notify();
		}
		System.out.println(counter);
		done = true;
		notify();
	}
	
	
	public int getNumSamples() {
		return numSamples;
	}
	
	public void setRgSet(RGSet rgSet) {
		this.rgSet = rgSet;
	}
	
	public void setManifest(ReadManifest manifest) {
		this.manifest = manifest;
	}
	
}

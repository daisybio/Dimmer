package dk.sdu.imada.jlumina.core.primitives;

import java.util.HashMap;
import java.util.HashSet;

import dk.sdu.imada.console.Util;
import dk.sdu.imada.jlumina.core.io.ReadManifest;

public class MSet extends MethylationData {

	int numSamples;
	RGSet rgSet;
	ReadManifest manifest;

	public MSet() {
		super();
	}

	public MSet(RGSet rgSet, ReadManifest manifest ) {
		super();
		this.rgSet = rgSet;
		this.manifest = manifest;
	}
	
	public synchronized void loadData() throws OutOfMemoryError {

		done = false;
		progress = 0;

		numSamples = rgSet.getSampleIDs().size();
		data = new HashMap<String, float[]>();
		this.bad_CpG_indices = new HashSet<>();
		
		int cpg_index = 0;

		for (CpG cpg : manifest.getCpgList()) {

			if (cpg.getInifniumType().equals("II")) { 
				float[] values = rgSet.getGreenSet().get(cpg.addressA);
				if(values == null){
					bad_CpG_indices.add(cpg_index);
				}
				else {
					if(Util.containsNegatives(values)){
						bad_CpG_indices.add(cpg_index);
					}
					else{
						data.put(cpg.getCpgName(), values);
					}
				}
			}

			if (cpg.getInifniumType().equals("I")) {
				float values [];
				if (cpg.colorChannel.equals("Red")) {
					values = rgSet.getRedSet().get(cpg.getAddressB());
				}else {
					values = rgSet.getGreenSet().get(cpg.getAddressB());
				}
				if(values == null){
					bad_CpG_indices.add(cpg_index);
				}
				else {
					if(Util.containsNegatives(values)){
						bad_CpG_indices.add(cpg_index);
					}
					else{
						data.put(cpg.getCpgName(), values);
					}
				}
			}
			cpg_index++;
			progress++;
			notify();
		}
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

package dk.sdu.imada.jlumina.core.statistics;

import dk.sdu.imada.jlumina.core.io.AbstractManifest;
import dk.sdu.imada.jlumina.core.io.ReadControlProbe;
import dk.sdu.imada.jlumina.core.io.ReadManifest;
import dk.sdu.imada.jlumina.core.primitives.RGSet;

public interface Normalization {
	//public HashMap<String, double[]> performNormalization( MethylationData methylationData, ReadManifest manifest);
	//public HashMap<String, float[]> performNormalization( MethylationData methylationData, ReadManifest manifest, char[] gender);
	//public void performNormalization( MethylationData methylationData, ReadManifest manifest, char[] gender, int nt) throws OutOfMemoryError;
	public void performNormalization(RGSet methylationData, AbstractManifest manifest, char[] gender, int nt) throws OutOfMemoryError;
	public void checkProgress();
	public boolean isDone();
	public void setDone(boolean done);
	public int getProgress();
	
	//We need that....
	public void setControlData(ReadControlProbe controlProbes);
	public void setManifest(ReadManifest manifest);
}

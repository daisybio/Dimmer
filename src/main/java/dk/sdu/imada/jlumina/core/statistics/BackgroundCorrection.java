package dk.sdu.imada.jlumina.core.statistics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import dk.sdu.imada.jlumina.core.io.AbstractManifest;
import dk.sdu.imada.jlumina.core.io.ReadControlProbe;
import dk.sdu.imada.jlumina.core.io.ReadManifest;
import dk.sdu.imada.jlumina.core.primitives.RGSet;

public class BackgroundCorrection implements Normalization {
	RGSet rgset;
	ReadControlProbe readControl;
	double percentage;
	
	public BackgroundCorrection(ReadControlProbe r, double p){
		this.readControl = r;
		this.percentage = p;
	}
	
	
	@Override
	public void performNormalization(RGSet methylationData, AbstractManifest manifest, char[] gender, int nt)
			throws OutOfMemoryError {
		int[] negative = readControl.getControlAddress("NEGATIVE", readControl,0);
		float[] bgGreen = calculateNoise(methylationData.getGreenSet(),negative,nt);
		float[] bgRed = calculateNoise(methylationData.getRedSet(),negative,nt);
		HashMap<Integer,float[]> greenBG =  calculateBackground(methylationData.getGreenSet(),bgGreen);
		HashMap<Integer,float[]> redBG =  calculateBackground(methylationData.getRedSet(),bgRed);
		methylationData.setGreenSet(greenBG); 
		methylationData.setRedSet(redBG);
	}

	public float[] calculateNoise(HashMap<Integer,float[]> color, int[] addresses,int sampleNo){
		int noAddresses = addresses.length;
		int noiseValue =(int) Math.ceil(noAddresses*percentage);
		float[] results = new float[sampleNo];
		for(int i = 0;i<sampleNo;i++){
			float[] sort = new float[noAddresses];
			for(int j=0;j<noAddresses;j++){
				float[] address = color.get(addresses[j]);
				sort[j]=address[i];
			}
			Arrays.sort(sort);
			results[i]=sort[noiseValue-1];
		}

		return results;
	}
	
	public HashMap<Integer,float[]> calculateBackground(HashMap<Integer, float[]> color, float[] vector){
		for (Entry<Integer, float[]> values: color.entrySet()) {
			float[] vals = values.getValue();
			int key = values.getKey();
			float[] temp = new float[vector.length];
			for(int i=0;i<temp.length;i++){
				temp[i] = vals[i]-vector[i];
				if(temp[i]<0){
					temp[i]=0;
				}
			}
			color.put(key, temp);
		}

		return color;
	}

	
	@Override
	public void checkProgress() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isDone() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setDone(boolean done) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getProgress() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void setControlData(ReadControlProbe controlProbes) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setManifest(ReadManifest manifest) {
		// TODO Auto-generated method stub
		
	}

}

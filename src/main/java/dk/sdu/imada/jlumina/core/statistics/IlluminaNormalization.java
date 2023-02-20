package dk.sdu.imada.jlumina.core.statistics;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import dk.sdu.imada.jlumina.core.io.AbstractManifest;
import dk.sdu.imada.jlumina.core.io.ReadControlProbe;
import dk.sdu.imada.jlumina.core.io.ReadManifest;
import dk.sdu.imada.jlumina.core.primitives.MSet;
import dk.sdu.imada.jlumina.core.primitives.RGSet;
import dk.sdu.imada.jlumina.core.primitives.USet;


public class IlluminaNormalization implements Normalization{
	HashMap<String, double[]> betaValues;
	RGSet rgSet;
	ReadManifest Manifest;
	MSet mSet;
	USet uSet;
	ReadControlProbe control;

	public IlluminaNormalization(RGSet rgset, ReadControlProbe control, ReadManifest manifest){
		this.Manifest = manifest;
		this.rgSet = rgset;
		this.control = control;
	}

	public RGSet normalizeIlluminaControl(RGSet rgset, ReadControlProbe control, int reference, int sampleNo){
		int[] AT_control = control.getControlAddress("NORM_A, NORM_T", control,0);
		int[] CG_control = control.getControlAddress("NORM_C, NORM_G", control,0);
		float[] meanGreen = getMeanValuePatient(rgset.getGreenSet(),CG_control, sampleNo);
		float[] meanRed = getMeanValuePatient(rgset.getRedSet(),AT_control, sampleNo);
		float ref = (meanGreen[reference-1]+meanRed[reference-1])/2;
		float[] factorGreen = getFactor(ref, meanGreen);
		float[] factorRed = getFactor(ref, meanRed);
		HashMap<Integer,float[]> green = new HashMap<Integer,float[]>();
		HashMap<Integer,float[]> red = new HashMap<Integer,float[]>();
		green = calculateNormalValues(green,rgset.getGreenSet(),factorGreen);
		red = calculateNormalValues(red,rgset.getRedSet(),factorRed);
		RGSet normalSet = new RGSet();
		normalSet.setGreenSet(green); 
		normalSet.setRedSet(red);
		return normalSet;
	}
	
	public float[] getMeanValuePatient(HashMap<Integer, float[]> color, int[] addresses,  int sampleNo){
		float[] mean = new float[sampleNo];
		float[] results = new float[sampleNo];
		int noAddresses = addresses.length;
		for(int i = 0;i<noAddresses;i++){
			float[] address = color.get(addresses[i]);
			for(int j=0;j<mean.length;j++){
				mean[j]=mean[j]+address[j];
			}
		}
		for(int k = 0;k<mean.length;k++){
			results[k]= mean[k]/noAddresses;

		}
		return results; 

	}


	public float[] getFactor(float ref, float[] vector){
		float[] factorI = new float[vector.length];
		for(int i =0;i<vector.length;i++){
			factorI[i]=ref/vector[i];
		}
		return factorI;
	}

	public HashMap<Integer,float[]> calculateNormalValues(HashMap<Integer,float[]> newColor,HashMap<Integer, float[]> color, float[] vector){
		for (Entry<Integer, float[]> values: color.entrySet()) {
			float[] vals = values.getValue();
			int key = values.getKey();
			float[] temp = new float[vector.length];
			for(int i=0;i<temp.length;i++){
				temp[i] = vals[i]*vector[i];		      
			}
			newColor.put(key, temp);
		}
		return newColor;
	}

	public int getSampleNo(int[] addresses, RGSet rgset){
		HashMap<Integer,float[]> green = rgset.getGreenSet();
		float[] data = green.get(addresses[0]);
		int number = data.length;
		return number;
	}
	

	@Override
	public void performNormalization(RGSet methylationData, AbstractManifest manifest, char[] gender, int nt)
			throws OutOfMemoryError {
		RGSet normalization = normalizeIlluminaControl(rgSet, control, 1,nt);
		rgSet = normalization;
		uSet = new USet(methylationData, Manifest);
		mSet = new MSet(methylationData,Manifest);
		mSet.loadData();
		uSet.loadData();
		HashMap<String,float[]> m = mSet.getData();		
		try{
			Scanner scan = new Scanner(new File("Ime.csv"));
			while(scan.hasNextLine()){
				String s = scan.nextLine();
				String[] st = s.split(",");
				String a = st[0]; 
				float[] val = m.get(a);
				for(int i = 0;i<val.length;i++){
					if((val[i]-Float.parseFloat(st[i+1])>0.0001)){
						System.out.println("Fejl: "+val[i]+" "+st[i+1]);
					}
				}
				
			}
			scan.close();
		}catch(FileNotFoundException fnfe){
			System.out.println("Filen ikke fundet");
		}
		
		HashMap<String,float[]> u = uSet.getData();		
		try{
			Scanner scan = new Scanner(new File("Iume.csv"));
			while(scan.hasNextLine()){
				String s = scan.nextLine();
				String[] st = s.split(",");
				String a = st[0]; 
				float[] val = m.get(a);
				for(int i = 0;i<val.length;i++){
					if((val[i]-Float.parseFloat(st[i+1])>0.0001)){
						System.out.println("Fejl: "+val[i]+" "+st[i+1]);
					}
				}
				
			}
			scan.close();
		}catch(FileNotFoundException fnfe){
			System.out.println("Filen ikke fundet");
		}
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

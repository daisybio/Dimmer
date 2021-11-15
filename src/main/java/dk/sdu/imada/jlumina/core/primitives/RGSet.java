package dk.sdu.imada.jlumina.core.primitives;

import java.util.ArrayList;
import java.util.HashMap;

import dk.sdu.imada.jlumina.core.io.Read450KSheet;
import dk.sdu.imada.jlumina.core.io.ReadIDAT;

public class RGSet {
	
	String input;
	
	//raw dataset
	HashMap<Integer, float[]> greenSet;
	HashMap<Integer, float[]> redSet;
	
	//methylated or unmethylated signal
	HashMap<String, float[]> data;
	
	ArrayList<String> sampleIDs;
	
	int numberSamples;
	
	boolean done;
	
	int progress;
	
	ArrayList<String> warnings;
	ArrayList<String> errors;
	
	public RGSet() {
	}

	public RGSet(String input) {
		this.input = input;
		done = false;
		
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public int getProgress() {
		return progress;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public synchronized void loadIDATs() {
		
		progress = 0;


		greenSet = new HashMap<Integer, float[]>();
		redSet = new HashMap<Integer, float[]>();
		sampleIDs = new ArrayList<String>();

		Read450KSheet s = new Read450KSheet(this.input);

		String basenames[] = s.getBaseName();

		int colId = 0;
		ReadIDAT gIdat = new ReadIDAT();
		ReadIDAT rIdat = new ReadIDAT();
		ReadIDAT aux = new ReadIDAT();
		aux.readNonEncryptedIDAT(basenames[0] + "_Grn.idat");
		int SIZE = aux.getnSNPsRead();

		numberSamples = basenames.length;
		
		int [] ilumminaIDs = new int[SIZE];
		float greenMeans[][] = new float[SIZE][basenames.length];
		float redMeans[][] = new float[SIZE][basenames.length];
		
		for (String b : basenames) {

			sampleIDs.add(b);
			
			String greenIdatFile = b + "_Grn.idat";
			String redIdatFile = b + "_Red.idat";
			
			gIdat.readNonEncryptedIDAT(greenIdatFile);
			rIdat.readNonEncryptedIDAT(redIdatFile);


			for (int i = 0; i < SIZE; i++) {
				ilumminaIDs[i] = rIdat.getIlluminaID()[i];
				greenMeans[i][colId] = gIdat.getMean()[i];
				redMeans[i][colId] = rIdat.getMean()[i];
			}
			colId++;
			notify();
			progress++;
		}
		int index = 0;
		for (int ids : ilumminaIDs) {
			greenSet.put(ids, greenMeans[index]);
			redSet.put(ids, redMeans[index]);
			index++;
		}
		done = true;
		notify();
	}
	
	/**
	 * Same functionality as loadIdats but handles missing sites and different sized files
	 */
	public synchronized void loadIDATsMap() {
		
		progress = 0;
		
		this.warnings = new ArrayList<String>();
		this.errors = new ArrayList<String>();

		greenSet = new HashMap<Integer, float[]>();
		redSet = new HashMap<Integer, float[]>();
		
		sampleIDs = new ArrayList<String>();

		Read450KSheet s = new Read450KSheet(this.input);

		String basenames[] = s.getBaseName();

		int colId = 0;
		ReadIDAT gIdat = new ReadIDAT();
		ReadIDAT rIdat = new ReadIDAT();
		ReadIDAT aux = new ReadIDAT();
		
		aux.readNonEncryptedIDAT(basenames[0] + "_Grn.idat");
		int SIZE = aux.getnSNPsRead();
		boolean diff_sizes = false;
		
		//checks if array sizes differ
		for (String b : basenames) {
			String greenIdatFile = b + "_Grn.idat";
			String redIdatFile = b + "_Red.idat";
			if( (aux.readnSNPs(greenIdatFile)!=SIZE) || (aux.readnSNPs(redIdatFile)!=SIZE) ){
				diff_sizes = true;
			}
			
		}
		
		if(diff_sizes){
			warnings.add("IDAT files have different array sizes");
		}
		

		numberSamples = basenames.length;
		
		for (String b : basenames) {

			sampleIDs.add(b);
			
			String greenIdatFile = b + "_Grn.idat";
			String redIdatFile = b + "_Red.idat";
			
			gIdat.readNonEncryptedIDAT(greenIdatFile);
			rIdat.readNonEncryptedIDAT(redIdatFile);
			
			int[] g_ids = gIdat.getIlluminaID();
			int[] r_ids = rIdat.getIlluminaID();
			
			int[] g_means = gIdat.getMean();
			int[] r_means = rIdat.getMean();
			
			
			//-1 is used for initialization to later filter out missing values
			for (int i = 0; i < g_ids.length; i++){
				float[] values = greenSet.get(g_ids[i]);
				//create value array if it doesnt exist
				if(values == null){
					values = initMinusOnes(numberSamples);
					greenSet.put(g_ids[i], values);
				}
				values[colId] = g_means[i];
			}
			
			for (int i = 0; i < r_ids.length; i++) {
				float[] values = redSet.get(r_ids[i]);
				//create value array if it doesnt exist
				if(values == null){
					values = initMinusOnes(numberSamples);
					redSet.put(r_ids[i], values);
				}
				values[colId] = r_means[i];
			}
			
			colId++;
			notify();
			progress++;
		}
		
		//filter missing values
		
		greenSet.entrySet().removeIf(
                entry -> (containsNegatives(entry.getValue())));
		redSet.entrySet().removeIf(
                entry -> (containsNegatives(entry.getValue())));

		done = true;
		notify();
	}

	public void setData(HashMap<String, float[]> data) {
		this.data = data;
	}
	
	public HashMap<String, float[]> getData() {
		return data;
	}
	
	public int getNumberSamples() {
		return numberSamples;
	}
	
	private float[] initMinusOnes(int size){
		float[] array = new float [size];
		for(int i = 0; i < array.length; i++){
			array[i] = -1;
		}
		return array;
	}
	
	public static boolean containsNegatives(float[] array){
		for(int i = 0; i< array.length; i++){
			if(array[i]<0){
				return true;
			}
		}
		return false;
	}
	

	/*public RGSet(String args) {

		HashMap<String, Read450K> greenSetBySample = new HashMap<String, Read450K>();
		HashMap<String, Read450K> redSetBySample = new HashMap<String, Read450K>();

		greenSet = new HashMap<Integer, int[]>();
		redSet = new HashMap<Integer, int[]>();

		sampleIDs = new ArrayList<String>();

		Read450KSheet s = new Read450KSheet(args);

		String basenames[] = s.getBaseName();

		for (String b : basenames) {
			sampleIDs.add(b);

			Read450K read450kGreen = new Read450K(false);
			Read450K read450kred = new Read450K(false);

			String greenIdatFile = b + "_Grn.idat";
			String redIdatFile = b + "_Red.idat";

			ReadIDAT gIdat = new ReadIDAT();
			gIdat.readNonEncryptedIDAT(greenIdatFile);
			ReadIDAT rIdat = new ReadIDAT();
			rIdat.readNonEncryptedIDAT(redIdatFile);

			read450kGreen.addIdat(gIdat);
			read450kred.addIdat(rIdat);

			greenSetBySample.put(b, read450kGreen);
			redSetBySample.put(b, read450kred);
		}

		int ncols = sampleIDs.size();

		int nrowsGreen = greenSetBySample.get(sampleIDs.get(0)).getIluminaIDs().size(); 
		for (int i = 0; i < nrowsGreen; i++) {
			int means[] = new int[ncols];
			int id = greenSetBySample.get(sampleIDs.get(0)).getIluminaID(i);
			for (int j = 0 ; j < ncols; j++) {
				means[j] = greenSetBySample.get(sampleIDs.get(j)).getMean(i);
			}
			greenSet.put(id, means);
		}

		int nrowsRed = greenSetBySample.get(sampleIDs.get(0)).getIluminaIDs().size(); 
		for (int i = 0; i < nrowsRed; i++) {
			int means[] = new int[ncols];
			int id = redSetBySample.get(sampleIDs.get(0)).getIluminaID(i);
			for (int j = 0 ; j < ncols; j++) {
				means[j] = redSetBySample.get(sampleIDs.get(j)).getMean(i);
			}
			redSet.put(id, means);
		}
	}*/

	public ArrayList<String> getSampleIDs() {
		return sampleIDs;
	}

	public void setGreenSet(HashMap<Integer, float[]> greenSet) {
		this.greenSet = greenSet;
	}

	public void setRedSet(HashMap<Integer, float[]> redSet) {
		this.redSet = redSet;
	}

	public HashMap<Integer, float[]> getGreenSet() {
		return greenSet;
	}

	public HashMap<Integer, float[]> getRedSet() {
		return redSet;
	}
	
	public boolean hasWarnings(){
		if(this.warnings==null){
			return false;
		}
		return this.warnings.size()!=0;
	}
	
	public ArrayList<String> getWarnings(){
		return this.warnings;
	}
}

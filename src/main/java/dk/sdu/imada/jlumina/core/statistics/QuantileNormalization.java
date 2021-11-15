package dk.sdu.imada.jlumina.core.statistics;

import java.util.ArrayList;
import java.util.HashMap;

import dk.sdu.imada.jlumina.core.io.AbstractManifest;
import dk.sdu.imada.jlumina.core.io.ReadControlProbe;
import dk.sdu.imada.jlumina.core.io.ReadManifest;
import dk.sdu.imada.jlumina.core.primitives.CpG;
import dk.sdu.imada.jlumina.core.primitives.RGSet;
import dk.sdu.imada.jlumina.search.util.DataUtil;

public class QuantileNormalization implements Normalization, Runnable {

	ReadManifest manifest;
	//MethylationData methylationData;
	char[] gender;

	boolean done;
	int progress;

	public QuantileNormalization() {
	}

	@SuppressWarnings("unused")
	private float[][] getQuantileNormalizedData(float m[][])  throws OutOfMemoryError {
		float mean[] = QuantileUtil.getRowMeans(m);
		float [][] normalized = QuantileUtil.getMeanReplaceMAtrix(m, mean);
		return normalized;
	}
	
	public void setGender(char[] gender) {
		this.gender = gender;
	}
	
	public char[] getGender() {
		return gender;
	}
	
	/**
	 * This method bla bla 
	 * @param m 
	 * @param nt
	 * @return
	 * @throws OutOfMemoryError
	 */
	private float[][] getFastQuantileNormalizedData(float m[][], int nt)  throws OutOfMemoryError {

		float[][] normalized = new float[m.length][m[0].length];

		ArrayList<Integer> begin = DataUtil.getColumnStartPoints(nt, m);
		ArrayList<Integer> end = DataUtil.getColumnEndPoints(m, begin, nt);

		FastQuantileUtil fqArray [] = new FastQuantileUtil[nt];
		QuantileExecutor executor[] = new QuantileExecutor[nt];
		
		Thread execThread[] = new Thread[nt];
		
		for (int i = 0; i < nt; i++) {
			fqArray[i] = new FastQuantileUtil(m, normalized, null, begin.get(i), end.get(i));
			executor[i] = new QuantileExecutor(fqArray[i]);
		}
		
		QuantileMonitor monitor = new QuantileMonitor(fqArray);
		Thread monitorThread = new Thread(monitor);
		monitorThread.start();
		
		for (int i = 0; i < nt; i++) {
			executor[i].setMonitor(monitor);
			execThread[i] = new Thread(executor[i]);
			execThread[i].start();
		}
		
		
		for (Thread t : execThread) {
			try {
				t.join();
			}catch(InterruptedException e) {
				
			}
		}
		
		return normalized;
		//float [][] normalized = QuantileUtil.getMeanReplaceMAtrix(m, rowMean);
		//return normalized;
	}

	// normalize first the non-sexual chromosomes then the sexual chromosomes
	private float[][] getNonStratifiedNormalization(float [][] toNormalize, int nt)  throws OutOfMemoryError {

		int x = 0;
		if(nt>toNormalize[0].length){
			System.out.println(toNormalize[0].length +" cores are used for speed-up...");
			System.out.println(nt-toNormalize[0].length + " additonally provided cores aren't used...");
			nt = toNormalize[0].length;
		}
		else{
			System.out.println(nt +" cores are used for speed-up...");
		}

		for (CpG cpg : manifest.getCpgList()) {
			if (cpg.getChromosome().equals("X")) {
				break;
			}
			x++;
		}

		int length = toNormalize.length - x ;
		//          x
		//0 1 2 3 4 5 6 7 8 9
		float [][] sex = new float[length][];
		float [][] all = new float[x][];

		for (int i = 0; i < x ; i++) {
			all[i] = toNormalize[i];
		}
		
		progress++;

		int index = 0;
		for (int i = x; i < toNormalize.length; i++) {
			sex[index++] = toNormalize[i]; 
		}
		progress++;

		//float[][] allossome = getQuantileNormalizedData(all);
		float[][] allossome = getFastQuantileNormalizedData(all, nt);
		progress++;
		
		float[][] sexual = new float[length][sex[0].length];

		//. same gender

		if (gender==null) {
			for (int i = 0; i < sex.length;i++) {
				for (int j = 0; j < sex[0].length; j++) {
					sexual[i][j] = sex[i][j];
				}
			}
		}else {
			
			ArrayList<Integer> xy = new ArrayList<>();
			ArrayList<Integer> xx = new ArrayList<>();

			int k = 0;
			for (char c : gender) {
				if (c=='F') 
					xx.add(k);
				else 
					xy.add(k);
				k++;
			}
			
			float[][] xyData = getSubSetMatrix(sex, xy);
			
			float[][] xxData = getSubSetMatrix(sex, xx);
			
			//float [][] xyNormalized = getQuantileNormalizedData(xyData);
			float [][] xyNormalized = getFastQuantileNormalizedData(xyData, nt);
			progress++;
			notify();

			//float [][] xxNormalized = getQuantileNormalizedData(xxData);
			float [][] xxNormalized = getFastQuantileNormalizedData(xxData, nt);
			progress++;
			notify();

			for (int i = 0; i < sexual.length; i++) {
				int z = 0;
				for (int j : xy) {
					sexual[i][j] = xyNormalized[i][z++]; 
				}
				z = 0;
				for (int j : xx) {
					sexual[i][j] = xxNormalized[i][z++]; 
				}
			}
		}
		

		float normalizedData [][] = new float[toNormalize.length][];

		index = 0;
		for (float [] v : allossome) {
			normalizedData[index++] = v;
		}

		for (float [] v : sexual) {
			normalizedData[index++] = v;
		}
		return normalizedData;
	}

	private float[][] getSubSetMatrix(float[][] data, ArrayList<Integer> columns)  throws OutOfMemoryError  {

		float mat[][] = new float[data.length][columns.size()];

		for (int i = 0 ; i < data.length; i++ ) {
			int index = 0;
			for (int j : columns) {
				mat[i][index++] = data[i][j];
			}
		}
		return mat;
	}

	@Override
	public synchronized void performNormalization(RGSet methylationData, AbstractManifest manifest,  char[] gender, int nt) throws OutOfMemoryError {

		done = false;
		progress = 0;
		
		HashMap<String, float[]> map = new HashMap<>();

		this.manifest = (ReadManifest) manifest;
		this.gender = gender;

		float toNormalize[][] = new float[manifest.getCpgList().length][];

		int index = 0;
		for (String cpg : manifest.getCpGsIDs()) {
			toNormalize[index++] = methylationData.getData().get(cpg);
//			if(index<=100){
//				System.out.println(index+"\t"+cpg);
//				for(float value: toNormalize[index-1]){
//				System.out.print(value+"\t");
//				}
//				System.out.println("");
//			}
		}
		

		toNormalize = getNonStratifiedNormalization(toNormalize, nt);
		notify();
		index = 0;
		for (String cpg : manifest.getCpGsIDs()) {
			map.put(cpg, toNormalize[index++]);
		
		}
		methylationData.setData(null); System.gc();
		methylationData.setData(map);

		done = true;
		notify();
	}

	@Override	
	public synchronized void checkProgress() {
		while (!isDone()) {
			try {
				wait();
			}catch(InterruptedException e){
			}
		}
	}

	@Override
	public void run() {

	}

	public boolean isDone() {
		return done;
	}
	
	@Override
	public int getProgress() {
		return progress;
	}

	@Override
	public synchronized void setDone(boolean done) {
		this.done = done;
		notifyAll();
	}

	@Override
	public void setControlData(ReadControlProbe controlProbes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setManifest(ReadManifest manifest) {
		// TODO Auto-generated method stub
		
	}
	
	/*HashMap<String, double[]> betaValues;
	RGSet  rgSet;
	MSet mSet;
	USet uSet;
	ReadManifest manifest;
	HashMap<String, double[]> rawBetaValues;

	public QuantileNormalization() {
	}

	public QuantileNormalization(RGSet rgSet, ReadManifest manifest) {
		this.rgSet = rgSet;
		this.manifest = manifest;
		this.mSet = new MSet(rgSet, manifest);
		this.uSet = new USet(rgSet, manifest);
		rawBetaValues = Util.getBeta(uSet, mSet, manifest, 0.0);
		System.out.println("Quantile normalization");
	}

	public QuantileNormalization(MSet mSet, USet uSet, ReadManifest manifest) {
		this.mSet = mSet;
		this.uSet = uSet;
		this.manifest = manifest;
	}	

	public HashMap<String, double[]> performNormalization() {

		ArrayList<int[]> mData = new ArrayList<>();
		ArrayList<int[]> uData = new ArrayList<>();

		HashMap<String, double[]> map = new HashMap<String, double[]>();

		for (String cpg : manifest.getCpGsIDs()) {
			mData.add(mSet.getMethylatedMap().get(cpg));
			uData.add(uSet.getUnmethylatedMap().get(cpg));
		}

		int unmethylated[][] = new int[mData.size()][];
		int methylated[][] = new int[uData.size()][];

		int index = 0;
		for (int[] u : uData) {
			unmethylated[index++] = u;
		}

		index = 0;
		for (int[] m : mData) {
			methylated[index++] = m;
		}

		double[] uRowMeans = QuantileUtil.getRowMeans(unmethylated);
		double[][] uNormalized  = QuantileUtil.getMeanReplaceMAtrix(unmethylated, uRowMeans);
		double[] mRowMeans = QuantileUtil.getRowMeans(methylated);
		double[][] mNormalized  = QuantileUtil.getMeanReplaceMAtrix(methylated, mRowMeans);

		double[][] beta = Util.getBeta(uNormalized, mNormalized, 0.0);

		index = 0;
		for (String cpg : manifest.getCpGsIDs()) {
			map.put(cpg, beta[index++]);
		}

		return map;
	}*/

	/*public HashMap<String, double[]> performNormalization() {

		String chrs[] = {"1", "2",  "3",  "4", "5", "6", "7", "8", "9", "10",
				"11", "12", "13", "14", "15", "16", "17", "18", "19", 
				"20", "21", "22","X", "Y" };

		HashMap<String, double[]> map = new HashMap<String, double[]>();

		for (String c : chrs) {

			String []cpgIDs = manifest.getCpGsIDsByChromosome(c);

			int unmethylated[][] = new int[cpgIDs.length][];
			int methylated[][] = new int[cpgIDs.length][];

			int index = 0;
			for (String cpg : cpgIDs) {
				try {
					unmethylated[index] = this.uSet.getUnmethylatedMap().get(cpg);
					methylated[index] = this.mSet.getMethylatedMap().get(cpg);
					index++;
				}catch(NullPointerException e) {
					System.out.println("No data in chromosome "+ c + " cpg " + cpg);
				}
			}

			double[] uRowMeans = QuantileUtil.getRowMeans(unmethylated);
			double[][] uNormalized  = QuantileUtil.getMeanReplaceMAtrix(unmethylated, uRowMeans);

			double[] mRowMeans = QuantileUtil.getRowMeans(methylated);
			double[][] mNormalized  = QuantileUtil.getMeanReplaceMAtrix(methylated, mRowMeans);

			double beta[][] = Util.getBeta(uNormalized, mNormalized, 0.0);
			index = 0;

			for (String cpg : cpgIDs) {
				map.put(cpg, beta[index++]);
			}
		}
		return map;
	}*/
}

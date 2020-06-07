package dk.sdu.imada.jlumina.core.statistics;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import dk.sdu.imada.jlumina.core.io.AbstractManifest;
import dk.sdu.imada.jlumina.core.io.ReadControlProbe;
import dk.sdu.imada.jlumina.core.io.ReadManifest;
import dk.sdu.imada.jlumina.core.primitives.Control;
import dk.sdu.imada.jlumina.core.primitives.CpG;
import dk.sdu.imada.jlumina.core.primitives.MSet;
import dk.sdu.imada.jlumina.core.primitives.RGSet;
import dk.sdu.imada.jlumina.core.primitives.USet;
import dk.sdu.imada.jlumina.core.util.MatrixUtil;

public class FunctionNormalization implements Normalization, Runnable{

	RGSet data;
	ReadControlProbe controlProbes;
	ReadManifest manifest;

	@Override
	public void performNormalization(RGSet methylationData, AbstractManifest manifest, char[] gender, int nt)
			throws OutOfMemoryError {

		this.data = methylationData;

		HashMap<Integer, float[]> greenControlQuantiles = getGreenControlQuantiles();
		HashMap<Integer, float[]> redControlQuantiles = getRedControlQuantiles();

		float [][] oobGreen = getPercentile(methylationData.getGreenSet(), "Red");
		float [][] oobRed = getPercentile(methylationData.getRedSet(), "Grn");

		USet uSet = new USet(methylationData, this.manifest);
		MSet mSet = new MSet(methylationData, this.manifest);
		uSet.loadData();
		mSet.loadData();

		HashMap<String, float[]> cn = MatrixUtil.getCN(uSet, mSet, this.manifest);
		mSet = null; uSet = null; System.gc();


		/* From the manifest split up this probes
			1 - probeType == "IGrn" & autosomal,
            2 - probeType == "IRed" & autosomal,
            3 - probeType == "II" & autosomal
            4 - "chrX",
            5 - "chrY"
		 */
		//Bisulfite conversion extraction for probe type II:
		getControlMatrix(greenControlQuantiles, redControlQuantiles, oobGreen, oobRed);
	}

	private float[][] getPercentile(HashMap<Integer, float[]> data, String color) {
		
		ArrayList<double[]> matrix = new ArrayList<>();
		
		ArrayList<float[]> matrix2 = new ArrayList<>();
		for (CpG c : this.manifest.getCpgList()) {
			//IRed or IGrn
			if (c.getInifniumType().equals("I") && c.getColorChannel().equals(color)) {
					matrix2.add(data.get(c.getAddressB()));	
					matrix2.add(data.get(c.getAddressA()));
			}
		}
		
		///quantiles by column:
		Percentile p = new Percentile();
		for (int col = 0; col < matrix2.get(0).length; col++) {
			float[]colSample = new float[matrix2.size()];
			for (int row = 0; row < matrix2.size(); row++) {
				colSample[row] = matrix2.get(row)[col];
			}

			double[] elementsQ = new double[3];
			p.setQuantile(1);
			elementsQ[0] = p.evaluate(MatrixUtil.toDouble(colSample));
			p.setQuantile(50);
			elementsQ[1] = p.evaluate(MatrixUtil.toDouble(colSample));
			p.setQuantile(99);
			elementsQ[2] = p.evaluate(MatrixUtil.toDouble(colSample));
			
			matrix.add(elementsQ);
		}
		
		float [][] floatMatrix= new float[matrix.size()][3];
		int j = 0;
		for (double[] m : matrix) {
			for (int i = 0; i < 3; i++) {
				floatMatrix[j][i] = (float)m[i];
			}
			j++;
			
		}
		matrix = null; matrix2 = null; System.gc();
		
		return floatMatrix;
	}

	public float[][] transposeMatrix(float [][] m){
		float[][] temp = new float[m[0].length][m.length];
		for (int i = 0; i < m.length; i++)
			for (int j = 0; j < m[0].length; j++)
				temp[j][i] = m[i][j];
		return temp;
	}

	//buildControlMatrix
	private float[][] getControlMatrix(HashMap<Integer, float[]> grnQuantiles, HashMap<Integer,
			float[]> redQuantiles, float[][]oobGrn, float[][]oobRed) {

		//don't use restoration probe type....

		ArrayList<float[]> currentRed = new ArrayList<>();
		ArrayList<float[]> currentGrn = new ArrayList<>();

		//## Bisulfite conversion extraction for probe type II:
		for (Control c : controlProbes.getControlList()) {
			if (c.getType().equals("BISULFITE CONVERSION II")) {
				currentRed.add(redQuantiles.get(c.getAddress()));
			}
		}		

		float bs2[] = getColMean(currentRed);
		//## Bisulfite conversion extraction for probe type I:
		currentRed = new ArrayList<>();
		for (Control c : controlProbes.getControlList()) {

			if (c.getType().equals("BISULFITE CONVERSION I")) { 

				if (c.getExtendedType().equals("BS Conversion I C1") || 
						c.getExtendedType().equals("BS Conversion I-C2") ||
						c.getExtendedType().equals("BS Conversion I-C3")) {
					currentGrn.add(grnQuantiles.get(c.getAddress()));	

				}else if (c.getExtendedType().equals("BS Conversion I-C4") || 
						c.getExtendedType().equals("BS Conversion I-C5") ||
						c.getExtendedType().equals("BS Conversion I-C6")) {

					currentRed.add(redQuantiles.get(c.getAddress()));					
				}
			}
		}

		float bs1[] = getColMean(currentRed, currentGrn);

		// Staining 
		currentRed = new ArrayList<>();
		currentGrn = new ArrayList<>();
		for (Control c : controlProbes.getControlList()) {
			if (c.getType().equals("STAINING")) {

				if (c.getExtendedType().equals("Biotin (High)")) {
					currentGrn.add(grnQuantiles.get(c.getAddress()));
				}else if (c.getExtendedType().equals("DNP (High)")) {
					currentRed.add(redQuantiles.get(c.getAddress()));
				}
			}
		}
		float stainRed[] = getColMean(currentRed);
		float stainGreen[] = getColMean(currentGrn);

		//## Extension
		currentRed = new ArrayList<>();
		currentGrn = new ArrayList<>();
		for (Control c : controlProbes.getControlList()) {
			if (c.getType().equals("EXTENSION")) {

				if (c.getExtendedType().equals("Extension (A)") || c.getExtendedType().equals("Extension (T)")) {
					currentRed.add(redQuantiles.get(c.getAddress()));
				}else if (c.getExtendedType().equals("Extension (C)") || c.getExtendedType().equals("Extension (G)")) {
					currentGrn.add(grnQuantiles.get(c.getAddress()));
				}
			}
		}
		float [][]extensionGrn = getAsMatrix(currentGrn);
		float [][]extensionRed = getAsMatrix(currentRed);

		//	Hybridization should be monitored only in the green channel
		currentRed = new ArrayList<>();
		currentGrn = new ArrayList<>();
		for (Control c : controlProbes.getControlList()) {
			if (c.getType().equals("HYBRIDIZATION")) {
				currentGrn.add(grnQuantiles.get(c.getAddress()));
			}
		}
		//don't use the mean!!
		float[][] hybGreen = getAsMatrix(currentGrn);


		//Hybridization should be monitored only in the green channel
		currentRed = new ArrayList<>();
		currentGrn = new ArrayList<>();
		for (Control c : controlProbes.getControlList()) {
			if (c.getType().equals("TARGET REMOVAL")) {
				currentGrn.add(grnQuantiles.get(c.getAddress()));
			}
		}
		float [][]targetRem = getAsMatrix(currentGrn);

		// Non-polymorphic probes
		currentRed = new ArrayList<>();
		currentGrn = new ArrayList<>();
		for (Control c : controlProbes.getControlList()) {
			if (c.getType().equals("NON-POLYMORPHIC")) {

				if (c.getExtendedType().equals("NP (A)") || c.getExtendedType().equals("NP (T)")) {
					currentRed.add(redQuantiles.get(c.getAddress()));
				}else if (c.getExtendedType().equals("NP (C)") || c.getExtendedType().equals("NP (G)")) {
					currentGrn.add(grnQuantiles.get(c.getAddress()));
				}
			}
		}
		float[][] nonPolyRed = getAsMatrix(currentRed);
		float[][] nonPolyGrn = getAsMatrix(currentGrn);
		//don't use the mean!!

		// Specificity II
		currentRed = new ArrayList<>();
		currentGrn = new ArrayList<>();
		for (Control c : controlProbes.getControlList()) {
			if (c.getType().equals("SPECIFICITY II")) {
				currentGrn.add(grnQuantiles.get(c.getAddress()));
				currentRed.add(redQuantiles.get(c.getAddress()));					
			}
		}
		float specIIratio[] = getRatio(currentGrn, currentRed);

		// Specificity I
		currentRed = new ArrayList<>();
		currentGrn = new ArrayList<>();
		for (Control c : controlProbes.getControlList()) {
			if (c.getType().equals("SPECIFICITY I")) {
				if (c.getExtendedType().equals("GT Mismatch 1 (PM)") ||
						c.getExtendedType().equals("GT Mismatch 2 (PM)") ||
						c.getExtendedType().equals("GT Mismatch 3 (PM)")) {
					currentGrn.add(grnQuantiles.get(c.getAddress()));
					currentRed.add(redQuantiles.get(c.getAddress()));					
				}
			}
		}

		float specIratio1[] = getRatio(currentRed, currentGrn);

		currentRed = new ArrayList<>();
		currentGrn = new ArrayList<>();
		for (Control c : controlProbes.getControlList()) {
			if (c.getType().equals("SPECIFICITY I")) {
				if (c.getExtendedType().equals("GT Mismatch 4 (PM)") ||
						c.getExtendedType().equals("GT Mismatch 5 (PM)") ||
						c.getExtendedType().equals("GT Mismatch 6 (PM)")) {
					currentGrn.add(grnQuantiles.get(c.getAddress()));
					currentRed.add(redQuantiles.get(c.getAddress()));					
				}
			}
		}
		float specIratio2[] = getRatio(currentGrn, currentRed);
		float specIratio [] = new float[specIratio1.length];
		for (int i = 0; i < specIratio.length; i++) {
			specIratio[i] = (specIratio1[i] + specIratio2[i] )/2.f;
		}

		currentRed = new ArrayList<>();
		currentGrn = new ArrayList<>();
		for (Control c : controlProbes.getControlList()) {
			if (c.getType().equals("NORM_A")) {
				currentRed.add(redQuantiles.get(c.getAddress()));					
			}
		}
		float[] normA = getColMean(currentRed);

		currentRed = new ArrayList<>();
		currentGrn = new ArrayList<>();
		for (Control c : controlProbes.getControlList()) {
			if (c.getType().equals("NORM_T")) {
				currentRed.add(redQuantiles.get(c.getAddress()));					
			}
		}
		float[] normT = getColMean(currentRed);

		currentRed = new ArrayList<>();
		currentGrn = new ArrayList<>();
		for (Control c : controlProbes.getControlList()) {
			if (c.getType().equals("NORM_C")) {
				currentGrn.add(grnQuantiles.get(c.getAddress()));					
			}
		}
		float[] normC = getColMean(currentGrn);

		currentRed = new ArrayList<>();
		currentGrn = new ArrayList<>();
		for (Control c : controlProbes.getControlList()) {
			if (c.getType().equals("NORM_G")) {
				currentGrn.add(grnQuantiles.get(c.getAddress()));					
			}
		}
		float[] normG = getColMean(currentGrn);

		// . dyebias
		float dyebias [] = new float [normA.length];//<- (normC + normG)/(normA + normT)
		for (int i = 0; i < dyebias.length; i++) {
			dyebias[i] = (normC[i] + normG[i]) / (normA[i] + normT[i]);
		}
		return null;
	}

	private float[] getRatio(ArrayList<float[]> green, ArrayList<float[]> red ) {

		float [] meanG = getColMean(green);
		float [] meanR = getColMean(red);

		float[] ratio = new float[meanG.length];

		for (int i = 0; i < meanG.length; i++) {
			ratio[i] = meanG[i]/meanR[i];
		}

		return ratio;
	}

	private float[][] getAsMatrix(ArrayList<float[] >matrix) {

		float[][] m = new float[matrix.size()][];

		int i = 0;
		for (float[] v : matrix) {
			m[i++] = v;
		}

		return m;
	}



	private float[] getColMean(ArrayList<float[] >matrix) {

		float[][] m = new float[matrix.size()][];

		int i = 0;
		for (float[] v : matrix) {
			m[i++] = v;
		}

		float meanArray[] = new float[m[0].length];
		for(int col = 0; col < m[0].length; col++) {
			float mean = 0.f;
			for (int row = 0; row < m.length; row++) {
				mean+=m[row][col]/m.length;
			}
			meanArray[col] = mean;
		}
		return meanArray;
	}

	private float[] getColMean(ArrayList<float[] >matrix1, ArrayList<float[] >matrix2) {


		ArrayList<float[] >matrixSum = new ArrayList<>();

		for (int row = 0; row < matrix1.size(); row++) {

			float[] row1 = matrix1.get(row);
			float[] row2 = matrix2.get(row);

			float sum[] = new float[row1.length];
			for (int col = 0; col < row1.length; col++) {
				sum[col] = row1[col] + row2[col]; 
			}
			matrixSum.add(sum);
		}

		float[][] m = new float[matrixSum.size()][];

		int i = 0;
		for (float[] v : matrixSum) {
			m[i++] = v;
		}

		float meanArray[] = new float[m[0].length];
		for(int col = 0; col < m[0].length; col++) {
			float mean = 0.f;
			for (int row = 0; row < m.length; row++) {
				mean+=m[row][col]/m.length;
			}
			meanArray[col] = mean;
		}
		return meanArray;
	}

	/*private float[] getColMeansByControlProbeType(String type, String color, HashMap<Integer, float[]> data) {

		//BISULFITE CONVERSION II
		ArrayList<float[]> values = new ArrayList<>();
		for (Control c : this.controlProbes.getControlList()) {
			if (c.getType().equals(type) && c.getColor().equals(color)) {
				values.add(data.get(c.getAddress()));
			}
		}

		float matrix[][] = new float[values.size()][];

		int i = 0;
		for (float r[] : values) {
			matrix[i++] = r;
		}

		float[] means = new float[matrix[0].length];

		for (int col = 0; col < matrix[0].length; col++) {
			float columnMean = 0.f;
			for (int row = 0; row < matrix.length; row++) {
				columnMean+=(matrix[row][col]/matrix.length);
			}

			means[col] = columnMean;
		}

		return means;
	}*/

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
	public void run() {
		// TODO Auto-generated method stub
	}

	@Override
	public void setControlData(ReadControlProbe controlProbes) {
		this.controlProbes = controlProbes;
	}

	@Override
	public void setManifest(ReadManifest manifest) {
		this.manifest = manifest;
	}


	private HashMap<Integer, float[]> getGreenControlQuantiles() {

		HashMap<Integer, float[]> controlData = new HashMap<>();

		for (Control c : this.controlProbes.getControlList()) {
			float[] g = this.data.getGreenSet().get(c.getAddress());
			controlData.put(c.getAddress(), g);
		}

		return controlData;
	}

	private HashMap<Integer, float[]> getRedControlQuantiles() {

		HashMap<Integer, float[]> controlData = new HashMap<>();

		for (Control c : this.controlProbes.getControlList()) {
			float[] r = this.data.getRedSet().get(c.getAddress());
			controlData.put(c.getAddress(), r);
		}

		return controlData;
	}
}

package dk.sdu.imada.jlumina.core.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.ArrayUtils;

import dk.sdu.imada.jlumina.core.io.ReadManifest;
import dk.sdu.imada.jlumina.core.primitives.CpG;
import dk.sdu.imada.jlumina.core.primitives.MSet;
import dk.sdu.imada.jlumina.core.primitives.MethylationData;
import dk.sdu.imada.jlumina.core.primitives.USet;

public class MatrixUtil {

	public static float[] getColumnArray(float [][]m, int col) {

		float column[] = new float[m.length];

		for (int i = 0; i < m.length; i++) {
			column[i] = m[i][col];
		}
		return column;
	}

	public static HashMap<String, float[]> getCN(USet uSet, MSet mSet, ReadManifest manifest) throws OutOfMemoryError {

		HashMap<String, float[]> cn = new HashMap<>();

		for (String cpg : manifest.getCpGsIDs()) {

			float [] u = uSet.getData().get(cpg) ;
			float [] m = mSet.getData().get(cpg) ;

			float r[] = new float[u.length];

			for (int  i = 0; i < u.length; i++) {
				r[i] = (float) (Math.log(m[i]+u[i])/Math.log(2));
			}
			cn.put(cpg, r);
		}
		return cn;
	}

	public static HashMap<String, float[]> getBeta(USet uSet, MSet mSet, 
			ReadManifest manifest, float offset)  throws OutOfMemoryError {

		HashMap<String, float[]> betaValues = new HashMap<String, float[]>();

		for (CpG cpg : manifest.getCpgList() ) {

			try {

				float unmethylated[] = uSet.getData().get(cpg.getCpgName());

				float methylated[] = mSet.getData().get(cpg.getCpgName());

				int lenght = unmethylated.length;

				float beta[] = new float[lenght];

				for (int i = 0; i < lenght; i++) {
					beta[i] = (float) methylated[i] / ((float) methylated[i] + (float) unmethylated[i] + offset);
				}

				betaValues.put(cpg.getCpgName(), beta);

			}catch(NullPointerException e) {

			}
		}	
		return betaValues;
	} 

	public static HashMap<String, float[]> getBeta(HashMap<String, float[]> unmethylated, 
			HashMap<String, float[]> methylated, ReadManifest manifest, float offset)  throws OutOfMemoryError{

		HashMap<String, float[]> betaValues = new HashMap<String, float[]>();

		for (String cpg : manifest.getCpGsIDs()) {

			try {

				float m[] = methylated.get(cpg);
				float u[] = unmethylated.get(cpg);
				float b[] = new float[m.length];

				for (int i = 0; i < b.length; i++) {
					b[i] = m[i] / (m[i] + u[i] + offset);
				}

				betaValues.put(cpg, b);

			}catch(NullPointerException e) {
			}
		}
		return betaValues;
	}

	public static float [][] getBetaAsMatrix(HashMap<String, float[]> unmethylated, 
			HashMap<String, float[]> methylated, ReadManifest manifest, float offset)  throws OutOfMemoryError{

		float[][] betaValues = new float[unmethylated.size()][];

		int index = 0;
		for (String cpg : manifest.getCpGsIDs()) {

			try {

				float m[] = methylated.get(cpg);
				float u[] = unmethylated.get(cpg);
				float b[] = new float[m.length];

				for (int i = 0; i < b.length; i++) {
					b[i] = m[i] / (m[i] + u[i] + offset);
				}

				betaValues[index++] = b;
				//betaValues.put(cpg, b);

			}catch(NullPointerException e) {
				System.out.println(cpg);
			}
		}
		return betaValues;
	}

	public static float[][] getBeta(float[][] unmethylated, float [][] methylated, float offset)  throws OutOfMemoryError {

		float beta[][] = new float[unmethylated.length][methylated[0].length];

		for (int i = 0; i < beta.length; i++) {
			for (int j = 0; j < beta[0].length; j++) {
				beta[i][j] = methylated[i][j] / (methylated[i][j] + unmethylated[i][j] + offset);
			}
		}
		return beta;
	}

	public static void writeData(float m [][], String output)  throws OutOfMemoryError {
		
		DecimalFormat numberFormatter = new DecimalFormat("#0.00000");
		
		try {
			File file = new File(output);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for (float v [] : m) {
				String line = "";
				int index = 0;
				for (float d : v) {
					if (index == v.length-1) {
						line += numberFormatter.format(d) + "\n";
					}else {
						line += numberFormatter.format(d) + ",";
					}
					index++;
				}
				bw.write(line);
			}

			bw.close();
		}catch(IOException e ){
		}
	}

	public static void writeArray(float m [], String output) {
		try {
			File file = new File(output);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for (float v : m) {
				bw.write(v + "\n");
			}

			bw.close();
		}catch(IOException e ){
		}
	}


	public static void writeArray(ArrayList<Float> m, String output) {
		try {
			File file = new File(output);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for (float v : m) {
				bw.write(v + "\n");
			}

			bw.close();
		}catch(IOException e ){
		}
	}

	public static void writeData(HashMap<String, float[]> map, String output) {

		try {
			File file = new File(output);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			for (String key : map.keySet()) {
				String line = key+",";
				int i = 0;
				float [] values = map.get(key); 
				for (float v : values) {
					if (i==values.length - 1) {
						line += v + "\n";
					}else {
						line += v + ",";
					}
					i++;
				}
				bw.write(line);
			}
			bw.close();
		}catch(IOException e ){

		}
	}

	public static HashMap<String, float[]> combineDataSets(MethylationData set1,
			MethylationData set2, ReadManifest manifest) throws OutOfMemoryError {

		HashMap<String, float[]> map = new HashMap<>();
		for (String cpg : manifest.getCpGsIDs()) {

			float v1 [] = set1.getData().get(cpg);
			float v2 [] = set2.getData().get(cpg);
			map.put(cpg, ArrayUtils.addAll(v1, v2));
			
		}
		return map;
	}

	public static float[][] transformToMatrix(HashMap<String, float[]> data, ReadManifest manifest) {

		float matrix[][] = new float[data.size()][];

		int i = 0;
		for (String cpg : manifest.getCpGsIDs()) {
			float [] d = data.get(cpg);
			matrix[i++] = d;
		}
		return matrix;
	}

	public static HashMap<String, float[]> transformToMap(float data[][], ReadManifest manifest) {

		int size = data.length;
		HashMap<String, float[]> map = new HashMap<>();
		for (int i = 0 ; i < size; i++) {
			map.put(manifest.getCpGsIDs()[i], data[i]);
		}
		return map;
	}

	public static float[][] getSubSetFromMatrix(float[][] data, int[] indexes) {

		float subSet[][] = new float[data.length][];

		int ncols = indexes.length;

		int r = 0;
		for (float v[] : data) {
			float subRow [] = new float[ncols];
			int i = 0;
			for (int c : indexes) {
				subRow[i++] = v[c];
			}
			subSet[r++] = subRow;
		}
		return subSet;
	}

	public static float[][] getSubSetFromMap(HashMap<String, float[]> data, String[] cpgs, int[] indexes) {

		float matrix[][] = new float[cpgs.length][];

		int ncol = indexes.length;
		int row = 0;

		for (String key : cpgs) {

			float values[] = data.get(key);

			int col = 0;
			float newValues[] = new float[ncol];

			for (int i : indexes) {
				newValues[col++] = values[i];
			}
			matrix[row++] = newValues;
		}

		return matrix;
	}

	public static float[][] createDiagonalMatrix(int n) {
		float[][] d = new float[n][n];
		for (int i = 0; i < n ;i ++) {
			for (int j = 0; j < n ; j++) {
				if (i == j) {
					d[i][j] = (float) 1.0;
				}
			}
		}
		return d;
	}
	
	public static float[][] toFloat(double m[][]) {
		float[][] m2 = new float[m.length][m[0].length];

		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[0].length; j++) {
				m2[i][j] = (float) m[i][j];
			}
		}

		return m2;
	}


	public static float[] toFloat(double m[]) {
		float[] m2 = new float[m.length];

		for (int i = 0; i < m.length; i++) {
			m2[i] = (float) m[i];
		}

		return m2;
	}


	public static double[][] toDouble(float m[][]) {
		double[][] m2 = new double[m.length][m[0].length];

		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[0].length; j++) {
				m2[i][j] = m[i][j];
			}
		}

		return m2;
	}


	public static double[] toDouble(float m[]) {
		double[] m2 = new double[m.length];

		for (int i = 0; i < m.length; i++) {
			m2[i] = m[i];
		}

		return m2;
	}

}
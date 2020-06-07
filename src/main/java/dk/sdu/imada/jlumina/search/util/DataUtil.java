package dk.sdu.imada.jlumina.search.util;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import au.com.bytecode.opencsv.CSVReader;
import dk.sdu.imada.jlumina.core.util.CSVUtil;
import dk.sdu.imada.jlumina.search.primitives.CPGResult;

public class DataUtil {

	// . 
	public static float [][] getMatrixCopy(int begin, int end, int nrow, float matrix[][]) {

		float m[][] = new float[nrow][matrix[0].length];

		int index = 0;
		for (int i = begin; i < end; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				m[index][j] = new Float(matrix[i][j]);
			}
			index++;
		}

		return m;
	}

	// . 
	public static  ArrayList<Integer> getRowStartPoints(int nt, float matrix[][]) {

		int division = matrix.length/nt;

		ArrayList<Integer> startPoints = new ArrayList<Integer>();

		int aux = 0;
		for (int i = 0; i < nt; i++) {

			startPoints.add(aux);

			aux += division; 
		}

		return startPoints;
	}
	
	public static  ArrayList<Integer> getStartPoints(int nt, int matrix) {

		int division = matrix/nt;

		ArrayList<Integer> startPoints = new ArrayList<Integer>();

		int aux = 0;
		for (int i = 0; i < nt; i++) {

			startPoints.add(aux);

			aux += division; 
		}

		return startPoints;
	}
	
	public static ArrayList<Integer> getRowEndPoints(int total, ArrayList<Integer> startPoints, int numThreads) {

		ArrayList<Integer> endPoints = new ArrayList<>();
		
		int nRows = total/numThreads;

		int lastPoint = startPoints.get(startPoints.size()-1);
		for (Integer p : startPoints) {
			int begin = p;
			int end = p + nRows;

			if (p == lastPoint) {
				end = total;
				nRows = end - begin;
			}

			endPoints.add(end);
		}
		return endPoints;
	}
	
	// . 
	public static  ArrayList<Integer> getColumnStartPoints(int nt, float matrix[][]) {

		int division = matrix[0].length/nt;

		ArrayList<Integer> startPoints = new ArrayList<Integer>();

		int aux = 0;
		for (int i = 0; i < nt; i++) {

			startPoints.add(aux);

			aux += division; 
		}

		return startPoints;
	}

	// . 
	public static  double [][] getData(String path, int skipLine, boolean rowName){
		int row = 0;

		if (rowName) {
			row = 1;
		}

		int numColumns = CSVUtil.countColumn(path, skipLine) - row;

		int numRows = CSVUtil.countRows(path, skipLine);

		double [][] phenotypeMatrix = new double[numRows][numColumns];

		CSVReader clinicalDataReader;
		try {
			clinicalDataReader = new CSVReader(new FileReader(path));


			//skip lines 
			for (int i = 0; i < skipLine; i++)
				clinicalDataReader.readNext();

			int i = 0;

			String [] nextLine;

			while ((nextLine = clinicalDataReader.readNext()) != null) {
				for (int j = row; j < numColumns + row; j++) {
					phenotypeMatrix[i][j- row] = Double.parseDouble(nextLine[j]);
				}
				i++;
			}
			clinicalDataReader.close();
		}catch (IOException e) {
			System.out.println("Can't read the file");
			return null;
		}catch (NumberFormatException e) {
			System.out.println("Be sure that your data is described by numbers only!");
			return null;
		}

		return phenotypeMatrix;
	}

	// . 
	public static Integer getSplitPoint(float[][] labels) {

		HashSet<String> uniqueValue = new HashSet<String>();

		for (int i = 0; i < labels.length; i++) {
			uniqueValue.add(""+labels[i][0]);
		}

		if (uniqueValue.size() > 2) {
			System.out.println("Something is wrong with your label. It has more than 2 values");
			return null;
		}

		String element="";
		for (String e : uniqueValue) {
			element=e;
		}

		int splitPoint = 0;
		for (int i = 1; i < labels.length; i++) {
			if ((""+labels[i][0]).equals(element)) {
				splitPoint = i;
				break;
			}
		}
		return splitPoint;
	}

	// . 
	public static ArrayList<float[][]> getMatrices(float beta[][], ArrayList<Integer> startPoints, int numThreads) {

		ArrayList<float[][]> matrices = new ArrayList<float[][]>();
		int nRows = beta.length/numThreads;

		int lastPoint = startPoints.get(startPoints.size()-1);
		for (Integer p : startPoints) {
			int begin = p;
			int end = p + nRows;

			if (p == lastPoint) {
				end = beta.length;
				nRows = end - begin;
			}

			matrices.add(DataUtil.getMatrixCopy(begin, end, nRows, beta));
		}
		return matrices;
	}
	
	
	public static ArrayList<Integer> getRowEndPoints(float beta[][], ArrayList<Integer> startPoints, int numThreads) {

		ArrayList<Integer> endPoints = new ArrayList<>();
		
		int nRows = beta.length/numThreads;

		int lastPoint = startPoints.get(startPoints.size()-1);
		for (Integer p : startPoints) {
			int begin = p;
			int end = p + nRows;

			if (p == lastPoint) {
				end = beta.length;
				nRows = end - begin;
			}

			endPoints.add(end);
		}
		return endPoints;
	}
	
	public static ArrayList<Integer> getColumnEndPoints(float beta[][], ArrayList<Integer> startPoints, int numThreads) {

		ArrayList<Integer> endPoints = new ArrayList<>();
		
		int nCols = beta[0].length/numThreads;

		int lastPoint = startPoints.get(startPoints.size()-1);
		for (Integer p : startPoints) {
			int begin = p;
			int end = p + nCols;

			if (p == lastPoint) {
				end = beta[0].length;
				nCols = end - begin;
			}

			endPoints.add(end);
		}
		return endPoints;
	}
	
	public static int[] getBinaryMethylationData(ArrayList<CPGResult> cpgResults, double cutoff, int col) {
		
		int [] binaryArray = new int[cpgResults.size()];
		
		for (int i = 0 ; i < binaryArray.length; i++) {
			if (cpgResults.get(i).pvalueList[col] <= cutoff) {
				binaryArray[i] = 1;
			}
		}
		
		return binaryArray;
	}
	
	  
	/**
	 * Shuffle an given double array
	 * @param ar 
	 */
	public static void shuffleArray(float[] ar) {
	    Random rnd = new Random();
	    
	    for (int i = ar.length - 1; i > 0; i--) {
	    	
	      int index = rnd.nextInt(i + 1);
	      // Simple swap
	      float a = ar[index];
	      ar[index] = ar[i];
	      ar[i] = a;
	      
	    }
	}
	
	/**
	 * Shuffle a given int array
	 * @param ar
	 */
	public static void shuffleArray(int[] ar) {
	    Random rnd = new Random();
	    
	    for (int i = ar.length - 1; i > 0; i--) {
	    	
	      int index = rnd.nextInt(i + 1);
	      // Simple swap
	      int a = ar[index];
	      ar[index] = ar[i];
	      ar[i] = a;
	    }
	}
}

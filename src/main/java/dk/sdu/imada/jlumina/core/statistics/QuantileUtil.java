package dk.sdu.imada.jlumina.core.statistics;

import java.util.Arrays;
import java.util.Comparator;

public class QuantileUtil {

	/*private static int[] getRanking(double column[]) {

		double columnAux[] = new double[column.length];
		System.arraycopy(column, 0, columnAux, 0, column.length);

		Arrays.sort(columnAux);
		TreeSet<Integer> treeSet = new TreeSet<Integer>();
		int[] indexes = new int[column.length];
		int i = 0;
		HashMap<Integer, Integer> hashMap = new HashMap<Integer, Integer>();

		for (double v : column) {

			int r = Arrays.binarySearch(columnAux, v);

			boolean conditionBegin = true;

			while (conditionBegin && r > 0) {
				if (columnAux[r - 1] == v) {
					r--;
				} else {
					conditionBegin = false;
				}
			}

			if (hashMap.containsKey(r)) {
				hashMap.put(r, hashMap.get(r) + 1);
			} else {
				hashMap.put(r, 0);
			}
			indexes[i] = r + hashMap.get(r);
			treeSet.add(r);
			i++;
		}

		return indexes;
	}

	
	 * static void get_ranks(double rank[], dataitem x[], int n){ int i,j,k;
	 * 
	 * i = 0;
	 * 
	 * while (i < n) { j = i; while ((j < n - 1) && (x[j].data == x[j +
	 * 1].data)) j++; if (i != j) { for (k = i; k <= j; k++) rank[k] = (i + j +
	 * 2) / 2.0; } else rank[i] = i + 1; i = j + 1; } return rank; }
	 

	private static double[] getRanking2(double column[]) {

		double columnAux[] = new double[column.length];
		System.arraycopy(column, 0, columnAux, 0, column.length);
		Arrays.sort(columnAux);

		double rank[] = new double[column.length];

		int i, j, k;

		i = 0;

		int n = column.length;

		while (i < n) {
			j = i;
			while ((j < n - 1) && (column[j] == column[j + 1]))
				j++;
			if (i != j) {
				for (k = i; k <= j; k++)
					rank[k] = (i + j + 2) / 2.0;
			} else
				rank[i] = i + 1;
			i = j + 1;
		}
		return rank;
	}

	public static double[][] getRankingMatrix2(int matrix[][]) {

		double[][] ranking = new double[matrix.length][matrix[0].length];

		for (int j = 0; j < ranking[0].length; j++) {
			double[] column = new double[ranking.length];
			for (int i = 0; i < ranking.length; i++) {
				column[i] = matrix[i][j];
			}

			double[] columnRank = getRanking2(column);

			for (int i = 0; i < ranking.length; i++) {
				ranking[i][j] = columnRank[i];
			}
		}
		return ranking;
	}

	public static int[][] getRankingMatrix(int matrix[][]) {

		int[][] ranking = new int[matrix.length][matrix[0].length];

		for (int j = 0; j < ranking[0].length; j++) {
			double[] column = new double[ranking.length];
			for (int i = 0; i < ranking.length; i++) {
				column[i] = matrix[i][j];
			}

			int[] columnRank = getRanking(column);

			for (int i = 0; i < ranking.length; i++) {
				ranking[i][j] = columnRank[i];
			}
		}
		return ranking;
	}

	public static double[][] getRankedValues(int[][] matrix) {
		double[][] rankedMatrix = new double[matrix.length][matrix[0].length];

		for (int j = 0; j < rankedMatrix[0].length; j++) {

			double[] column = new double[rankedMatrix.length];
			for (int i = 0; i < rankedMatrix.length; i++) {
				column[i] = matrix[i][j];
			}

			Arrays.sort(column);
			for (int i = 0; i < rankedMatrix.length; i++) {
				rankedMatrix[i][j] = column[i];
			}
		}
		return rankedMatrix;
	}

	public static double[] getMean(double[][] matrix) {
		double mean[] = new double[matrix.length];
		TreeSet<Double> set = new TreeSet<Double>();

		int i = 0;
		for (double[] row : matrix) {
			mean[i] = StatUtils.mean(row);
			set.add(mean[i]);
			i++;
		}

		return mean;
	}

	public static double[][] normalizeMatrix(int[][] originalMatrix,
			int[][] rankMatrix, double[] rankedMeans) {
		double normalizedMatrix[][] = new double[originalMatrix.length][originalMatrix[0].length];
		for (int i = 0; i < originalMatrix.length; i++) {
			for (int j = 0; j < originalMatrix[0].length; j++) {
				int index = rankMatrix[i][j];
				normalizedMatrix[i][j] = rankedMeans[index];
			}
		}
		return normalizedMatrix;
	}

	public static double[][] normalizeMatrix(int[][] rankMatrix,
			double[] rankedMeans) {

		double normalizedMatrix[][] = new double[rankMatrix.length][rankMatrix[0].length];

		for (int i = 0; i < rankMatrix.length; i++) {
			for (int j = 0; j < rankMatrix[0].length; j++) {
				normalizedMatrix[i][j] = rankedMeans[rankMatrix[i][j]];
			}
		}
		return normalizedMatrix;
	}*/

	// ===============================================

	public static float[] getRowMeans(float[][] data) {

		float[] ret = new float[data.length];

		for (int col = 0; col < data[0].length; col++) {
			float[] temp = new float[data.length];
			for (int row = 0; row < data.length; row++) {
				temp[row] = data[row][col];
			}
			Arrays.sort(temp);
			for (int row = 0; row < data.length; row++) {
				ret[row] += ((float) temp[row]) / data[0].length;
			}
		}

		return ret;
	}

	public static float[][] getMeanReplaceMAtrix(float[][] data,
			float[] meanVector) {

		float[][] ret = new float[data.length][data[0].length];

		for (int col = 0; col < data[0].length; col++) {
			RankItem[] items = new RankItem[data.length];
			for (int row = 0; row < data.length; row++) {
				items[row] = new RankItem(row, data[row][col]);
			}

			Arrays.sort(items, new Comparator<RankItem>() {
				@Override
				public int compare(RankItem o1, RankItem o2) {
					return Double.compare(o1.value, o2.value);
				}
			});
			
			float[] ranks = getRanksForRankItems(items);
			
			for (int row = 0; row < data.length; row++) {
				int origIndex = items[row].position;
				if(ranks[row] - (int)ranks[row] > 0.4){
					ret[origIndex][col] = (float) (0.5*(meanVector[((int)ranks[row])-1] + meanVector[((int)ranks[row])])); 
				}else{
					ret[origIndex][col] = meanVector[((int)ranks[row])-1];
				}
			}

		}

		return ret;
	}
	
	public static float[] getRanksForRankItems(RankItem[] items) {
		float[] rank = new float[items.length];

		int j, k, i = 0;

		while (i < items.length) {
			j = i;
			while ((j < items.length - 1)
					&& (items[j].value == items[j + 1].value)) {
				j++;
			}

			if (i != j) {
				for (k = i; k <= j; k++) {
					rank[k] = (float) ((i + j + 2) / 2.0);
				}
			} else {
				rank[i] = i + 1;
			}
			i = j + 1;
		}

		return rank;
	}

}

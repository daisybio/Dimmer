package dk.sdu.imada.jlumina.core.statistics;

import java.util.Arrays;
import java.util.Comparator;

public class FastQuantileUtil {

	float data[][];
	float rowMean[];
	float[][] normalized;
	
	boolean doneReplace, doneRowMean;
	
	int begin; 
	int end;
	
	public FastQuantileUtil(float data[][], float[][] normalized, float rowMean[], int begin, int end) throws OutOfMemoryError{
		this.data = data;
		this.rowMean = rowMean;
		this.begin = begin;
		this.end = end;
		this.normalized = normalized;
		
		doneReplace = false;
		doneRowMean = false;
	}

	public boolean isDoneReplace() {
		return doneReplace;
	}
	
	public boolean isDoneRowMean() {
		return doneRowMean;
	}
	
	public void setRowMean(float[] rowMean) {
		this.rowMean = rowMean;
	}
	
	public float[][] getData() {
		return data;
	}
	
	public synchronized void setRowMeans() throws OutOfMemoryError {
		doneRowMean = false;
		this.rowMean = new float[data.length];
		//System.out.println(data.length);
		for (int col = begin; col < end; col++) {
			float[] temp = new float[data.length];
			for (int row = 0; row < data.length; row++) {
				temp[row] = data[row][col];
			}
			Arrays.sort(temp);
			for (int row = 0; row < data.length; row++) {
				rowMean[row] += ((float) temp[row]) / data[0].length;
			}
		}
		doneRowMean = true;
		notify();
	}
	
	/*public void setRowMean(float[] rowMean) {
		this.rowMean = rowMean;
	}*/
	
	/*public float[] getRowMean() {
		return rowMean;
	}*/

	public synchronized void setMeanReplaceMAtrix() throws OutOfMemoryError {
		doneReplace = false;
		for (int col = begin; col < end; col++) {
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
					normalized[origIndex][col] = (float) (0.5*(rowMean[((int)ranks[row])-1] + rowMean[((int)ranks[row])])); 
				}else{
					normalized[origIndex][col] = rowMean[((int)ranks[row])-1];
				}
			}
		}
		doneReplace = true;
		notify();
	}

	private float[] getRanksForRankItems(RankItem[] items) throws OutOfMemoryError {
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

	public float[][] getNormalized() {
		return normalized;
	}

	public float[] getMean() {
		return rowMean;
	}
}

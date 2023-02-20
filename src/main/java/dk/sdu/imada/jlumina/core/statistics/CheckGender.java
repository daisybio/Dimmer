package dk.sdu.imada.jlumina.core.statistics;

import java.util.HashMap;

import org.apache.commons.math3.stat.descriptive.rank.Median;

import dk.sdu.imada.jlumina.core.io.ReadManifest;
import dk.sdu.imada.jlumina.core.primitives.CpG;
import dk.sdu.imada.jlumina.core.primitives.MSet;
import dk.sdu.imada.jlumina.core.primitives.USet;
import dk.sdu.imada.jlumina.core.util.MatrixUtil;

public class CheckGender {

	USet uSet;
	MSet mSet;
	ReadManifest manifest;
	double cutoff;

	public CheckGender(USet uSet, MSet mSet, ReadManifest manifest, double cutoff) {
		this.uSet = uSet;
		this.mSet = mSet;
		this.manifest = manifest;
		this.cutoff = cutoff;
	}

	public char[] getGender() {

		HashMap<String, float[]> cn = MatrixUtil.getCN(uSet, mSet, manifest);

		float y[][] = new float[manifest.getCpGsByChromosome("Y").length][];
		float x[][] = new float[manifest.getCpGsByChromosome("X").length][];

		int index = 0;
		for (CpG cpg : manifest.getCpGsByChromosome("Y")) {
			y[index++] = cn.get(cpg.getCpgName());
		}

		index = 0;
		for (CpG cpg : manifest.getCpGsByChromosome("X")) {
			x[index++] = cn.get(cpg.getCpgName());
		}

		float yMediansByColumn[] = getMediansByColumn(y);
		float xMediansByColumn[] = getMediansByColumn(x);

		double [] medianDifference = new double[yMediansByColumn.length];

		for (int i = 0; i < yMediansByColumn.length; i++) {
			medianDifference[i] = yMediansByColumn[i] - xMediansByColumn[i];
			//System.out.println(medianDifference[i]);
		}
		
		
		char[] gender = new char[medianDifference.length];
		for (int i = 0; i < medianDifference.length; i++) {
			gender[i] = (medianDifference[i] < cutoff) ? 'F' : 'M';
		}
		
		 /*
		  * TODO k-means implementation for double checking the sex predicition.
		  * The k-means checking should produce the same result in geneder[i]
		  */

		return gender;
	}


	// fine!
	private float[] getMediansByColumn(float[][] matrix) {
		float [] medians = new float[matrix[0].length];

		for (int j = 0; j < medians.length; j++) {
			double column[] = new double[matrix.length];
			for (int i = 0; i < matrix.length; i++) {
				column[i] = matrix[i][j];
			}
			Median median = new Median();
			median.setData(column);
			medians[j] = (float) median.evaluate();
		}
		return medians;
	}
}

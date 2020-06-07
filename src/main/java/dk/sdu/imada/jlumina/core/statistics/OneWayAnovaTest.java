package dk.sdu.imada.jlumina.core.statistics;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.stat.inference.OneWayAnova;

import au.com.bytecode.opencsv.CSVReader;
import dk.sdu.imada.jlumina.core.io.ReadManifest;
import dk.sdu.imada.jlumina.core.util.CSVUtil;

@SuppressWarnings("unused")
public class OneWayAnovaTest {

	public static void main(String args[]) throws IOException {

		ReadManifest manifest = new ReadManifest("/Users/diogo/Desktop/manifest_summary.csv");

		HashMap<String, double[]> map = new HashMap<>();
		int nrows = CSVUtil.countRows("/Users/diogo/Desktop/beta.csv", 1);
		double matrix[][] = new double[nrows][];

		try {
			CSVReader reader = new CSVReader(new FileReader("/Users/diogo/Desktop/beta.csv"));
			reader.readNext();

			for (int i = 0; i < nrows; i++) {

				String data[] = reader.readNext();
				String key = data[0];
				double values[] = new double[data.length - 1];

				for (int j = 1; j < data.length; j++) {
					values[j-1] = Double.parseDouble(data[j]);
					//matrix[i][j-1] = Double.parseDouble(data[j]);;
				}
				matrix[i] = values;
				map.put(key, values);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		double [] pvalues = new double[matrix.length];
		int index = 0;
		OneWayAnova anova = new OneWayAnova();
		for (double []v : matrix) {

			//v[6  7  8 24 25 26]
			double [] cd8 = {v[6], v[7], v[8], v[24], v[25], v[26]};
			// 3  4  5 21 22 23
			double [] cd4 = {v[3], v[4], v[5], v[21], v[22], v[23]};
			// 15 16 17 33 34 35
			double [] nk = {v[15], v[16], v[17], v[33], v[34], v[35]};
			// 9 10 11 27 28 29
			double [] bcell = {v[9], v[10], v[11], v[27], v[28], v[29]};
			// 12 13 14 30 31 32
			double [] mono = {v[12], v[13], v[14], v[30], v[31], v[32]};
			// 0  1  2 18 19 20
			double [] gran = {v[0], v[1], v[2], v[18], v[19], v[20]};
			
			ArrayList<double[]> groups = new ArrayList<>();
			
			groups.add(cd8); groups.add(cd4); groups.add(nk);
			groups.add(bcell); groups.add(mono); groups.add(gran);

			pvalues[index++] = anova.anovaPValue(groups);
			
		}

		/*ArrayList<Double> fstatistic = new ArrayList<>();
		OneWayAnova anova = new OneWayAnova();
		for (String id : manifest.getCpGsIDs()) {

			ArrayList<double[]> groups = new ArrayList<>();

			double v[] = map.get(id);
			//v[6  7  8 24 25 26]
			double [] cd8 = {v[6], v[7], v[8], v[24], v[25], v[26]};
			// 3  4  5 21 22 23
			double [] cd4 = {v[3], v[4], v[5], v[21], v[22], v[23]};
			// 15 16 17 33 34 35
			double [] nk = {v[15], v[16], v[17], v[33], v[34], v[35]};
			// 9 10 11 27 28 29
			double [] bcell = {v[9], v[10], v[11], v[27], v[28], v[29]};
			// 12 13 14 30 31 32
			double [] mono = {v[12], v[13], v[14], v[30], v[31], v[12]};
			 // 0  1  2 18 19 20
			double [] gran = {v[0], v[1], v[2], v[18], v[19], v[20]};

			groups.add(cd8); groups.add(cd4); groups.add(nk);
			groups.add(bcell); groups.add(mono); groups.add(gran);

			fstatistic.add(anova.anovaPValue(groups));

			System.out.println();
		}*/

		//Util.writeArray(fstatistic, "/Users/diogo/Desktop/ftest_pvalues2.csv");
		//Util.writeArray(pvalues, "/Users/diogo/Desktop/ftest_Pvalues.csv");
		System.out.println("ending");
	}

}

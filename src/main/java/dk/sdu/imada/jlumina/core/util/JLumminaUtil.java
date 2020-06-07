package dk.sdu.imada.jlumina.core.util;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVReader;
import dk.sdu.imada.jlumina.core.io.ReadIDAT;
import dk.sdu.imada.jlumina.core.io.ReadManifest;
import dk.sdu.imada.jlumina.core.primitives.MSet;
import dk.sdu.imada.jlumina.core.primitives.RGSet;
import dk.sdu.imada.jlumina.core.primitives.USet;
import dk.sdu.imada.jlumina.core.statistics.QuantileNormalization;

public class JLumminaUtil {

	ReadManifest manifest;
	
	Phenotype phenotype;
	
	String sampleAnnotationFilePath;
	
	HashMap<String, float[]> beta;
	
	String path;

	public void load(String sampleAnnotationFilePath, int numThreads) {

		path = "";
		
		for (int i = 0; i < sampleAnnotationFilePath.split("/").length -1 ; i++) {
			path+= sampleAnnotationFilePath.split("/")[i] + "/";
		}
		
		this.sampleAnnotationFilePath = sampleAnnotationFilePath;

		phenotype = new Phenotype(sampleAnnotationFilePath);

		testDataType();
	}

	boolean isEpic = false;

	private void testDataType() {

		String sentrixPosition =  phenotype.getPhenotypeData().get("Sentrix_Position")[0];

		String sentrixID =  phenotype.getPhenotypeData().get("Sentrix_ID")[0];

		String idatFilePath = path + sentrixID + "/" + sentrixID + "_" + sentrixPosition + "_Grn.idat";

		ReadIDAT gIdat = new ReadIDAT();

		System.out.println(idatFilePath);
		gIdat.readNonEncryptedIDAT(idatFilePath);

		int v = gIdat.getnSNPsRead();

		if (v == 622399) {
			isEpic = false;
			System.out.println("Ilumina 450K");
		}else {	
			isEpic = true;
			System.out.println("Epic 850K");
		}

		setManifest();
	}


	public HashMap<String, float[]> setMethylationSet(float offset) {
		
		RGSet rgSet;
		rgSet = new RGSet(this.sampleAnnotationFilePath);
		rgSet.loadIDATs();
		
		USet uSet = new USet();
		uSet.setManifest(manifest);
		uSet.setRgSet(rgSet);
		uSet.loadData();
		
		MSet mSet = new MSet();
		mSet.setManifest(manifest);
		mSet.setRgSet(rgSet);
		mSet.loadData();
	
		return MatrixUtil.getBeta(uSet, mSet, manifest, offset);
	}

	public HashMap<String, float[]> setMethylationSet(QuantileNormalization quantileNormalization, float offset) {
		
		RGSet rgSet;
		USet uSet;
		MSet mSet;
		
		rgSet = new RGSet(this.sampleAnnotationFilePath);
		rgSet.loadIDATs();
		
		uSet = new USet();
		uSet.setManifest(manifest);
		uSet.setRgSet(rgSet);
		uSet.loadData();
		
		mSet = new MSet();
		mSet.setManifest(manifest);
		mSet.loadData();
		
		quantileNormalization.performNormalization(mSet, manifest, null, 1);
		quantileNormalization.performNormalization(uSet, manifest, null, 1);
	
		return MatrixUtil.getBeta(uSet, mSet, manifest, offset);
	}

	private void setManifest() {

		String mf = "";

		if (isEpic) {

			mf = "resources/epic_manifest.csv";

			if (getClass().getClassLoader().getResourceAsStream(mf)==null) {
				mf = "epic_manifest.csv";
			}

		}else {
			mf = "resources/manifest_summary.csv";

			if (getClass().getClassLoader().getResourceAsStream(mf)==null) {
				mf = "manifest_summary.csv";
			}
		}

		this.manifest = new ReadManifest(mf);

		this.manifest.loadManifest();
	}

	public Phenotype getPhenotype() {
		return phenotype;
	}

	public class Phenotype {

		HashMap<String, String[]> data;

		private CSVReader csvReader;

		public Phenotype(String sampleAnnotatFilFilePath) {

			ArrayList<String[]> rawData = new ArrayList<>();

			data = new HashMap<>();

			try {

				csvReader = new CSVReader(new FileReader(sampleAnnotatFilFilePath));

				String nextLine[] = null;

				while ((nextLine = csvReader.readNext()) != null ) {
					rawData.add(nextLine);
				}

				String colnames[] = rawData.get(0);

				for (int col = 0; col < colnames.length; col++) {

					String[] array = new String[rawData.size() - 1];

					for (int row = 1; row < rawData.size(); row++) {
						array[row-1] = rawData.get(row)[col];
					}
					data.put(colnames[col], array);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public HashMap<String, String[]> getPhenotypeData() {
			return data;
		}
	}

	public ReadManifest getManifest() {
		return manifest;
	}
}

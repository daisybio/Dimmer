package dk.sdu.imada.jlumina.core.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVReader;
import dk.sdu.imada.jlumina.core.util.CSVUtil;

public class Read450KSheet {

	// csv file
	String data[][];
	// directory of the file
	String baseDir;
	//number rows
	int nrows;
	//number columns
	int ncols;

	public Read450KSheet(String inputFile) {

		try {
			CSVReader reader = new CSVReader(new FileReader(inputFile));
			baseDir = new File(inputFile).getParent() + "/";
			String header[] = reader.readNext();

			int condition = 0 ;
			for (String toks : header ){
				if (toks.equals("Sentrix_ID") || toks.equals("Sentrix_Position")) {
					condition++;
				}
			}

			if (condition == 2) {
				this.nrows = CSVUtil.countRows(inputFile, 0);
				this.ncols = header.length;

				data = new String[nrows][ncols];
				data[0] = header;

				for (int i = 1; i < nrows; i++) {
					data[i] = reader.readNext();
				}

				reader.close();
			}else {
				System.err.println("No mandatory fields not found: Sentrix_ID && Sentrix_Position");
			}

		}catch(IOException e) {
			System.err.println("Can't read " + inputFile);
		}
	}

	public String[] getField(String header) {

		try {

			int index = getIndexOfHeader(header);
			String v [] = new String[nrows - 1];

			for (int i = 1; i < nrows; i++) {
				v[i - 1] = data[i][index];
			}

			return v;

		}catch (IndexOutOfBoundsException e) {
			System.err.println("Can't find the column " + header);
		}

		return null;	
	}

	public String[] getBaseName() {

		int id = getIndexOfHeader("Sentrix_ID");
		int position = getIndexOfHeader("Sentrix_Position");

		String [] baseNames = new String[nrows - 1];

		for (int i = 1; i < nrows; i++) {

			String testPath = baseDir + data[i][id] + "_" + data[i][position] + "_Grn.idat";

			if (!new File(testPath).exists()) {
				baseNames[i-1] = baseDir + data[i][id] + "/" + data[i][id] + "_" + data[i][position]; 
			}else {
				baseNames[i-1] = baseDir + data[i][id] + "_" + data[i][position];
			}
			
			//baseNames[i-1] = baseDir + data[i][id] + "_" + data[i][position]; 
		}

		return baseNames;
	}

	private Integer getIndexOfHeader(String header) {

		for (int i = 0 ; i < data[0].length; i++) {
			if (header.equals(data[0][i])) {
				return i;
			}
		}
		return null;
	}

}

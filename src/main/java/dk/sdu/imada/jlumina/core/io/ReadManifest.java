package dk.sdu.imada.jlumina.core.io;

import java.io.IOException;
import java.io.InputStreamReader;

import au.com.bytecode.opencsv.CSVReader;
import dk.sdu.imada.jlumina.core.primitives.CpG;
import dk.sdu.imada.jlumina.core.util.CSVUtil;


/**
 * @author diogo
 * Read the manifest file (CpG information)
 *
 */
public class ReadManifest extends AbstractManifest {

	public ReadManifest(String inputFile) {
		super(inputFile);
	}

	public synchronized void loadManifest() {
		
		progress = 0;
		done = false;
		
		try {
			//int nrows = CSVUtil.countRows(inputFile, 1);
			int nrows = CSVUtil.countRows(getClass().getClassLoader().getResourceAsStream(inputFile), 1);
			
			cpgList = new CpG[nrows];
			//CSVReader reader = new CSVReader(new FileReader(inputFile));
			CSVReader reader = new CSVReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(inputFile)));
			reader.readNext();

			for (int i = 0; i < nrows; i++) {
				String [] line = reader.readNext();

				String field0 = line[0];
				Integer field1, field2, field5, field7;

				try {
					field1 = Integer.parseInt(line[1]);
				}catch(NumberFormatException e) {
					field1 = -1;
				}

				try {
					field2 = Integer.parseInt(line[2]);
				}catch(NumberFormatException e) {
					field2 = -1;
				}

				String field3 = line[3];

				String field4 = line[4];

				try {
					field5 = Integer.parseInt(line[5]);
				}catch(NumberFormatException e) {
					field5 = -1;
				}

				String field6 = line[6];

				try {
					field7 = Integer.parseInt(line[7]);
				}catch(NumberFormatException e) {
					field7 = -1;
				}

				String field8 = line[8];

				String field9 = line[9];

				CpG cpg = new CpG( field0, field1, field2, field3, field4, field5, field6, field7, field8, field9);

				cpgList[i] = cpg;
			}
			reader.close();
		}catch(IOException e) {
			System.err.println("Can't read " + inputFile);
		}
		done = true;
		progress++;
		notify();
	}
}

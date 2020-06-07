package dk.sdu.imada.jlumina.core.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import au.com.bytecode.opencsv.CSVReader;

public class CSVUtil {

	// .
	public static int countRows(String filename, int skip) {

		try {

			InputStream is = new BufferedInputStream(new FileInputStream(filename));

			try {
				byte[] c = new byte[1024];
				int count = 0;
				int readChars = 0;
				boolean empty = true;
				while ((readChars = is.read(c)) != -1) {
					empty = false;
					for (int i = 0; i < readChars; ++i) {
						if (c[i] == '\n') {
							++count;
						}
					}
				}
				return (count == 0 && !empty) ? 1 : (count - skip);
			} finally {
				is.close();
			}
		}catch (IOException e) {
			System.out.println("Error in " + filename + ": I couldn't get the number of rows");
			return 0;
		}catch (Exception e) {
			return 0;
		}
	}
	
	
	public static int countRows(InputStream is, int skip) {

		try {

			try {
				byte[] c = new byte[1024];
				int count = 0;
				int readChars = 0;
				boolean empty = true;
				while ((readChars = is.read(c)) != -1) {
					empty = false;
					for (int i = 0; i < readChars; ++i) {
						if (c[i] == '\n') {
							++count;
						}
					}
				}
				return (count == 0 && !empty) ? 1 : (count - skip);
			} finally {
				is.close();
			}
		}catch (IOException e) {
			System.out.println("Error in  get the number of rows");
			return 0;
		}catch (Exception e) {
			return 0;
		}
	}

	// . 
	public static int countColumn(String filename, int skip) {

		try {

			CSVReader reader = new CSVReader(new FileReader(filename));

			for (int i = 0; i < skip; i++) 
				reader.readNext();

			String[] header = reader.readNext();

			reader.close();

			return header.length;

		}catch (IOException e) {
			System.out.println("Error in " + filename + ": I couldn't get the number of columns");
			return 0;
		}catch (Exception e) {
			return 0;
		}
	}

	// . 
	public static String[] getHeader(String filename) throws IOException, Exception {

		CSVReader reader = new CSVReader(new FileReader(filename), ',');

		String[] header = reader.readNext();

		reader.close();
		
		return header;
	}



	public static void save(String fileName, ArrayList<double[]> parameters) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(new FileOutputStream(fileName));
		for (double[] p : parameters){
			String line = "";
			for (double v : p) {
				line += v + ";";
			}
			line = StringUtils.chomp(line, ";");
			pw.println(line);
		}
		pw.close();
	}

	public static double [][] getData(String path, int skipLine, boolean rowName) throws IOException, NumberFormatException, Exception {

		int row = 0;

		if (rowName) {
			row = 1;
		}

		int numColumns = CSVUtil.countColumn(path, skipLine) - row;

		int numRows = CSVUtil.countRows(path, skipLine);

		double [][] phenotypeMatrix = new double[numRows][numColumns];

		CSVReader clinicalDataReader = new CSVReader(new FileReader(path));

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

		return phenotypeMatrix;
	}

	public static void checkData(String path, int skipLine, boolean rowName) throws IOException, Exception {

		// read the file;
		int row = 0;

		if (rowName) {
			row = 1;
		}

		int numColumns = CSVUtil.countColumn(path, skipLine) - row;

		CSVReader clinicalDataReader = new CSVReader(new FileReader(path));

		//skip lines 
		for (int i = 0; i < skipLine; i++) {
			clinicalDataReader.readNext();
		}

		String [] nextLine;

		while ((nextLine = clinicalDataReader.readNext()) != null) {
			for (int j = row; j < numColumns + row; j++) {
				Double.parseDouble(nextLine[j]);
			}
		}
		clinicalDataReader.close();
	}
}

package dk.sdu.imada.gui.controllers.util;

import java.io.FileReader;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVReader;
import dk.sdu.imada.gui.controllers.FXPopOutMsg;
import dk.sdu.imada.gui.controllers.MainController;
import dk.sdu.imada.jlumina.core.util.CSVUtil;
import javafx.application.Platform;

public class ReadDimmerProjectUtil implements Runnable {

	MainController mainController;
	String input;
	double progress;
	boolean done;

	float original[];
	float emp[];
	float fwer[];
	float fdr[];
	float sdc[];
	float diff[];
	String data [][];
	boolean check;

	public ReadDimmerProjectUtil(MainController mainController, String input) {
		super();
		this.mainController = mainController;
		this.input = input;
		this.progress = 0;
		this.done = false;
		check = true;
	}

	public boolean isCheck() {
		return check;
	}

	public boolean isDone() {
		return done;
	}
	
	public void setProgress(double progress) {
		this.progress = progress;
	}

	private void loadPermutationfile() {

		this.done = false;
		this.progress = 0;

		try {
			int numCols = CSVUtil.countColumn(input, 0);
			int numRows = CSVUtil.countRows(input, 0) - 1;
			this.mainController.loadManifest(numRows);

			data = new String[numRows][numCols - 1];
			CSVReader reader = new CSVReader(new FileReader(input));

			String [] nextLine = reader.readNext();
			int i = 0;

			if (nextLine[0].equals("CPG")) {
				original = new float[numRows];
				emp = new float[numRows];
				fwer = new float[numRows];
				sdc = new float[numRows];
				fdr = new float[numRows];
				diff = new float[numRows];

				while ((nextLine = reader.readNext()) != null) {
					data[i][0] = nextLine[0];
					data[i][1] = nextLine[1];
					data[i][2] = nextLine[2];
					
					original[i] = Float.parseFloat(nextLine[3]);
					emp[i] = Float.parseFloat(nextLine[4]);
					fdr[i] = Float.parseFloat(nextLine[5]);
					fwer[i] = Float.parseFloat(nextLine[6]);
					sdc[i] = Float.parseFloat(nextLine[7]);
					diff[i] = Float.parseFloat(nextLine[8]);
					i++;
					progress = (double) i/(double) numRows;
				}
				reader.close();

			}else {
				check = false;
				Platform.runLater(()->FXPopOutMsg.showWarning("It seems this is not a Dimmer Project File"));
			}

		}catch(IOException e) {
			System.out.println("Error io exception");
			Platform.runLater(()->FXPopOutMsg.showWarning("The file can't be loaded"));
			check = false;
		}catch(NumberFormatException e) {
			System.out.println("Error number exception");
			Platform.runLater(()->FXPopOutMsg.showWarning("The file can't be loaded"));
			check = false;
		} catch (Exception e) {
			System.out.println("other " + e.getMessage());
			Platform.runLater(()->FXPopOutMsg.showWarning("The file can't be loaded"));
			e.printStackTrace();
			check = false;
		}
		done = true;
	}

	public String[][] getData() {
		return data;
	}

	public float[] getOriginal() {
		return original;
	}

	public float[] getEmpirical() {
		return emp;
	}

	public float[] getFwer() {
		return fwer;
	}

	public float[] getSdc() {
		return sdc;
	}

	public float[] getFdr() {
		return fdr;
	}

	public float[] getDiff() {
		return diff;
	}

	public double getProgress() {
		return progress;
	}

	@Override
	public void run() {
		loadPermutationfile();
	}
}

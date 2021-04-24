package dk.sdu.imada.jlumina.core.primitives;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVReader;
import dk.sdu.imada.jlumina.core.util.CSVUtil;

public abstract class MethylationData extends RGSet{

	int progress;
	boolean done;

	public MethylationData() {
		progress = 0;
		done = false;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public boolean isDone() {
		return done;
	}
	
	public synchronized void loadData(String input) throws OutOfMemoryError{

		done = false;

		progress = 0;

		data = new HashMap<>();
		int nrows = CSVUtil.countRows(input, 1);


		try {
			CSVReader reader = new CSVReader(new FileReader(input));
			reader.readNext();
			for (int i = 0; i < nrows; i++) {
				try {
					String next[] = reader.readNext();
					float [] values = new float[next.length - 1];
					for (int j = 1; j < next.length; j++) {
						values[j - 1] = Integer.parseInt(next[j]);
					}
					data.put(next[0], values);
				}catch(NumberFormatException e) {
					e.printStackTrace();
				}catch (NullPointerException e) {
					e.printStackTrace();
				}
				progress++;
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		done = true;
		notify();
	}

	/*public synchronized void loadData(String input) {

		done = false;

		progress = 0;

		data = new HashMap<>();

		int nrows = CSVUtil.countRows(input, 1);

		try {
			CSVReader reader = new CSVReader(new FileReader(input));
			reader.readNext();

			for (int i = 0; i < nrows; i++) {
				try {
					String next[] = reader.readNext();
					float [] values = new float[next.length - 1];
					for (int j = 1; j < next.length; j++) {
						values[j - 1] = Integer.parseInt(next[j]);
					}
					data.put(next[0], values);
				}catch(NumberFormatException e) {
					e.printStackTrace();
				}catch (NullPointerException e) {
					e.printStackTrace();
				}
				progress++;
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		done = true;
		notify();
	}*/

	public int getProgress() {
		return progress;
	}

	public synchronized void checkProgress() {
		while (!done) {
			try {
				wait(1);
			}catch(InterruptedException e) {
			}
		}
	}
}

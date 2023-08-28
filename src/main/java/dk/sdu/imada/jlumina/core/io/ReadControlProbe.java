package dk.sdu.imada.jlumina.core.io;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import au.com.bytecode.opencsv.CSVReader;
import dk.sdu.imada.jlumina.core.primitives.Control;
import dk.sdu.imada.jlumina.core.util.CSVUtil;


public class ReadControlProbe extends AbstractManifest {
	Control[] controlList;

	public ReadControlProbe(String inputFile) {	
		super(inputFile);
	}

	@Override
	public synchronized void loadManifest() {
		System.out.println("Loading control probes...");
		progress = 0;
		done = false;


		try {
			//int nrows = CSVUtil.countRows(inputFile, 1);
			int nrows = CSVUtil.countRows(getClass().getClassLoader().getResourceAsStream(inputFile), 1);

			controlList = new Control[nrows];

			//CSVReader reader = new CSVReader(new FileReader(inputFile));
			CSVReader reader = new CSVReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(inputFile)));
			reader.readNext();


			for (int i = 0; i < nrows; i++) {

				String [] line = reader.readNext();
				Integer field0;

				String field1, field2, field3;

				try {

					field0 = Integer.parseInt(line[0]);
				}catch(NumberFormatException e) {
					field0 = -1;
				}
				field1 = line[1];
				field2 = line[2];
				field3 = line[3];
				Control control = new Control( field0, field1, field2, field3);
				controlList[i] = control;
			}
			reader.close();

		}catch(IOException e) {
			System.err.println("Can't read " + inputFile);
		}
		done = true;
		progress++;
		notify();
	}




	public Control[] getControlList(){
		return controlList;
	}

	public int[] getControlAddress(String controlType, ReadControlProbe control, int asList){
		ArrayList<Integer> list = new ArrayList<Integer>();
		String[] types = controlType.split(", ");
		Control[] clist = control.getControlList();

		if(asList==1){	
			for(int i=0;i<types.length;i++){
				for(int j = 0;j<clist.length;j++){
					String type = clist[j].getType();
					if(types[i].equals(type)){
						list.add(clist[j].getAddress());
					}
				}
			}
		}else{
			for(int i=0;i<types.length;i++){
				for(int j = 0;j<clist.length;j++){
					String type = clist[j].getType();
					if(types[i].equals(type)){
						list.add(clist[j].getAddress());	
					}
				}
			}
		}
		int[] AList = new int[list.size()];
		for(int k=0;k<list.size();k++){
			AList[k]=list.get(k);
		}
		return AList;
	}
}
package dk.sdu.imada.jlumina.core.io;

import java.io.InputStream;
import java.util.ArrayList;

import dk.sdu.imada.jlumina.core.primitives.CpG;

public abstract class AbstractManifest {
	String inputFile;
	InputStream inputStream;
	CpG [] cpgList;
	boolean done;
	int progress;
	
	public AbstractManifest(String inputFile) {
		this.inputFile = inputFile;
	}
	
	public int getProgress() {
		return progress;
	}
	
	public boolean isDone() {
		return done;
	}
	
	public abstract void loadManifest();
	
	public synchronized void checkProgress() {
		while (!done) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public CpG getByAddressA(int addressA) {

		for (CpG cpg : cpgList) {
			if (addressA == cpg.getAddressA()) {
				return cpg;
			}
		}
		return null;
	}

	public CpG getByAddressB(int addressB) {
		for (CpG cpg : cpgList) {
			if (addressB == cpg.getAddressB()) {
				return cpg;
			}
		}
		return null;
	}

	public CpG getCpGByName(String cpgName) {
		for (CpG cpg : cpgList) {
			if (cpgName == cpg.getCpgName()) {
				return cpg;
			}
		}
		return null;		
	}

	public CpG[] getCpgList() {
		return cpgList;
	}
	
	public void setCpGList(CpG[]  cpg){
		this.cpgList = cpg;
	}

	public String[] getCpGsIDs() {
		String cpgIDs[] = new String[getCpgList().length];

		int index = 0;
		for (CpG cpg: getCpgList()) {
			cpgIDs[index] = cpg.getCpgName();
			index++;
		}
		return cpgIDs;
	}

	public String[] getCpGsIDsByChromosome(String chr) {
		ArrayList<String> cpgByChrArray = new ArrayList<String>();

		for (CpG cpg: getCpgList()) {
			if (cpg.getChromosome().equals(chr)) {
				cpgByChrArray.add(cpg.getCpgName());
			}
		}

		String cpgIDs[] = new String[cpgByChrArray.size()];
		int i = 0;
		for (String s : cpgByChrArray) {
			cpgIDs[i++] = s;
		}
		return cpgIDs;
	}

	public CpG[] getCpGsByChromosome(String chr) {
		ArrayList<CpG> cpgByChrArray = new ArrayList<CpG>();

		for (CpG cpg: getCpgList()) {
			if (cpg.getChromosome().equals(chr)) {
				cpgByChrArray.add(cpg);
			}
		}

		CpG cpgIDs[] = new CpG[cpgByChrArray.size()];
		int i = 0;
		for (CpG s : cpgByChrArray) {
			cpgIDs[i++] = s;
		}
		return cpgIDs;
	}
}

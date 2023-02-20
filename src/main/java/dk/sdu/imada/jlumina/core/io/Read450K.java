package dk.sdu.imada.jlumina.core.io;

import java.util.ArrayList;

public class Read450K {

	ArrayList<Integer> iluminaIDs;
	ArrayList<Integer> means;
	ArrayList<Integer> sd;
	ArrayList<Integer> nBeads;
	boolean extended;

	public ArrayList<Integer> getIluminaIDs() {
		return iluminaIDs;
	}

	public ArrayList<Integer> getMeans() {
		return means;
	}

	public ArrayList<Integer> getSd() {
		return sd;
	}

	public ArrayList<Integer> getnBeads() {
		return nBeads;
	}

	public Read450K( boolean extended) {

		iluminaIDs = new ArrayList<Integer>();

		means = new ArrayList<Integer>();

		sd = new ArrayList<Integer>();

		nBeads = new ArrayList<Integer>();

		this.extended = extended;
	}

	public void addIdat(ReadIDAT idat) {
		int total = idat.getIlluminaID().length;

		if (extended) {
			for (int i = 0; i < total; i++) {
				iluminaIDs.add(idat.getIlluminaID()[i]);
				means.add(idat.getMean()[i]);
				sd.add(idat.getSD()[i]);
				nBeads.add(idat.getNBeads()[i]);
			}
		}else {
			for (int i = 0; i < total; i++) {
				iluminaIDs.add(idat.getIlluminaID()[i]);
				means.add(idat.getMean()[i]);
			}
		}
	}

	public void addIluminaID(int e) {
		iluminaIDs.add(e);
	}

	public void addMean(int e) {
		means.add(e);
	}

	public void addSD(int e) {
		sd.add(e);
	}

	public void addNBeads(int e) {
		nBeads.add(e);
	}

	public int getIluminaID(int index) {
		return iluminaIDs.get(index);
	}

	public int getMean(int index) {
		return means.get(index);
	}

	public int getSD(int index) {
		return sd.get(index);
	}

	public int getNBeads(int index) {
		return nBeads.get(index);
	}

	public boolean isExtended() {
		return extended;
	}
}

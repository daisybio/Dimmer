package dk.sdu.imada.jlumina.core.primitives;

public class CpG {

	String cpgName;
	int addressA;
	int addressB;
	String inifniumType;
	String chromosome;
	String chromosome36;
	int mapInfo;
	int mapInfo36;
	String colorChannel;
	String regionType;

	public CpG(String cpgName, 	int addressA, int addressB, String inifniumType,
			String chromosome,	int mapInfo, String chromosome36, int mapInfo36, String colorChannel, String regionType) {
		this.addressA = addressA;
		this.cpgName = cpgName;
		this.addressB = addressB;
		this.inifniumType = inifniumType;
		this.chromosome = chromosome;
		this.chromosome36 = chromosome36;
		this.mapInfo = mapInfo;
		this.mapInfo36 = mapInfo36;
		this.colorChannel = colorChannel;
		this.regionType = regionType;
	}

	public String getCpgName() {
		return cpgName;
	}

	public void setCpgName(String cpgName) {
		this.cpgName = cpgName;
	}

	public int getAddressA() {
		return addressA;
	}

	public void setAddressA(int addressA) {
		this.addressA = addressA;
	}

	public int getAddressB() {
		return addressB;
	}

	public void setAddressB(int addressB) {
		this.addressB = addressB;
	}

	public String getInifniumType() {
		return inifniumType;
	}

	public void setInifniumType(String inifniumType) {
		this.inifniumType = inifniumType;
	}

	public String getChromosome() {
		return chromosome;
	}

	public void setChromosome(String chromosome) {
		this.chromosome = chromosome;
	}

	public String getChromosome36() {
		return chromosome36;
	}

	public void setChromosome36(String chromosome36) {
		this.chromosome36 = chromosome36;
	}

	public int getMapInfo() {
		return mapInfo;
	}

	public void setMapInfo(int mapInfo) {
		this.mapInfo = mapInfo;
	}

	public int getMapInfo36() {
		return mapInfo36;
	}

	public void setMapInfo36(int mapInfo36) {
		this.mapInfo36 = mapInfo36;
	}
	
	public String getColorChannel() {
		return colorChannel;
	}
	
	public void setColorChannel(String colorChannel) {
		this.colorChannel = colorChannel;
	}
	
	public String getRegionType() {
		return regionType;
	}
	
	public void setRegionType(String regionType) {
		this.regionType = regionType;
	}
}

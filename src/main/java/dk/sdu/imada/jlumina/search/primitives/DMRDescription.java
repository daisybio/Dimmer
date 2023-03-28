package dk.sdu.imada.jlumina.search.primitives;

public class DMRDescription {

	DMR island;
	String chromosome;
	int beginPosition;
	int endPosition;
	String beginCPG;
	String endCPG;
	int size;
	

	public DMRDescription() {
	}

	public DMRDescription(DMR island, String [] cpgs, String []chr, int [] positions) {
		this.island = island;
		setChromosome(chr[island.beginPosition]);
		setBeginPosition(positions[island.beginPosition]);
		setEndPosition(positions[island.beginPosition + island.totalCpgs - 1]);
		setBeginCPG(cpgs[island.beginPosition]);
		setEndCPG(cpgs[island.beginPosition + island.totalCpgs - 1]);
		setSize(getEndPosition() - getBeginPosition() + 1);
	}
	
	public DMR getIsland() {
		return island;
	}

	public String getChromosome() {
		return chromosome;
	}

	public void setChromosome(String chromosome) {
		this.chromosome = chromosome;
	}

	public int getBeginPosition() {
		return beginPosition;
	}

	public void setBeginPosition(int beginPosition) {
		this.beginPosition = beginPosition;
	}

	public int getEndPosition() {
		return endPosition;
	}

	public void setEndPosition(int endPosition) {
		this.endPosition = endPosition;
	}

	public String getBeginCPG() {
		return beginCPG;
	}

	public void setBeginCPG(String beginCPG) {
		this.beginCPG = beginCPG;
	}

	public String getEndCPG() {
		return endCPG;
	}

	public void setEndCPG(String endCPG) {
		this.endCPG = endCPG;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
	
	public void log() {
		System.out.print(getChromosome() + " ");
		System.out.print(getBeginPosition() + " ");
		System.out.print(getEndPosition() + " ");
		System.out.print(getBeginCPG()+ " ");
		System.out.print(getEndCPG() + " ");
		System.out.print(island.score + " ");
		System.out.println("");
	}
	
	String link;
	
	public void setLink() {
		link = "https://genome.ucsc.edu/cgi-bin/hgTracks?db=hg19&lastVirtModeType=default&lastVirtModeExtraState=&virtModeType=default&virtMode=0&nonVirtPosition=&position=";
		link+= "chr" + getChromosome() + "%3A" + getBeginPosition() + "-" + getEndPosition();
	}
	
	public String getLink() {
		return link;
	}
	
	////chr10%3A133252000-133280861
	@Override
	public String toString() {
		return "#CpGs: " + island.totalCpgs + "; Chr" + getChromosome() + "; Pos: " + getBeginPosition() + "-" + getEndPosition();
	}
}

package dk.sdu.imada.jlumina.core.util;

import java.util.ArrayList;

import dk.sdu.imada.jlumina.core.io.AbstractManifest;
import dk.sdu.imada.jlumina.core.io.ReadControlProbe;
import dk.sdu.imada.jlumina.core.io.ReadManifest;
import dk.sdu.imada.jlumina.core.primitives.RGSet;

public abstract class AbstractQualityControl {
	
	RGSet rgSet;
	ReadManifest manifest; 
	ReadControlProbe controlProbe;
	
	public AbstractQualityControl(RGSet rgSet, ReadManifest manifest, ReadControlProbe controlProbe) {
		this.rgSet = rgSet;
		this.manifest = manifest;
		this.controlProbe = controlProbe;
	}
	
	/*
	 * the idea is exclude the CpGs from the manifest object
	 * but also return the list of excluded CpG
	 */
	public abstract ArrayList<String> removeBadCpGs(float cutoff);
	
	
}

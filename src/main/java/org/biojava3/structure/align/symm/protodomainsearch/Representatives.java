package org.biojava3.structure.align.symm.protodomainsearch;

import java.util.HashMap;
import java.util.List;

import org.biojava.bio.structure.scop.ScopDatabase;
import org.biojava.bio.structure.scop.ScopDescription;
import org.biojava.bio.structure.scop.ScopDomain;
import org.biojava.bio.structure.scop.ScopFactory;

public abstract class Representatives {

	private static volatile Representatives defaultNames;
	private static Object defaultNamesLock = new Object();

	public static void set(Representatives representatives) {
		defaultNames = representatives;
	}
	
	public static void setDefault() {
		synchronized (defaultNamesLock) {
			defaultNames = new SuperfamilyRepresentatives(Utils.getInstance().getCache());
		}
	}
	
	public static Representatives get() {
		if (defaultNames == null) {
			setDefault();
		}
		return defaultNames;
	}

	protected HashMap<String,ScopDescription> buildSuperfamiliesList() {
		ScopDatabase scop = ScopFactory.getSCOP();
		superfamilies = new HashMap<String,ScopDescription>();
		for (ScopDomain domain : getDomains()) {
			final ScopDescription sf = scop.getScopDescriptionBySunid(domain.getSuperfamilyId());
			superfamilies.put(domain.getScopId(), sf);
		}
		return superfamilies;
	}
	
	public HashMap<String,ScopDescription> getSuperfamilies() {
		if (superfamilies == null) superfamilies = buildSuperfamiliesList();
		return superfamilies;
	}
	
	private HashMap<String,ScopDescription> superfamilies;
	
	public abstract List<ScopDomain> getDomains();
	
}
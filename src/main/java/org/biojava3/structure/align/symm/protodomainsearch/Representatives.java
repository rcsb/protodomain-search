package org.biojava3.structure.align.symm.protodomainsearch;

import java.util.List;

import org.biojava.bio.structure.scop.ScopDomain;

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
	
	public static List<ScopDomain> get() {
		if (defaultNames == null) {
			setDefault();
		}
		return defaultNames.getDomains();
	}

	public abstract List<ScopDomain> getDomains();
	
}
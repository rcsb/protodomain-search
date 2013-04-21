package org.biojava3.structure.align.symm.protodomainsearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.biojava.bio.structure.scop.ScopDatabase;
import org.biojava.bio.structure.scop.ScopDomain;

public class NamesRepresentatives extends Representatives {

	@Override
	public List<ScopDomain> getDomains() {
		return domains;
	}

	public NamesRepresentatives(String... names) {
		this(Arrays.asList(names));
	}
	
	public NamesRepresentatives(List<String> names) {
		ScopDatabase scop = Utils.setBerkeleyScop();
		domains = new ArrayList<ScopDomain>();
		for (String name : names) {
			domains.add(scop.getDomainByScopID(name));
		}
	}
	
	private List<ScopDomain> domains;
}

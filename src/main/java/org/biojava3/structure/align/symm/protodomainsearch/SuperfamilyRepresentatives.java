package org.biojava3.structure.align.symm.protodomainsearch;

import java.util.ArrayList;
import java.util.List;

import org.biojava.bio.structure.align.util.AtomCache;
import org.biojava.bio.structure.scop.ScopCategory;
import org.biojava.bio.structure.scop.ScopDatabase;
import org.biojava.bio.structure.scop.ScopDescription;
import org.biojava.bio.structure.scop.ScopDomain;
import org.biojava.bio.structure.scop.ScopFactory;
import org.biojava.bio.structure.scop.ScopNode;

public class SuperfamilyRepresentatives extends Representatives {

	private int[] sunIds;
	
	private Integer repsPerSf;
	
	private AtomCache cache;

	public SuperfamilyRepresentatives(AtomCache cache) {
		this(cache, null);
	}
	
	public SuperfamilyRepresentatives(AtomCache cache, Integer numReps) {
		this(cache, null, new int[] { 46456, 48724, 51349, 53931, 56572, 56835 });
	}
	
	public SuperfamilyRepresentatives(AtomCache cache, Integer numReps, int[] sunIds) {
		this.cache = cache;
		this.repsPerSf = numReps;
		this.sunIds = sunIds;
	}

	private static void getDomainsUnder(int sunId, List<ScopDomain> domains) {
		
		final ScopDatabase scop = ScopFactory.getSCOP();
		final ScopDescription description = scop.getScopDescriptionBySunid(sunId);
		
		if (description.getCategory().equals(ScopCategory.Domain)) { // base case
			for (ScopDomain domain : scop.getScopDomainsBySunid(sunId)) {
				domains.add(domain);
			}
		} else { // recurse
			final ScopNode node = scop.getScopNode(sunId);
			for (int s : node.getChildren()) {
				getDomainsUnder(s, domains);
			}
		}
	}

	@Override
	public List<ScopDomain> getDomains() {
		List<ScopDomain> domains = new ArrayList<ScopDomain>();
		ScopDatabase scop = Utils.setBerkeleyScop(cache.getPath());
		for (int sunId : sunIds) {
			List<ScopDomain> current = new ArrayList<ScopDomain>();
			getDomainsUnder(sunId, current);
			int i = 0;
			for (ScopDomain domain : domains) {
				if (repsPerSf != null && i >= repsPerSf) break;
				domains.add(domain);
				i++;
			}
		}
		return domains;
	}

}

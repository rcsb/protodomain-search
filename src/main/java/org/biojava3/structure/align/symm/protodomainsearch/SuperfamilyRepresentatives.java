package org.biojava3.structure.align.symm.protodomainsearch;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.biojava.bio.structure.align.util.AtomCache;
import org.biojava.bio.structure.scop.ScopCategory;
import org.biojava.bio.structure.scop.ScopDatabase;
import org.biojava.bio.structure.scop.ScopDescription;
import org.biojava.bio.structure.scop.ScopDomain;
import org.biojava.bio.structure.scop.ScopFactory;
import org.biojava.bio.structure.scop.ScopNode;

public class SuperfamilyRepresentatives extends Representatives {

	private static final Logger logger = LogManager.getLogger(SuperfamilyRepresentatives.class.getPackage().getName());

	private int[] sunIds;

	private Integer repsPerSf;

	private AtomCache cache;

	public SuperfamilyRepresentatives(AtomCache cache) {
		this(cache, null);
	}

	public SuperfamilyRepresentatives(AtomCache cache, Integer numReps) {
		this(cache, numReps, new int[] { 46456, 48724, 51349, 53931, 56572, 56835 });
	}

	public SuperfamilyRepresentatives(AtomCache cache, Integer numReps, int[] sunIds) {
		this.cache = cache;
		this.repsPerSf = numReps;
		this.sunIds = sunIds;
	}

	private static void getDomainsUnder(int sunId, List<ScopDomain> domains, Integer repsPerSf) {

		final ScopDatabase scop = ScopFactory.getSCOP();

		final ScopDescription description = scop.getScopDescriptionBySunid(sunId);
		final ScopNode node = scop.getScopNode(sunId);
		final ScopCategory category = description.getCategory();

		if (category.equals(ScopCategory.Superfamily)) {

			/*
			 * We would prefer having one domain from each family, as best as possible.
			 * To do this, make a list of queues of domains per family
			 * Then crawl through in the correct order
			 */

			int totalDomains = 0;
			// first, make the list
			List<Queue<ScopDomain>> domainsInFamilies = new ArrayList<Queue<ScopDomain>>();
			for (int familyId : node.getChildren()) {
				final ScopNode familyNode = scop.getScopNode(familyId);
				Queue<ScopDomain> inThisFamily = new LinkedList<ScopDomain>();
				for (int domainId : familyNode.getChildren()) {
					ScopDomain thisDomain = scop.getScopDomainsBySunid(domainId).get(0); // just add the first species and Px available
					inThisFamily.add(thisDomain);
					totalDomains++;
				}
				domainsInFamilies.add(inThisFamily);
			}
			
			// okay, now add domains in a good order
			int preferredFamily = 0;
			for (int i = 0; i < repsPerSf; i++) {
				Queue<ScopDomain> queue;
				int nTried = 0;
				do {
					queue = domainsInFamilies.get(preferredFamily);
					if (preferredFamily < domainsInFamilies.size()-1) {
						preferredFamily++;
					} else {
						preferredFamily = 0;
					}
					nTried++;
					if (nTried >= domainsInFamilies.size()) {
						break; // there are none left
					}
					if (queue.isEmpty()) continue;
				} while (queue.isEmpty());
				if (i >= totalDomains) {
					break;
				}
				if (!queue.isEmpty()) {
					final ScopDomain domain = queue.poll();
					domains.add(domain);
				} else {
					logger.warn("Missing 1 domain from a family in superfamily " + description.getClassificationId());
				}
			}

		} else {

			// we're on class or fold
			for (int s : node.getChildren()) {
				getDomainsUnder(s, domains, repsPerSf);
			}

		}

	}

	private List<ScopDomain> domains;

	private static volatile int x = 0;
	
	private void setDomains() {
		domains = new ArrayList<ScopDomain>();
		Utils.setBerkeleyScop(cache.getPath()); // critical; don't remove!
		for (int sunId : sunIds) {
			getDomainsUnder(sunId, domains, repsPerSf);
		}
	}

	@Override
	public List<ScopDomain> getDomains() {
		if (domains == null) setDomains();
		return domains;
	}

}

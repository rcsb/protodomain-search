package org.biojava3.structure.align.symm.protodomainsearch;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.scop.ScopDatabase;
import org.biojava.bio.structure.scop.ScopDescription;
import org.biojava.bio.structure.scop.ScopDomain;
import org.biojava.bio.structure.scop.ScopFactory;
import org.biojava3.structure.align.symm.ResourceList;
import org.biojava3.structure.align.symm.ResourceList.NameProvider;
import org.junit.Before;
import org.junit.Test;


public class SuperfamilyRepresentativesTest {

	@Before
	public void setUp() throws StructureException {
		ResourceList.set(NameProvider.defaultNameProvider(), ResourceList.DEFAULT_PDB_DIR);
	}

	/**
	 * <strong>Warning: this test takes a very long time</strong>.
	 */
//	@Test
	public void verifySize() {
		SuperfamilyRepresentatives reps = new SuperfamilyRepresentatives(ResourceList.get().getCache());
		List<ScopDomain> domains = reps.getDomains();
		assertEquals(125932, domains.size());
		HashMap<String,ScopDescription> sfs = reps.getSuperfamilies();
		assertEquals(1832, sfs.size());
	}

	/**
	 * <strong>Warning: this test takes a very long time</strong>.
	 */
//	@Test
	public void verify1PerSf() {
		SuperfamilyRepresentatives reps = new SuperfamilyRepresentatives(ResourceList.get().getCache(), 1);
		List<ScopDomain> domains = reps.getDomains();
		assertEquals(1832, domains.size());
		Set<Integer> includedSfs = new HashSet<Integer>();
		for (ScopDomain domain : domains) {
			int sf = domain.getSuperfamilyId();
			assertFalse(includedSfs.contains(sf));
			includedSfs.add(sf);
		}
		HashMap<String,ScopDescription> sfs = reps.getSuperfamilies();
		assertEquals(1832, sfs.size());
		for (ScopDescription d : sfs.values()) {
			assertTrue(includedSfs.contains(d.getSunID()));
		}
	}

	/**
	 * <strong>Warning: this test takes a very long time</strong>.
	 */
//	@Test
	public void verify2PerSf() {
		SuperfamilyRepresentatives reps = new SuperfamilyRepresentatives(ResourceList.get().getCache(), 2);
		List<ScopDomain> domains = reps.getDomains();
		assertEquals(2917, domains.size());
		Set<Integer> includedFamilies = new HashSet<Integer>();
		for (ScopDomain domain : domains) {
			int sf = domain.getFamilyId();
			assertFalse(includedFamilies.contains(sf));
			includedFamilies.add(sf);
		}
	}
	
}

package org.biojava3.structure.align.symm.protodomainsearch;

import org.biojava.bio.structure.scop.ScopDomain;

public interface RepresentativeSignificance {

	public boolean isSignificant(ScopDomain domain);
	
}

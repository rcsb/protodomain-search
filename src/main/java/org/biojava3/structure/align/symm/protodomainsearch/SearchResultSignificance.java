package org.biojava3.structure.align.symm.protodomainsearch;

import org.biojava3.structure.align.symm.census2.Alignment;

public interface SearchResultSignificance {

	public boolean isPossiblySignificant(Alignment alignment);
	
	public boolean isSignificant(Alignment alignment);

}

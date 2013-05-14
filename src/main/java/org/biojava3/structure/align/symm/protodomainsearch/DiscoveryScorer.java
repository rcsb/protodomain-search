package org.biojava3.structure.align.symm.protodomainsearch;

import java.util.List;

import org.biojava3.structure.align.symm.census2.Result;


public interface DiscoveryScorer {

	boolean isSignificant(Result queryResult);
	
	double score(SearchResult result);
	
	List<Discovery> getSignificant(SearchResult result); // can also reorder
	
}

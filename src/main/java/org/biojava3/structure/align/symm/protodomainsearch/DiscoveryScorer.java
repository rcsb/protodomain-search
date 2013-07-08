package org.biojava3.structure.align.symm.protodomainsearch;

import java.util.List;

import org.biojava3.structure.align.symm.census2.Result;

/**
 * Something that determines a score and the significance of {@link Discovery Discoveries}.
 * 
 * @author dmyerstu
 */
public interface DiscoveryScorer {

	/**
	 * From a {@link SearchResult} {@code result} containing 1 query, returns a new list containing a subset of
	 * {@link Discovery Discoveries} from {@code result.getDiscoveries()}, where elements in the list are sorted from
	 * most significant to least significant.
	 */
	public List<Discovery> getSignificant(SearchResult result); // can also reorder

	/**
	 * Decides whether a query symmetry alignment Result is significant.
	 */
	public boolean isSignificant(Result queryResult);

	/**
	 * Assigns a numerical score to a {@link SearchResult}, based on its query protodomain and alignment, and its
	 * {@link Discovery Discoveries}.
	 */
	public double score(SearchResult result);

}

package org.biojava3.structure.align.symm.protodomainsearch;

import org.biojava3.structure.align.symm.census2.Alignment;

/**
 * Something that can determine whether {@link Discovery Discoveries} are significant.
 * 
 * @author dmyerstu
 */
public interface SearchResultSignificance {

	/**
	 * Generally speaking, whether an Alignment is <em>sufficiently good</em> to warrant gathering additional
	 * information. The method is poorly named; this method should concern likelihood, not possibility.
	 * 
	 * @param alignment
	 *            An Alignment between a query protodomain and a target domain
	 */
	public boolean isPossiblySignificant(Alignment alignment);

	/**
	 * Generally speaking, whether an Alignment is sufficiently good to warrant listing or displaying to a "user". All
	 * Discoveries, including those not considered significant by this method, are considered good enough to be logged
	 * and recorded in XML results.
	 * 
	 * @param alignment
	 * @return
	 */
	public boolean isSignificant(Alignment alignment);

}

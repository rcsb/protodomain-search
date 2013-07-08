package org.biojava3.structure.align.symm.protodomainsearch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.biojava3.structure.align.symm.census2.Result;

/**
 * A factory for {@link DiscoveryScorer} objects.
 * @author dmyerstu
 */
public class DiscoveryScorerFactory {

	public static DiscoveryScorer sensible() {
		return sensible(0.4f, 0.4f, 0.5f);
	}
	
	/**
	 * Warning: scoring will not function in the expected way if there are multiple {@link SearchResult} of the same query protodomain.
	 */
	public static DiscoveryScorer sensible(final float queryTmScore, final float discoveryTmScore, final float domainDomainCoefficient) {
		
		return new DiscoveryScorer() {
			
			private HashMap<String,Double> scores = new HashMap<>();
			
			@Override
			public double score(SearchResult result) {
				if (!scores.containsKey(result.getQuery().getProtodomain())) throw new IllegalStateException("getSignificant(SearchResult) must be called first, per the DiscoveryScorer interface");
				return scores.get(result.getQuery().getProtodomain());
			}
			
			@Override
			public List<Discovery> getSignificant(SearchResult result) {
				
				List<Discovery> trimmed = new ArrayList<Discovery>();
				
				double sum = 0; // we're going to calculate score here so we don't have to do this again
				
				// remove insignificant
				for (Discovery discovery : result.getDiscoveries()) {
					if (discovery.getAlignment() == null) continue;
					if (discovery.getDomainDomain() == null) continue;
					if (discovery.getProtodomain() == null) continue;
					if (discovery.getAlignment().getTmScore() < discoveryTmScore) continue;
					trimmed.add(discovery);
					sum += discovery.getAlignment().getTmScore() - domainDomainCoefficient * discovery.getDomainDomain().getTmScore();
				}
				
				// sort
				scores.put(result.getQuery().getProtodomain(), sum);
				Comparator<Discovery> comp = new Comparator<Discovery>() {
					@Override
					public int compare(Discovery o1, Discovery o2) {
						final Double s1 = new Double(o1.getAlignment().getTmScore() - domainDomainCoefficient * o1.getDomainDomain().getTmScore());
						final Double s2 = new Double(o2.getAlignment().getTmScore() - domainDomainCoefficient * o2.getDomainDomain().getTmScore());
						return s1.compareTo(s2);
					}
				};
				SortedSet<Discovery> sorted = new TreeSet<>(comp);
				sorted.addAll(trimmed);
				
				// now return a list
				List<Discovery> list = new ArrayList<Discovery>(sorted.size());
				list.addAll(sorted);
				return list;
			}
			
			@Override
			public boolean isSignificant(Result queryResult) {
				if (queryResult.getAlignment() == null) return false;
				if (queryResult.getAxis() == null) return false;
				if (queryResult.getOrder() == null || queryResult.getOrder() < 2) return false;
				if (queryResult.getAlignment().getTmScore() < discoveryTmScore) return false;
				return true;
			}
		};
	}
	
}

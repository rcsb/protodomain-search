package org.biojava3.structure.align.symm.protodomainsearch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class Rescorer {

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.err.println("Usage: " + Rescorer.class.getSimpleName() + " input-file output-file [scorer-factory-method]");
			return;
		}
		Rescorer rescorer = new Rescorer(DiscoveryScorerFactory.sensible());
		rescorer.rescore(new File(args[0]), new File(args[1]));
	}

	private DiscoveryScorer scorer;
	
	public Rescorer(DiscoveryScorer scorer) {
		this.scorer = scorer;
	}
	
	public void rescore(File input, File output) throws IOException {
		SearchResults results = SearchResults.fromXML(input);
		SearchResults rescored = rescore(results);
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(output))) {
			bw.write(rescored.toXML());
		}
	}
	
	public SearchResults rescore(SearchResults results) {
		
		SearchResults newResults = new SearchResults();
		newResults.setTimestamp(results.getTimestamp());
		
		final HashMap<String,Double> scores = new HashMap<String,Double>();
		
		for (SearchResult result : results.getData()) {
			if (!scorer.isSignificant(result.getQuery())) {
				List<Discovery> discoveries = scorer.getSignificant(result);
				SearchResult newResult = new SearchResult();
				newResult.setQuery(result.getQuery());
				newResult.setDiscoveries(discoveries);
				newResults.add(newResult);
				double score = scorer.score(result);
				scores.put(result.getQuery().getProtodomain(), score);
			}
		}
		
		// now sort
		Comparator<SearchResult> comp = new Comparator<SearchResult>() {
			@Override
			public int compare(SearchResult o1, SearchResult o2) {
				return scores.get(o1.getQuery().getProtodomain()).compareTo(scores.get(o2.getQuery().getProtodomain()));
			}
		};
		SortedSet<SearchResult> sorted = new TreeSet<SearchResult>(comp);
		sorted.addAll(newResults.getData());
		newResults.getData().clear();
		newResults.addAll(sorted);
		
		return newResults;
	}
	
}

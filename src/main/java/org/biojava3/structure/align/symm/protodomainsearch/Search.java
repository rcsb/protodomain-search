package org.biojava3.structure.align.symm.protodomainsearch;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.biojava.bio.structure.align.StructureAlignment;
import org.biojava.bio.structure.align.ce.CeMain;
import org.biojava.bio.structure.align.util.AtomCache;
import org.biojava.bio.structure.scop.ScopDomain;
import org.biojava3.structure.align.symm.census2.Census;
import org.biojava3.structure.align.symm.census2.Census.AlgorithmGiver;
import org.biojava3.structure.align.symm.census2.Result;
import org.biojava3.structure.align.symm.census2.Results;
import org.biojava3.structure.align.symm.census2.Significance;
import org.biojava3.structure.align.symm.census2.SignificanceFactory;

public class Search {

	static final Logger logger = Logger.getLogger(Search.class.getPackage().getName());

	static {
		BasicConfigurator.configure();
	}

	private boolean checkDiscoveryDomain;

	private boolean checkDiscoverySymmetry = true;

	private final List<ScopDomain> representatives;
	private final Results queries;
	private SearchResults results;

	public Search(File census, AtomCache cache) throws IOException {
		this(Results.fromXML(census), Representatives.get(), cache);
	}

	private AlgorithmGiver algorithm = new AlgorithmGiver() {
		@Override
		public StructureAlignment getAlgorithm() {
			return new CeMain();
		}
	};

	private AlgorithmGiver symmetryAlgorithm = Census.AlgorithmGiver.getDefault();
	private Significance symmetrySignificance = SignificanceFactory.getForCensus();

	public void search() {
		for (Result query : queries.getData()) {
			SearchJob job = new SearchJob(query, representatives);
			job.setAlgorithm(algorithm);
			job.setSymmetryAlgorithm(symmetryAlgorithm);
			job.setCache(cache);
			job.setSymmetrySignificance(symmetrySignificance);
			try {
				SearchResult result = job.call();
				results.add(result);
			} catch (RuntimeException e) {
				logger.error("Error on query " + query.getScopId(), e);
			}
		}
	}

	private AtomCache cache;

	public Search(Results queries, List<ScopDomain> representatives, AtomCache cache) {
		this.representatives = representatives;
		this.queries = queries;
		this.results = new SearchResults();
		this.cache = cache;
	}

	public boolean isCheckDiscoveryDomain() {
		return checkDiscoveryDomain;
	}

	public void setCheckDiscoveryDomain(boolean checkDiscoveryDomain) {
		this.checkDiscoveryDomain = checkDiscoveryDomain;
	}

	public boolean isCheckDiscoverySymmetry() {
		return checkDiscoverySymmetry;
	}

	public void setCheckDiscoverySymmetry(boolean checkDiscoverySymmetry) {
		this.checkDiscoverySymmetry = checkDiscoverySymmetry;
	}

	public List<ScopDomain> getRepresentatives() {
		return representatives;
	}

	public Results getQueries() {
		return queries;
	}

	public SearchResults getResults() {
		return results;
	}

	public static void main(String[] args) {

	}

}

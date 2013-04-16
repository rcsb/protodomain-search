package org.biojava3.structure.align.symm.protodomainsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.align.StructureAlignment;
import org.biojava.bio.structure.align.model.AFPChain;
import org.biojava.bio.structure.align.util.AFPChainScorer;
import org.biojava.bio.structure.align.util.AtomCache;
import org.biojava.bio.structure.scop.ScopDatabase;
import org.biojava.bio.structure.scop.ScopDescription;
import org.biojava.bio.structure.scop.ScopDomain;
import org.biojava.bio.structure.scop.ScopFactory;
import org.biojava3.structure.align.symm.census2.Alignment;
import org.biojava3.structure.align.symm.census2.Census.AlgorithmGiver;
import org.biojava3.structure.align.symm.census2.CensusJob;
import org.biojava3.structure.align.symm.census2.Result;
import org.biojava3.structure.align.symm.census2.Significance;
import org.biojava3.structure.align.symm.protodomain.Protodomain;

public class SearchJob implements Callable<SearchResult> {

	static final Logger logger = Logger.getLogger(SearchJob.class.getPackage().getName());

	static {
		BasicConfigurator.configure();
		logger.setLevel(Level.DEBUG);
	}

	private final Result query;
	
	private Integer count;

	public void setCount(int count) {
		this.count = count;
	}

	public SearchResultSignificance getSignificance() {
		return significance;
	}

	public void setSignificance(SearchResultSignificance significance) {
		this.significance = significance;
	}

	public Significance getSymmetrySignificance() {
		return symmetrySignificance;
	}

	public void setSymmetrySignificance(Significance symmetrySignificance) {
		this.symmetrySignificance = symmetrySignificance;
	}

	public AlgorithmGiver getSymmetryAlgorithm() {
		return symmetryAlgorithm;
	}

	public void setSymmetryAlgorithm(AlgorithmGiver symmetryAlgorithm) {
		this.symmetryAlgorithm = symmetryAlgorithm;
	}

	private List<ScopDomain> representatives;

	public void setRepresentatives(List<ScopDomain> representatives) {
		this.representatives = representatives;
	}

	public SearchJob(Result query, List<ScopDomain> representatives) {
		this.query = query;
		this.representatives = representatives;
	}

	private AFPChain alignProtodomainDomain(String protodomain, ScopDomain domain, Atom[] ca2) throws IOException, StructureException {
		Atom[] ca1 = cache.getAtoms(protodomain);
		return align(protodomain, domain.getScopId(), ca1, ca2, algorithm.getAlgorithm());
	}

	private AFPChain alignDomainDomain(ScopDomain queryDomain, ScopDomain domain) throws IOException, StructureException {
		return align(queryDomain.getScopId(), domain.getScopId(), algorithm);
	}
	
	private Result getSymmetry(ScopDomain domain) {
		//ScopDescription superfamily = scop.getScopDescriptionBySunid(domain.getSuperfamilyId());
		ScopDescription superfamily = null; // TODO HELP!
		CensusJob job = new CensusJob(cache, symmetryAlgorithm, symmetrySignificance);
		job.setCount(0);
		job.setDomain(domain);
		job.setSuperfamily(superfamily);
		return job.call();
	}

	private SearchResultSignificance significance;

	private AtomCache cache;

	private Significance symmetrySignificance;

	private AlgorithmGiver symmetryAlgorithm;

	private AlgorithmGiver algorithm;

	public void setCache(AtomCache cache) {
		this.cache = cache;
	}

	public void setAlgorithm(AlgorithmGiver algorithm) {
		this.algorithm = algorithm;
	}

	private AFPChain align(String name1, String name2, AlgorithmGiver algorithm) throws IOException, StructureException {
		Atom[] ca1 = cache.getAtoms(name1);
		Atom[] ca2 = cache.getAtoms(name2);
		AFPChain afpChain = align(name1, name2, ca1, ca2, algorithm.getAlgorithm());
		return afpChain;
	}

	private AFPChain align(String name1, String name2, Atom[] ca1, Atom[] ca2, StructureAlignment alg) throws StructureException, IOException {
		if (!Utils.sanityCheckPreAlign(ca1, ca2)) throw new RuntimeException("Can't align using same structure.");
		AFPChain afpChain = alg.align(ca1, ca2);
		if (afpChain == null) return null;
		afpChain.setName1(name1);
		afpChain.setName2(name2);
		double realTmScore = AFPChainScorer.getTMScore(afpChain, ca1, ca2);
		afpChain.setTMScore(realTmScore);
		return afpChain;
	}

	private ScopDomain queryDomain;
	
	public void setQueryDomain(ScopDomain queryDomain) {
		this.queryDomain = queryDomain;
	}

	@Override
	public SearchResult call() throws Exception {
		
		if (cache == null || count == null || queryDomain == null || representatives == null) throw new IllegalStateException("Cache, domain, count, and representatives must be set first");
		final String queryScopId = query.getScopId();
		final String protodomain = query.getProtodomain();
		List<Discovery> discoveries = new ArrayList<Discovery>();
		
		for (ScopDomain domain : representatives) {

			if (domain.getRanges() == null || domain.getRanges().isEmpty()) {
				logger.debug("Skipping " + domain.getScopId() + " because SCOP ranges for it are not defined");
				continue;
			}
			try {
				
				logger.info("Working on " + domain.getScopId() + " against " + protodomain);
				
				Atom[] ca2 = cache.getAtoms(domain.getScopId());
				AFPChain afpChain = alignProtodomainDomain(protodomain, domain, ca2);
				Result symmetryResult = null;
				Alignment domainDomain = null;
				Alignment alignment = new Alignment(afpChain);
				String resultProtodomain = null;
				
				if (significance.isPossiblySignificant(alignment)) {
					try {
						resultProtodomain = Protodomain.fromReferral(afpChain, ca2, 1, cache).getString();
					} catch (RuntimeException e) {
						logger.error("Could not get protodomain for result " + domain.getScopId() + " from " + queryScopId, e);
					}
					try {
						symmetryResult = getSymmetry(domain);
					} catch (RuntimeException e) {
						logger.error("Couldn't get the symmetry for " + domain, e);
					}
					try {
						AFPChain domainDomainAfpChain = alignDomainDomain(queryDomain, domain);
						domainDomain = new Alignment(domainDomainAfpChain);
					} catch (RuntimeException e) {
						logger.error("Failed aligning " + queryScopId + " against " + domain.getScopId(), e);
					}
				};
				
				Discovery discovery = new Discovery();
				discovery.setAlignment(alignment);
				discovery.setResult(symmetryResult);
				discovery.setProtodomain(resultProtodomain);
				discovery.setDomainDomain(domainDomain);
				
			} catch (Exception e) {
				logger.error("An error occured on " + queryScopId);
				continue;
			}
		}

		SearchResult result = new SearchResult();
		result.setQuery(query);
		result.setDiscoveries(discoveries);
		
		return result;
	}

}

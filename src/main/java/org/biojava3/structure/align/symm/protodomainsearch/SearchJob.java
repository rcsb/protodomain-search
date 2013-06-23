package org.biojava3.structure.align.symm.protodomainsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.align.StructureAlignment;
import org.biojava.bio.structure.align.client.PdbPair;
import org.biojava.bio.structure.align.model.AFPChain;
import org.biojava.bio.structure.align.util.AFPChainScorer;
import org.biojava.bio.structure.align.util.AtomCache;
import org.biojava.bio.structure.scop.ScopDescription;
import org.biojava.bio.structure.scop.ScopDomain;
import org.biojava3.structure.align.symm.census2.Alignment;
import org.biojava3.structure.align.symm.census2.Census.AlgorithmGiver;
import org.biojava3.structure.align.symm.census2.CensusJob;
import org.biojava3.structure.align.symm.census2.Result;
import org.biojava3.structure.align.symm.census2.Significance;
import org.biojava3.structure.align.symm.census2.representatives.Representatives;
import org.biojava3.structure.align.symm.protodomain.Protodomain;
import org.rcsb.fatcat.server.dao.DBAlignment;
import org.rcsb.fatcat.server.util.AlignmentCache;

public class SearchJob implements Callable<SearchResult> {

	private static final Logger logger = LogManager.getLogger(SearchJob.class.getPackage().getName());

	private AlgorithmGiver algorithm;

	private AtomCache cache;

	private boolean checkDiscoveryDomain;

	private boolean checkDiscoverySymmetry;

	private Integer count;

	private final Result query;

	private ScopDomain queryDomain;

	private List<ScopDomain> representatives;

	private SearchResultSignificance significance;

	private HashMap<String, ScopDescription> superfamilies;

	private AlgorithmGiver symmetryAlgorithm;

	private Significance symmetrySignificance;

	public SearchJob(Result query, List<ScopDomain> representatives) {
		this.query = query;
		this.representatives = representatives;
	}

	public SearchJob(Result query, Representatives representatives) {
		this(query, representatives.getDomains());
	}

	@Override
	public SearchResult call() throws Exception {

		if (cache == null || count == null || queryDomain == null || representatives == null || superfamilies == null) {
			throw new IllegalStateException(
					"Cache, domain, count, representatives, and superfamilies must be set first");
		}
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
						logger.error("Could not get protodomain for result " + domain.getScopId() + " from "
								+ queryScopId, e);
					}

					if (checkDiscoverySymmetry) {
						try {
							symmetryResult = getSymmetry(domain);
						} catch (RuntimeException e) {
							logger.error("Couldn't get the symmetry for " + domain, e);
						}
					}
					if (checkDiscoveryDomain) {
						DBAlignment aln = null;
						try {
							PdbPair pair = new PdbPair(queryScopId, domain.getScopId());
							aln = AlignmentCache.getInstance().getDbAlignment(pair);
						} catch (RuntimeException e) {
							logger.error("Failed getting cached result on " + domain.getScopId(), e);
						}
						if (aln == null) {
							try {
								AFPChain domainDomainAfpChain = alignDomainDomain(queryDomain, domain);
								domainDomain = new Alignment(domainDomainAfpChain);
							} catch (RuntimeException e) {
								logger.error("Failed aligning " + queryScopId + " against " + domain.getScopId(), e);
							}
						} else {
							domainDomain = new Alignment();
							domainDomain.setRmsd(aln.getRmsdOpt());
							domainDomain.setAlignScore(aln.getScore());
							domainDomain.setIdentity((float) (aln.getID() / 100.0));
							domainDomain.setSimilarity((float) (aln.getSim1() / 100.0));
							Double tmScore = aln.getTmScore();
							if (tmScore != null) {
								domainDomain.setTmScore((float) (double) tmScore);
							}
							domainDomain.setzScore(aln.getProbability());
						}
					}
				}
				;

				Discovery discovery = new Discovery();
				discovery.setAlignment(alignment);
				discovery.setResult(symmetryResult);
				discovery.setProtodomain(resultProtodomain);
				discovery.setDomainDomain(domainDomain);
				discoveries.add(discovery);

			} catch (Exception e) {
				logger.error("An error occured on " + queryScopId, e);
				continue;
			}
		}

		SearchResult result = new SearchResult();
		result.setQuery(query);
		result.setDiscoveries(discoveries);

		return result;
	}

	public SearchResultSignificance getSignificance() {
		return significance;
	}

	public AlgorithmGiver getSymmetryAlgorithm() {
		return symmetryAlgorithm;
	}

	public Significance getSymmetrySignificance() {
		return symmetrySignificance;
	}

	public void setAlgorithm(AlgorithmGiver algorithm) {
		this.algorithm = algorithm;
	}

	public void setCache(AtomCache cache) {
		this.cache = cache;
	}

	public void setCheckDiscoveryDomain(boolean checkDiscoveryDomain) {
		this.checkDiscoveryDomain = checkDiscoveryDomain;
	}

	public void setCheckDiscoverySymmetry(boolean checkDiscoverySymmetry) {
		this.checkDiscoverySymmetry = checkDiscoverySymmetry;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setQueryDomain(ScopDomain queryDomain) {
		this.queryDomain = queryDomain;
	}

	public void setRepresentatives(List<ScopDomain> representatives) {
		this.representatives = representatives;
	}

	public void setSignificance(SearchResultSignificance significance) {
		this.significance = significance;
	}

	public void setSuperfamilies(HashMap<String, ScopDescription> superfamilies) {
		this.superfamilies = superfamilies;
	}

	public void setSymmetryAlgorithm(AlgorithmGiver symmetryAlgorithm) {
		this.symmetryAlgorithm = symmetryAlgorithm;
	}

	public void setSymmetrySignificance(Significance symmetrySignificance) {
		this.symmetrySignificance = symmetrySignificance;
	}

	private AFPChain align(String name1, String name2, AlgorithmGiver algorithm) throws IOException, StructureException {
		Atom[] ca1 = cache.getAtoms(name1);
		Atom[] ca2 = cache.getAtoms(name2);
		AFPChain afpChain = align(name1, name2, ca1, ca2, algorithm.getAlgorithm());
		return afpChain;
	}

	private AFPChain align(String name1, String name2, Atom[] ca1, Atom[] ca2, StructureAlignment alg)
			throws StructureException, IOException {
		if (!Utils.sanityCheckPreAlign(ca1, ca2)) throw new RuntimeException("Can't align using same structure.");
		AFPChain afpChain = alg.align(ca1, ca2);
		if (afpChain == null) return null;
		afpChain.setName1(name1);
		afpChain.setName2(name2);
		double realTmScore = AFPChainScorer.getTMScore(afpChain, ca1, ca2);
		afpChain.setTMScore(realTmScore);
		return afpChain;
	}

	private AFPChain alignDomainDomain(ScopDomain queryDomain, ScopDomain domain) throws IOException,
	StructureException {
		return align(queryDomain.getScopId(), domain.getScopId(), algorithm);
	}

	private AFPChain alignProtodomainDomain(String protodomain, ScopDomain domain, Atom[] ca2) throws IOException,
	StructureException {
		Atom[] ca1 = cache.getAtoms(protodomain);
		return align(protodomain, domain.getScopId(), ca1, ca2, algorithm.getAlgorithm());
	}

	private Result getSymmetry(ScopDomain domain) {
		ScopDescription superfamily = superfamilies.get(domain.getScopId());
		CensusJob job = new CensusJob(cache, symmetryAlgorithm, symmetrySignificance);
		job.setCount(0);
		job.setDomain(domain);
		job.setSuperfamily(superfamily);
		return job.call();
	}

}

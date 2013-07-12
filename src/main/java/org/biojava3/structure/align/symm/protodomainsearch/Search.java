package org.biojava3.structure.align.symm.protodomainsearch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.biojava.bio.structure.align.StructureAlignment;
import org.biojava.bio.structure.align.ce.CeMain;
import org.biojava.bio.structure.align.util.AtomCache;
import org.biojava.bio.structure.scop.ScopDatabase;
import org.biojava.bio.structure.scop.ScopDomain;
import org.biojava.bio.structure.scop.ScopFactory;
import org.biojava3.core.util.ConcurrencyTools;
import org.biojava3.structure.align.symm.census2.Census;
import org.biojava3.structure.align.symm.census2.Census.AlgorithmGiver;
import org.biojava3.structure.align.symm.census2.Result;
import org.biojava3.structure.align.symm.census2.Results;
import org.biojava3.structure.align.symm.census2.Significance;
import org.biojava3.structure.align.symm.census2.SignificanceFactory;
import org.biojava3.structure.align.symm.census2.representatives.Representatives;

/**
 * Runs a search of a list of query protodomains against a list of representative domains.
 * 
 * @author dmyerstu
 * 
 */
public class Search {

	private static final Logger logger = LogManager.getLogger(Search.class.getPackage().getName());

	private AlgorithmGiver algorithm = new AlgorithmGiver() {
		@Override
		public StructureAlignment getAlgorithm() {
			return new CeMain();
		}
	};

	private AtomCache cache;

	private boolean checkDiscoveryDomain = true;

	private boolean checkDiscoverySymmetry = true;

	private File outputFile;
	private int printFrequency = 20;
	private final Results queries;
	private List<ScopDomain> representatives;

	private SearchResults results;

	private AlgorithmGiver symmetryAlgorithm = Census.AlgorithmGiver.getDefault();

	private Significance symmetrySignificance = SignificanceFactory.forCensus();

	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			System.err.println("Usage: " + Search.class.getSimpleName() + " census-file output-file");
			return;
		}
		ScopFactory.setScopDatabase(ScopFactory.VERSION_1_75A);
		final File census = new File(args[1]);
		final File output = new File(args[2]);
		searchDefault(census, output);
	}

	public static void searchDefault(File census, File output) {
		try {
			AtomCache cache = new AtomCache();
			Utils.setInstance(new Utils(cache));
			Search search = new Search(census);
			search.setCache(cache);
			search.setOutputFile(output);
			search.search();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Search(File census) throws IOException {
		this(Runtime.getRuntime().availableProcessors() - 1, Results.fromXML(census));
	}

	public Search(int maxThreads, Results queries) {
		if (maxThreads < 1) maxThreads = 1;
		ConcurrencyTools.setThreadPoolSize(maxThreads);
		this.queries = queries;
		results = new SearchResults();
	}

	public AtomCache getCache() {
		return cache;
	}

	public File getOutputFile() {
		return outputFile;
	}

	public int getPrintFrequency() {
		return printFrequency;
	}

	public Results getQueries() {
		return queries;
	}

	public List<ScopDomain> getRepresentatives() {
		return representatives;
	}

	public SearchResults getResults() {
		return results;
	}

	public boolean isCheckDiscoveryDomain() {
		return checkDiscoveryDomain;
	}

	public boolean isCheckDiscoverySymmetry() {
		return checkDiscoverySymmetry;
	}

	public void print(SearchResults results) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
			String xml;
			xml = results.toXML();
			out.print(xml);
			out.flush();
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			out.close();
		}
	}

	public void search() {

		if (outputFile == null) throw new IllegalStateException("Must set file first");
		if (cache == null) cache = new AtomCache();

		ScopDatabase scop = ScopFactory.getSCOP();

		if (representatives == null) {
			representatives = Representatives.get().getDomains();
		}

		try {

			List<Future<SearchResult>> futures = new ArrayList<Future<SearchResult>>();

			// submit jobs
			int count = 0;
			for (Result query : queries.getData()) {
				try {
					if (count % 1000 == 0) logger.info("Working on " + count + " / " + queries.getData().size());
					SearchJob job = new SearchJob(query, representatives);
					job.setAlgorithm(algorithm);
					job.setCount(count);
					job.setSuperfamilies(Representatives.get().getSuperfamilies());
					job.setSymmetryAlgorithm(symmetryAlgorithm);
					job.setCache(cache);
					job.setCheckDiscoveryDomain(checkDiscoveryDomain);
					job.setCheckDiscoverySymmetry(checkDiscoverySymmetry);
					job.setQueryDomain(scop.getDomainByScopID(query.getScopId()));
					job.setSymmetrySignificance(symmetrySignificance);
					logger.debug("Submitting new job for " + query.getScopId() + " (job #" + count + ")");
					Future<SearchResult> result = ConcurrencyTools.submit(job);
					futures.add(result);
					count++;
				} catch (RuntimeException e) {
					logger.error(e);
				}
			}

			// wait for job returns and print
			for (Future<SearchResult> future : futures) {
				SearchResult result = null;
				try {
					logger.debug("Waiting for a job to finish");
					boolean flag = false;
					// We should do this in case the job gets interrupted
					// Sometimes the OS or JVM might do this
					// Use the flag instead of future == null because future.get() may actually return null
					while (!flag) {
						try {
							result = future.get();
							flag = true;
						} catch (InterruptedException e) {
							logger.debug("The calling thread was interrupted"); // probably not a concern
						}
					}
				} catch (ExecutionException e) {
					logger.error("Error on result (" + futures.size() + " remain)", e);
					continue;
				}
				logger.debug("Result was returned for " + results.size() + " / " + queries.size());
				logger.debug(result);
				results.add(result);
				if (results.size() % printFrequency == 0) {
					logger.debug("Printing to stream ");
					print(results);
				}
			}

			logger.debug("Printing leftover results to stream");
			print(results);
			logger.info("Finished!");

		} finally {
			ConcurrencyTools.shutdown();
		}

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

	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	public void setPrintFrequency(int printFrequency) {
		this.printFrequency = printFrequency;
	}

	public void setRepresentatives(List<ScopDomain> representatives) {
		this.representatives = representatives;
	}

}

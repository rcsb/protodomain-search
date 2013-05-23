package org.biojava3.structure.align.symm.protodomainsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.align.StructureAlignment;
import org.biojava.bio.structure.align.ce.CeMain;
import org.biojava.bio.structure.scop.ScopDatabase;
import org.biojava.bio.structure.scop.ScopDomain;
import org.biojava.bio.structure.scop.ScopFactory;
import org.biojava3.structure.align.symm.CeSymm;
import org.biojava3.structure.align.symm.ResourceList;
import org.biojava3.structure.align.symm.ResourceList.ElementTextIgnoringDifferenceListener;
import org.biojava3.structure.align.symm.ResourceList.NameProvider;
import org.biojava3.structure.align.symm.census2.Census.AlgorithmGiver;
import org.biojava3.structure.align.symm.census2.Result;
import org.biojava3.structure.align.symm.census2.Results;
import org.biojava3.structure.align.symm.census2.SignificanceFactory;
import org.junit.Before;
import org.junit.Test;


public class SearchJobTest {

	@Before
	public void setUp() throws StructureException {
		ResourceList.set(NameProvider.defaultNameProvider(), ResourceList.DEFAULT_PDB_DIR);
	}
	
	static SearchJob forTest(String queryFile, String... names) {
		Result result;
		try {
			result = Results.fromXML(queryFile).getData().get(0);
		} catch (IOException e) {
			throw new RuntimeException("Could not input file", e);
		}
		return forTest(result, names);
	}
	static SearchJob forTest(Result result, String... names) {
		if (!Utils.isSet()) Utils.setInstance(new Utils());
		Representatives reps = new NamesRepresentatives(names);
		SearchJob job = new SearchJob(result, reps);
		ScopDatabase scop = ScopFactory.getSCOP(ScopFactory.VERSION_1_75B);
		ScopDomain domain = scop.getDomainByScopID(result.getScopId());
		job.setQueryDomain(domain);
		job.setAlgorithm(new AlgorithmGiver() {
			@Override
			public StructureAlignment getAlgorithm() {
				return new CeMain();
			}
		});
		job.setCheckDiscoveryDomain(true);
		job.setCheckDiscoverySymmetry(true);
		job.setSymmetryAlgorithm(new AlgorithmGiver() {
			@Override
			public StructureAlignment getAlgorithm() {
				return new CeSymm();
			}
		});
		Representatives.set(reps);
		job.setCache(Utils.getInstance().getCache());
		job.setCount(0);
		job.setSignificance(SearchResultSignificanceFactory.ultraLiberal());
		job.setSuperfamilies(reps.getSuperfamilies());
		job.setSymmetrySignificance(SignificanceFactory.ultraLiberal());
		return job;
	}

	@Test
	public void test() throws Exception {
		
		// run the job
		File inputCensus = ResourceList.get().openFile("protodomainsearch/1_query.xml");
		Result result = Results.fromXML(inputCensus).getData().get(0);
		SearchJob job = forTest(result, "d2jaja_", "d2dsmb1");
		SearchResult searchResult = job.call();
		assertEquals(result, searchResult.getQuery());
		for (Discovery discovery : searchResult.getDiscoveries()) {
			System.out.println(discovery);
		}
		SearchResults results = new SearchResults();
		results.add(searchResult);
		
		// write the results
		File actualFile = new File("actual1querysearchresults.xml");
//		actualFile.deleteOnExit();
		BufferedWriter bw = new BufferedWriter(new FileWriter(actualFile));
		bw.write(results.toXML());
		bw.close();
		
		// now compare the XML
		File expectedFile = ResourceList.get().openFile("protodomainsearch/1_query_search_results.xml");
		ElementTextIgnoringDifferenceListener listener = new ElementTextIgnoringDifferenceListener("timestamp");
		boolean similar = ResourceList.compareXml(expectedFile, actualFile, listener);
		assertTrue("XML output differs", similar);
	}

}

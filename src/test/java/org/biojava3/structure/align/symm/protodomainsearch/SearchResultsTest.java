package org.biojava3.structure.align.symm.protodomainsearch;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.biojava.bio.structure.StructureException;
import org.biojava3.structure.align.symm.ResourceList;
import org.biojava3.structure.align.symm.ResourceList.NameProvider;
import org.junit.Before;
import org.junit.Test;


public class SearchResultsTest {

	@Before
	public void setUp() throws StructureException {
		ResourceList.set(NameProvider.defaultNameProvider());
	}
	
	@Test
	public void testRead() throws IOException {
		File file = ResourceList.get().openFile("protodomainsearch/1_query_search_results.xml");
		SearchResults results = SearchResults.fromXML(file);
		assertEquals(1, results.getData().size());
	}
	
}

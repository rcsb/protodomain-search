package org.biojava3.structure.align.symm.protodomainsearch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.biojava3.structure.align.symm.census2.Alignment;

/**
 * A class that makes pretty HTML pages from {@link SearchResults}.
 * 
 * @author dmyerstu
 */
public class HTMLWriter {

	public static interface QueryNamer {
		String name(SearchResult result, int index);
	}

	public static final QueryNamer DEFAULT_QUERY_NAMER = new QueryNamer() {
		@Override
		public String name(SearchResult result, int index) {
			return index + ".html";
		}
	};

	private QueryNamer namer;

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.err.println("Usage: HTMLWriter results-file output-dir");
			return;
		}
		SearchResults results = SearchResults.fromXML(args[0]);
		String dir = args[1];
		HTMLWriter writer = new HTMLWriter(DEFAULT_QUERY_NAMER);
		writer.write(results, dir);
	}

	public HTMLWriter(QueryNamer namer) {
		super();
		this.namer = namer;
	}

	public void write(SearchResult result, File file) throws IOException {

		ArrayList<Map<String, Object>> discoveryData = new ArrayList<>();
		int i = 0;
		for (Discovery discovery : result.getDiscoveries()) {
			discoveryData.add(getDiscovery(discovery, i));
			i++;
		}
		Map<String, Object> query = new HashMap<>();
		query.put("query.domain", result.getQuery().getScopId());
		query.put("query.classification", result.getQuery().getClassification());
		query.put("query.pdb_id",
				result.getQuery().getProtodomain().substring(result.getQuery().getProtodomain().indexOf('.')));
		query.put("query.order", result.getQuery().getOrder());
		query.put("query.description", result.getQuery().getDescription());
		query.put("query.rank", result.getQuery().getRank());
		putAlignment(query, "query", result.getQuery().getAlignment());

		VelocityEngine ve = new VelocityEngine();
		ve.init();
		VelocityContext context = new VelocityContext();
		context.put("results", discoveryData);
		context.put("query", query);

		Template template = ve.getTemplate("src/main/resources/web/query.vm");
		StringWriter writer = new StringWriter();
		template.merge(context, writer);

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			bw.write(writer.toString());
		}
	}

	public void write(SearchResults results, String dir) throws IOException {
		if (!dir.endsWith("/")) dir += "/";
		writeSummary(results, new File(dir + "summary.html"));
		for (int i = 0; i < results.getData().size(); i++) {
			write(results.getData().get(i), new File(dir + namer.name(results.getData().get(i), i)));
		}
	}

	private Map<String, Object> getDiscovery(Discovery discovery, int rank) {
		Map<String, Object> map = new HashMap<>();
		map.put("domain", discovery.getResult().getScopId());
		map.put("rank", rank);
		map.put("classification", discovery.getResult().getClassification());
		map.put("pdb_id", discovery.getProtodomain().substring(discovery.getProtodomain().indexOf('.')));
		map.put("referred.protodomain", discovery.getProtodomain());
		putAlignment(map, "referred", discovery.getAlignment());
		if (discovery.getDomainDomain() == null) {
			map.put("dd", null);
		} else {
			putAlignment(map, "dd", discovery.getDomainDomain());
		}
		if (discovery.getResult().getAlignment() == null) {
			map.put("internal", null);
		} else {
			map.put("internal.protodomain", discovery.getResult().getProtodomain());
			map.put("internal.order", discovery.getResult().getOrder());
			putAlignment(map, "internal", discovery.getResult().getAlignment());
		}
		return map;
	}

	private void putAlignment(Map<String, Object> map, String prefix, Alignment alignment) {
		map.put(prefix + ".tm_score", alignment.getTmScore());
		map.put(prefix + ".similarity", alignment.getSimilarity());
		map.put(prefix + ".identity", alignment.getIdentity());
	}

	private void writeSummary(SearchResults results, File file) throws IOException {

		VelocityEngine ve = new VelocityEngine();
		ve.init();
		VelocityContext context = new VelocityContext();
		ArrayList<Map<String, Object>> queryData = new ArrayList<>();
		context.put("queries", queryData);

		Template template = ve.getTemplate("src/main/resources/web/summary.vm");
		StringWriter writer = new StringWriter();
		template.merge(context, writer);

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			bw.write(writer.toString());
		}
	}

}

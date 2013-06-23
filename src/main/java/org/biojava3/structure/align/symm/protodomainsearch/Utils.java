package org.biojava3.structure.align.symm.protodomainsearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.align.ce.AbstractUserArgumentProcessor;
import org.biojava.bio.structure.align.util.AtomCache;
import org.biojava.bio.structure.scop.BerkeleyScopInstallation;
import org.biojava.bio.structure.scop.ScopDatabase;
import org.biojava.bio.structure.scop.ScopDomain;
import org.biojava.bio.structure.scop.ScopFactory;

public class Utils {

	private AtomCache cache;
	public AtomCache getCache() {
		return cache;
	}

	public static boolean isSet() {
		return me != null;
	}
	public static final String NEWLINE;
	static {
		NEWLINE = System.getProperty("line.separator");
	}
	public Utils() {
		this(new AtomCache());
	}
	public Utils(String pdbDir) {
		this(new AtomCache(pdbDir, false));
	}
	public Utils(AtomCache cache) {
		this.cache = cache;
	}

	public void setCache(AtomCache cache) {
		this.cache = cache;
	}

	private static Utils me;

	public static Utils getInstance() {
		if (Utils.me == null) throw new IllegalStateException("Must set instance first");
		return me;
	}

	public static void setInstance(Utils instance) {
		Utils.me = instance;
	}

	public static Class<?>[] classes(Object[] args) {
		Class<?>[] classes = new Class<?>[args.length];
		int i = 0;
		for (Object o : args) {
			classes[i] = o.getClass();
			i++;
		}
		return classes;
	}

	public static double[] doubleArray(Collection<Double> de) {
		double[] r = new double[de.size()];
		int i = 0;
		for (Double d : de) {
			r[i] = d;
			i++;
		}
		return r;
	}

	public static String linkifyScopClassification(String scopId, String scopClassification) {
		return "<a href=\"http://scop.berkeley.edu/search/?key=" + scopId + "\">" + scopClassification + "</a>";
	}

	public static String linkifyAlignment(String referringProtodomain, String resultDomain, String resultProtodomain) {
		return "<a href=\"http://www.pdb.org/pdb/workbench/showPrecalcAlignment.do?action=pw_ce&amp;name1=" + referringProtodomain + "&amp;name2=" + resultDomain + "\">" + resultProtodomain + "</a>";
	}

	public static String linkifyPdbStructure(String pdbId, String scopId) {
		return "<a href=\"http://www.pdb.org/pdb/explore/explore.do?structureId=" + pdbId + "\">" + scopId + "</a>";
	}

	public static String linkifySymmetry(String protodomainString, String scopId) {
		return "<a href=\"http://source.rcsb.org/jfatcatserver/showSymmetry.jsp?name1=" + scopId + "\">" + protodomainString + "</a>";
	}

	public static String removeChain(String pdbId) {
		if (pdbId.contains(".")) {
			return pdbId.substring(0, pdbId.indexOf("."));
		}
		return pdbId;
	}

	public static String repeat(String s, int n) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			sb.append(s);
		}
		return sb.toString();
	}

	public static Atom[] getAllAtoms(String name, AtomCache cache) {
		try {
			Atom[] ca = null;
			try {
				ca = cache.getAtoms(name);
			} catch (StructureException e) {
				try {
					ca = cache.getAtoms(name.substring(0, name.length() - 1) + "_");
				} catch (StructureException e2) {
					throw e2;
				}
			}
			return ca;
		} catch (Exception e) {
			throw new RuntimeException("Could not load required atoms", e);
		}
	}

	private static final Logger logger = LogManager.getLogger(Utils.class.getName());

	/**
	 * Tries to return the PDB description. Just logs and returns the string "unknown" on error.
	 */
	public static String getDescription(String name) {
		try {
			AtomCache cache = new AtomCache();
			String description = cache.getStructure(name).getPDBHeader().getDescription();
			description = description.split("\\|")[1].substring(1);
		} catch (Exception e) {
			logger.warn("Couldn't get PDB header or structure for " + name, e);
		}
		return "unknown";
	}

	/**
	 * Tries to return the PDB Id. Just logs and returns the string "unknown" on error.
	 */
	public static String getPdbId(String scopId) {
		ScopDatabase scop = ScopFactory.getSCOP(ScopFactory.VERSION_1_75B);
		ScopDomain domain = scop.getDomainByScopID(scopId);
		if (domain == null) return "unknown";
		return domain.getPdbId();
	}
	
	public static String formatDecimal(double number) {
		return formatDecimal(number, 2);
	}
	public static String formatDecimal(double number, int nDigits) {
		NumberFormat nf = new DecimalFormat();
		nf.setMaximumFractionDigits(nDigits);
		String complete = nf.format(number);
		return complete.replace('-', '−'); // extreme OCD
	}

	public static String formatPercentage(double number) {
		return formatPercentage(number, 2);
	}
	public static String formatPercentage(double number, int nDigits) {
		NumberFormat nf = new DecimalFormat();
		nf.setMaximumFractionDigits(nDigits);
		String complete = nf.format(number*100.0) + "%";
		return complete.replace('-', '−'); // extreme OCD
	}

	public static boolean sanityCheckPreAlign(Atom[] ca1, Atom[] ca2) {
		if (ca1 == ca2) return false;
		if (ca1[0].getGroup().getChain().getParent() == ca2[0].getGroup().getChain().getParent()) return false;
		return true;
	}

	public static void printCmd(String cmd) {
		System.out.println(cmd);
		BufferedReader br;
		try {
			Process process = Runtime.getRuntime().exec(cmd);
			br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String s;
			while ((s = br.readLine()) != null) {
				System.out.println('\t' + s);
			}
			br.close();
		} catch (IOException e) {
			System.out.println("<error>");
		}
	}

	public static String formatDuration(long millis) {
		String hrs = millis / (60 * 60 * 1000) + " hours, ";
		String mins = millis / (60 * 1000) % 60 + " minutes, ";
		String secs = "and " + millis / 1000 % 60 + " seconds.";
		return hrs + mins + secs;
	}

	/**
	 * Converts space indentation to tab indentation, assuming no lines have trailing whitespace.
	 * @param input
	 * @param output
	 * @param nSpaces
	 * @throws IOException
	 */
	public static void spacesToTabs(File input, File output, int nSpaces) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(input));
		PrintWriter pw = new PrintWriter(output);
		String line = "";
		while ((line = br.readLine()) != null) {
			String trimmed = line.trim();
			int indent = (int) (((float) (line.length() - trimmed.length())) / (float) nSpaces);
			pw.println(repeat("\t", indent) + trimmed);
		}
		br.close();
		pw.close();
	}

	public static String spacesToTabs(String input, int nSpaces) throws IOException {
		StringBuilder sb = new StringBuilder();
		String[] lines = input.split(NEWLINE);
		for (String line : lines) {
			String trimmed = line.trim();
			int indent = (int) (((float) (line.length() - trimmed.length())) / (float) nSpaces);
			sb.append(repeat("\t", indent) + trimmed + NEWLINE);
		}
		return sb.toString();
	}
}

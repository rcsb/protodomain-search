package org.biojava3.structure.align.symm.protodomainsearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.align.ce.AbstractUserArgumentProcessor;
import org.biojava.bio.structure.align.util.AtomCache;
import org.biojava.bio.structure.scop.BerkeleyScopInstallation;
import org.biojava.bio.structure.scop.ScopDatabase;
import org.biojava.bio.structure.scop.ScopFactory;

public class Utils {

	private AtomCache cache;
	public AtomCache getCache() {
		return cache;
	}
	
	public Utils(AtomCache cache) {
		super();
		this.cache = cache;
	}

	public void setCache(AtomCache cache) {
		this.cache = cache;
	}

	private static Utils me;
	
	public static Utils getInstance() {
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

	public static ScopDatabase setBerkeleyScop(String pdbDir) {
		System.setProperty(AbstractUserArgumentProcessor.PDB_DIR, pdbDir);
		ScopDatabase scop = ScopFactory.getSCOP();
		if (!scop.getClass().getName().equals(BerkeleyScopInstallation.class.getName())) { // for efficiency
			ScopFactory.setScopDatabase(new BerkeleyScopInstallation());
		}
		return ScopFactory.getSCOP();
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

	public static String formatDecimal(double number) {
		NumberFormat nf = new DecimalFormat();
		nf.setMaximumFractionDigits(3);
		return nf.format(number);
	}

	public static String formatPercentage(double number) {
		NumberFormat nf = new DecimalFormat();
		nf.setMaximumFractionDigits(3);
		return nf.format(number*100.0) + "%";
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

}

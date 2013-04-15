/**
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 * Created on Sep 30, 2011
 * Created by Douglas Myers-Turnbull
 *
 * @since 3.0.2
 */
package org.biojava3.structure.align.symm.protodomainsearch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A list of {@link SearchResult SearchResults}.
 * 
 * @author dmyerstu
 */
@XmlRootElement(name = "SearchResults", namespace = "http://source.rcsb.org")
public class SearchResults implements Serializable {

	private static JAXBContext jaxbContext;
	private static final long serialVersionUID = -2059479473730736971L;
	private List<SearchResult> data = new ArrayList<SearchResult>();

	private String timestamp;

	static {
		try {
			jaxbContext = JAXBContext.newInstance(SearchResults.class);
		} catch (Exception e) {
			throw new RuntimeException(e); // fatal
		}
	}

	public static SearchResults fromXML(File file) throws IOException {

		try {

			Unmarshaller un = jaxbContext.createUnmarshaller();
			FileInputStream fis = new FileInputStream(file);
			SearchResults results = (SearchResults) un.unmarshal(fis);

			// due to a side effect by JAXB
			List<SearchResult> newData = new ArrayList<SearchResult>(results.getData().size());
			for (SearchResult result : results.getData()) {
				if (result != null) newData.add(result);
			}
			results.setData(newData);

			return results;

		} catch (JAXBException e) {
			throw new IOException(e);
		}

	}

	public static SearchResults fromXML(File[] files) throws IOException {
		SearchResults results = new SearchResults();
		for (File file : files) {
			results.getData().addAll(fromXML(file).getData());
		}
		return results;
	}

	public static SearchResults fromXML(String file) throws IOException {
		return fromXML(new File(file));
	}

	public static SearchResults getExistingResults(File file) throws IOException {
		if (file.exists()) {
			return SearchResults.fromXML(file);
		}
		return null;
	}

	public static SearchResults getExistingResults(String file) throws IOException {
		return getExistingResults(new File(file));
	}

	public SearchResults() {
		timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}

	public boolean add(SearchResult e) {
		return data.add(e);
	}

	public boolean addAll(Collection<? extends SearchResult> c) {
		return data.addAll(c);
	}

	public boolean contains(Object o) {
		return data.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return data.containsAll(c);
	}

	public List<SearchResult> getData() {
		return data;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setData(List<SearchResult> data) {
		this.data = data;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public int size() {
		return data.size();
	}

	public String toXML() throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);

		try {
			Marshaller m = jaxbContext.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(this, ps);
		} catch (JAXBException e) {
			throw new IOException(e);
		}

		return baos.toString();

	}

}

package org.biojava3.structure.align.symm.protodomainsearch;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.biojava3.structure.align.symm.census2.Result;

public class SearchResult implements Serializable {

	private static final long serialVersionUID = 3140370494380799717L;

	private Result query;
	
	private List<Discovery> discoveries;
	
	public Result getQuery() {
		return query;
	}

	public void setQuery(Result query) {
		this.query = query;
	}

	@XmlElement(name="discovery")
	public List<Discovery> getDiscoveries() {
		return discoveries;
	}

	public void setDiscoveries(List<Discovery> discoveries) {
		this.discoveries = discoveries;
	}

	@Override
	public String toString() {
		return "SearchResult [query=" + query + ", discoveries=" + discoveries.size() + "]";
	}

}

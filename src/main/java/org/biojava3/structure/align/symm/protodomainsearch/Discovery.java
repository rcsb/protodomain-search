package org.biojava3.structure.align.symm.protodomainsearch;

import java.io.Serializable;

import org.biojava3.structure.align.symm.census2.Alignment;
import org.biojava3.structure.align.symm.census2.Result;

/**
 * One query–target pair from the search.
 * 
 * @author dmyerstu
 * @see SearchResult, which contains multiple Discoveries
 */
public class Discovery implements Serializable {

	private static final long serialVersionUID = 6933505297004465548L;

	private Alignment alignment;

	private Alignment domainDomain;

	private String protodomain;

	private Result result;

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Discovery other = (Discovery) obj;
		if (alignment == null) {
			if (other.alignment != null) return false;
		} else if (!alignment.equals(other.alignment)) return false;
		if (domainDomain == null) {
			if (other.domainDomain != null) return false;
		} else if (!domainDomain.equals(other.domainDomain)) return false;
		if (protodomain == null) {
			if (other.protodomain != null) return false;
		} else if (!protodomain.equals(other.protodomain)) return false;
		if (result == null) {
			if (other.result != null) return false;
		} else if (!result.equals(other.result)) return false;
		return true;
	}

	/**
	 * The Alignment between the query protodomain and the target domain.
	 */
	public Alignment getAlignment() {
		return alignment;
	}

	/**
	 * The Alignment between the query domain and the target domain. Frequently, good scores for this are considered
	 * bad.
	 */
	public Alignment getDomainDomain() {
		return domainDomain;
	}

	/**
	 * A String representation of the result protodomain referred by the query protodomain onto the target domain. In
	 * other words, this is the protodomain that corresponds to the {@link #getAlignment() alignment} between the query
	 * protodomain and target domain.
	 */
	public String getProtodomain() {
		return protodomain;
	}

	/**
	 * The symmetry result of the query that led to this Discovery.
	 */
	public Result getResult() {
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (alignment == null ? 0 : alignment.hashCode());
		result = prime * result + (domainDomain == null ? 0 : domainDomain.hashCode());
		result = prime * result + (protodomain == null ? 0 : protodomain.hashCode());
		result = prime * result + (this.result == null ? 0 : this.result.hashCode());
		return result;
	}

	public void setAlignment(Alignment alignment) {
		this.alignment = alignment;
	}

	public void setDomainDomain(Alignment domainDomain) {
		this.domainDomain = domainDomain;
	}

	public void setProtodomain(String protodomain) {
		this.protodomain = protodomain;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return "Discovery [result=" + result + ", alignment=" + alignment + ", protodomain=" + protodomain
				+ ", domainDomain=" + domainDomain + "]";
	}

}

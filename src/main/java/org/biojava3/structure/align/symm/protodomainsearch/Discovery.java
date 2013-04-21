package org.biojava3.structure.align.symm.protodomainsearch;

import java.io.Serializable;

import org.biojava3.structure.align.symm.census2.Alignment;
import org.biojava3.structure.align.symm.census2.Result;

public class Discovery implements Serializable {

	private static final long serialVersionUID = 6933505297004465548L;

	private Result result;
	
	private Alignment alignment;
	
	private String protodomain;
	
	private Alignment domainDomain;

	public Alignment getDomainDomain() {
		return domainDomain;
	}

	public void setDomainDomain(Alignment domainDomain) {
		this.domainDomain = domainDomain;
	}

	public String getProtodomain() {
		return protodomain;
	}

	public void setProtodomain(String protodomain) {
		this.protodomain = protodomain;
	}

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	public Alignment getAlignment() {
		return alignment;
	}

	public void setAlignment(Alignment alignment) {
		this.alignment = alignment;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alignment == null) ? 0 : alignment.hashCode());
		result = prime * result + ((domainDomain == null) ? 0 : domainDomain.hashCode());
		result = prime * result + ((protodomain == null) ? 0 : protodomain.hashCode());
		result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
		return result;
	}

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

	@Override
	public String toString() {
		return "Discovery [result=" + result + ", alignment=" + alignment + ", protodomain=" + protodomain
				+ ", domainDomain=" + domainDomain + "]";
	}
	
}

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
	
}

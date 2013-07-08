package org.biojava3.structure.align.symm.protodomainsearch;

/**
 * An exception in setting a score for an object, particularly an alignment.
 * @author dmyerstu
 */
public class ScoringException extends Exception {

	private static final long serialVersionUID = 6113164535848619625L;

	public ScoringException() {
		super();
	}

	public ScoringException(String message, Throwable cause) {
		super(message, cause);
	}

	public ScoringException(String message) {
		super(message);
	}

	public ScoringException(Throwable cause) {
		super(cause);
	}

}

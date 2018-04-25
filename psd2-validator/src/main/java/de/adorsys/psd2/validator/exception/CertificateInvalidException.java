package de.adorsys.psd2.validator.exception;

import java.security.cert.CertificateException;

public class CertificateInvalidException extends CertificateException {
	
	private static final long serialVersionUID = 607142441068474387L;
	
	private String code;

	public CertificateInvalidException() {
		super();
	}
	
	public CertificateInvalidException(String code, String message) {
		super(message);
		this.setCode(code);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}

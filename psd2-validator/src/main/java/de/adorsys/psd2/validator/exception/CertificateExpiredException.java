package de.adorsys.psd2.validator.exception;

import java.security.cert.CertificateException;

public class CertificateExpiredException extends CertificateException {

	
	private static final long serialVersionUID = -2316365538155348400L;
	
	private String code;

	public CertificateExpiredException() {
		super();
	}
	
	public CertificateExpiredException(String code, String message) {
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

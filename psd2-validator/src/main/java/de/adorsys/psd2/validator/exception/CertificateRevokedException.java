package de.adorsys.psd2.validator.exception;

import java.security.cert.CertificateException;

public class CertificateRevokedException extends CertificateException {


	private static final long serialVersionUID = 8360672341641252685L;
	
	private String code;

	public CertificateRevokedException() {
		super();
	}
	
	public CertificateRevokedException(String code, String message) {
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

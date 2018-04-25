package de.adorsys.psd2.validator.exception;

import java.security.cert.CertificateException;

public class CertificateMissingException extends CertificateException {

	private static final long serialVersionUID = -472462583035078260L;
	
	private String code;

	public CertificateMissingException() {
		super();
	}
	
	public CertificateMissingException(String code, String message) {
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

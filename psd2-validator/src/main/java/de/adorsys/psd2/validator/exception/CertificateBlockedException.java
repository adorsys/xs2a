package de.adorsys.psd2.validator.exception;

import java.security.cert.CertificateException;

public class CertificateBlockedException extends CertificateException {

	
	private static final long serialVersionUID = 6819925808702885158L;
	
	private String code;

	public CertificateBlockedException() {
		super();
	}
	
	public CertificateBlockedException(String code,String message) {
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

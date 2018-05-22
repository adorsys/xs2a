package de.adorsys.psd2.validator.certificate;

import no.difi.certvalidator.api.FailedValidationException;

@SuppressWarnings("serial")
public class FailedCertValidationException extends FailedValidationException {

	private String code;
	
	public FailedCertValidationException(String message) {
		super(message);
	}
	
	public FailedCertValidationException(String code,String message) {
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

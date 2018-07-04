package de.adorsys.psd2.validator.certificate;

public enum CertificateErrorMsgCode {
	CERTIFICATE_INVALID("The contents of the corporate seal certificate are not matching PSD2 general PSD2 or attribute requirements."), 
	CERTIFICATE_EXPIRED("Corporate seal certificate is expired."), 
	CERTIFICATE_BLOCKED("Corporate seal certificate has been blocked by the ASPSP."), 
	CERTIFICATE_REVOKED("Corporate seal certificate has been revoked by QSTP."), 
	CERTIFICATE_MISSING("Corporate seal certificate was not available in the request but is mandated for the corresponding."),
	SIGNATURE_INVALID("Application layer eIDAS Signature for TPP authentication is not correct."),
	SIGNATURE_MISSING("Apllication layer eIDAS Signature for TPP authentication is mandated by the ASPSP but is missing.");
	
	private String description;

	CertificateErrorMsgCode(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return description;
	}

}

package de.adorsys.psd2.validator.certificate;

public enum CertificateErrorMessageCode {
	CERTIFICATE_INVALID("The contents of the corporate seal certificate are not matching PSD2 general PSD2 or attribute requirements."), 
	CERTIFICATE_EXPIRED("Corporate seal certificate is expired."), 
	CERTIFICATE_BLOCKED("Corporate seal certificate has been blocked by the ASPSP."), 
	CERTIFICATE_REVOKED("Corporate seal certificate has been revoked by QSTP."), 
	CERTIFICATE_MISSING("Corporate seal certificate was not available in the request but is mandated for the corresponding.");

	private String description;

	private CertificateErrorMessageCode(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return description;
	}

}

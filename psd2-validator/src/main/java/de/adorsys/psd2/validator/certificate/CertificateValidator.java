package de.adorsys.psd2.validator.certificate;

import java.security.cert.CertificateException;

public interface CertificateValidator {

	void validate(String encodedCert) throws CertificateException;
	
	
}

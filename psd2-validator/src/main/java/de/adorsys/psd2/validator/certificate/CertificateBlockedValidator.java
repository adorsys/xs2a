package de.adorsys.psd2.validator.certificate;

import java.security.cert.CertificateException;

public class CertificateBlockedValidator implements CertificateValidator {

	@Override
	public void validate(String encodedCert) throws CertificateException {
		// NOPMD TODO implement or clarify a way to load blocked certificates by
		// ASPSP and
		// check if it contain the entry certificate (encodedCert)
	}

}
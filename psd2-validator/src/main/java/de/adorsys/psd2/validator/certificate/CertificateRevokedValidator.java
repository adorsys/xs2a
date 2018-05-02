package de.adorsys.psd2.validator.certificate;

import java.security.cert.CertificateException;

public class CertificateRevokedValidator implements CertificateValidator {

	@Override
	public void validate(String encodedCert) throws CertificateException {
		// NOPMD TODO implement revoked certificate validator

	}

}

package de.adorsys.keycloak.extension.clientregistration.certs;

import de.adorsys.keycloak.extension.clientregistration.certs.exceptions.CertificateValidationException;
import de.adorsys.keycloak.extension.clientregistration.certs.rules.CertRule;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

public class CertValidator implements Validator {

	private static CertificateFactory certFactory;

	private List<CertRule> certRules;

	public boolean validate(X509Certificate certificate) {
		for (CertRule certRule : certRules) {
			certRule.check(certificate);
		}
		return true;
	};

	public boolean validate(InputStream inputStream) throws CertificateValidationException {
		X509Certificate certificate = getCertificate(inputStream);
		return validate(certificate);
	}

	public X509Certificate validate(byte[] bytes) throws CertificateValidationException {
		X509Certificate certificate = getCertificate(new ByteArrayInputStream(bytes));
		validate(certificate);
		return certificate;
	}

	private X509Certificate getCertificate(InputStream inputStream) throws CertificateValidationException {
		try {
			if (certFactory == null)
				certFactory = CertificateFactory.getInstance("X.509");
			return (X509Certificate) certFactory.generateCertificate(inputStream);
		} catch (CertificateException e) {
			throw new CertificateValidationException(e.getMessage());
		}
	}

}

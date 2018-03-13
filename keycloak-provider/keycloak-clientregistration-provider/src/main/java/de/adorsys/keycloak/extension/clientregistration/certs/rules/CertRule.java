package de.adorsys.keycloak.extension.clientregistration.certs.rules;

import de.adorsys.keycloak.extension.clientregistration.certs.exceptions.CertificateValidationException;

import java.security.cert.X509Certificate;

public interface CertRule {

	public boolean check(X509Certificate certificate) throws CertificateValidationException;
}

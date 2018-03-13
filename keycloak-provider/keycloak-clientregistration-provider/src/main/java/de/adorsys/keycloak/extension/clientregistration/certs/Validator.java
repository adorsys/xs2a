package de.adorsys.keycloak.extension.clientregistration.certs;

import de.adorsys.keycloak.extension.clientregistration.certs.exceptions.CertificateValidationException;

import java.io.InputStream;
import java.security.cert.X509Certificate;

public interface Validator {

	public boolean validate(X509Certificate certificate) throws CertificateValidationException;

	public boolean validate(InputStream inputStream) throws CertificateValidationException;

	public X509Certificate validate(byte[] bytes) throws CertificateValidationException;

}

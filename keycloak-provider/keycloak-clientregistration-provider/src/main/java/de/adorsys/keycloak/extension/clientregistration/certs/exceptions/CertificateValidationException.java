package de.adorsys.keycloak.extension.clientregistration.certs.exceptions;

public class CertificateValidationException extends RuntimeException {

	public CertificateValidationException(String message) {
		super(message);
	}
}

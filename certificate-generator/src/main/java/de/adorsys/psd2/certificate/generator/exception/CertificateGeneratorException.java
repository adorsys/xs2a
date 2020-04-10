package de.adorsys.psd2.certificate.generator.exception;

public class CertificateGeneratorException extends RuntimeException {

    public CertificateGeneratorException(String message) {
        super(message);
    }

    public CertificateGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }

}

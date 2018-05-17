package de.adorsys.psd2.validator.certificate;

import java.security.cert.X509Certificate;

import no.difi.certvalidator.api.CertificateBucket;
import no.difi.certvalidator.api.CertificateValidationException;
import no.difi.certvalidator.api.FailedValidationException;
import no.difi.certvalidator.api.ValidatorRule;


public class BlackListRule implements ValidatorRule {

    private final CertificateBucket certificates;

    public BlackListRule(CertificateBucket certificates) {
        this.certificates = certificates;
    }

    @Override
    public void validate(X509Certificate certificate) throws CertificateValidationException {
        for (X509Certificate cert : certificates) {
            if (cert.equals(certificate))
                throw new FailedValidationException("Certificate is blacklisted.");
        }
    }
}
package de.adorsys.keycloak.extension.clientregistration.certs.rules;

import de.adorsys.keycloak.extension.clientregistration.CertVerifier;
import de.adorsys.keycloak.extension.clientregistration.certs.exceptions.CertificateValidationException;
import org.jboss.logging.Logger;

import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;

public class ExpirationRule implements CertRule {

    private static final Logger log = Logger.getLogger(CertVerifier.class);

    @Override
    public boolean check(X509Certificate certificate) throws CertificateValidationException {
        try {
            log.info("== Expiration Check ==");
            certificate.checkValidity(new Date());
            return true ;
        } catch (CertificateNotYetValidException | CertificateExpiredException e) {
            log.info(e.getMessage());
            throw  new CertificateValidationException("Certificate Expired");
        }
    }
}

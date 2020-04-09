package de.adorsys.psd2.certificate.generator.service;

import de.adorsys.psd2.certificate.generator.exception.CertificateGeneratorException;
import de.adorsys.psd2.certificate.generator.model.IssuerData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.springframework.stereotype.Service;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

@Slf4j
@Service
public class IssuerDataService {

    private final KeysProvider keysProvider;

    @Getter
    private IssuerData issuerData;

    public IssuerDataService(KeysProvider keysProvider) {
        this.keysProvider = keysProvider;
        this.issuerData = generateIssuerData();
    }

    private IssuerData generateIssuerData() {
        IssuerData data = new IssuerData();
        X509Certificate cert = keysProvider.loadCertificate();

        try {
            data.setX500name(new JcaX509CertificateHolder(cert).getSubject());
        } catch (CertificateEncodingException ex) {
            throw new CertificateGeneratorException("Could not read issuer data from certificate", ex);
        }

        data.setPrivateKey(keysProvider.loadPrivateKey());
        return data;
    }
}

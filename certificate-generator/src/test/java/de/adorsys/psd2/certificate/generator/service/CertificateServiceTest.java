package de.adorsys.psd2.certificate.generator.service;

import de.adorsys.psd2.certificate.generator.model.CertificateRequest;
import de.adorsys.psd2.certificate.generator.model.CertificateResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;

import static de.adorsys.psd2.certificate.generator.service.ExportUtil.exportToString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class CertificateServiceTest {
    private final KeysProvider keysProvider = new KeysProvider();
    private final IssuerDataService issuerDataService = new IssuerDataService(keysProvider);
    private final CertificateService certificateService = new CertificateService(issuerDataService);

    @Test
    void newCertificateCreatesCertAndKey() {
        CertificateRequest certificateRequest = CertificateRequest.builder()
                                                    .authorizationNumber("12345")
                                                    .countryName("Germany")
                                                    .organizationName("adorsys")
                                                    .commonName("XS2A Sandbox")
                                                    .build();
        CertificateResponse certificateResponse = certificateService.newCertificate(certificateRequest);
        assertNotNull(certificateResponse.getPrivateKey());
        assertNotNull(certificateResponse.getEncodedCert());
    }

    @Test
    void exportPrivateKeyToStringResultsInSingleLinePrimaryKey() {
        PrivateKey key = keysProvider.loadPrivateKey();
        String result = exportToString(key).trim();
        assertTrue(result.startsWith("-----BEGIN RSA PRIVATE KEY-----"));
        assertTrue(result.endsWith("-----END RSA PRIVATE KEY-----"));
    }

}

package de.adorsys.psd2.certificate.generator.service;

import de.adorsys.psd2.certificate.generator.model.CertificateRequest;
import de.adorsys.psd2.certificate.generator.model.CertificateResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.PrivateKey;

import static de.adorsys.psd2.certificate.generator.service.ExportUtil.exportToString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class CertificateServiceTest {
    private KeysProvider keysProvider;
    private CertificateService certificateService;

    @BeforeEach
    void setUp() {
        keysProvider = new KeysProvider();
        ReflectionTestUtils.setField(keysProvider, "issuerPrivateKey", "certificates/MyRootCA.key");
        ReflectionTestUtils.setField(keysProvider, "issuerCertificate", "certificates/MyRootCA.pem");
        IssuerDataService issuerDataService = new IssuerDataService(keysProvider);
        certificateService = new CertificateService(issuerDataService);
    }

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

/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

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

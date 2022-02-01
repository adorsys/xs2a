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

package de.adorsys.psd2.validator.certificate;

import de.adorsys.psd2.validator.certificate.util.CertificateUtils;
import no.difi.certvalidator.api.CertificateValidationException;
import no.difi.certvalidator.util.SimpleCertificateBucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CertificateValidatorFactoryTest {

    private SimpleCertificateBucket blockedCertBucket;
    private SimpleCertificateBucket rootCertBucket;
    private SimpleCertificateBucket intermediateCertBucket;

    @BeforeEach
    void init() {
        blockedCertBucket = new SimpleCertificateBucket(CertificateUtils.getCertificates("blockedcert"));
        rootCertBucket = new SimpleCertificateBucket(CertificateUtils.getCertificates("rootcert", "TCA3.crt"));
        intermediateCertBucket = new SimpleCertificateBucket(CertificateUtils.getCertificates("intermediatecert"));
    }

    @Test
    void when_ValidCertificate_Expected_True() {
        String encodedCert = CertificateUtils.getCertificateByName("certificateValid.crt");
        CertificateValidatorFactory validatorFactory = new CertificateValidatorFactory(blockedCertBucket,
                                                                                       rootCertBucket, intermediateCertBucket);

        assertThrows(
            CertificateValidationException.class, () -> validatorFactory.validate(encodedCert)
        );
    }

    @Test
    void when_InValidCertificate_Expected_Exception() {
        String encodedCert = CertificateUtils.getCertificateByName("certificateInvalid.crt");
        CertificateValidatorFactory validatorFactory = new CertificateValidatorFactory(blockedCertBucket,
                                                                                       rootCertBucket, intermediateCertBucket);

        assertThrows(
            CertificateValidationException.class, () -> validatorFactory.validate(encodedCert)
        );
    }
}

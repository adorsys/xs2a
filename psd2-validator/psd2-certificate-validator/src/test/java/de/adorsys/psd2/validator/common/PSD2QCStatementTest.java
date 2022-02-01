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

package de.adorsys.psd2.validator.common;

import com.nimbusds.jose.util.X509CertUtils;
import de.adorsys.psd2.validator.certificate.util.CertificateUtils;
import no.difi.certvalidator.api.CertificateValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PSD2QCStatementTest {

    private X509Certificate validCertificate;
    private X509Certificate invalidCertificate;

    @BeforeEach
    void setUp() {
        validCertificate = X509CertUtils.parse(CertificateUtils.getCertificateByName("certificateValid.crt"));
        invalidCertificate = X509CertUtils.parse(CertificateUtils.getCertificateByName("certificateInvalid.crt"));
    }

    @Test
    void psd2QCType_success() throws CertificateValidationException {
        PSD2QCType psd2QCType = PSD2QCStatement.psd2QCType(validCertificate);
        assertNotNull(psd2QCType);
    }

    @Test
    void psd2QCType_QCStatementNotFound() {
        assertThrows(CertificateValidationException.class, () -> PSD2QCStatement.psd2QCType(invalidCertificate));
    }

}

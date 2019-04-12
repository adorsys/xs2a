/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.validator.common;

import com.nimbusds.jose.util.X509CertUtils;
import de.adorsys.psd2.validator.certificate.util.CertificateUtils;
import no.difi.certvalidator.api.CertificateValidationException;
import org.junit.Before;
import org.junit.Test;

import java.security.cert.X509Certificate;

import static org.junit.Assert.assertNotNull;

public class PSD2QCStatementTest {

    private X509Certificate validCertificate;
    private X509Certificate invalidCertificate;

    @Before
    public void setUp() {
        validCertificate = X509CertUtils.parse(CertificateUtils.getCertificateByName("certificateValid.crt"));
        invalidCertificate = X509CertUtils.parse(CertificateUtils.getCertificateByName("certificateInvalid.crt"));
    }

    @Test
    public void psd2QCType_success() throws CertificateValidationException {
        PSD2QCType psd2QCType = PSD2QCStatement.psd2QCType(validCertificate);
        assertNotNull(psd2QCType);
    }

    @Test(expected = CertificateValidationException.class)
    public void psd2QCType_QCStatementNotFound() throws CertificateValidationException {
        PSD2QCStatement.psd2QCType(invalidCertificate);
    }

}

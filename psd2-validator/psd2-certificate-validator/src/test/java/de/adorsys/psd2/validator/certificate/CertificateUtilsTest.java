/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.validator.certificate;

import de.adorsys.psd2.validator.certificate.util.CertificateUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CertificateUtilsTest {

	@Test
	void test_getRootCertificate() {

		X509Certificate[] rootCertList = CertificateUtils.getCertificates("rootcert", "TCA3.crt");
		assertTrue(rootCertList.length >= 1);
	}

	@Test
	void test_getCertificateByName() {

		String encodedCert = CertificateUtils.getCertificateByName("certificateValid.crt");
		assertTrue(StringUtils.isNotBlank(encodedCert));
	}

}

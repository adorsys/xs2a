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

import java.security.cert.CertificateException;

import org.junit.Before;
import org.junit.Test;

import de.adorsys.psd2.validator.certificate.util.CertificateUtils;
import no.difi.certvalidator.api.CertificateValidationException;
import no.difi.certvalidator.util.SimpleCertificateBucket;

public class CertificateValidatorFactoryTest {

	private SimpleCertificateBucket blockedCertBucket;
	private SimpleCertificateBucket rootCertBucket;
	private SimpleCertificateBucket intermediateCertBucket;

	@Before
	public void init() {

		blockedCertBucket = new SimpleCertificateBucket(CertificateUtils.getCertificates("blockedcert"));
		rootCertBucket = new SimpleCertificateBucket(CertificateUtils.getCertificates("rootcert", "TCA3.crt"));
		intermediateCertBucket = new SimpleCertificateBucket(CertificateUtils.getCertificates("intermediatecert"));
	}

	@Test(expected = CertificateValidationException.class)
	public void when_ValidCertificate_Expected_True() throws CertificateException, CertificateValidationException {

		String encodedCert = CertificateUtils.getCertificateByName("certificateValid.crt");

		CertificateValidatorFactory validatorFactory = new CertificateValidatorFactory(blockedCertBucket,
				rootCertBucket, intermediateCertBucket);

		validatorFactory.validate(encodedCert);
	}

	@Test(expected = CertificateValidationException.class)
	public void when_InValidCertificate_Expected_Exception()
			throws CertificateException, CertificateValidationException {

		String encodedCert = CertificateUtils.getCertificateByName("certificateInvalid.crt");

		CertificateValidatorFactory validatorFactory = new CertificateValidatorFactory(blockedCertBucket,
				rootCertBucket, intermediateCertBucket);

		validatorFactory.validate(encodedCert);
	}
}

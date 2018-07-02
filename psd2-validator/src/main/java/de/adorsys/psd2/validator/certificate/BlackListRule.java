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

import no.difi.certvalidator.api.CertificateBucket;
import no.difi.certvalidator.api.ValidatorRule;

import java.security.cert.X509Certificate;

public class BlackListRule implements ValidatorRule {

	private final CertificateBucket certificates;

	BlackListRule(CertificateBucket certificates) {
		this.certificates = certificates;
	}

	@Override
	public void validate(X509Certificate certificate) throws FailedCertValidationException {
		for (X509Certificate cert : certificates) {
			if (cert.equals(certificate)) {
                throw new FailedCertValidationException(CertificateErrorMsgCode.CERTIFICATE_BLOCKED.name(),
                                                        CertificateErrorMsgCode.CERTIFICATE_BLOCKED.toString()
                );
            }
		}
	}
}

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
import java.security.cert.X509Certificate;

import org.apache.commons.lang3.StringUtils;

import com.nimbusds.jose.util.X509CertUtils;

import no.difi.certvalidator.Validator;
import no.difi.certvalidator.ValidatorBuilder;
import no.difi.certvalidator.api.CertificateValidationException;
import no.difi.certvalidator.util.SimpleCertificateBucket;

public class CertificateValidatorFactory {

	private Validator validator;

	public CertificateValidatorFactory(SimpleCertificateBucket blockedCertBucket,
			SimpleCertificateBucket rootCertificates, SimpleCertificateBucket intermediateCertificates) {

		validator = ValidatorBuilder.newInstance().addRule(new ExpirationRuleExt()).addRule(new CRLRuleExt())
				.addRule(new BlackListRule(blockedCertBucket))
				.addRule(new ChainRuleExt(rootCertificates, intermediateCertificates)).build();
	}

	public boolean validate(String encodedCert) throws CertificateException, CertificateValidationException {

		if (StringUtils.isBlank(encodedCert)) {
			throw new FailedCertValidationException(CertificateErrorMsgCode.CERTIFICATE_MISSING.name(),
					CertificateErrorMsgCode.CERTIFICATE_MISSING.toString());
		}

		X509Certificate cert = X509CertUtils.parse(encodedCert);
		
		if(cert == null) {
			throw new FailedCertValidationException(CertificateErrorMsgCode.CERTIFICATE_MISSING.name(),
					CertificateErrorMsgCode.CERTIFICATE_MISSING.toString());
		}

		validator.validate(cert);

		return true;
	}
}

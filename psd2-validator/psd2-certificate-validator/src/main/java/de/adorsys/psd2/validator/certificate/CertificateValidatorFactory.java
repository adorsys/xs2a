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

import com.nimbusds.jose.util.X509CertUtils;
import no.difi.certvalidator.Validator;
import no.difi.certvalidator.ValidatorBuilder;
import no.difi.certvalidator.api.CertificateValidationException;
import no.difi.certvalidator.util.SimpleCertificateBucket;
import org.apache.commons.lang3.StringUtils;

import java.security.cert.X509Certificate;

public class CertificateValidatorFactory {

	private Validator validator;

	public CertificateValidatorFactory(SimpleCertificateBucket blockedCertBucket,
			SimpleCertificateBucket rootCertificates, SimpleCertificateBucket intermediateCertificates) {

		validator = ValidatorBuilder.newInstance().addRule(new ExpirationRuleExt()).addRule(new CRLRuleExt())
				.addRule(new BlackListRule(blockedCertBucket))
				.addRule(new ChainRuleExt(rootCertificates, intermediateCertificates)).build();
	}

	public boolean validate(String encodedCert) throws CertificateValidationException {

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

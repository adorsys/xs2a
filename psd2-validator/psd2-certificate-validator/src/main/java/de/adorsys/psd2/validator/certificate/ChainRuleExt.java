/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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

import no.difi.certvalidator.api.CertificateBucket;
import no.difi.certvalidator.api.CertificateValidationException;
import no.difi.certvalidator.rule.ChainRule;

import java.security.cert.X509Certificate;

public class ChainRuleExt extends ChainRule {

	ChainRuleExt(CertificateBucket rootCertificates, CertificateBucket intermediateCertificates) {
		super(rootCertificates, intermediateCertificates);
	}

	@Override
	public void validate(X509Certificate certificate) throws FailedCertValidationException {

		try {
			super.validate(certificate);
		} catch (CertificateValidationException e) {
			throw new FailedCertValidationException(CertificateErrorMsgCode.CERTIFICATE_INVALID.name(),
					CertificateErrorMsgCode.CERTIFICATE_INVALID.toString());
		}
	}

}

/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.validator.certificate;

import no.difi.certvalidator.api.FailedValidationException;
import no.difi.certvalidator.rule.ExpirationRule;

import java.security.cert.X509Certificate;

public class ExpirationRuleExt extends ExpirationRule {

    @Override
	public void validate(X509Certificate certificate) throws FailedCertValidationException {

		try {
			super.validate(certificate);
		} catch (FailedValidationException e) {
			throw new FailedCertValidationException(CertificateErrorMsgCode.CERTIFICATE_EXPIRED.name(),
					CertificateErrorMsgCode.CERTIFICATE_EXPIRED.toString());
		}
	}
}

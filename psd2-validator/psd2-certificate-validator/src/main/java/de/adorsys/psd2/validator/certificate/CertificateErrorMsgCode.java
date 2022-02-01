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

public enum CertificateErrorMsgCode {
	CERTIFICATE_INVALID("The contents of the Signature/corporate seal certificate are not matching PSD2 general PSD2 or attribute requirements."),
	CERTIFICATE_EXPIRED("Signature/corporate seal certificate is expired."),
	CERTIFICATE_BLOCKED("Signature/corporate seal certificate has been blocked by the ASPSP."),
	CERTIFICATE_REVOKED("Signature/corporate seal certificate has been revoked by QSTP."),
	CERTIFICATE_MISSING("Signature/corporate seal certificate was not available in the request but is mandated for the corresponding."),
	SIGNATURE_INVALID("Application layer eIDAS Signature for TPP authentication is not correct."),
	SIGNATURE_MISSING("Application layer eIDAS Signature for TPP authentication is mandated by the ASPSP but is missing."),
    FORMAT_ERROR("Format of certain request fields are not matching the XS2A requirements. An explicit path to the corresponding field might be added in the return message.");

	private String description;

	CertificateErrorMsgCode(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return description;
	}

}

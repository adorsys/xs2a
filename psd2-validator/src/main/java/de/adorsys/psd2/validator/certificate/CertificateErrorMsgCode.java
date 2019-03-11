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

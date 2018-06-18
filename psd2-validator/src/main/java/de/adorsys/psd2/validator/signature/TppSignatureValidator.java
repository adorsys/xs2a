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

package de.adorsys.psd2.validator.signature;

import com.nimbusds.jose.util.X509CertUtils;
import org.apache.commons.lang3.StringUtils;
import org.tomitribe.auth.signatures.Signature;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TppSignatureValidator {

	/**
	 * mandatory header fields for http signature in case of psd2
	 */
	private static final List<String> MANDATORY_HEADERS_PSD2 = Arrays
			.asList("digest", "tpp-transaction-id", "tpp-request-id", "date");

	/**
	 * signature should not be null signature should be conform with psd2
	 * addition signature should be verifiable by the entry certificate
	 *
	 * @param signature Signature to verify
	 * @return true if signature is correct, false otherwise
	 */
	public boolean verifySignature(String signature, String tppEncodedCert, Map<String, String> headers)
			throws NoSuchAlgorithmException, SignatureException, IOException {

		if (StringUtils.isBlank(signature)) {
			throw new IllegalArgumentException("SIGNATURE_MISSING");
		}

		if (StringUtils.isBlank(tppEncodedCert)) {
			throw new IllegalArgumentException("CERTIFICAT_MISSING");
		}

		X509Certificate cert = X509CertUtils.parse(tppEncodedCert);

		PublicKey key = cert.getPublicKey();

		Signature signatureData = Signature.fromString(signature);

		if (!signatureData.getHeaders().containsAll(MANDATORY_HEADERS_PSD2)) {
			throw new IllegalArgumentException("SIGNATURE_INVALID");
		}

		SignatureVerifier verifier = new SignatureVerifier(key, signatureData);
		return verifier.verify("method", "uri", headers);
	}

}

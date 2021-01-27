/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.validator.signature.impl;


import com.nimbusds.jose.util.X509CertUtils;
import de.adorsys.psd2.validator.certificate.util.CertificateUtils;
import de.adorsys.psd2.validator.signature.SignatureVerifier;
import de.adorsys.psd2.validator.signature.service.CertificateConstants;
import de.adorsys.psd2.validator.signature.service.RequestHeaders;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.tomitribe.auth.signatures.Signature;
import org.tomitribe.auth.signatures.Verifier;

import java.security.cert.X509Certificate;
import java.util.Map;

@Slf4j
public class SignatureVerifierImpl implements SignatureVerifier {

    @Override
    public boolean verify(String signature, String tppEncodedCert, Map<String, String> headers, String method, String url) {
        X509Certificate certificate = X509CertUtils.parse(
            CertificateUtils.normalizeCertificate(tppEncodedCert)
        );

        if (certificate == null) {
            log.warn("TPP Certificate couldn't be parsed!");
            return false;
        }

        Signature signatureData = Signature.fromString(signature);
        if (!isKeyIdValid(certificate, signatureData.getKeyId())) {
            log.warn("Key ID is invalid!");
            return false;
        }

        Map<String, String> headersMap = RequestHeaders.fromMap(headers).toMap();

        try {
            Verifier verifier = new Verifier(certificate.getPublicKey(), signatureData);
            return verifier.verify(method, url, headersMap);

        } catch (Exception e) {
            log.warn("Signature verification has an error: {}", e.getMessage());
            return false;
        }
    }

    private boolean isKeyIdValid(X509Certificate certificate, String keyId) {
        return StringUtils.equals(keyId, getKeyIdFromCertificate(certificate));
    }

    private String getKeyIdFromCertificate(X509Certificate certificate) {
        return CertificateConstants.CERTIFICATE_SERIAL_NUMBER_ATTRIBUTE
                   + CertificateConstants.EQUALS_SIGN_SEPARATOR
                   + certificate.getSerialNumber().toString(16) // toString(16) is used to provide hexadecimal coding as mentioned in specification
                   + CertificateConstants.COMMA_SEPARATOR
                   + CertificateConstants.CERTIFICATION_AUTHORITY_ATTRIBUTE
                   + CertificateConstants.EQUALS_SIGN_SEPARATOR
                   + certificate.getIssuerX500Principal()
                         .getName()
                         .replace(CertificateConstants.SPACE_SEPARATOR, CertificateConstants.HEXADECIMAL_SPACE_SEPARATOR)
                         .replace("\n", "");
    }
}

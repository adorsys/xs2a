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

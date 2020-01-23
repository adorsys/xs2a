/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import de.adorsys.psd2.validator.certificate.util.CertificateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tomitribe.auth.signatures.MissingRequiredHeaderException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TppSignatureValidatorTest {
    private static final String DIGEST_HEADER_NAME = "Digest";
    private static final String TPP_TRANSACTION_ID_HEADER_NAME = "TPP-Transaction-ID";
    private static final String X_REQUEST_ID_HEADER_NAME = "x-request-id";
    private static final String PSU_ID_HEADER_NAME = "PSU-ID";
    private static final String TIMESTAMP_HEADER_NAME = "Timestamp";
    private static final String PSU_ID = "PSU-1234";

    private String signature;
    private String tppEncodedValidCert = "";
    private String tppEncodedInvalidCert = "";

    @BeforeEach
    void init() {
        signature = new SignatureGeneratorUtil().generateSignature();

        tppEncodedValidCert = CertificateUtils.getCertificateByName("certificateValid.crt");

        tppEncodedInvalidCert = CertificateUtils.getCertificateByName("certificateInvalid.crt");
    }

    @Test
    void when_ValidSignature_Expected_true() throws IOException {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(DIGEST_HEADER_NAME, new SignatureGeneratorUtil().generateDigest());
        headersMap.put(TPP_TRANSACTION_ID_HEADER_NAME, "3dc3d5b3-7023-4848-9853-f5400a64e80f");
        headersMap.put(X_REQUEST_ID_HEADER_NAME, "99391c7e-ad88-49ec-a2ad-99ddcb1f7721");
        headersMap.put(PSU_ID_HEADER_NAME, PSU_ID);
        headersMap.put(TIMESTAMP_HEADER_NAME, "Sun, 06 Aug 2017 15:02:37 GMT");

        boolean verifySignature = new TppSignatureValidator().verifySignature(signature, tppEncodedValidCert, headersMap);
        assertTrue(verifySignature);
    }

    @Test
    void when_ValidSignature_And_MissingHeaderAttribute_Expected_MissingRequiredHeaderException() {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(DIGEST_HEADER_NAME, new SignatureGeneratorUtil().generateDigest());
        headersMap.put(TPP_TRANSACTION_ID_HEADER_NAME, "xxxxxx");
        headersMap.put(X_REQUEST_ID_HEADER_NAME, "0000000");
        headersMap.put(PSU_ID_HEADER_NAME, PSU_ID);

        TppSignatureValidator tppSignatureValidator = new TppSignatureValidator();

        assertThrows(MissingRequiredHeaderException.class, () -> tppSignatureValidator.verifySignature(signature, tppEncodedValidCert, headersMap));
    }

    @Test
    void when_ValidSignatureAndFalseCert_Expected_False() throws IOException {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(DIGEST_HEADER_NAME, new SignatureGeneratorUtil().generateDigest());
        headersMap.put(TPP_TRANSACTION_ID_HEADER_NAME, "3dc3d5b3-7023-4848-9853-f5400a64e80f");
        headersMap.put(X_REQUEST_ID_HEADER_NAME, "99391c7e-ad88-49ec-a2ad-99ddcb1f7721");
        headersMap.put(PSU_ID_HEADER_NAME, PSU_ID);
        headersMap.put(TIMESTAMP_HEADER_NAME, "Sun, 06 Aug 2017 15:02:37 GMT");

        assertFalse(new TppSignatureValidator().verifySignature(signature, tppEncodedInvalidCert, headersMap));
    }
}

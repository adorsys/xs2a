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

import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SignatureVerifierImplTest {
    private static final JsonReader jsonReader = new JsonReader();

    private SignatureVerifierImpl signatureVerifier = new SignatureVerifierImpl();
    private static final String POST_METHOD = "POST";
    private static final String GET_METHOD = "GET";
    private static final String URI = "/request-uri/example";
    private Map<String, String> headerMap;

    @BeforeEach
    void setUp() {
        fillDefaultHeaders();
    }

    @Test
    void verify_success_POST() {
        // when
        boolean actualResult = signatureVerifier.verify(signature(), certificate(), headerMap, POST_METHOD, URI);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void verify_nonNormalisedCertificate_success_POST() {
        // when
        boolean actualResult = signatureVerifier.verify(signature(), nonNormalizedCertificate(), headerMap, POST_METHOD, URI);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void verify_success_GET() {
        // when
        boolean actualResult = signatureVerifier.verify(signature(), certificate(), headerMap, GET_METHOD, URI);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void verify_emptySomeHeaders() {
        headerMap.remove("x-request-id");
        headerMap.remove("psu-ip-address");

        // when
        boolean actualResult = signatureVerifier.verify(signature(), certificate(), headerMap, POST_METHOD, URI);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void verify_wrongSignatureHash() {
        // when
        boolean actualResult = signatureVerifier.verify(wrongSignatureHash(), certificate(), headerMap, POST_METHOD, URI);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void verify_wrongSignatureHeader() {
        // when
        boolean actualResult = signatureVerifier.verify(signatureWrongHeaders(), certificate(), headerMap, POST_METHOD, URI);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void verify_wrongSignatureKeyId() {
        // when
        boolean actualResult = signatureVerifier.verify(signatureWrongKeyId(), certificate(), headerMap, POST_METHOD, URI);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void verify_wrongTppCertificate() {
        // when
        boolean actualResult = signatureVerifier.verify(signature(), wrongCertificate(), headerMap, POST_METHOD, URI);

        // then
        assertThat(actualResult).isFalse();
    }

    private void fillDefaultHeaders() {
        headerMap = new HashMap<>();
        headerMap.put("accept", "application/json");
        headerMap.put("psu-ip-address", "1.1.1.1");
        headerMap.put("psu-id", "anton.brueckner");
        headerMap.put("x-request-id", "2f77a125-aa7a-45c0-b414-cea25a116035");
        headerMap.put("signature", signature());
        headerMap.put("digest", "SHA-256=cE4iyBDKyP5qdfUyHuB4eZf5iqA5pSAjTzl8l89Oh20=");
        headerMap.put("date", "Sun, 06 Aug 2019 15:02:37 GMT");
        headerMap.put("tpp-signature-certificate", certificate());
        headerMap.put("tpp-redirect-uri", "http://bank.de.com/redirect-uri");
    }

    private String certificate() {
        return jsonReader.getStringFromFile("signature/tpp_signature_certificate.txt");
    }

    private String nonNormalizedCertificate() {
        return jsonReader.getStringFromFile("signature/tpp_signature_non_normalised_certificate.txt");
    }

    private String signature() {
        return jsonReader.getStringFromFile("signature/correct_signature.txt");
    }

    private String signatureWrongKeyId() {
        return jsonReader.getStringFromFile("signature/signature_wrong_keyid.txt");
    }

    private String signatureWrongHeaders() {
        return jsonReader.getStringFromFile("signature/signature_wrong_headers.txt");
    }

    private String wrongSignatureHash() {
        return jsonReader.getStringFromFile("signature/signature_wrong_hash.txt");
    }

    private String wrongCertificate() {
        return jsonReader.getStringFromFile("signature/wrong_tpp_signature_certificate.txt");
    }
}

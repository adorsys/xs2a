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

import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SignatureVerifierImplTest {
    private static final JsonReader jsonReader = new JsonReader();

    private SignatureVerifierImpl signatureVerifier = new SignatureVerifierImpl();
    private static final String POST_METHOD = "POST";
    private static final String GET_METHOD = "GET";
    private static final String URI = "/request-uri/example";
    private Map<String, String> headerMap = new HashMap();

    @Before
    public void setUp() {
        fillDefaultHeaders();
    }

    @Test
    public void verify_success_POST() {
        // when
        boolean actualResult = signatureVerifier.verify(signature(), certificate(), headerMap, POST_METHOD, URI);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    public void verify_success_GET() {
        // when
        boolean actualResult = signatureVerifier.verify(signature(), certificate(), headerMap, GET_METHOD, URI);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    public void verify_emptySomeHeaders() {
        headerMap.remove("x-request-id");
        headerMap.remove("psu-ip-address");

        // when
        boolean actualResult = signatureVerifier.verify(signature(), certificate(), headerMap, POST_METHOD, URI);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    public void verify_wrongSignatureHash() {
        // when
        boolean actualResult = signatureVerifier.verify(wrongSignatureHash(), certificate(), headerMap, POST_METHOD, URI);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    public void verify_wrongSignatureHeader() {
        // when
        boolean actualResult = signatureVerifier.verify(signatureWrongHeaders(), certificate(), headerMap, POST_METHOD, URI);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    public void verify_wrongSignatureKeyId() {
        // when
        boolean actualResult = signatureVerifier.verify(signatureWrongKeyId(), certificate(), headerMap, POST_METHOD, URI);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    public void verify_wrongTppCertificate() {
        // when
        boolean actualResult = signatureVerifier.verify(signature(), wrong_certificate(), headerMap, POST_METHOD, URI);

        // then
        assertThat(actualResult).isFalse();
    }

    private void fillDefaultHeaders() {
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

    private String wrong_certificate() {
        return jsonReader.getStringFromFile("signature/wrong_tpp_signature_certificate.txt");
    }
}

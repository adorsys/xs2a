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

package de.adorsys.psd2.validator.signature.service;

import de.adorsys.psd2.validator.signature.service.algorithm.EncodingAlgorithm;
import de.adorsys.psd2.validator.signature.service.algorithm.HashingAlgorithm;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class DigestTest {
    private static final String REQUEST_BODY = "{\"hello\": \"world\"}";
    private static final Charset UTF8_CHARSET = StandardCharsets.UTF_8;
    private static final String EQUAL_SEPARATOR = "=";

    private static final String HASHING_ALGORITHM_256 = "SHA-256";
    private static final String DIGEST_VALUE_256 = "X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=";

    private static final String HASHING_ALGORITHM_512 = "SHA-512";
    private static final String DIGEST_VALUE_512 = "WZDPaVn/7XgHaAy8pmojAkGWoRx2UFChF41A2svX+TaPm+AbwAgBWnrIiYllu7BNNyealdVLvRwEmTHWXvJwew==";

    private static final String EXPECTED_DIGEST_HEADER_256_VALUE = HASHING_ALGORITHM_256 + EQUAL_SEPARATOR + DIGEST_VALUE_256;
    private static final String EXPECTED_DIGEST_HEADER_512_VALUE = HASHING_ALGORITHM_512 + EQUAL_SEPARATOR + DIGEST_VALUE_512;

    @Test
    public void build_256() {
        // given
        EncodingAlgorithm encodingAlgorithm = EncodingAlgorithm.BASE64;
        HashingAlgorithm hashingAlgorithm = HashingAlgorithm.SHA256;

        // when
        Digest digest = Digest.builder()
                            .requestBody(REQUEST_BODY)
                            .hashingAlgorithm(hashingAlgorithm)
                            .encodingAlgorithm(encodingAlgorithm)
                            .charset(UTF8_CHARSET)
                            .build();

        // then
        assertThat(digest.getHeaderValue()).isEqualTo(EXPECTED_DIGEST_HEADER_256_VALUE);
    }

    @Test
    public void build_512() {
        // given
        EncodingAlgorithm encodingAlgorithm = EncodingAlgorithm.BASE64;
        HashingAlgorithm hashingAlgorithm = HashingAlgorithm.SHA512;

        // when
        Digest digest = Digest.builder()
                            .requestBody(REQUEST_BODY)
                            .hashingAlgorithm(hashingAlgorithm)
                            .encodingAlgorithm(encodingAlgorithm)
                            .charset(UTF8_CHARSET)
                            .build();

        // then
        assertThat(digest.getHeaderValue()).isEqualTo(EXPECTED_DIGEST_HEADER_512_VALUE);
    }
}

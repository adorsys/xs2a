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

package de.adorsys.psd2.validator.signature.service;

import de.adorsys.psd2.validator.signature.service.algorithm.EncodingAlgorithm;
import de.adorsys.psd2.validator.signature.service.algorithm.HashingAlgorithm;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class DigestTest {
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
    void build_256() {
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
    void build_512() {
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

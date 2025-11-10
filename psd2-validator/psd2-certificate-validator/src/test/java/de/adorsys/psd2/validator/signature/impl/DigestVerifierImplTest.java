/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.validator.signature.impl;

import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DigestVerifierImplTest {
    private static final JsonReader jsonReader = new JsonReader();
    private static final String CORRECT_DIGEST = "SHA-256=VJzQpqwPuSYNFHjvNH1T9cUQWceEHdv0IIMjPvvrklQ=";
    private static final String WRONG_ALGORITHM_DIGEST = "SHA-512=VJzQpqwPuSYNFHjvNH1T9cUQWceEHdv0IIMjPvvrklQ=";
    private static final String EMPTY_ALGORITHM_DIGEST = "jkdgfdt43VJzQpqwPuSYNFHjvNH1T9cUQWceEHdv0IIMjPvvrklQ=";
    private static final String WRONG_DIGEST = "SHA-256=VJzQpqwPuSYN_wrong_digest_0IIMjPvvrklQ=";

    private DigestVerifierImpl digestVerifier;

    @BeforeEach
    void setUp() {
        digestVerifier = new DigestVerifierImpl();
    }

    @Test
    void verify_success() {
        // when
        boolean actualResult = digestVerifier.verify(CORRECT_DIGEST, getBody());

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void verify_wrong_digest() {
        // when
        boolean actualResult = digestVerifier.verify(WRONG_DIGEST, getBody());

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void verify_null_digest() {
        // when
        boolean actualResult = digestVerifier.verify(null, getBody());

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void verify_wrong_algorithm() {
        // when
        boolean actualResult = digestVerifier.verify(WRONG_ALGORITHM_DIGEST, getBody());

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void verify_empty_algorithm() {
        // when
        boolean actualResult = digestVerifier.verify(EMPTY_ALGORITHM_DIGEST, getBody());

        // then
        assertThat(actualResult).isFalse();
    }

    private String getBody() {
        return jsonReader.getStringFromFile("json/payment_request.json");
    }
}

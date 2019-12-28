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

import static org.assertj.core.api.Assertions.assertThat;

public class DigestVerifierImplTest {
    private static final JsonReader jsonReader = new JsonReader();
    private static final String CORRECT_DIGEST = "SHA-256=x2iyTnu8glTS4NQk/X7jdpga/v4+AuxVRteArO42n9c=";
    private static final String WRONG_ALGORITHM_DIGEST = "SHA-512=x2iyTnu8glTS4NQk/X7jdpga/v4+AuxVRteArO42n9c=";
    private static final String EMPTY_ALGORITHM_DIGEST = "jkdgfdt43x2iyTnu8glTS4NQk/X7jdpga/v4+AuxVRteArO42n9c=";
    private static final String WRONG_DIGEST = "SHA-256=x2iyTnu8gl_wrong_digest_RteArO42n9c=";

    private DigestVerifierImpl digestVerifier;

    @Before
    public void setUp() {
        digestVerifier = new DigestVerifierImpl();
    }

    @Test
    public void verify_success() {
        // when
        boolean actualResult = digestVerifier.verify(CORRECT_DIGEST, getBody());

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    public void verify_wrong_digest() {
        // when
        boolean actualResult = digestVerifier.verify(WRONG_DIGEST, getBody());

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    public void verify_null_digest() {
        // when
        boolean actualResult = digestVerifier.verify(null, getBody());

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    public void verify_wrong_algorithm() {
        // when
        boolean actualResult = digestVerifier.verify(WRONG_ALGORITHM_DIGEST, getBody());

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    public void verify_empty_algorithm() {
        // when
        boolean actualResult = digestVerifier.verify(EMPTY_ALGORITHM_DIGEST, getBody());

        // then
        assertThat(actualResult).isFalse();
    }

    private String getBody() {
        return jsonReader.getStringFromFile("json/payment_request.json");
    }
}

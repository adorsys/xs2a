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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Provides hashed value generation according to specification [RFC5843] (Additional Hash Algorithms for HTTP Instance Digests)
 * @see <a href="https://www.rfc-editor.org/rfc/pdfrfc/rfc5843.txt.pdf">HTTP Instance Digests</a>
 *
 */
public class Digest {
    private String headerValue;
    private Digest(String headerValue) {
        this.headerValue = headerValue;
    }

    public static DigestBuilder builder() {
        return new DigestBuilder();
    }

    public String getHeaderValue() {
        return headerValue;
    }

    public static final class DigestBuilder {
        private static final String EQUALS_SIGN_SEPARATOR = "=";

        private String requestBody;
        private HashingAlgorithm hashingAlgorithm = HashingAlgorithm.SHA256;
        private EncodingAlgorithm encodingAlgorithm = EncodingAlgorithm.BASE64;
        private Charset charset = StandardCharsets.UTF_8;

        private DigestBuilder() {
        }

        public DigestBuilder requestBody(String requestBody) {
            this.requestBody = requestBody;
            return this;
        }

        /**
         * @param hashingAlgorithm The only hash algorithms that may be used to calculate the Digest within the context of this specification are SHA-256 and SHA-512 as defined in [RFC5843].
         * @return prepared DigestBuilder with hashingAlgorithm
         */
        public DigestBuilder hashingAlgorithm(HashingAlgorithm hashingAlgorithm) {
            this.hashingAlgorithm = hashingAlgorithm;
            return this;
        }

        /**
         * @param encodingAlgorithm Algorithm for encoding output result after hashing. BASE64 sets as default.
         * @return prepared DigestBuilder with encodingAlgorithm
         */
        DigestBuilder encodingAlgorithm(EncodingAlgorithm encodingAlgorithm) {
            this.encodingAlgorithm = encodingAlgorithm;
            return this;
        }

        public DigestBuilder charset(Charset charset) {
            this.charset = charset;
            return this;
        }

        public Digest build() {
            byte[] digestBytes = hashingAlgorithm.getHashingService()
                                         .hash(requestBody, charset);

            String digestEncoded = encodingAlgorithm.getEncodingService()
                                           .encode(digestBytes);

            return new Digest(buildDigestHeader(hashingAlgorithm.getAlgorithmName(), digestEncoded));
        }

        private String buildDigestHeader(String algorithmName, String digestEncoded) {
            return algorithmName + EQUALS_SIGN_SEPARATOR + digestEncoded;
        }
    }
}

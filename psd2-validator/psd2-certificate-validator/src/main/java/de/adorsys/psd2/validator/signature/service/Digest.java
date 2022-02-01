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

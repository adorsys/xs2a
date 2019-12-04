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

import de.adorsys.psd2.validator.signature.DigestVerifier;
import de.adorsys.psd2.validator.signature.service.CertificateConstants;
import de.adorsys.psd2.validator.signature.service.Digest;
import de.adorsys.psd2.validator.signature.service.algorithm.HashingAlgorithm;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class DigestVerifierImpl implements DigestVerifier {
    @Override
    public boolean verify(String digestFromRequest, String body) {
        if (StringUtils.isBlank(digestFromRequest)) {
            return false;
        }

        return handleHashAlgorithm(digestFromRequest)
                   .map(alg -> buildDigest(body, alg))
                   .map(dg -> digestFromRequest.equals(dg.getHeaderValue()))
                   .orElse(false);
    }

    private Digest buildDigest(String requestBody, HashingAlgorithm algorithm) {
        return Digest.builder()
                   .requestBody(requestBody)
                   .hashingAlgorithm(algorithm)
                   .build();
    }

    private Optional<HashingAlgorithm> handleHashAlgorithm(String digestFromRequest) {
        String[] digestContent = digestFromRequest.split(CertificateConstants.EQUALS_SIGN_SEPARATOR);
        if (digestContent.length < 2) {
            return Optional.empty();
        }

        return HashingAlgorithm.fromValue(digestContent[0]);
    }
}

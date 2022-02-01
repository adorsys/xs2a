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

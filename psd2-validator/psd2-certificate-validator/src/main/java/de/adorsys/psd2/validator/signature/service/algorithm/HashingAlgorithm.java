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

package de.adorsys.psd2.validator.signature.service.algorithm;

import de.adorsys.psd2.validator.signature.service.algorithm.hashing.HashingService;
import de.adorsys.psd2.validator.signature.service.algorithm.hashing.Sha256HashingService;
import de.adorsys.psd2.validator.signature.service.algorithm.hashing.Sha512HashingService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum HashingAlgorithm {
    SHA256("SHA-256", new Sha256HashingService()),
    SHA512("SHA-512", new Sha512HashingService());

    private String algorithmName;
    private HashingService hashingService;

    private static final Map<String, HashingAlgorithm> CONTAINER = new HashMap<>();

    static {
        for (HashingAlgorithm algorithm : values()) {
            CONTAINER.put(algorithm.getAlgorithmName(), algorithm);
        }
    }

    HashingAlgorithm(String algorithmName, HashingService hashingService) {
        this.algorithmName = algorithmName;
        this.hashingService = hashingService;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public HashingService getHashingService() {
        return hashingService;
    }

    public static Optional<HashingAlgorithm> fromValue(String text) {
        if (text != null) {
            return Optional.ofNullable(CONTAINER.get(text.trim()));
        }
        return Optional.empty();
    }
}

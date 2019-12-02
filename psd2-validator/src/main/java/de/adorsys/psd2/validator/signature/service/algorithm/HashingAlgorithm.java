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

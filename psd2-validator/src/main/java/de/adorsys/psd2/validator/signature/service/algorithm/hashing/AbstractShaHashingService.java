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

package de.adorsys.psd2.validator.signature.service.algorithm.hashing;

import de.adorsys.psd2.validator.signature.service.algorithm.HashingAlgorithm;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class AbstractShaHashingService implements HashingService {

    @Override
    public byte[] hash(String data, Charset charset) {
        try {
            return MessageDigest.getInstance(getAlgorithm().getAlgorithmName())
                           .digest(data.getBytes(charset));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("No such hashing algorithm: " + getAlgorithm().getAlgorithmName());
        }
    }

    protected abstract HashingAlgorithm getAlgorithm();
}

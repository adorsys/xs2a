/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.consent;

import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Optional;

@Service
public class Base64AspspDataService implements AspspDataEncoder<byte[], String>, AspspDataDecoder<String, byte[]> {

    @Override
    public String encode(byte[] bytePayload) {
        return Optional.ofNullable(bytePayload)
                   .map(b -> Base64.getEncoder().encodeToString(b))
                   .orElse(null);
    }

    @Override
    public byte[] decode(String base64Payload) {
        return Optional.ofNullable(base64Payload)
                                      .map(s -> Base64.getDecoder().decode(s))
                                      .orElse(null);
    }
}

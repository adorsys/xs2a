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

package de.adorsys.psd2.consent.service;

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

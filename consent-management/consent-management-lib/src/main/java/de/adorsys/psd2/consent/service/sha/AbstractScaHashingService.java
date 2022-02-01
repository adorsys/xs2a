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

package de.adorsys.psd2.consent.service.sha;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class AbstractScaHashingService implements HashingService {
    @Override
    public byte[] hash(byte[] data, Charset charset) {
        try{
            return MessageDigest.getInstance(getAlgorithmName())
                       .digest(data);
        } catch( NoSuchAlgorithmException e){
            throw new IllegalArgumentException("No such hashing algorithm: "  + getAlgorithmName());
        }
    }

    public abstract String getAlgorithmName();
}

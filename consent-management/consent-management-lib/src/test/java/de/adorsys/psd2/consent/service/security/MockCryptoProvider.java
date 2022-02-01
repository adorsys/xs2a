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

package de.adorsys.psd2.consent.service.security;

import de.adorsys.psd2.consent.service.security.provider.CryptoProvider;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;


public class MockCryptoProvider implements CryptoProvider {
    private final boolean alwaysFail;
    private final String cryptoProviderId;

    MockCryptoProvider(String cryptoProviderId, boolean alwaysFail) {
        this.alwaysFail = alwaysFail;
        this.cryptoProviderId = cryptoProviderId;
    }

    @Override
    public Optional<EncryptedData> encryptData(byte[] data, String password) {
        if (alwaysFail) {
            return Optional.empty();
        }

        String encrypted = new String(data) + password;
        return Optional.of(new EncryptedData(encrypted.getBytes()));
    }

    @Override
    public Optional<DecryptedData> decryptData(byte[] data, String password) {
        if (alwaysFail) {
            return Optional.empty();
        }

        String decrypted = StringUtils.removeEnd(new String(data), password);
        return Optional.of(new DecryptedData(decrypted.getBytes()));
    }

    @Override
    public String getCryptoProviderId() {
        return cryptoProviderId;
    }
}

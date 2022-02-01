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

package de.adorsys.psd2.xs2a.service.spi;

import de.adorsys.psd2.consent.api.AspspDataService;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_BYTE_ARRAY;
import static org.apache.commons.lang3.ArrayUtils.nullToEmpty;

/**
 * This is a stateful object that provides access to encrypted AspspConsentData array stored in the database
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class SpiAspspConsentDataProviderImpl implements SpiAspspConsentDataProvider {
    private final String encryptedConsentId;
    private final AspspDataService aspspDataService;

    private byte[] lastKnownData = EMPTY_BYTE_ARRAY;

    @Override
    @NotNull
    public byte[] loadAspspConsentData() {
        byte[] readData = aspspDataService.readAspspConsentData(encryptedConsentId)
                                     .map(AspspConsentData::getAspspConsentDataBytes)
                                     .orElse(EMPTY_BYTE_ARRAY);
        lastKnownData = readData;
        return readData;
    }

    @Override
    public void updateAspspConsentData(@Nullable byte[] aspspConsentData) {
        if (Arrays.equals(nullToEmpty(aspspConsentData), lastKnownData)) {
            // Do nothing if nothing changed
            return;
        }
        if (aspspConsentData == null || Arrays.equals(EMPTY_BYTE_ARRAY, aspspConsentData)) {
            clearAspspConsentData();
            return;
        }

        aspspDataService.updateAspspConsentData(new AspspConsentData(aspspConsentData, encryptedConsentId));
        lastKnownData = aspspConsentData;
    }

    @Override
    public void clearAspspConsentData() {
        aspspDataService.deleteAspspConsentData(encryptedConsentId);
        lastKnownData = EMPTY_BYTE_ARRAY;
    }
}

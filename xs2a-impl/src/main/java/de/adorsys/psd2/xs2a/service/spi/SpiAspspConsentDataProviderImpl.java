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
                                     .map(AspspConsentData::getAspspConsentData)
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

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
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_BYTE_ARRAY;

/**
 * This implementation to be used to temporarily hold consent data array
 * in case if no Consent/Payment ID provided yet.
 * Once it is provided with "saveWith" method, it is saved in the database.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class InitialSpiAspspConsentDataProvider implements SpiAspspConsentDataProvider {
    private final AspspDataService aspspDataService;

    private byte[] aspspConsentData = EMPTY_BYTE_ARRAY;
    private String encryptedConsentId;

    @Override
    @NotNull
    public byte[] loadAspspConsentData() {
        return Arrays.copyOf(aspspConsentData, aspspConsentData.length);
    }

    @Override
    public void updateAspspConsentData(byte[] aspspConsentData) {
        this.aspspConsentData = aspspConsentData;
        if (StringUtils.isNotBlank(encryptedConsentId)) {
            updateAspspConsentDataOnServer();
        }
    }

    @Override
    public void clearAspspConsentData() {
        this.aspspConsentData = EMPTY_BYTE_ARRAY;
        if (StringUtils.isNotBlank(encryptedConsentId)) {
            aspspDataService.deleteAspspConsentData(encryptedConsentId);
        }
    }

    /**
     * Links this consent data object to some consent / payment
     * @param encryptedConsentId Consent/Payment ID that will be returned to TPP
     */
    public void saveWith(@NotNull String encryptedConsentId) {
        this.encryptedConsentId = encryptedConsentId;
        updateAspspConsentDataOnServer();
    }

    private void updateAspspConsentDataOnServer() {
        if (Arrays.equals(EMPTY_BYTE_ARRAY, aspspConsentData)) {
            aspspDataService.deleteAspspConsentData(encryptedConsentId);
        } else {
            aspspDataService.updateAspspConsentData(new AspspConsentData(aspspConsentData, encryptedConsentId));
        }
    }
}

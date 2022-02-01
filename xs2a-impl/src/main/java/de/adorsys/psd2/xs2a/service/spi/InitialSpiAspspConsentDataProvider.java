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

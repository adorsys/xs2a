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
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * Allows to establish stateful AspspConsentDataProvider objects in Spring Context
 */
@Component
@RequiredArgsConstructor
public class SpiAspspConsentDataProviderFactory {
    private final AspspDataService aspspDataService;

    /**
     * Establishes SpiAspspConsentDataProvider object that is linked to existing Consent/Payment ID
     * @param encryptedConsentId Consent/Payment ID received from TPP
     * @return stateful SpiAspspConsentDataProvider object
     */
    @NotNull
    public SpiAspspConsentDataProvider getSpiAspspDataProviderFor(@NotNull String encryptedConsentId) {
        return new SpiAspspConsentDataProviderImpl(encryptedConsentId, aspspDataService);
    }

    /**
     * Prrovides SpiAspspConsentDataProvider object to store AspspConsentData array if Consent/Payment ID is not yet set
     * @return stateful SpiAspspConsentDataProvider object
     */
    @NotNull
    public InitialSpiAspspConsentDataProvider getInitialAspspConsentDataProvider() {
        return new InitialSpiAspspConsentDataProvider(aspspDataService);
    }
}

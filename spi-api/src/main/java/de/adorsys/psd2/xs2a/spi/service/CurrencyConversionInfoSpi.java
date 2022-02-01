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

package de.adorsys.psd2.xs2a.spi.service;

import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiCurrencyConversionInfo;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import org.jetbrains.annotations.NotNull;

/**
 * Interface to be used for currency conversion info SPI implementation
 */
public interface CurrencyConversionInfoSpi {//maybe need to create default impl

    /**
     * Reads currency conversion info of payment
     *
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param payment                  SpiPayment
     * @param authorisationId          a unique identifier of authorisation process
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return Returns SpiCurrencyConversionInfo as a part of SpiResponse
     */
    @NotNull
    default SpiResponse<SpiCurrencyConversionInfo> getCurrencyConversionInfo(@NotNull SpiContextData contextData, @NotNull SpiPayment payment, @NotNull String authorisationId, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return SpiResponse.<SpiCurrencyConversionInfo>builder().build();
    }
}

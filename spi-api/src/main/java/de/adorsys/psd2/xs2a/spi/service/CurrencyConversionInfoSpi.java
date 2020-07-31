/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

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
import de.adorsys.psd2.xs2a.spi.domain.fund.SpiFundsConfirmationRequest;
import de.adorsys.psd2.xs2a.spi.domain.fund.SpiFundsConfirmationResponse;
import de.adorsys.psd2.xs2a.spi.domain.piis.SpiPiisConsent;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FundsConfirmationSpi {
    /**
     * Queries ASPSP to check the sufficiency of requested account funds
     *
     * @param contextData                 holder of call's context data (e.g. about PSU and TPP)
     * @param spiPiisConsent              PIIS Consent object. May be null if the request is done from a workflow without a consent.
     * @param spiFundsConfirmationRequest Object, that contains all request data from TPP
     * @param aspspConsentDataProvider    Provides access to read/write encrypted data to be stored in the consent
     *                                    management system. May be null if PIIS consent is null, because we can't save
     *                                    anything in ASPSP consent data in case when there is no PIIS consent.
     * @return Funds confirmation response with information whether the requested amount can be booked on the account or not
     */
    @NotNull
    SpiResponse<SpiFundsConfirmationResponse> performFundsSufficientCheck(@NotNull SpiContextData contextData,
                                                                          @Nullable SpiPiisConsent spiPiisConsent,
                                                                          @NotNull SpiFundsConfirmationRequest spiFundsConfirmationRequest,
                                                                          @Nullable SpiAspspConsentDataProvider aspspConsentDataProvider);
}

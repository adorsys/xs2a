/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.piis.PiisConsent;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.fund.SpiFundsConfirmationRequest;
import de.adorsys.psd2.xs2a.spi.domain.fund.SpiFundsConfirmationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FundsConfirmationSpi {
    /**
     * Queries ASPSP to check the sufficiency of requested account funds
     *
     * @param contextData                 holder of call's context data (e.g. about PSU and TPP)
     * @param piisConsent                 PIIS Consent object. May be null if the request is done from a workflow without a consent.
     * @param spiFundsConfirmationRequest Object, that contains all request data from TPP
     * @param aspspConsentData            Encrypted data that may be stored in the consent management system in the consent linked to a request.<br>
     *                                    May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return Funds confirmation response with information whether the requested amount can be booked on the account or not
     */
    @NotNull
    SpiResponse<SpiFundsConfirmationResponse> performFundsSufficientCheck(@NotNull SpiContextData contextData, @Nullable PiisConsent piisConsent, @NotNull SpiFundsConfirmationRequest spiFundsConfirmationRequest, @NotNull AspspConsentData aspspConsentData);
}

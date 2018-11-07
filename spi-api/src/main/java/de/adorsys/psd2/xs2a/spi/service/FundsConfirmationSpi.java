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

import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.fund.SpiFundsConfirmationConsent;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FundsConfirmationSpi {
    /**
     * Queries ASPSP to check the sufficiency of requested account funds
     *
     * @param psuData          ASPSP identifier(s) of the psu
     * @param consent          Consent for funds confirmation
     * @param reference        PSU account data
     * @param amount           Requested amount of funds
     * @param aspspConsentData Encrypted data that may be stored in the consent management system in the consent linked to a request.<br>
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return 'true' if the requested amount can be booked on the account, 'false' otherwise
     */
    @NotNull
    SpiResponse<Boolean> performFundsSufficientCheck(@NotNull SpiPsuData psuData, @Nullable SpiFundsConfirmationConsent consent, @NotNull SpiAccountReference reference, @NotNull SpiAmount amount, @NotNull AspspConsentData aspspConsentData);
}

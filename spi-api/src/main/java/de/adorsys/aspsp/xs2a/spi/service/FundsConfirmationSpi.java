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

package de.adorsys.aspsp.xs2a.spi.service;

import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.fund.SpiFundsConfirmationConsent;

public interface FundsConfirmationSpi {
    /**
     * Queries ASPSP to check the sufficiency of requested account funds
     *
     * @param reference        PSU account data
     * @param amount           Requested amount of funds
     * @param aspspConsentData Encrypted data that may be stored in the consent management system in the consent linked to a request.<br>
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return 'true' if the requested amount can be booked on the account, 'false' otherwise
     */
    SpiResponse<Boolean> peformFundsSufficientCheck(SpiFundsConfirmationConsent consent, SpiAccountReference reference, SpiAmount amount, AspspConsentData aspspConsentData);
}

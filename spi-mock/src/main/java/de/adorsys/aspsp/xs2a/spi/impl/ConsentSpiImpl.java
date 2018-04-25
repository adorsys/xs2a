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

package de.adorsys.aspsp.xs2a.spi.impl;

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiCreateConsentRequest;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
import de.adorsys.aspsp.xs2a.spi.test.data.ConsentMockData;

public class ConsentSpiImpl implements ConsentSpi {

    @Override
    public String createAccountConsents(SpiCreateConsentRequest accountInformationConsentRequest,
                                        boolean withBalance, boolean tppRedirectPreferred) {

        return ConsentMockData.createAccountConsent(accountInformationConsentRequest, withBalance, tppRedirectPreferred);
    }

    @Override
    public SpiTransactionStatus getAccountConsentStatusById(String consentId) {
        return ConsentMockData.getAccountConsentsStatus(consentId);

    }

    @Override
    public SpiAccountConsent getAccountConsentById(String consentId) {
        return ConsentMockData.getAccountConsent(consentId);
    }

    @Override
    public boolean deleteAccountConsentsById(String consentId) {
        if (ConsentMockData.getAccountConsent(consentId) != null) {
            ConsentMockData.deleteAccountConcent(consentId);
            return true;
        }
        return false;
    }
}

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

package de.adorsys.psd2.consent.api.service;

import de.adorsys.psd2.consent.api.piis.CmsPiisValidationInfo;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceSelector;

import java.util.Currency;
import java.util.List;

public interface PiisConsentService {

    /**
     * Retrieves piis consent data for validation by account reference
     *
     * @param currency              Chosen currency
     * @param accountIdentifierName The name of account reference identifier
     * @param accountIdentifier     The value of account reference identifier
     * @return Validation information about piis consents
     */
    List<CmsPiisValidationInfo> getPiisConsentListByAccountIdentifier(Currency currency, AccountReferenceSelector accountIdentifierName, String accountIdentifier);
}

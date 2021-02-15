/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.ais;

import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.consent.CardAccountHandler;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AccountServicesHolder {
    private final Xs2aAisConsentService aisConsentService;
    private final TppService tppService;
    private final AccountHelperService accountHelperService;
    private final CardAccountHandler cardAccountHandler;

    public Optional<AisConsent> getAccountConsentById(String consentId) {
        return aisConsentService.getAccountConsentById(consentId);
    }

    public SpiAccountReference findAccountReference(List<AccountReference> accountReferences, String accountId) {
        return accountHelperService.findAccountReference(accountReferences, accountId);
    }

    public SpiContextData getSpiContextData() {
        return accountHelperService.getSpiContextData();
    }

    public String getTppId() {
        return tppService.getTppId();
    }

    ActionStatus createActionStatus(boolean withBalance, TypeAccess typeAccess, ResponseObject response) {
        return accountHelperService.createActionStatus(withBalance, typeAccess, response);
    }

    public boolean needsToUpdateUsage(AisConsent aisConsent) {
        return accountHelperService.needsToUpdateUsage(aisConsent);
    }

    public void consentActionLog(String tppId, String consentId, ActionStatus actionStatus, String requestUri, boolean updateUsage,
                                 String resourceId, String transactionId) {
        aisConsentService.consentActionLog(tppId, consentId, actionStatus, requestUri, updateUsage, resourceId, transactionId);
    }

    public String hidePanInAccountReference(String pan) {
        return cardAccountHandler.hidePanInAccountReference(pan);
    }
}

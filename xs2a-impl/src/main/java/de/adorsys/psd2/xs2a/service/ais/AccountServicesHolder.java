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

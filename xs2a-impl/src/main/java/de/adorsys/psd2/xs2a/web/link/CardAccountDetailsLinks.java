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

package de.adorsys.psd2.xs2a.web.link;

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public class CardAccountDetailsLinks extends AbstractLinks {

    public CardAccountDetailsLinks(String httpUrl, String accountId, AisConsent aisConsent) {
        super(httpUrl);

        AccountAccess accountAccess = aisConsent.getAccess();
        AisConsentData consentData = aisConsent.getConsentData();
        boolean isConsentGlobal = consentData.getAllPsd2() != null;
        List<AccountReference> balances = accountAccess.getBalances();
        if (hasAccessToCardSource(balances) &&
                isValidAccountByAccess(accountId, balances, isConsentGlobal)) {

            setBalances(buildPath(UrlHolder.CARD_BALANCES_URL, accountId));
        }

        List<AccountReference> transactions = accountAccess.getTransactions();

        if (hasAccessToCardSource(transactions) &&
                isValidAccountByAccess(accountId, accountAccess.getTransactions(), isConsentGlobal)) {
            setTransactions(buildPath(UrlHolder.CARD_TRANSACTIONS_URL, accountId));
        }
    }

    private boolean isValidAccountByAccess(String accountId, List<AccountReference> allowedAccountData, boolean isConsentGlobal) {
        return isConsentGlobal ||
                   CollectionUtils.isNotEmpty(allowedAccountData)
                       && allowedAccountData.stream()
                              .anyMatch(a -> accountId.equals(a.getResourceId()));
    }

    private boolean hasAccessToCardSource(List<AccountReference> references) {
        if (CollectionUtils.isEmpty(references)) {
            return true;
        }
        return !references.stream()
                    .allMatch(AccountReference::isNotCardAccount);
    }
}

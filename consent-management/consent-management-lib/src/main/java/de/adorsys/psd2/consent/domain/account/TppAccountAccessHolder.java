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

package de.adorsys.psd2.consent.domain.account;

import de.adorsys.psd2.consent.api.AccountInfo;
import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import lombok.Value;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Currency;
import java.util.EnumSet;
import java.util.List;

import static de.adorsys.psd2.consent.api.TypeAccess.*;

@Value
public class TppAccountAccessHolder extends AccountAccessHolder<TppAccountAccess> {

    public TppAccountAccessHolder(AisAccountAccessInfo accountAccessInfo) {
        super(accountAccessInfo);
    }

    @Override
    public void doFillAccess(List<AccountInfo> info, TypeAccess typeAccess) {
        if (CollectionUtils.isNotEmpty(info)) {
            info.forEach(a -> addAccountAccess(a.getAccountIdentifier(),
                typeAccess,
                a.getAccountType(),
                getCurrencyByString(a.getCurrency())));
        }
    }

    private void addAccountAccess(String accountIdentifier, TypeAccess typeAccess, AccountReferenceType accountReferenceType, Currency currency) {
        accountAccesses.add(new TppAccountAccess(accountIdentifier, typeAccess, accountReferenceType, currency));
        if (EnumSet.of(BALANCE, TRANSACTION).contains(typeAccess)) {
            accountAccesses.add(new TppAccountAccess(accountIdentifier, ACCOUNT, accountReferenceType, currency));
        }
    }
}

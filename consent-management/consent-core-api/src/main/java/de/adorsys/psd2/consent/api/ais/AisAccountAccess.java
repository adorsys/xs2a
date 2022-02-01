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

package de.adorsys.psd2.consent.api.ais;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import lombok.Value;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Value
public class AisAccountAccess {
    private List<AccountReference> accounts;
    private List<AccountReference> balances;
    private List<AccountReference> transactions;

    private String availableAccounts;
    private String allPsd2;
    private String availableAccountsWithBalance;
    @Nullable
    private AdditionalInformationAccess accountAdditionalInformationAccess;

    @JsonIgnore
    public boolean isNotEmpty() {
        return !(CollectionUtils.isEmpty(accounts)
                     && CollectionUtils.isEmpty(balances)
                     && CollectionUtils.isEmpty(transactions)
                     && allPsd2 == null
                     && availableAccounts == null
                     && availableAccountsWithBalance == null);
    }
}

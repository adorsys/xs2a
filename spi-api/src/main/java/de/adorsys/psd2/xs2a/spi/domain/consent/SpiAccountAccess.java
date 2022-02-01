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

package de.adorsys.psd2.xs2a.spi.domain.consent;

import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAdditionalInformationAccess;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpiAccountAccess {
    private List<SpiAccountReference> accounts;
    private List<SpiAccountReference> balances;
    private List<SpiAccountReference> transactions;
    private AccountAccessType availableAccounts;
    private AccountAccessType allPsd2;
    private AccountAccessType availableAccountsWithBalance;
    private SpiAdditionalInformationAccess spiAdditionalInformationAccess;

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(this.accounts)
                   && CollectionUtils.isEmpty(this.balances)
                   && CollectionUtils.isEmpty(this.transactions)
                   && this.allPsd2 == null
                   && this.availableAccounts == null
                   && this.availableAccountsWithBalance == null;
    }
}

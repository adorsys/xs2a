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

package de.adorsys.psd2.xs2a.domain.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.adorsys.psd2.xs2a.core.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceSelector;
import de.adorsys.psd2.xs2a.domain.CashAccountType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.Xs2aBalance;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Currency;
import java.util.List;

import static de.adorsys.psd2.xs2a.core.profile.AccountReferenceType.*;

@Data
public class Xs2aAccountDetails {
    private final String aspspAccountId;
    @Size(max = 35)
    @NotNull
    private final String resourceId;

    private final String iban;

    private final String bban;

    @Size(max = 35)
    private final String pan;

    @Size(max = 35)
    private final String maskedPan;

    @Size(max = 35)
    private final String msisdn;

    @NotNull
    private final Currency currency;

    private final String name;

    private final String displayName;

    @Size(max = 35)
    private final String product;

    private final CashAccountType cashAccountType;

    private final AccountStatus accountStatus;

    private final String bic;

    @Size(max = 70)
    private final String linkedAccounts;

    private final Xs2aUsageType usageType;

    private final String details;

    private final List<Xs2aBalance> balances;

    private Links links = new Links();

    private final String ownerName;

    private final Xs2aAddress ownerAddress;

    @JsonIgnore
    public AccountReferenceSelector getAccountSelector() {
        if (StringUtils.isNotBlank(iban)) {
            return new AccountReferenceSelector(IBAN, this.iban);
        }
        if (StringUtils.isNotBlank(bban)) {
            return new AccountReferenceSelector(BBAN, this.bban);
        }
        if (StringUtils.isNotBlank(pan)) {
            return new AccountReferenceSelector(PAN, this.pan);
        }
        if (StringUtils.isNotBlank(msisdn)) {
            return new AccountReferenceSelector(MSISDN, this.msisdn);
        }
        if (StringUtils.isNotBlank(maskedPan)) {
            return new AccountReferenceSelector(MASKED_PAN, this.maskedPan);
        }
        throw new IllegalArgumentException("At least one account reference property must be set!");
    }
}

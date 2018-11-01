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

package de.adorsys.psd2.xs2a.spi.domain.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Currency;
import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
public class SpiAccountDetails {
    private String resourceId;
    /**
     * International Bank Account Number
     * 2 letters CountryCode + 2 digits checksum + BBAN
     * DE89 3704 0044 0532 0130 00 (Sample for Germany)
     */
    private String iban;
    /**
     * Basic Bank Account Number
     * 8 symbols bank id + account number
     * 3704 0044 0532 0130 00 (Sample for Germany)
     */
    private String bban;
    /**
     * Primary Account Number
     * 0000 0000 0000 0000 (Example)
     */
    private String pan;

    /**
     * Same as previous, several signs are masked with "*"
     */
    private String maskedPan;

    /**
     * Mobile Subscriber Integrated Services Digital Number
     * 00499113606980 (Adorsys tel nr)
     */
    private String msisdn;
    private Currency currency;
    private String name;
    private String product;
    private SpiAccountType cashSpiAccountType;
    private SpiAccountStatus spiAccountStatus;

    /**
     * SWIFT
     * 4 letters bankCode + 2 letters CountryCode + 2 symbols CityCode + 3 symbols BranchCode
     * DEUTDE8EXXX (Deuche Bank AG example)
     */
    private String bic;
    private String linkedAccounts;
    private SpiUsageType usageType;
    private String details;

    private List<SpiAccountBalance> balances;

    @JsonIgnore
    public Optional<SpiAccountBalance> getFirstBalance() {
        return balances.stream()
                   .filter(bal -> SpiBalanceType.INTERIM_AVAILABLE == bal.getSpiBalanceType())
                   .findFirst();
    }

    public void updateFirstBalance(SpiAccountBalance balance) {
        balances.stream()
            .filter(bal -> SpiBalanceType.INTERIM_AVAILABLE == bal.getSpiBalanceType())
            .findFirst()
            .map(b -> balance);
    }

    public void emptyBalances() {
        balances = null;
    }
}

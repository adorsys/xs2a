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

package de.adorsys.aspsp.xs2a.spi.domain.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.springframework.data.annotation.Id;

import java.util.Currency;
import java.util.List;
import java.util.Optional;

@Value
@AllArgsConstructor
public class SpiAccountDetails {
    @Id
    @Setter
    @NonFinal
    private String id;
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
     * Permanent Account Number
     * 3 letters + 1 letter(Type) + 1 letter(First letter of Surname)+ 4 digits +1 check digit
     * Types: P-Individual, C-Company, A-Association, F-Firm, H-HUF, T-Trust
     * AEYPM5403H (Example)
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
    private String accountType;
    private SpiAccountType cashSpiAccountType;

    /**
     * SWIFT
     * 4 letters bankCode + 2 letters CountryCode + 2 symbols CityCode + 3 symbols BranchCode
     * DEUTDE8EXXX (Deuche Bank AG example)
     */
    private String bic;
    private List<SpiBalances> balances;

    @JsonIgnore
    public Optional<SpiBalances> getFirstBalance() {
        return balances.isEmpty()
                   ? Optional.empty()
                   : Optional.of(balances.get(0));
    }

    public void updateFirstBalance(SpiBalances balance) {
        balances.set(0, balance);
    }
}

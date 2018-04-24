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
import org.springframework.util.CollectionUtils;

import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.springframework.util.CollectionUtils.isEmpty;

@Value
@AllArgsConstructor
public class SpiAccountDetails {
    @Id
    @Setter
    @NonFinal
    private String id;
    private String iban;
    private String bban;
    private String pan;
    private String maskedPan;
    private String msisdn;
    private Currency currency;
    private String name;
    private String accountType;
    private SpiAccountType cashSpiAccountType;
    private String bic;
    private List<SpiBalances> balances;

    @JsonIgnore
    public Optional<SpiBalances> getFirstBalance() {
        return isEmpty(balances)
               ? Optional.of(balances.get(0))
               : Optional.empty();
    }

    public void updateFirstBalance(SpiBalances balance) {
        balances.set(0, balance);
    }
}

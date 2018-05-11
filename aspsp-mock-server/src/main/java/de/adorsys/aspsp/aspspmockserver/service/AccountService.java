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

package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.aspspmockserver.repository.AccountRepository;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AccountService {
    private AccountRepository accountRepository;
    private ConsentService consentService;

    public SpiAccountDetails addOrUpdateAccount(SpiAccountDetails accountDetails) {
        return accountRepository.save(accountDetails);
    }

    public List<SpiAccountDetails> getAllAccounts(String consentId, boolean withBalance) {
        return Optional.ofNullable(consentService.getConsent(consentId))
                   .map(con -> getSpiAccountDetailsByConsent(con.get(), withBalance))
                   .orElse(Collections.emptyList());
    }

    private List<SpiAccountDetails> getSpiAccountDetailsByConsent(SpiAccountConsent consent, boolean withBalance) {
        Set<String> accounts = getAccounts(consent.getAccess().getAccounts());
        accounts.addAll(getAccounts(consent.getAccess().getBalances()));
        accounts.addAll(getAccounts(consent.getAccess().getTransactions()));
        List<SpiAccountDetails> accountDetailsList = accountRepository.findByIbanIn(accounts);

        if (!withBalance || !consent.isWithBalance()) {
            return accountDetailsList.stream()
                       .map(this::getAccountDetailsWithoutBalances)
                       .collect(Collectors.toList());
        }
        return accountDetailsList;
    }

    public Optional<SpiAccountDetails> getAccount(String id) {
        return Optional.ofNullable(accountRepository.findOne(id));
    }

    public boolean deleteAccountById(String id) {
        if (id != null && accountRepository.exists(id)) {
            accountRepository.delete(id);
            return true;
        }
        return false;
    }

    public Optional<List<SpiBalances>> getBalances(String accountId) {
        return Optional.ofNullable(accountRepository.findOne(accountId))
                   .map(SpiAccountDetails::getBalances);
    }

    private SpiAccountDetails getAccountDetailsWithoutBalances(SpiAccountDetails accountDetails) {
        return new SpiAccountDetails(accountDetails.getId(), accountDetails.getIban(), accountDetails.getBban(), accountDetails.getPan(),
            accountDetails.getMaskedPan(), accountDetails.getMsisdn(), accountDetails.getCurrency(), accountDetails.getName(),
            accountDetails.getAccountType(), accountDetails.getCashSpiAccountType(), accountDetails.getBic(), null);
    }

    private Set<String> getAccounts(List<SpiAccountReference> list) {
        return Optional.ofNullable(list)
                   .map(l -> l.stream()
                                 .map(SpiAccountReference::getIban)
                                 .collect(Collectors.toSet()))
                   .orElse(Collections.emptySet());
    }
}

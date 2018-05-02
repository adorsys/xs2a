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

import java.util.List;
import java.util.Objects;
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
        SpiAccountConsent consent = consentService.getConsent(consentId);
        List<SpiAccountDetails> result = null;
        if (consent != null) {
            Set<String> accounts = Optional.ofNullable(getAccounts(consent.getAccess().getAccounts()))
                .orElse(null);
            result = accountRepository.findByIdIn(accounts);

            if (!withBalance || !consent.isWithBalance()) {
                result = Objects.requireNonNull(result).stream().map(this::deleteBalances).collect(Collectors.toList());
            }
        }
        return result;
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

    private SpiAccountDetails deleteBalances(SpiAccountDetails initialId) {
        return new SpiAccountDetails(initialId.getId(), initialId.getIban(), initialId.getBban(), initialId.getPan(),
            initialId.getMaskedPan(), initialId.getMsisdn(), initialId.getCurrency(), initialId.getName(),
            initialId.getAccountType(), initialId.getCashSpiAccountType(), initialId.getBic(), null);
    }

    private Set<String> getAccounts(List<SpiAccountReference> list) {
        return Optional.ofNullable(list)
            .map(l -> l.stream()
                .map(SpiAccountReference::getAccountId)
                .collect(Collectors.toSet())).orElse(null);
    }
}

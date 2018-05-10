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

import de.adorsys.aspsp.aspspmockserver.repository.PsuRepository;
import de.adorsys.aspsp.xs2a.spi.domain.Psu;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@AllArgsConstructor
public class AccountService {
    private final PsuRepository psuRepository;

    public Optional<SpiAccountDetails> addAccount(SpiAccountDetails accountDetails) {
        return psuRepository.findPsuByAccountDetailsList_Iban(getAccountIban(accountDetails))
            .map(psu -> addAccountInPsu(psu, accountDetails))
            .orElse(Optional.empty());
    }

    public Optional<SpiAccountDetails> updateAccount(SpiAccountDetails accountDetails) {
        return psuRepository.findPsuByAccountDetailsList_Iban(getAccountIban(accountDetails))
            .map(psu -> updateAccountInPsu(psu, accountDetails))
            .orElse(Optional.empty());
    }

    public List<SpiAccountDetails> getAllAccounts(String psuId, boolean withBalance) {
        //TODO this is a task https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/71
        return Collections.emptyList();
    }

    public Optional<SpiAccountDetails> getAccountById(String accountId) {
        return psuRepository.findPsuByAccountDetailsList_Id(accountId)
            .map(psu -> findAccountInListById(psu.getAccountDetailsList(), accountId))
            .orElse(Optional.empty());
    }

    public Optional<SpiAccountDetails> getAccountByIban(String iban, Currency currency) {
        return psuRepository.findPsuByAccountDetailsList_Iban(iban)
            .map(psu -> findAccountInListByIban(psu.getAccountDetailsList(), iban, currency))
            .orElse(Optional.empty());
    }

    public boolean deleteAccountById(String accountId) {
        return psuRepository.findPsuByAccountDetailsList_Id(accountId)
            .map(psu -> removeAccountFromPsu(psu, accountId))
            .orElse(false);
    }

    public List<SpiBalances> getBalances(String accountId) {
        return psuRepository.findPsuByAccountDetailsList_Id(accountId)
            .map(psu -> getAccountBalances(psu, accountId))
            .orElse(Collections.emptyList());
    }

    private Optional<SpiAccountDetails> addAccountInPsu(Psu psu, SpiAccountDetails accountDetails) {
        psu.getAccountDetailsList().add(accountDetails);
        psuRepository.save(psu);
        return Optional.of(accountDetails);
    }

    private Optional<SpiAccountDetails> updateAccountInPsu(Psu psu, SpiAccountDetails accountDetails) {
        List<SpiAccountDetails> newList = deleteAccountFromListById(psu.getAccountDetailsList(), accountDetails.getId());
        if (isEmpty(newList)) {
            return Optional.empty();
        } else {
            newList.add(accountDetails);
            psu.setAccountDetailsList(newList);
            psuRepository.save(psu);

            return Optional.of(accountDetails);
        }
    }

    private boolean removeAccountFromPsu(Psu psu, String accountId) {
        List<SpiAccountDetails> newList = deleteAccountFromListById(psu.getAccountDetailsList(), accountId);
        if (isEmpty(newList)) {
            return false;
        } else {
            psu.setAccountDetailsList(newList);
            psuRepository.save(psu);
            return true;
        }
    }

    private List<SpiAccountDetails> deleteAccountFromListById(List<SpiAccountDetails> list, String accountId) {
        return list.removeIf(acc -> acc.getId().equals(accountId))
            ? list
            : Collections.emptyList();
    }

    private List<SpiBalances> getAccountBalances(Psu psu, String accountId) {
        return findAccountInListById(psu.getAccountDetailsList(), accountId)
            .map(SpiAccountDetails::getBalances)
            .orElse(Collections.emptyList());
    }

    private Optional<SpiAccountDetails> findAccountInListById(List<SpiAccountDetails> list, String accountId) {
        return list.stream()
            .filter(acc -> acc.getId().equals(accountId))
            .findFirst();
    }

    private Optional<SpiAccountDetails> findAccountInListByIban(List<SpiAccountDetails> list, String iban, Currency currency) {
        return list.stream()
            .filter(acc -> getAccountIban(acc).equals(iban) && getAccountCurrency(acc).equals(currency.getCurrencyCode()))
            .findFirst();
    }

    private String getAccountIban(SpiAccountDetails accountDetails) {
        return Optional.ofNullable(accountDetails)
            .map(acc -> acc.getIban())
            .orElse("");
    }

    private String getAccountCurrency(SpiAccountDetails accountDetails) {
        return Optional.ofNullable(accountDetails.getCurrency())
            .map(cur -> cur.getCurrencyCode())
            .orElse("");
    }
}

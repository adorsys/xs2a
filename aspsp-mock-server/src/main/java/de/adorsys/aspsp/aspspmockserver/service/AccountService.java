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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AccountService {
    private final PsuRepository psuRepository;

    public Optional<SpiAccountDetails> addAccount(String psuId, SpiAccountDetails accountDetails) {
        return Optional.ofNullable(psuRepository.findOne(psuId))
                   .map(psu -> addAccountToPsuAndSave(psu, accountDetails))
                   .flatMap(psu -> findAccountInPsuById(psu, accountDetails.getId()));
    }

    public Optional<SpiAccountDetails> updateAccount(SpiAccountDetails accountDetails) {
        return Optional.ofNullable(accountDetails.getId())
                   .flatMap(psuRepository::findPsuByAccountDetailsList_Id)
                   .map(psu -> updateAccountInPsu(psu, accountDetails))
                   .flatMap(psu -> findAccountInPsuById(psu, accountDetails.getId()));
    }

    public List<SpiAccountDetails> getAllAccounts() {
        return psuRepository.findAll().stream()
                   .flatMap(psu -> psu.getAccountDetailsList().stream())
                   .collect(Collectors.toList());
    }

    public Optional<SpiAccountDetails> getAccountById(String accountId) {
        return psuRepository.findPsuByAccountDetailsList_Id(accountId)
                   .flatMap(psu -> findAccountInPsuById(psu, accountId));
    }

    public List<SpiAccountDetails> getAccountsByIban(String iban) {
        return psuRepository.findPsuByAccountDetailsList_Iban(iban).stream()
                   .flatMap(psu -> psu.getAccountDetailsList().stream())
                   .filter(aD -> aD.getIban().equals(iban))
                   .collect(Collectors.toList());
    }

    public List<SpiBalances> getAccountBalancesById(String accountId) {
        return psuRepository.findPsuByAccountDetailsList_Id(accountId)
                   .flatMap(psu -> findAccountInPsuById(psu, accountId))
                   .map(SpiAccountDetails::getBalances)
                   .orElse(Collections.emptyList());
    }

    public List<SpiAccountDetails> getAccountsByPsuId(String psuId) {
        return Optional.ofNullable(psuRepository.findOne(psuId)).map(Psu::getAccountDetailsList)
                   .orElse(Collections.emptyList());
    }

    public void deleteAccountById(String accountId) {
        psuRepository.findPsuByAccountDetailsList_Id(accountId)
            .map(psu -> getPsuWithFilteredAccountListById(psu, accountId))
            .map(psuRepository::save);
    }

    private Psu updateAccountInPsu(Psu psu, SpiAccountDetails accountDetails) {
        Psu filteredPsu = getPsuWithFilteredAccountListById(psu, accountDetails.getId());
        return addAccountToPsuAndSave(filteredPsu, accountDetails);
    }

    private Optional<SpiAccountDetails> findAccountInPsuById(Psu psu, String accountId) {
        return psu.getAccountDetailsList().stream()
                   .filter(acc -> acc.getId().equals(accountId))
                   .findFirst();
    }

    private Psu getPsuWithFilteredAccountListById(Psu psu, String accountId) {
        psu.setAccountDetailsList(getFilteredAccountDetailsListFromPsuById(psu, accountId));
        return psu;
    }

    private Psu addAccountToPsuAndSave(Psu psu, SpiAccountDetails accountDetails) {
        psu.getAccountDetailsList().add(accountDetails);
        return psuRepository.save(psu);
    }

    private List<SpiAccountDetails> getFilteredAccountDetailsListFromPsuById(Psu psu, String accountId) {
        return psu.getAccountDetailsList().stream()
                   .filter(ad -> !ad.getId().equals(accountId))
                   .collect(Collectors.toList());
    }
}

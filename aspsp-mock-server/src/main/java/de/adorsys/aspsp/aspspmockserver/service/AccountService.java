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
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AccountService {
    private PsuRepository psuRepository;

    public List<SpiAccountDetails> getAllAccounts() {
        return psuRepository.findAll().stream()
                   .flatMap(psu -> psu.getAccountDetailsList().stream())
                   .collect(Collectors.toList());
    }

    public List<SpiAccountDetails> getAccountsByIban(String iban) {
        return psuRepository.findPsuByAccountDetailsList_Iban(iban).stream()
                   .flatMap(psu -> psu.getAccountDetailsList().stream())
                   .collect(Collectors.toList());
    }

    public Optional<SpiAccountDetails> getAccount(String accountId) {
        return psuRepository.findPsuByAccountDetailsList_Id(accountId).stream()
                   .flatMap(psu -> psu.getAccountDetailsList().stream())
                   .filter(ad -> ad.getId().equals(accountId))
                   .findFirst();
    }

    public SpiAccountDetails[] getAccountsByPsuId(String psuId) {
        List<SpiAccountDetails> list = Optional.ofNullable(psuRepository.findOne(psuId)).map(Psu::getAccountDetailsList)
                                                         .orElse(Collections.emptyList());
        return list.toArray(new SpiAccountDetails[0]);

    }

    public void deleteAccountById(String accountId) {
        Optional.ofNullable(accountId)
            .map(id -> psuRepository.findPsuByAccountDetailsList_Id(id))
            .map(lst -> lst.stream()
                            .map(psu -> deleteAccountDetailsFromPsu(psu, accountId)))
            .map(lst -> lst.map(psu -> psuRepository.save(psu)));
    }

    private Psu deleteAccountDetailsFromPsu(Psu psu, String accounId) {
        List<SpiAccountDetails> nx = removeAccountDetailsById(psu.getAccountDetailsList(), accounId);
        psu.setAccountDetailsList(nx);
        return psu;
    }

    private List<SpiAccountDetails> removeAccountDetailsById(List<SpiAccountDetails> details, String accounId) {
        return details.stream()
                   .filter(ad -> !ad.getId().equals(accounId))
                   .collect(Collectors.toList());
    }
}

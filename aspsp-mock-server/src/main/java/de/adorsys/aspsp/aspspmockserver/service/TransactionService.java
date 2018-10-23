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

import de.adorsys.aspsp.aspspmockserver.repository.TransactionRepository;
import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountDetails;
import de.adorsys.psd2.aspsp.mock.api.account.AspspTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    public List<AspspTransaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Optional<AspspTransaction> getTransactionById(String transactionId, String accountId) {
        Optional<AspspAccountDetails> details = accountService.getAccountById(accountId);
        return details.map(det -> transactionRepository.findOneByTransactionIdAndAccount(det.getIban(), det.getCurrency(), transactionId));
    }

    public Optional<String> saveTransaction(AspspTransaction transaction) {
        return Optional.ofNullable(transactionRepository.save(transaction))
                   .map(AspspTransaction::getTransactionId);
    }

    public List<AspspTransaction> getTransactionsByPeriod(String accountId, LocalDate dateFrom, LocalDate dateTo) {
        Optional<AspspAccountDetails> details = accountService.getAccountById(accountId);
        return details.map(det -> transactionRepository.findAllByDates(det.getIban(), det.getCurrency(), dateFrom, dateTo))
                   .orElseGet(Collections::emptyList);
    }

}

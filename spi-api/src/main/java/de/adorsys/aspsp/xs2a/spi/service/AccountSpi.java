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

package de.adorsys.aspsp.xs2a.spi.service;

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiTransaction;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AccountSpi {

    List<SpiBalances> readBalances(String accountId);

    List<SpiTransaction> readTransactionsByPeriod(String accountId, LocalDate dateFrom, LocalDate dateTo);

    Optional<SpiTransaction> readTransactionById(String transactionId, String accountId);

    String saveTransaction(SpiTransaction transaction);

    SpiAccountDetails readAccountDetails(String accountId);

    List<SpiAccountDetails> readAccountsByPsuId(String psuId);

    List<SpiAccountDetails> readAccountDetailsByIban(String iban);

    List<SpiAccountDetails> readAccountDetailsByIbans(Collection<String> ibans);

    List<String> readPsuAllowedPaymentProductList(SpiAccountReference reference);
}

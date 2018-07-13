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

    /**
     * Queries ASPSP to get List of transactions dependant on period and accountId
     *
     * @param accountId String representation of ASPSP account primary identifier
     * @param dateFrom  Date representing the beginning of the search period
     * @param dateTo    Date representing the ending of the search period
     * @return List of transactions
     */
    List<SpiTransaction> readTransactionsByPeriod(String accountId, LocalDate dateFrom, LocalDate dateTo);

    /**
     * Queries ASPSP to (GET) transaction by its primary identifier and account identifier
     *
     * @param transactionId String representation of ASPSP primary identifier of transaction
     * @param accountId     String representation of ASPSP account primary identifier
     * @return Transaction
     */
    Optional<SpiTransaction> readTransactionById(String transactionId, String accountId);

    /**
     * Queries ASPSP to (GET) AccountDetails by primary ASPSP account identifier
     *
     * @param accountId String representation of ASPSP account primary identifier
     * @return Account details
     */
    SpiAccountDetails readAccountDetails(String accountId);

    /**
     * Queries ASPSP to (GET) a list of account details of a certain PSU by identifier
     *
     * @param psuId String representing ASPSP`s primary identifier of PSU
     * @return List of account details
     */
    List<SpiAccountDetails> readAccountsByPsuId(String psuId);

    /**
     * Queries ASPSP to (GET) List of AccountDetails by IBAN
     *
     * @param iban String representation of Account IBAN
     * @return List of account details
     */
    List<SpiAccountDetails> readAccountDetailsByIban(String iban);

    /**
     * Queries ASPSP to (GET) list of account details with certain account IBANS
     *
     * @param ibans a collection of Strings representing account IBANS
     * @return List of account details
     */
    List<SpiAccountDetails> readAccountDetailsByIbans(Collection<String> ibans);

    /**
     * Queries ASPSP to (GET) list of allowed payment products for current PSU by its account reference
     *
     * @param reference Account reference
     * @return a list of allowed payment products
     */
    List<String> readPsuAllowedPaymentProductList(SpiAccountReference reference);
}

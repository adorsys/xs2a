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

package de.adorsys.psd2.xs2a.spi.service;

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.account.*;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.List;

public interface AccountSpi {

    /**
     * Requests a list of account details
     *
     * @param withBalance      boolean representing if the responded AccountDetails should contain balance
     * @param accountConsent   SpiAccountConsent
     * @param aspspConsentData Encrypted data that may be stored in the consent management system in the consent linked to a request
     * @return List of account details
     */
    SpiResponse<List<SpiAccountDetails>> requestAccountList(boolean withBalance, @NotNull SpiAccountConsent accountConsent, @NotNull AspspConsentData aspspConsentData);

    /**
     * Requests an account detail for account
     *
     * @param withBalance      Boolean representing if the responded AccountDetails should contain balance
     * @param accountReference   SpiAccountReference
     * @param accountConsent   SpiAccountConsent
     * @param aspspConsentData Encrypted data that may be stored in the consent management system in the consent linked to a request
     * @return Account detail
     */
    SpiResponse<SpiAccountDetails> requestAccountDetailForAccount(boolean withBalance, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull AspspConsentData aspspConsentData);

    /**
     * Requests a list of transactions
     *
     * @param withBalance      boolean representing if the responded AccountDetails should contain balance
     * @param dateFrom         Date representing the beginning of the search period.<br>
     *                         If null, transactions will not be limited by start date
     * @param dateTo           Date representing the ending of the search period. <br>
     *                         If null, transactions will not be limited by end date
     * @param accountReference   SpiAccountReference
     * @param accountConsent   SpiAccountConsent
     * @param aspspConsentData Encrypted data that may be stored in the consent management system in the consent linked to a request
     * @return List of transactions
     */
    SpiResponse<SpiTransactionReport> requestTransactionsForAccount(String acceptMediaType, boolean withBalance, @NotNull LocalDate dateFrom, @NotNull LocalDate dateTo, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull AspspConsentData aspspConsentData);

    /**
     * Requests an transaction by transactionId
     *
     * @param transactionId    String representation of ASPSP transaction primary identifier
     * @param accountConsent   SpiAccountConsent
     * @param aspspConsentData Encrypted data that may be stored in the consent management system in the consent linked to a request
     * @return Transaction
     */
    SpiResponse<SpiTransaction> requestTransactionForAccountByTransactionId(@NotNull String transactionId, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull AspspConsentData aspspConsentData);

    /**
     * Requests a list of account balances
     *
     * @param accountReference   SpiAccountReference
     * @param accountConsent   SpiAccountConsent
     * @param aspspConsentData Encrypted data that may be stored in the consent management system in the consent linked to a request
     * @return List of account balances
     */
    SpiResponse<List<SpiAccountBalance>> requestBalancesForAccount(@NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull AspspConsentData aspspConsentData);
}

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

import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.*;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface AccountSpi {

    /**
     * Requests a list of account details
     *
     * @param contextData              known Context of this call
     * @param withBalance              boolean representing if the responded AccountDetails should contain balance
     * @param accountConsent           SpiAccountConsent
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return List of account details
     */
    SpiResponse<List<SpiAccountDetails>> requestAccountList(@NotNull SpiContextData contextData, boolean withBalance, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * Requests a list of trusted beneficiaries
     *
     * @param contextData              known Context of this call
     * @param accountReference         SpiAccountReference
     * @param accountConsent           SpiAccountConsent
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return List of trusted beneficiaries
     */
    SpiResponse<List<SpiTrustedBeneficiaries>> requestTrustedBeneficiariesList(@NotNull SpiContextData contextData, SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * Requests an account detail for account
     *
     * @param contextData              known Context of this call
     * @param withBalance              Boolean representing if the responded AccountDetails should contain balance
     * @param accountReference         SpiAccountReference
     * @param accountConsent           SpiAccountConsent
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return Account detail
     */
    SpiResponse<SpiAccountDetails> requestAccountDetailForAccount(@NotNull SpiContextData contextData, boolean withBalance, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * Requests a list of transactions
     *
     * @param contextData                    known Context of this call
     * @param spiTransactionReportParameters Transaction report parameters (acceptMediaType, withBalance, dateFrom, dateTo, bookingStatus, entryReferenceFrom, deltaList)
     * @param accountReference               SpiAccountReference
     * @param accountConsent                 SpiAccountConsent
     * @param aspspConsentDataProvider       Provides access to read/write encrypted data to be stored in the consent management system
     * @return List of transactions
     */
    SpiResponse<SpiTransactionReport> requestTransactionsForAccount(@NotNull SpiContextData contextData, @NotNull SpiTransactionReportParameters spiTransactionReportParameters, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * Requests an transaction by transactionId
     *
     * @param contextData              known Context of this call
     * @param transactionId            String representation of ASPSP transaction primary identifier
     * @param accountReference         SpiAccountReference
     * @param accountConsent           SpiAccountConsent
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return Transaction
     */
    SpiResponse<SpiTransaction> requestTransactionForAccountByTransactionId(@NotNull SpiContextData contextData, @NotNull String transactionId, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * Requests a list of account balances
     *
     * @param contextData              known Context of this call
     * @param accountReference         SpiAccountReference
     * @param accountConsent           SpiAccountConsent
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return List of account balances
     */
    SpiResponse<List<SpiAccountBalance>> requestBalancesForAccount(@NotNull SpiContextData contextData, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * Requests a list of transactions by download link
     *
     * @param contextData              known Context of this call
     * @param accountConsent           SpiAccountConsent
     * @param downloadId               download identifier, provided by ASPSP to TPP for downloading transactions
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return A response, containing a stream for downloading transactions and it's content type
     */
    default SpiResponse<SpiTransactionsDownloadResponse> requestTransactionsByDownloadLink(@NotNull SpiContextData contextData, @NotNull SpiAccountConsent accountConsent, @NotNull String downloadId, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return SpiResponse.<SpiTransactionsDownloadResponse>builder()
                   .error(new TppMessage(MessageErrorCode.SERVICE_NOT_SUPPORTED))
                   .build();
    }
}

/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
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

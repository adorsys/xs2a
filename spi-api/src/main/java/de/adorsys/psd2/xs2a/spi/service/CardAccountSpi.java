/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.*;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface to be used for card account SPI implementation
 */
public interface CardAccountSpi {

    /**
     * Requests a list of card account details
     *
     * @param contextData              known Context of this call
     * @param accountConsent           SpiAccountConsent
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return List of card account details
     */
    SpiResponse<List<SpiCardAccountDetails>> requestCardAccountList(@NotNull SpiContextData contextData, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * Requests an account details for card account
     *
     * @param contextData              known Context of this call
     * @param accountReference         SpiAccountReference
     * @param accountConsent           SpiAccountConsent
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return card account details
     */
    SpiResponse<SpiCardAccountDetails> requestCardAccountDetailsForAccount(@NotNull SpiContextData contextData, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * Requests a list of card transactions
     *
     * @param contextData                    known Context of this call
     * @param spiTransactionReportParameters Transaction report parameters (acceptMediaType, dateFrom, dateTo, bookingStatus, entryReferenceFrom, deltaList)
     * @param accountReference               SpiAccountReference
     * @param accountConsent                 SpiAccountConsent
     * @param aspspConsentDataProvider       Provides access to read/write encrypted data to be stored in the consent management system
     * @return List of card transactions
     */
    SpiResponse<SpiCardTransactionReport> requestCardTransactionsForAccount(@NotNull SpiContextData contextData, @NotNull SpiTransactionReportParameters spiTransactionReportParameters, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * Requests a list of card account balances
     *
     * @param contextData              known Context of this call
     * @param accountReference         SpiAccountReference
     * @param accountConsent           SpiAccountConsent
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return List of account balances
     */
    SpiResponse<List<SpiAccountBalance>> requestCardBalancesForAccount(@NotNull SpiContextData contextData, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

}

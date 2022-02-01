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
     * @param spiCardTransactionReportParameters Card transaction report parameters (acceptMediaType, dateFrom, dateTo, bookingStatus, entryReferenceFrom, deltaList)
     * @param accountReference               SpiAccountReference
     * @param accountConsent                 SpiAccountConsent
     * @param aspspConsentDataProvider       Provides access to read/write encrypted data to be stored in the consent management system
     * @return List of card transactions
     */
    SpiResponse<SpiCardTransactionReport> requestCardTransactionsForAccount(@NotNull SpiContextData contextData, @NotNull SpiTransactionReportParameters spiCardTransactionReportParameters, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

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

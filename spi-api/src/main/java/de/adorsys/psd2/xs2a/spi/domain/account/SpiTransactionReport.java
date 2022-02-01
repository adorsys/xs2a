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

package de.adorsys.psd2.xs2a.spi.domain.account;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Transaction report of SPI layer to be used as a container for account reference, transactions and balances.
 * Also, holds encoded file download identifier.
 */
@Value
public class SpiTransactionReport {
    public static final String RESPONSE_TYPE_JSON = "application/json";
    public static final String RESPONSE_TYPE_XML = "application/xml";
    public static final String RESPONSE_TYPE_TEXT = "text/plain";

    /**
     * This field stores the file download identifier. To be used in further calls, when TPP asks
     * the SPI for the transaction list file.
     */
    private String downloadId;

    private List<SpiTransaction> transactions;
    @Nullable
    private List<SpiAccountBalance> balances;
    @NotNull
    private String responseContentType;

    private byte[] transactionsRaw;

    private SpiTransactionLinks spiTransactionLinks;
    private int totalPages;
}

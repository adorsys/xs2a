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

package de.adorsys.psd2.xs2a.spi.domain.payment.response;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiLinks;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * A response object that is returned by the ASPSP after requesting the payment status.
 */
@Data
@AllArgsConstructor
public class SpiGetPaymentStatusResponse {
    public static final String RESPONSE_TYPE_JSON = "application/json";
    public static final String RESPONSE_TYPE_XML = "application/xml";

    /**
     * The current transaction status of the requested payment.
     */
    @NotNull
    private TransactionStatus transactionStatus;

    /**
     * The funds available check indicates, whether funds are available for said customer. The data element is contained
     * if supported by the ASPSP, if a funds check has been performed, and if the transaction status is ATCT, ACWC or ACCP.
     */
    @Nullable
    private Boolean fundsAvailable;

    /**
     * Content type to be used for returning response to TPP.
     */
    @NotNull
    private final String responseContentType;

    /**
     * Body of the payment status response to be returned to the TPP.
     * Should not be null if response content type is not application/json.
     */
    @Nullable
    private final byte[] paymentStatusRaw;

    /**
     * Message from ASPSP to PSU. May be null.
     */
    @Nullable
    private String psuMessage;
    private final SpiLinks links;
    private final Set<TppMessageInformation> tppMessageInformation;
}

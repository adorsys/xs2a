/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

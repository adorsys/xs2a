/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.domain.pis;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.Links;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.MediaType;

import java.util.Set;

@Data
public class ReadBulkPaymentExtendedStatusResponse {
    @NotNull
    private String groupStatus;
    @Nullable
    private String reasonCode;
    @Nullable
    private String reasonProprietary;
    @Nullable
    private Boolean fundsAvailable;
    @Nullable
    private String originalTransactionInformationAndStatus;
    @Nullable
    private String transactionFees;
    @Nullable
    private String currencyConversionFees;
    @Nullable
    private String estimatedTotalAmount;
    @Nullable
    private String estimatedInterbankSettlementAmount;
    @Nullable
    private String psuMessage;
    @Nullable
    private String ownerNames;
    private MediaType responseContentType;
    private byte[] paymentStatusRaw;
    private ErrorHolder errorHolder;
    @JsonProperty("_links")
    private Links links;
    private Set<TppMessageInformation> tppMessageInformation;

    public ReadBulkPaymentExtendedStatusResponse(@NotNull String groupStatus,
            @Nullable String reasonCode,
            @Nullable String reasonProprietary,
            @Nullable Boolean fundsAvailable,
            @Nullable String originalTransactionInformationAndStatus,
            @Nullable String transactionFees,
            @Nullable String currencyConversionFees,
            @Nullable String estimatedTotalAmount,
            @Nullable String estimatedInterbankSettlementAmount,
            @Nullable String psuMessage,
            @Nullable String ownerNames,
            @NotNull MediaType responseContentType,
            byte[] paymentStatusRaw,
            @Nullable Links links,
            @Nullable Set<TppMessageInformation> tppMessageInformation) {
        this.groupStatus = groupStatus;
        this.reasonCode = reasonCode;
        this.reasonProprietary = reasonProprietary;
        this.fundsAvailable = fundsAvailable;
        this.originalTransactionInformationAndStatus = originalTransactionInformationAndStatus;
        this.transactionFees = transactionFees;
        this.currencyConversionFees = currencyConversionFees;
        this.estimatedTotalAmount = estimatedTotalAmount;
        this.estimatedInterbankSettlementAmount = estimatedInterbankSettlementAmount;
        this.psuMessage = psuMessage;
        this.ownerNames = ownerNames;
        this.responseContentType = responseContentType;
        this.paymentStatusRaw = paymentStatusRaw;
        this.links = links;
        this.tppMessageInformation = tppMessageInformation;
    }

    public ReadBulkPaymentExtendedStatusResponse(ErrorHolder errorHolder) {
        this.errorHolder = errorHolder;
    }

    public boolean hasError() {
        return errorHolder != null;
    }
}
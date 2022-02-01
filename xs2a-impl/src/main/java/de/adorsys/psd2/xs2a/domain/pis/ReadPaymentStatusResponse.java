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

package de.adorsys.psd2.xs2a.domain.pis;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.domain.Links;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.MediaType;

import java.util.Set;

@Data
public class ReadPaymentStatusResponse {
    private TransactionStatus status;
    @Nullable
    private Boolean fundsAvailable;
    private MediaType responseContentType;
    private byte[] paymentStatusRaw;
    private ErrorHolder errorHolder;

    @Nullable
    private String psuMessage;
    @JsonProperty("_links")
    private Links links;
    private Set<TppMessageInformation> tppMessageInformation;

    public ReadPaymentStatusResponse(@NotNull TransactionStatus status, @Nullable Boolean fundsAvailable,
                                     @NotNull MediaType responseContentType, byte[] paymentStatusRaw,
                                     @Nullable String psuMessage,
                                     @Nullable Links links,
                                     @Nullable Set<TppMessageInformation> tppMessageInformation) {
        this.status = status;
        this.fundsAvailable = fundsAvailable;
        this.responseContentType = responseContentType;
        this.paymentStatusRaw = paymentStatusRaw;
        this.psuMessage = psuMessage;
        this.links = links;
        this.tppMessageInformation = tppMessageInformation;
    }

    public ReadPaymentStatusResponse(ErrorHolder errorHolder) {
        this.errorHolder = errorHolder;
    }

    public boolean hasError() {
        return errorHolder != null;
    }
}

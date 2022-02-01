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
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.domain.CustomContentTypeProvider;
import de.adorsys.psd2.xs2a.domain.Links;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.MediaType;

import java.util.Set;

@Data
@AllArgsConstructor
public class GetPaymentStatusResponse implements CustomContentTypeProvider {
    @NotNull
    private TransactionStatus transactionStatus;
    @Nullable
    private Boolean fundsAvailable;
    @NotNull
    private final MediaType responseContentType;
    @Nullable
    private final byte[] paymentStatusRaw;
    @Nullable
    private String psuMessage;
    @JsonProperty("_links")
    private Links links;
    private Set<TppMessageInformation> tppMessageInformation;

    public boolean isResponseContentTypeJson() {
        return MediaType.APPLICATION_JSON.includes(responseContentType);
    }

    @Override
    public MediaType getCustomContentType() {
        return responseContentType;
    }
}

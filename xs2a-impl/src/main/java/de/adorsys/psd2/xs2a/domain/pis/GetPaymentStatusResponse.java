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

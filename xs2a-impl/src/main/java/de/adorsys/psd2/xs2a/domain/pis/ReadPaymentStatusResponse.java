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

import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.MediaType;

@Data
public class ReadPaymentStatusResponse {
    private TransactionStatus status;
    @Nullable
    private Boolean fundsAvailable;
    private MediaType responseContentType;
    @Nullable
    private byte[] paymentStatusRaw;
    private ErrorHolder errorHolder;

    @Nullable
    private String psuMessage;

    public ReadPaymentStatusResponse(@NotNull TransactionStatus status, @Nullable Boolean fundsAvailable,
                                     @NotNull MediaType responseContentType, @Nullable byte[] paymentStatusRaw,
                                     @Nullable String psuMessage) {
        this.status = status;
        this.fundsAvailable = fundsAvailable;
        this.responseContentType = responseContentType;
        this.paymentStatusRaw = paymentStatusRaw;
        this.psuMessage = psuMessage;
    }

    public ReadPaymentStatusResponse(ErrorHolder errorHolder) {
        this.errorHolder = errorHolder;
    }

    public boolean hasError() {
        return errorHolder != null;
    }
}

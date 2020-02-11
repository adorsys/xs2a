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

package de.adorsys.psd2.xs2a.service.payment.create;

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationResponse;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
public class PisPaymentInfoCreationObject {
    private final PaymentInitiationParameters paymentInitiationParameters;
    private final TppInfo tppInfo;
    private final PaymentInitiationResponse response;
    private final byte[] paymentData;
    private final String internalRequestId;
    private final OffsetDateTime creationTimestamp;
    private final String contentType;
}

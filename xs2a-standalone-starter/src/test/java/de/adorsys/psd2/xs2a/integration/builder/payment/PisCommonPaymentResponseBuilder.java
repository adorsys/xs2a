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

package de.adorsys.psd2.xs2a.integration.builder.payment;

import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.integration.builder.PsuIdDataBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;

import java.util.Collections;

public class PisCommonPaymentResponseBuilder {
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.RCVD;

    public static PisCommonPaymentResponse buildPisCommonPaymentResponse() {
        PisCommonPaymentResponse commonPaymentResponse = new PisCommonPaymentResponse();
        commonPaymentResponse.setTransactionStatus(TRANSACTION_STATUS);
        commonPaymentResponse.setTppInfo(TppInfoBuilder.buildTppInfo());
        commonPaymentResponse.setPsuData(Collections.singletonList(PsuIdDataBuilder.buildPsuIdData()));
        return commonPaymentResponse;
    }
}

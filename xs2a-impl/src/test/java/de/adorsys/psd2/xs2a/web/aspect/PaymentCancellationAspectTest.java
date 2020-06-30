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

package de.adorsys.psd2.xs2a.web.aspect;

import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.psd2.xs2a.service.link.PaymentCancellationAspectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentCancellationAspectTest {
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "1111111111111";
    private static final PisPaymentCancellationRequest REQUEST =
        new PisPaymentCancellationRequest(SINGLE, PAYMENT_PRODUCT, PAYMENT_ID, true, new TppRedirectUri("", ""));


    @InjectMocks
    private PaymentCancellationAspect aspect;

    @Mock
    private PaymentCancellationAspectService paymentCancellationAspectService;

    @Test
    void cancelPayment() {
        ResponseObject<CancelPaymentResponse> responseObject = ResponseObject.<CancelPaymentResponse>builder()
                                            .body(new CancelPaymentResponse())
                                            .build();

        aspect.cancelPayment(responseObject, REQUEST);
        verify(paymentCancellationAspectService).cancelPayment(responseObject, REQUEST);
    }
}

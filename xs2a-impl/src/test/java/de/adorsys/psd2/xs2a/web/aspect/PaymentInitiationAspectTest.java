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

import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.SinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.link.PaymentAspectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentInitiationAspectTest {

    @InjectMocks
    private PaymentInitiationAspect aspect;

    @Mock
    private PaymentAspectService paymentAspectService;
    @Mock
    private PaymentInitiationParameters requestParameters;

    @Test
    void createPaymentAspect() {
        ResponseObject<PaymentInitiationResponse> responseObject = ResponseObject.<PaymentInitiationResponse>builder()
                                            .body(new SinglePaymentInitiationResponse())
                                            .build();
        aspect.createPaymentAspect(responseObject, null, requestParameters);
        verify(paymentAspectService).createPaymentAspect(responseObject, requestParameters);
    }
}

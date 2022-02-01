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

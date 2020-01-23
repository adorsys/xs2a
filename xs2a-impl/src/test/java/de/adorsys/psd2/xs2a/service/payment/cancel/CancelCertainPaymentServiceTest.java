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

package de.adorsys.psd2.xs2a.service.payment.cancel;

import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.payment.CancelPaymentService;
import de.adorsys.psd2.xs2a.service.payment.SpiPaymentFactory;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelCertainPaymentServiceTest {

    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    @InjectMocks
    private CancelCertainPaymentService cancelCertainPaymentService;

    @Mock
    private CancelPaymentService cancelPaymentService;
    @Mock
    private SpiPaymentFactory spiPaymentFactory;

    private PisPaymentCancellationRequest paymentCancellationRequest;
    private JsonReader jsonReader = new JsonReader();
    private PisCommonPaymentResponse commonPaymentResponse;

    @BeforeEach
    void setUp() {
        paymentCancellationRequest = jsonReader.getObjectFromFile("json/service/payment/pis-payment-cancellation-request.json",
                                                                  PisPaymentCancellationRequest.class);
        commonPaymentResponse = jsonReader.getObjectFromFile("json/service/payment/pis-common-payment-response.json",
                                                             PisCommonPaymentResponse.class);
    }

    @Test
    void cancelPayment_success() {
        Optional<? extends SpiPayment> spiSinglePayment = Optional.of(new SpiSinglePayment(PAYMENT_PRODUCT));
        Mockito.doReturn(spiSinglePayment).when(spiPaymentFactory).createSpiPaymentByPaymentType(any(), eq(PAYMENT_PRODUCT), eq(PaymentType.SINGLE));

        when(cancelPaymentService.initiatePaymentCancellation(spiSinglePayment.get(),
                                                              paymentCancellationRequest.getEncryptedPaymentId(),
                                                              paymentCancellationRequest.getTppExplicitAuthorisationPreferred(),
                                                              paymentCancellationRequest.getTppRedirectUri())).thenReturn(null);

        cancelCertainPaymentService.cancelPayment(commonPaymentResponse, paymentCancellationRequest);

        verify(spiPaymentFactory, times(1)).createSpiPaymentByPaymentType(any(), eq(PAYMENT_PRODUCT), eq(PaymentType.SINGLE));
        verify(cancelPaymentService, times(1)).initiatePaymentCancellation(spiSinglePayment.get(),
                                                                           paymentCancellationRequest.getEncryptedPaymentId(),
                                                                           paymentCancellationRequest.getTppExplicitAuthorisationPreferred(),
                                                                           paymentCancellationRequest.getTppRedirectUri());
    }

    @Test
    void cancelPayment_pisPaymentsListIsEmpty() {
        commonPaymentResponse.setPayments(null);

        ResponseObject<CancelPaymentResponse> actualResponse = cancelCertainPaymentService.cancelPayment(commonPaymentResponse, paymentCancellationRequest);

        verify(spiPaymentFactory, never()).createSpiPaymentByPaymentType(any(), any(), any());
        verify(cancelPaymentService, never()).initiatePaymentCancellation(any(), any(), any(), any());

        assertTrue(actualResponse.hasError());
        assertEquals(ErrorType.PIS_404, actualResponse.getError().getErrorType());
        assertEquals(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_PAYMENT, actualResponse.getError().getTppMessage().getMessageErrorCode());
    }

    @Test
    void cancelPayment_spiPaymentIsEmpty() {
        Mockito.doReturn(Optional.empty()).when(spiPaymentFactory).createSpiPaymentByPaymentType(any(), eq(PAYMENT_PRODUCT), eq(PaymentType.SINGLE));

        ResponseObject<CancelPaymentResponse> actualResponse = cancelCertainPaymentService.cancelPayment(commonPaymentResponse, paymentCancellationRequest);

        verify(spiPaymentFactory, times(1)).createSpiPaymentByPaymentType(any(), eq(PAYMENT_PRODUCT), eq(PaymentType.SINGLE));
        verify(cancelPaymentService, never()).initiatePaymentCancellation(any(), any(), any(), any());

        assertTrue(actualResponse.hasError());
        assertEquals(ErrorType.PIS_404, actualResponse.getError().getErrorType());
        assertEquals(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_PAYMENT, actualResponse.getError().getTppMessage().getMessageErrorCode());
    }

    @Test
    void isCommonPayment() {
        assertFalse(cancelCertainPaymentService.isCommonPayment());
    }
}

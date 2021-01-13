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

package de.adorsys.psd2.xs2a.service.payment.support.cancel;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.psd2.xs2a.service.mapper.payment.SpiPaymentFactory;
import de.adorsys.psd2.xs2a.service.payment.CancelPaymentService;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelCertainPaymentServiceTest {
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";

    @Mock
    private CancelPaymentService cancelPaymentService;
    @Mock
    private SpiPaymentFactory spiPaymentFactory;

    @InjectMocks
    private CancelCertainPaymentService cancelCertainPaymentService;


    private PisPaymentCancellationRequest paymentCancellationRequest;
    private final JsonReader jsonReader = new JsonReader();
    private PisCommonPaymentResponse commonPaymentResponse;

    @BeforeEach
    void setUp() {
        paymentCancellationRequest = jsonReader.getObjectFromFile("json/support/cancel/pis-payment-cancellation-request.json",
                                                                  PisPaymentCancellationRequest.class);
        commonPaymentResponse = jsonReader.getObjectFromFile("json/support/cancel/pis-common-payment-response.json",
                                                             PisCommonPaymentResponse.class);
    }

    @Test
    void cancelPayment_success() {
        // Given
        Optional<SpiPayment> spiSinglePayment = Optional.of(new SpiSinglePayment(PAYMENT_PRODUCT));
        doReturn(spiSinglePayment).when(spiPaymentFactory).getSpiPayment(commonPaymentResponse);

        CancelPaymentResponse cancelPaymentResponse = new CancelPaymentResponse();
        when(cancelPaymentService.initiatePaymentCancellation(spiSinglePayment.get(),
                                                              paymentCancellationRequest.getEncryptedPaymentId(),
                                                              paymentCancellationRequest.getTppExplicitAuthorisationPreferred(),
                                                              paymentCancellationRequest.getTppRedirectUri()))
            .thenReturn(ResponseObject.<CancelPaymentResponse>builder()
                            .body(cancelPaymentResponse)
                            .build());

        // When
        ResponseObject<CancelPaymentResponse> actualResponse =
            cancelCertainPaymentService.cancelPayment(commonPaymentResponse, paymentCancellationRequest);

        // Then
        assertFalse(actualResponse.hasError());
        assertEquals(cancelPaymentResponse, actualResponse.getBody());
    }

    @Test
    void cancelPayment_noPayment() {
        // Given
        when(spiPaymentFactory.getSpiPayment(commonPaymentResponse)).thenReturn(Optional.empty());

        // When
        ResponseObject<CancelPaymentResponse> actualResponse = cancelCertainPaymentService.cancelPayment(commonPaymentResponse, paymentCancellationRequest);

        // Then
        verify(cancelPaymentService, never()).initiatePaymentCancellation(any(), any(), any(), any());

        assertTrue(actualResponse.hasError());
        assertEquals(ErrorType.PIS_404, actualResponse.getError().getErrorType());
        assertEquals(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_PAYMENT, actualResponse.getError().getTppMessage().getMessageErrorCode());
    }
}

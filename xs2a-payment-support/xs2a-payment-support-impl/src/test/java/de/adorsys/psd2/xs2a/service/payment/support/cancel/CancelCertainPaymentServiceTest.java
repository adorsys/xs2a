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

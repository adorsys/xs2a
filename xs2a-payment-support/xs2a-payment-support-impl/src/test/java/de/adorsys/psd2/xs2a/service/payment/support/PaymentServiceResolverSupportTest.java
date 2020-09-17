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

package de.adorsys.psd2.xs2a.service.payment.support;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
import de.adorsys.psd2.xs2a.config.factory.ReadPaymentFactory;
import de.adorsys.psd2.xs2a.config.factory.ReadPaymentStatusFactory;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.service.payment.cancel.CancelCommonPaymentService;
import de.adorsys.psd2.xs2a.service.payment.cancel.CancelPaymentService;
import de.adorsys.psd2.xs2a.service.payment.create.CreateCommonPaymentService;
import de.adorsys.psd2.xs2a.service.payment.create.CreatePaymentService;
import de.adorsys.psd2.xs2a.service.payment.read.ReadCommonPaymentService;
import de.adorsys.psd2.xs2a.service.payment.read.ReadPaymentService;
import de.adorsys.psd2.xs2a.service.payment.status.ReadCommonPaymentStatusService;
import de.adorsys.psd2.xs2a.service.payment.status.ReadPaymentStatusService;
import de.adorsys.psd2.xs2a.service.payment.support.cancel.CancelCertainPaymentService;
import de.adorsys.psd2.xs2a.service.payment.support.create.CreateBulkPaymentService;
import de.adorsys.psd2.xs2a.service.payment.support.create.CreatePeriodicPaymentService;
import de.adorsys.psd2.xs2a.service.payment.support.create.CreateSinglePaymentService;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceResolverSupportTest {
    private static final String STANDARD_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String RAW_PAYMENT_PRODUCT = "raw-product";

    @Mock
    private StandardPaymentProductsResolver standardPaymentProductsResolver;
    @Mock
    private CreateCommonPaymentService createCommonPaymentService;
    @Mock
    private CreateSinglePaymentService createSinglePaymentService;
    @Mock
    private CreatePeriodicPaymentService createPeriodicPaymentService;
    @Mock
    private CreateBulkPaymentService createBulkPaymentService;
    @Mock
    private ReadCommonPaymentService readCommonPaymentService;
    @Mock
    private ReadPaymentFactory readPaymentFactory;
    @Mock
    private ReadCommonPaymentStatusService readCommonPaymentStatusService;
    @Mock
    private ReadPaymentStatusFactory readPaymentStatusFactory;
    @Mock
    private CancelCommonPaymentService cancelCommonPaymentService;
    @Mock
    private CancelCertainPaymentService cancelCertainPaymentService;

    @Mock
    private ReadPaymentService mockReadPaymentService;
    @Mock
    private ReadPaymentStatusService mockReadPaymentStatusService;

    @InjectMocks
    private PaymentServiceResolverSupport paymentServiceResolverSupport;

    @Test
    void getCreatePaymentService_raw() {
        // Given
        when(standardPaymentProductsResolver.isRawPaymentProduct(RAW_PAYMENT_PRODUCT)).thenReturn(true);

        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(RAW_PAYMENT_PRODUCT, PaymentType.SINGLE);

        // When
        CreatePaymentService createPaymentService = paymentServiceResolverSupport.getCreatePaymentService(paymentInitiationParameters);

        // Then
        assertEquals(createCommonPaymentService, createPaymentService);
    }

    @Test
    void getCreatePaymentService_single() {
        // Given
        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(STANDARD_PAYMENT_PRODUCT, PaymentType.SINGLE);

        // When
        CreatePaymentService createPaymentService = paymentServiceResolverSupport.getCreatePaymentService(paymentInitiationParameters);

        // Then
        assertEquals(createSinglePaymentService, createPaymentService);
    }

    @Test
    void getCreatePaymentService_periodic() {
        // Given
        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(STANDARD_PAYMENT_PRODUCT, PaymentType.PERIODIC);

        // When
        CreatePaymentService createPaymentService = paymentServiceResolverSupport.getCreatePaymentService(paymentInitiationParameters);

        // Then
        assertEquals(createPeriodicPaymentService, createPaymentService);
    }

    @Test
    void getCreatePaymentService_bulk() {
        // Given
        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(STANDARD_PAYMENT_PRODUCT, PaymentType.BULK);

        // When
        CreatePaymentService createPaymentService = paymentServiceResolverSupport.getCreatePaymentService(paymentInitiationParameters);

        // Then
        assertEquals(createBulkPaymentService, createPaymentService);
    }

    @Test
    void getReadPaymentService_raw() {
        // Given
        when(standardPaymentProductsResolver.isRawPaymentProduct(RAW_PAYMENT_PRODUCT)).thenReturn(true);

        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        pisCommonPaymentResponse.setPaymentProduct(RAW_PAYMENT_PRODUCT);

        // When
        ReadPaymentService readPaymentService = paymentServiceResolverSupport.getReadPaymentService(pisCommonPaymentResponse);

        // Then
        assertEquals(readCommonPaymentService, readPaymentService);
    }

    @Test
    void getReadPaymentService_standard() {
        // Given
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        pisCommonPaymentResponse.setPaymentProduct(STANDARD_PAYMENT_PRODUCT);
        pisCommonPaymentResponse.setPaymentType(PaymentType.SINGLE);

        when(readPaymentFactory.getService("payments")).thenReturn(mockReadPaymentService);

        // When
        ReadPaymentService readPaymentService = paymentServiceResolverSupport.getReadPaymentService(pisCommonPaymentResponse);

        // Then
        assertEquals(mockReadPaymentService, readPaymentService);
    }

    @Test
    void getReadPaymentStatusService_raw() {
        // Given
        when(standardPaymentProductsResolver.isRawPaymentProduct(RAW_PAYMENT_PRODUCT)).thenReturn(true);

        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        pisCommonPaymentResponse.setPaymentProduct(RAW_PAYMENT_PRODUCT);

        // When
        ReadPaymentStatusService readPaymentStatusService = paymentServiceResolverSupport.getReadPaymentStatusService(pisCommonPaymentResponse);

        // Then
        assertEquals(readCommonPaymentStatusService, readPaymentStatusService);
    }

    @Test
    void getReadPaymentStatusService_standard() {
        // Given
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        pisCommonPaymentResponse.setPaymentProduct(STANDARD_PAYMENT_PRODUCT);
        pisCommonPaymentResponse.setPaymentType(PaymentType.SINGLE);

        when(readPaymentStatusFactory.getService(ReadPaymentStatusFactory.SERVICE_PREFIX + "payments")).thenReturn(mockReadPaymentStatusService);

        // When
        ReadPaymentStatusService readPaymentStatusService = paymentServiceResolverSupport.getReadPaymentStatusService(pisCommonPaymentResponse);

        // Then
        assertEquals(mockReadPaymentStatusService, readPaymentStatusService);
    }

    @Test
    void getCancelPaymentService_raw() {
        // Given
        when(standardPaymentProductsResolver.isRawPaymentProduct(RAW_PAYMENT_PRODUCT)).thenReturn(true);

        PisPaymentCancellationRequest pisPaymentCancellationRequest =
            new PisPaymentCancellationRequest(PaymentType.SINGLE, RAW_PAYMENT_PRODUCT, null, null, null);

        // When
        CancelPaymentService cancelPaymentService = paymentServiceResolverSupport.getCancelPaymentService(pisPaymentCancellationRequest);

        // Then
        assertEquals(cancelCommonPaymentService, cancelPaymentService);
    }

    @Test
    void getCancelPaymentService_standard() {
        // Given
        PisPaymentCancellationRequest pisPaymentCancellationRequest =
            new PisPaymentCancellationRequest(PaymentType.SINGLE, STANDARD_PAYMENT_PRODUCT, null, null, null);

        // When
        CancelPaymentService cancelPaymentService = paymentServiceResolverSupport.getCancelPaymentService(pisPaymentCancellationRequest);

        // Then
        assertEquals(cancelCertainPaymentService, cancelPaymentService);
    }

    private PaymentInitiationParameters buildPaymentInitiationParameters(String paymentProduct, PaymentType paymentType) {
        PaymentInitiationParameters paymentInitiationParameters = new PaymentInitiationParameters();
        paymentInitiationParameters.setPaymentProduct(paymentProduct);
        paymentInitiationParameters.setPaymentType(paymentType);
        return paymentInitiationParameters;
    }
}

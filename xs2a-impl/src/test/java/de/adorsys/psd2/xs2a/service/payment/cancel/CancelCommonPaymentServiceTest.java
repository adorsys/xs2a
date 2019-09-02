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

package de.adorsys.psd2.xs2a.service.payment.cancel;

import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentInfoMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.payment.CancelPaymentService;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CancelCommonPaymentServiceTest {
    public static final String PAYMENT_PRODUCT = "sepa-credit-transfers";

    @InjectMocks
    private CancelCommonPaymentService cancelCommonPaymentService;

    @Mock
    private CancelPaymentService cancelPaymentService;
    @Spy
    private Xs2aToSpiPaymentInfoMapper xs2aToSpiPaymentInfoMapper = new Xs2aToSpiPaymentInfoMapper(new Xs2aToSpiPsuDataMapper());

    @Captor
    private ArgumentCaptor<SpiPaymentInfo> spiPaymentInfoCaptor;

    private PisPaymentCancellationRequest paymentCancellationRequest;
    private JsonReader jsonReader = new JsonReader();
    private PisCommonPaymentResponse commonPaymentResponse;

    @Before
    public void setUp() {
        paymentCancellationRequest = jsonReader.getObjectFromFile("json/service/payment/pis-payment-cancellation-request.json",
                                                                  PisPaymentCancellationRequest.class);
        commonPaymentResponse = jsonReader.getObjectFromFile("json/service/payment/pis-common-payment-response.json",
                                                             PisCommonPaymentResponse.class);
    }

    @Test
    public void cancelPayment_success() {
        when(cancelPaymentService.initiatePaymentCancellation(spiPaymentInfoCaptor.capture(),
                                                              eq(paymentCancellationRequest.getEncryptedPaymentId()),
                                                              eq(paymentCancellationRequest.getTppExplicitAuthorisationPreferred()),
                                                              eq(paymentCancellationRequest.getTppRedirectUri()))).thenReturn(null);

        cancelCommonPaymentService.cancelPayment(commonPaymentResponse, paymentCancellationRequest);

        verify(cancelPaymentService, times(1)).initiatePaymentCancellation(any(SpiPaymentInfo.class),
                                                                           eq(paymentCancellationRequest.getEncryptedPaymentId()),
                                                                           eq(paymentCancellationRequest.getTppExplicitAuthorisationPreferred()),
                                                                           eq(paymentCancellationRequest.getTppRedirectUri()));
        Assert.assertEquals(xs2aToSpiPaymentInfoMapper.mapToSpiPaymentInfo(commonPaymentResponse), spiPaymentInfoCaptor.getValue());
    }

    @Test
    public void isCommonPayment() {
        Assert.assertTrue(cancelCommonPaymentService.isCommonPayment());
    }
}

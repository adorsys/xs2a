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

package de.adorsys.psd2.xs2a.service.payment.cancel;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentInfoMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.payment.CancelPaymentService;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.xs2a.reader.JsonReader;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelCommonPaymentServiceTest {
    public static final String PAYMENT_PRODUCT = "sepa-credit-transfers";

    @InjectMocks
    private CancelCommonPaymentService cancelCommonPaymentService;

    @Mock
    private CancelPaymentService cancelPaymentService;
    @Spy
    private final Xs2aToSpiPaymentInfoMapper xs2aToSpiPaymentInfoMapper = new Xs2aToSpiPaymentInfoMapper(new Xs2aToSpiPsuDataMapper());

    @Captor
    private ArgumentCaptor<SpiPaymentInfo> spiPaymentInfoCaptor;

    private PisPaymentCancellationRequest paymentCancellationRequest;
    private final JsonReader jsonReader = new JsonReader();
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
        when(cancelPaymentService.initiatePaymentCancellation(spiPaymentInfoCaptor.capture(),
                                                              eq(paymentCancellationRequest.getEncryptedPaymentId()),
                                                              eq(paymentCancellationRequest.getTppExplicitAuthorisationPreferred()),
                                                              eq(paymentCancellationRequest.getTppRedirectUri()))).thenReturn(null);

        cancelCommonPaymentService.cancelPayment(commonPaymentResponse, paymentCancellationRequest);

        verify(cancelPaymentService, times(1)).initiatePaymentCancellation(any(SpiPaymentInfo.class),
                                                                           eq(paymentCancellationRequest.getEncryptedPaymentId()),
                                                                           eq(paymentCancellationRequest.getTppExplicitAuthorisationPreferred()),
                                                                           eq(paymentCancellationRequest.getTppRedirectUri()));
        Assertions.assertThat(xs2aToSpiPaymentInfoMapper.mapToSpiPaymentInfo(commonPaymentResponse)).isEqualToComparingFieldByFieldRecursively(spiPaymentInfoCaptor.getValue());
    }
}

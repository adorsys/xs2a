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

package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInformationResponse;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aPaymentInfoMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentInfoMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.CommonPaymentSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReadCommonPaymentServiceTest {
    private static final PsuIdData PSU_DATA = new PsuIdData(null, null, null, null);
    private static final TppInfo TPP_INFO = buildTppInfo();
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final CommonPayment COMMON_PAYMENT = buildCommonPayment();
    private static final SpiPaymentInfo SPI_PAYMENT_INFO = new SpiPaymentInfo(PRODUCT);
    private static final SpiContextData SPI_CONTEXT_DATA = getSpiContextData();
    private static final PisPaymentInfo PIS_PAYMENT_INFO = getPisPaymentInfo();
    private static final AspspConsentData SOME_ASPSP_CONSENT_DATA = new AspspConsentData(new byte[0], "some consent id");

    @InjectMocks
    private ReadCommonPaymentService readCommonPaymentService;

    @Mock
    private CommonPaymentSpi commonPaymentSpi;
    @Mock
    private PisAspspDataService pisAspspDataService;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private Xs2aToSpiPaymentInfoMapper xs2aToSpiPaymentInfoMapper;
    @Mock
    private SpiToXs2aPaymentInfoMapper spiToXs2aPaymentInfoMapper;

    @Before
    public void init() {
        when(xs2aToSpiPaymentInfoMapper.mapToSpiPaymentInfo(COMMON_PAYMENT)).thenReturn(SPI_PAYMENT_INFO);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(spiToXs2aPaymentInfoMapper.mapToXs2aPaymentInfo(any())).thenReturn(PIS_PAYMENT_INFO);
        when(commonPaymentSpi.getPaymentById(SPI_CONTEXT_DATA, SPI_PAYMENT_INFO, SOME_ASPSP_CONSENT_DATA)).thenReturn(SpiResponse.<SpiPaymentInfo>builder()
                                                                                                                          .aspspConsentData(SOME_ASPSP_CONSENT_DATA.respondWith("some data".getBytes()))
                                                                                                                          .payload(SPI_PAYMENT_INFO)
                                                                                                                          .success());
    }

    @Test
    public void getPayment_success() {
        //When
        PaymentInformationResponse<PisPaymentInfo> actualResponse = readCommonPaymentService.getPayment(COMMON_PAYMENT, PSU_DATA, SOME_ASPSP_CONSENT_DATA);

        //Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getPayment()).isEqualTo(PIS_PAYMENT_INFO);
    }

    @Test
    public void getPayment_failed() {
        //Given
        ErrorHolder expectedError = ErrorHolder.builder(MessageErrorCode.RESOURCE_UNKNOWN_404)
                                        .messages(Collections.singletonList("Payment not found"))
                                        .build();
        SpiResponse<SpiPaymentInfo> failSpiResponse = SpiResponse.<SpiPaymentInfo>builder()
                                                          .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                                                          .fail(SpiResponseStatus.LOGICAL_FAILURE);

        when(commonPaymentSpi.getPaymentById(SPI_CONTEXT_DATA, SPI_PAYMENT_INFO, SOME_ASPSP_CONSENT_DATA)).thenReturn(failSpiResponse);
        when(spiErrorMapper.mapToErrorHolder(failSpiResponse, ServiceType.PIS))
            .thenReturn(expectedError);

        //When
        PaymentInformationResponse<PisPaymentInfo> actualResponse = readCommonPaymentService.getPayment(COMMON_PAYMENT, PSU_DATA, SOME_ASPSP_CONSENT_DATA);

        //Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getPayment()).isNull();
        assertThat(actualResponse.getErrorHolder()).isNotNull();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(expectedError);
    }

    private static CommonPayment buildCommonPayment() {
        CommonPayment request = new CommonPayment();
        request.setPaymentType(PaymentType.SINGLE);
        request.setPaymentProduct("sepa-credit-transfers");
        request.setPaymentData(new byte[16]);
        request.setTppInfo(TPP_INFO);

        return request;
    }

    private static TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("registrationNumber");
        tppInfo.setTppName("tppName");
        tppInfo.setTppRoles(Collections.singletonList(TppRole.PISP));
        tppInfo.setAuthorityId("authorityId");
        return tppInfo;
    }

    private static SpiContextData getSpiContextData() {
        return new SpiContextData(
            new SpiPsuData("", "", "", ""),
            new TppInfo(),
            UUID.randomUUID()
        );
    }

    private static PisPaymentInfo getPisPaymentInfo() {
        return new PisPaymentInfo();
    }
}

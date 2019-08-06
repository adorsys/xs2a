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

package de.adorsys.psd2.xs2a.service.authorization.pis.stage.cancellation;

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.consent.CmsToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiSinglePaymentMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_400;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class PisCancellationScaMethodSelectedStageTest {
    private final List<String> ERROR_MESSAGE_TEXT = Arrays.asList("message 1", "message 2", "message 3");
    private static final String PAYMENT_ID = "123456789";
    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";

    @InjectMocks
    private PisCancellationScaMethodSelectedStage pisCancellationScaMethodSelectedStage;

    @Mock
    private PisAspspDataService pisAspspDataService;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private Xs2aPisCommonPaymentMapper xs2aPisCommonPaymentMapper;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;
    @Mock
    private PaymentCancellationSpi paymentCancellationSpi;
    @Mock
    private CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper;
    @Mock
    private Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;

    @Before
    public void setUp() {
        ErrorHolder errorHolder = ErrorHolder.builder(PIS_400)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR, "message 1, message 2, message 3"))
                                      .build();

        when(spiErrorMapper.mapToErrorHolder(any(SpiResponse.class), eq(ServiceType.PIS)))
            .thenReturn(errorHolder);
        when(pisCommonPaymentServiceEncrypted.getPisCancellationAuthorisationById(AUTHORISATION_ID))
            .thenReturn(Optional.of(buildGetPisAuthorisationResponse()));

        when(requestProviderService.getRequestId()).thenReturn(UUID.randomUUID());
    }

    @Test
    public void apply_paymentCancellationSpi_verifyScaAuthorisationAndCancelPayment_fail() {
        String errorMessagesString = ERROR_MESSAGE_TEXT.toString().replace("[", "").replace("]", "");
        SpiResponse<SpiResponse.VoidResponse> spiErrorMessage = SpiResponse.<SpiResponse.VoidResponse>builder()
                                                                    .error(new TppMessage(MessageErrorCode.FORMAT_ERROR, "Format error"))
                                                                    .build();

        // generate an error
        when(paymentCancellationSpi.verifyScaAuthorisationAndCancelPayment(any(), any(), any(), any())).thenReturn(spiErrorMessage);

        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisCancellationScaMethodSelectedStage.apply(buildXs2aUpdatePisPsuDataRequest(), buildResponse(PAYMENT_ID));


        // Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getErrorHolder().getTppMessageInformationList().iterator().next().getMessageErrorCode()).isEqualTo(MessageErrorCode.FORMAT_ERROR);
        assertThat(actualResponse.getErrorHolder().getTppMessageInformationList().iterator().next().getText()).isEqualTo(errorMessagesString);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataRequest buildXs2aUpdatePisPsuDataRequest() {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
        request.setAuthorisationId(AUTHORISATION_ID);
        request.setPaymentId(PAYMENT_ID);
        return request;
    }

    private GetPisAuthorisationResponse buildResponse(String paymentId) {
        GetPisAuthorisationResponse pisAuthorisationResponse = new GetPisAuthorisationResponse();
        pisAuthorisationResponse.setPaymentType(PaymentType.SINGLE);
        pisAuthorisationResponse.setPaymentProduct(PAYMENT_PRODUCT);
        PisPaymentInfo pisPaymentInfo = buildPisPaymentInfo(paymentId);
        pisAuthorisationResponse.setPaymentInfo(pisPaymentInfo);
        pisAuthorisationResponse.setPayments(getPisPayment());
        return pisAuthorisationResponse;
    }

    private PisPaymentInfo buildPisPaymentInfo(String paymentId) {
        PisPaymentInfo pisPaymentInfo = new PisPaymentInfo();
        pisPaymentInfo.setPaymentProduct(PAYMENT_PRODUCT);
        pisPaymentInfo.setPaymentType(PaymentType.SINGLE);
        pisPaymentInfo.setPaymentId(paymentId);
        return pisPaymentInfo;
    }

    private List<PisPayment> getPisPayment() {
        PisPayment pisPayment = new PisPayment();
        pisPayment.setTransactionStatus(TransactionStatus.RCVD);
        return Collections.singletonList(pisPayment);
    }

    private GetPisAuthorisationResponse buildGetPisAuthorisationResponse() {
        return new GetPisAuthorisationResponse();
    }

}

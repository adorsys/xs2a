/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.event.EventType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.psd2.xs2a.domain.fund.FundsConfirmationResponse;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aFundsConfirmationMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiFundsConfirmationRequestMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.fund.SpiFundsConfirmationRequest;
import de.adorsys.psd2.xs2a.spi.domain.fund.SpiFundsConfirmationResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.FundsConfirmationSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIIS_400;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FundsConfirmationServiceTest {
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData(new byte[0], "Some Consent ID");
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(null, null, null, null);
    private static final SpiContextData SPI_CONTEXT_DATA = new SpiContextData(new SpiPsuData(null, null, null, null), new TppInfo());
    private final List<String> ERROR_MESSAGE_TEXT = Arrays.asList("message 1", "message 2", "message 3");

    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;
    @Mock
    private Xs2aToSpiFundsConfirmationRequestMapper xs2aToSpiFundsConfirmationRequestMapper;
    @Mock
    private SpiToXs2aFundsConfirmationMapper spiToXs2aFundsConfirmationMapper;
    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private FundsConfirmationConsentDataService fundsConfirmationConsentDataService;
    @Mock
    private FundsConfirmationSpi fundsConfirmationSpi;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private SpiErrorMapper spiErrorMapper;

    @InjectMocks
    private FundsConfirmationService fundsConfirmationService;


    @Before
    public void setUp() {
        when(xs2aToSpiFundsConfirmationRequestMapper.mapToSpiFundsConfirmationRequest(buildFundsConfirmationRequest()))
            .thenReturn(buildSpiFundsConfirmationRequest());
        when(fundsConfirmationConsentDataService.getAspspConsentData(anyString()))
            .thenReturn(ASPSP_CONSENT_DATA);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA))
            .thenReturn(SPI_CONTEXT_DATA);
        when(spiToXs2aFundsConfirmationMapper.mapToFundsConfirmationResponse(buildSpiFundsConfirmationResponse()))
            .thenReturn(buildFundsConfirmationResponse());
    }

    @Test
    public void fundsConfirmation_Success_ShouldRecordEvent() {
        when(aspspProfileServiceWrapper.isPiisConsentSupported()).thenReturn(false);
        when(fundsConfirmationSpi.performFundsSufficientCheck(any(), any(), any(), any()))
            .thenReturn(new SpiResponse<>(buildSpiFundsConfirmationResponse(), ASPSP_CONSENT_DATA));

        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);
        FundsConfirmationRequest request = buildFundsConfirmationRequest();

        // When
        fundsConfirmationService.fundsConfirmation(request);

        // Then
        verify(xs2aEventService, times(1)).recordTppRequest(argumentCaptor.capture(), any());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.FUNDS_CONFIRMATION_REQUEST_RECEIVED);
    }

    @Test
    public void fundsConfirmation_fundsConfirmationSpi_performFundsSufficientCheck_fail() {
        // Given:
        String errorMessagesString = ERROR_MESSAGE_TEXT.toString().replace("[", "").replace("]", "");

        ErrorHolder errorHolder = ErrorHolder.builder(MessageErrorCode.FORMAT_ERROR)
                                      .errorType(PIIS_400)
                                      .messages(ERROR_MESSAGE_TEXT)
                                      .build();
        when(spiErrorMapper.mapToErrorHolder(any(SpiResponse.class), eq(ServiceType.PIIS)))
            .thenReturn(errorHolder);


        when(aspspProfileServiceWrapper.isPiisConsentSupported()).thenReturn(false);
        when(fundsConfirmationSpi.performFundsSufficientCheck(any(), any(), any(), any()))
            .thenReturn(SpiResponse.<SpiFundsConfirmationResponse>builder()
                            .aspspConsentData(ASPSP_CONSENT_DATA)
                            .message(ERROR_MESSAGE_TEXT)
                            .fail(SpiResponseStatus.LOGICAL_FAILURE));

        // When
        ResponseObject<FundsConfirmationResponse> response = fundsConfirmationService.fundsConfirmation(buildFundsConfirmationRequest());

        //Than
        assertThat(response.hasError()).isTrue();
        assertThat(response.getBody()).isNull();
        assertThat(response.getError().getErrorType()).isEqualTo(ErrorType.PIIS_400);
        assertThat(response.getError().getTppMessage().getText()).isEqualTo(errorMessagesString);
        assertThat(response.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.FORMAT_ERROR);
    }

    private FundsConfirmationRequest buildFundsConfirmationRequest() {
        return new FundsConfirmationRequest();
    }

    private SpiFundsConfirmationRequest buildSpiFundsConfirmationRequest() {
        return new SpiFundsConfirmationRequest();
    }

    private SpiFundsConfirmationResponse buildSpiFundsConfirmationResponse() {
        SpiFundsConfirmationResponse response = new SpiFundsConfirmationResponse();
        response.setFundsAvailable(true);
        return response;
    }

    private FundsConfirmationResponse buildFundsConfirmationResponse() {
        FundsConfirmationResponse response = new FundsConfirmationResponse();
        response.setFundsAvailable(true);
        return response;
    }
}

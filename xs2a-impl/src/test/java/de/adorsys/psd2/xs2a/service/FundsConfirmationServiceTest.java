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
import de.adorsys.psd2.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiFundsConfirmationRequestMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.spi.domain.fund.SpiFundsConfirmationRequest;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.FundsConfirmationSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FundsConfirmationServiceTest {
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData(new byte[0], "Some Consent ID");
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(null, null, null, null);
    private static final SpiPsuData SPI_PSU_DATA = new SpiPsuData(null, null, null, null);

    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;
    @Mock
    private Xs2aToSpiFundsConfirmationRequestMapper xs2aToSpiFundsConfirmationRequestMapper;
    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private FundsConfirmationConsentDataService fundsConfirmationConsentDataService;
    @Mock
    private FundsConfirmationPsuDataService fundsConfirmationPsuDataService;
    @Mock
    private Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;
    @Mock
    private FundsConfirmationSpi fundsConfirmationSpi;
    @InjectMocks
    private FundsConfirmationService fundsConfirmationService;

    @Before
    public void setUp() {
        when(xs2aToSpiFundsConfirmationRequestMapper.mapToSpiFundsConfirmationRequest(buildFundsConfirmationRequest()))
            .thenReturn(buildSpiFundsConfirmationRequest());
        when(fundsConfirmationConsentDataService.getAspspConsentDataByConsentId(anyString()))
            .thenReturn(ASPSP_CONSENT_DATA);
        when(fundsConfirmationPsuDataService.getPsuDataByConsentId(anyString())).thenReturn(PSU_ID_DATA);
        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA))
            .thenReturn(SPI_PSU_DATA);
    }

    @Test
    public void fundsConfirmation_Success_ShouldRecordEvent() {
        when(aspspProfileServiceWrapper.isPiisConsentSupported()).thenReturn(false);
        when(fundsConfirmationSpi.performFundsSufficientCheck(any(), anyString(), any(), any()))
            .thenReturn(new SpiResponse<>(Boolean.TRUE, ASPSP_CONSENT_DATA));

        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);
        FundsConfirmationRequest request = buildFundsConfirmationRequest();

        // When
        fundsConfirmationService.fundsConfirmation(request);

        // Then
        verify(xs2aEventService, times(1)).recordTppRequest(argumentCaptor.capture(), any());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.FUNDS_CONFIRMATION_REQUEST_RECEIVED);
    }

    private FundsConfirmationRequest buildFundsConfirmationRequest() {
        return new FundsConfirmationRequest();
    }

    private SpiFundsConfirmationRequest buildSpiFundsConfirmationRequest() {
        return new SpiFundsConfirmationRequest();
    }
}

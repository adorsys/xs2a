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

package de.adorsys.psd2.xs2a.service.authorization.ais.stage.embedded;

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.consent.AisConsentDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiResponseStatusToXs2aMessageErrorCodeMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiVerifyScaAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AisScaAuthenticatedStageTest {
    private static final String CONSENT_ID = "Test consentId";
    private static final String WRONG_CONSENT_ID = "wrong consent id";
    private static final String TEST_AUTHENTICATION_DATA = "Test authenticationData";
    private static final String PSU_ID = "Test psuId";
    private static final ConsentStatus VALID_CONSENT_STATUS = ConsentStatus.VALID;
    private static final ConsentStatus PARTIALLY_AUTHORISED_CONSENT_STATUS = ConsentStatus.PARTIALLY_AUTHORISED;
    private static final ScaStatus FAILED_SCA_STATUS = ScaStatus.FAILED;
    private static final ScaStatus FINALIZED_SCA_STATUS = ScaStatus.FINALISED;
    private static final SpiResponseStatus RESPONSE_STATUS = SpiResponseStatus.LOGICAL_FAILURE;
    private static final MessageErrorCode ERROR_CODE = MessageErrorCode.FORMAT_ERROR;
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(PSU_ID, null, null, null);
    private static final SpiPsuData SPI_PSU_DATA = new SpiPsuData(null, null, null, null);
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData(new byte[0], "Some Consent ID");
    private static final SpiContextData SPI_CONTEXT_DATA = new SpiContextData(SPI_PSU_DATA, new TppInfo(), UUID.randomUUID());

    @InjectMocks
    private AisScaAuthenticatedStage scaAuthenticatedStage;

    @Mock
    private Xs2aAisConsentService aisConsentService;
    @Mock
    private AisConsentDataService aisConsentDataService;
    @Mock
    private AisConsentSpi aisConsentSpi;
    @Mock
    private Xs2aAisConsentMapper aisConsentMapper;
    @Mock
    private SpiResponseStatusToXs2aMessageErrorCodeMapper messageErrorCodeMapper;
    @Mock
    private Xs2aToSpiPsuDataMapper psuDataMapper;
    @Mock
    private UpdateConsentPsuDataReq request;
    @Mock
    private SpiAccountConsent spiAccountConsent;
    @Mock
    private AccountConsent accountConsent;
    @Mock
    private SpiScaConfirmation scaConfirmation;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private SpiErrorMapper spiErrorMapper;

    @Before
    public void setUp() {
        when(request.getConsentId())
            .thenReturn(CONSENT_ID);

        when(request.getPsuData())
            .thenReturn(PSU_ID_DATA);

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID))
            .thenReturn(Optional.empty());

        when(psuDataMapper.mapToSpiPsuData(any(PsuIdData.class)))
            .thenReturn(SPI_PSU_DATA);

        when(aisConsentMapper.mapToSpiScaConfirmation(eq(request), any()))
            .thenReturn(scaConfirmation);

        when(aisConsentMapper.mapToSpiAccountConsent(accountConsent))
            .thenReturn(spiAccountConsent);

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        doNothing()
            .when(aisConsentDataService).updateAspspConsentData(ASPSP_CONSENT_DATA);

        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA))
            .thenReturn(SPI_CONTEXT_DATA);
    }

    @Test
    public void apply_Success() {
        when(aisConsentSpi.verifyScaAuthorisation(SPI_CONTEXT_DATA, scaConfirmation, spiAccountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(VALID_CONSENT_STATUS));

        doNothing()
            .when(aisConsentService).updateConsentStatus(CONSENT_ID, VALID_CONSENT_STATUS);

        when(request.getScaAuthenticationData())
            .thenReturn(TEST_AUTHENTICATION_DATA);

        when(aisConsentService.findAndTerminateOldConsentsByNewConsentId(CONSENT_ID))
            .thenReturn(true);

        UpdateConsentPsuDataResponse actualResponse = scaAuthenticatedStage.apply(request);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getScaAuthenticationData()).isEqualTo(TEST_AUTHENTICATION_DATA);
        assertThat(actualResponse.getScaStatus()).isEqualTo(FINALIZED_SCA_STATUS);
        assertThat(actualResponse.getResponseLinkType()).isEqualTo(START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION);
    }

    @Test
    public void apply_Failure_SpiResponseWithError() {
        when(aisConsentSpi.verifyScaAuthorisation(SPI_CONTEXT_DATA, scaConfirmation, spiAccountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildErrorSpiResponse());

        when(messageErrorCodeMapper.mapToMessageErrorCode(RESPONSE_STATUS))
            .thenReturn(ERROR_CODE);

        when(spiErrorMapper.mapToErrorHolder(buildErrorSpiResponse(), ServiceType.AIS))
            .thenReturn(ErrorHolder.builder(ERROR_CODE).errorType(ErrorType.AIS_400).build());

        UpdateConsentPsuDataResponse actualResponse = scaAuthenticatedStage.apply(request);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getScaStatus()).isEqualTo(FAILED_SCA_STATUS);
        assertThat(actualResponse.getMessageError().getErrorType()).isEqualTo(ErrorType.AIS_400);
    }

    @Test
    public void apply_Identification_wrongId_Failure() {
        //Given
        when(request.getConsentId()).thenReturn(WRONG_CONSENT_ID);

        //When
        UpdateConsentPsuDataResponse actualResponse = scaAuthenticatedStage.apply(request);

        //Then
        assertThat(actualResponse.getScaStatus()).isEqualTo(ScaStatus.FAILED);
        assertThat(actualResponse.getMessageError().getErrorType()).isEqualTo(ErrorType.AIS_400);
        assertThat(actualResponse.getMessageError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.CONSENT_UNKNOWN_400);
    }

    @Test
    public void apply_Success_verifyUpdateMultilevelScaRequiredMethodIsCalled() {
        when(aisConsentSpi.verifyScaAuthorisation(SPI_CONTEXT_DATA, scaConfirmation, spiAccountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(PARTIALLY_AUTHORISED_CONSENT_STATUS));

        doNothing()
            .when(aisConsentService).updateConsentStatus(CONSENT_ID, PARTIALLY_AUTHORISED_CONSENT_STATUS);

        when(request.getScaAuthenticationData())
            .thenReturn(TEST_AUTHENTICATION_DATA);

        when(aisConsentService.findAndTerminateOldConsentsByNewConsentId(CONSENT_ID))
            .thenReturn(true);

        UpdateConsentPsuDataResponse actualResponse = scaAuthenticatedStage.apply(request);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getScaAuthenticationData()).isEqualTo(TEST_AUTHENTICATION_DATA);
        assertThat(actualResponse.getScaStatus()).isEqualTo(FINALIZED_SCA_STATUS);
        assertThat(actualResponse.getResponseLinkType()).isEqualTo(START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION);

        verify(aisConsentService).updateMultilevelScaRequired(CONSENT_ID, true);
    }

    @Test
    public void apply_Success_verifyUpdateMultilevelScaRequiredMethodIsNotCalled() {
        when(aisConsentSpi.verifyScaAuthorisation(SPI_CONTEXT_DATA, scaConfirmation, spiAccountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(VALID_CONSENT_STATUS));

        doNothing()
            .when(aisConsentService).updateConsentStatus(CONSENT_ID, VALID_CONSENT_STATUS);

        when(request.getScaAuthenticationData())
            .thenReturn(TEST_AUTHENTICATION_DATA);

        when(aisConsentService.findAndTerminateOldConsentsByNewConsentId(CONSENT_ID))
            .thenReturn(true);

        UpdateConsentPsuDataResponse actualResponse = scaAuthenticatedStage.apply(request);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getScaAuthenticationData()).isEqualTo(TEST_AUTHENTICATION_DATA);
        assertThat(actualResponse.getScaStatus()).isEqualTo(FINALIZED_SCA_STATUS);
        assertThat(actualResponse.getResponseLinkType()).isEqualTo(START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION);

        verify(aisConsentService, never()).updateMultilevelScaRequired(CONSENT_ID, true);
    }

    @Test
    public void apply_Success_verifyUpdateConsentStatusMethodIsCalled() {
        when(aisConsentSpi.verifyScaAuthorisation(SPI_CONTEXT_DATA, scaConfirmation, spiAccountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(VALID_CONSENT_STATUS));

        when(accountConsent.getConsentStatus())
            .thenReturn(PARTIALLY_AUTHORISED_CONSENT_STATUS);

        doNothing()
            .when(aisConsentService).updateConsentStatus(CONSENT_ID, VALID_CONSENT_STATUS);

        when(request.getScaAuthenticationData())
            .thenReturn(TEST_AUTHENTICATION_DATA);

        when(aisConsentService.findAndTerminateOldConsentsByNewConsentId(CONSENT_ID))
            .thenReturn(true);

        UpdateConsentPsuDataResponse actualResponse = scaAuthenticatedStage.apply(request);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getScaAuthenticationData()).isEqualTo(TEST_AUTHENTICATION_DATA);
        assertThat(actualResponse.getScaStatus()).isEqualTo(FINALIZED_SCA_STATUS);
        assertThat(actualResponse.getResponseLinkType()).isEqualTo(START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION);

        verify(aisConsentService).updateConsentStatus(CONSENT_ID, VALID_CONSENT_STATUS);
    }

    @Test
    public void apply_Success_verifyUpdateConsentStatusMethodIsNotCalled() {
        when(aisConsentSpi.verifyScaAuthorisation(SPI_CONTEXT_DATA, scaConfirmation, spiAccountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(PARTIALLY_AUTHORISED_CONSENT_STATUS));

        when(accountConsent.getConsentStatus())
            .thenReturn(PARTIALLY_AUTHORISED_CONSENT_STATUS);

        doNothing()
            .when(aisConsentService).updateConsentStatus(CONSENT_ID, VALID_CONSENT_STATUS);

        when(request.getScaAuthenticationData())
            .thenReturn(TEST_AUTHENTICATION_DATA);

        when(aisConsentService.findAndTerminateOldConsentsByNewConsentId(CONSENT_ID))
            .thenReturn(true);

        UpdateConsentPsuDataResponse actualResponse = scaAuthenticatedStage.apply(request);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getScaAuthenticationData()).isEqualTo(TEST_AUTHENTICATION_DATA);
        assertThat(actualResponse.getScaStatus()).isEqualTo(FINALIZED_SCA_STATUS);
        assertThat(actualResponse.getResponseLinkType()).isEqualTo(START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION);

        verify(aisConsentService, never()).updateConsentStatus(CONSENT_ID, PARTIALLY_AUTHORISED_CONSENT_STATUS);
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private SpiResponse<SpiVerifyScaAuthorisationResponse> buildSuccessSpiResponse(ConsentStatus consentStatus) {
        return SpiResponse.<SpiVerifyScaAuthorisationResponse>builder()
                   .payload(new SpiVerifyScaAuthorisationResponse(consentStatus))
                   .aspspConsentData(ASPSP_CONSENT_DATA)
                   .success();
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private SpiResponse<SpiVerifyScaAuthorisationResponse> buildErrorSpiResponse() {
        return SpiResponse.<SpiVerifyScaAuthorisationResponse>builder()
                   .aspspConsentData(ASPSP_CONSENT_DATA)
                   .fail(RESPONSE_STATUS);
    }
}

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

package de.adorsys.psd2.xs2a.service.authorization.ais.stage;


import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.service.consent.AisConsentDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiResponseStatusToXs2aMessageErrorCodeMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAuthenticationObjectMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthenticationObject;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION;
import static de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.START_AUTHORISATION_WITH_TRANSACTION_AUTHORISATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AisScaStartAuthorisationStageTest {
    private static final String CONSENT_ID = "Test consentId";
    private static final String PASSWORD = "Test password";
    private static final String PSU_ID = "Test psuId";
    private static final String TEST_AUTHENTICATION_METHOD_ID = "sms";
    private static final SpiResponseStatus RESPONSE_STATUS = SpiResponseStatus.LOGICAL_FAILURE;
    private static final ScaStatus FAILED_SCA_STATUS = ScaStatus.FAILED;
    private static final ScaStatus AUTHENTICATED_SCA_STATUS = ScaStatus.PSUAUTHENTICATED;
    private static final ScaStatus METHOD_SELECTED_SCA_STATUS = ScaStatus.SCAMETHODSELECTED;
    private static final MessageErrorCode FORMAT_ERROR_CODE = MessageErrorCode.FORMAT_ERROR;
    private static final MessageErrorCode SCA_METHOD_UNKNOWN_ERROR_CODE = MessageErrorCode.SCA_METHOD_UNKNOWN;
    private static final MessageErrorCode PSU_CREDENTIALS_INVALID_ERROR_CODE = MessageErrorCode.PSU_CREDENTIALS_INVALID;
    private static final SpiPsuData SPI_PSU_DATA = new SpiPsuData(PSU_ID, null, null, null);
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(PSU_ID, null, null, null);
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData(new byte[0], "Some Consent ID");
    private static final List<SpiAuthenticationObject> MULTIPLE_SPI_SCA_METHODS = Arrays.asList(buildSpiSmsAuthenticationObject(), buildSpiPhotoAuthenticationObject());
    private static final List<Xs2aAuthenticationObject> MULTIPLE_CMS_SCA_METHODS = Arrays.asList(buildXs2aSmsAuthenticationObject(), buildXs2aPhotoAuthenticationObject());
    private static final List<SpiAuthenticationObject> ONE_SPI_SCA_METHOD = Collections.singletonList(buildSpiSmsAuthenticationObject());
    private static final List<SpiAuthenticationObject> NONE_SPI_SCA_METHOD = Collections.emptyList();
    private static final SpiContextData SPI_CONTEXT_DATA = new SpiContextData(SPI_PSU_DATA, new TppInfo());

    @InjectMocks
    private AisScaStartAuthorisationStage scaStartAuthorisationStage;

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
    private SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;

    @Before
    public void setUp() {
        when(request.getConsentId())
            .thenReturn(CONSENT_ID);

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(accountConsent);

        when(aisConsentMapper.mapToSpiAccountConsent(accountConsent))
            .thenReturn(spiAccountConsent);

        when(psuDataMapper.mapToSpiPsuData(any(PsuIdData.class)))
            .thenReturn(SPI_PSU_DATA);

        when(request.getPassword())
            .thenReturn(PASSWORD);

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        when(spiToXs2aAuthenticationObjectMapper.mapToXs2aAuthenticationObject(buildSpiSmsAuthenticationObject())).thenReturn(buildXs2aSmsAuthenticationObject());

        when(spiToXs2aAuthenticationObjectMapper.mapToXs2aAuthenticationObject(buildSpiPhotoAuthenticationObject())).thenReturn(buildXs2aPhotoAuthenticationObject());

        when(spiToXs2aAuthenticationObjectMapper.mapToXs2aListAuthenticationObject(Arrays.asList(buildSpiSmsAuthenticationObject(), buildSpiPhotoAuthenticationObject()))).thenReturn(Arrays.asList(buildXs2aSmsAuthenticationObject(), buildXs2aPhotoAuthenticationObject()));

        doNothing()
            .when(aisConsentDataService).updateAspspConsentData(ASPSP_CONSENT_DATA);

        when(request.getPsuData())
            .thenReturn(PSU_ID_DATA);

        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA))
            .thenReturn(SPI_CONTEXT_DATA);
    }

    @Test
    public void apply_AllAvailableAccounts_Success() {
        //Given
        ArgumentCaptor<ConsentStatus> argumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);
        when(accountConsent.getAisConsentRequestType())
            .thenReturn(AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS);
        when(accountConsent.isRecurringIndicator())
            .thenReturn(false);
        when(aspspProfileServiceWrapper.isScaByOneTimeAvailableAccountsConsentRequired())
            .thenReturn(false);
        when(aisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, SPI_PSU_DATA, PASSWORD, spiAccountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(SpiAuthorisationStatus.SUCCESS));
        //When
        UpdateConsentPsuDataResponse actualResponse = scaStartAuthorisationStage.apply(request);
        //Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getScaStatus()).isEqualTo(ScaStatus.FINALISED);
        verify(aisConsentService, times(1)).updateConsentStatus(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(ConsentStatus.VALID);
    }

    @Test
    public void apply_Failure_AuthorisationStatusSpiResponseFailed() {
        when(aisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, SPI_PSU_DATA, PASSWORD, spiAccountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildErrorSpiResponse(SpiAuthorisationStatus.FAILURE));

        UpdateConsentPsuDataResponse actualResponse = scaStartAuthorisationStage.apply(request);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getScaStatus()).isEqualTo(FAILED_SCA_STATUS);
        assertThat(actualResponse.getErrorCode()).isEqualTo(PSU_CREDENTIALS_INVALID_ERROR_CODE);
    }

    @Test
    public void apply_MultipleAvailableScaMethods_Success() {
        when(aisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, SPI_PSU_DATA, PASSWORD, spiAccountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(SpiAuthorisationStatus.SUCCESS));

        when(aisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiAccountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(MULTIPLE_SPI_SCA_METHODS));

        UpdateConsentPsuDataResponse actualResponse = scaStartAuthorisationStage.apply(request);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getPsuId()).isEqualTo(PSU_ID);
        assertThat(actualResponse.getAvailableScaMethods()).isEqualTo(MULTIPLE_CMS_SCA_METHODS);
        assertThat(actualResponse.getScaStatus()).isEqualTo(AUTHENTICATED_SCA_STATUS);
        assertThat(actualResponse.getResponseLinkType()).isEqualTo(START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION);
    }

    @Test
    public void apply_OneAvailableScaMethod_Success() {
        when(request.getPsuData())
            .thenReturn(PSU_ID_DATA);

        when(aisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, SPI_PSU_DATA, PASSWORD, spiAccountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(SpiAuthorisationStatus.SUCCESS));

        when(aisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiAccountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(ONE_SPI_SCA_METHOD));

        SpiAuthenticationObject scaMethod = ONE_SPI_SCA_METHOD.get(0);

        when(aisConsentSpi.requestAuthorisationCode(SPI_CONTEXT_DATA, TEST_AUTHENTICATION_METHOD_ID, spiAccountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(new SpiAuthorizationCodeResult()));

        UpdateConsentPsuDataResponse actualResponse = scaStartAuthorisationStage.apply(request);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getPsuId()).isEqualTo(PSU_ID);
        assertThat(actualResponse.getChosenScaMethod()).isEqualTo(buildXs2aSmsAuthenticationObject());
        assertThat(actualResponse.getScaStatus()).isEqualTo(METHOD_SELECTED_SCA_STATUS);
        assertThat(actualResponse.getResponseLinkType()).isEqualTo(START_AUTHORISATION_WITH_TRANSACTION_AUTHORISATION);
    }

    @Test
    public void apply_OneAvailableScaMethod_Failure_ResponseWithError() {
        when(aisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, SPI_PSU_DATA, PASSWORD, spiAccountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(SpiAuthorisationStatus.SUCCESS));

        when(aisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiAccountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(ONE_SPI_SCA_METHOD));

        SpiAuthenticationObject scaMethod = ONE_SPI_SCA_METHOD.get(0);

        when(aisConsentSpi.requestAuthorisationCode(SPI_CONTEXT_DATA, TEST_AUTHENTICATION_METHOD_ID, spiAccountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildErrorSpiResponse(new SpiAuthorizationCodeResult()));

        when(messageErrorCodeMapper.mapToMessageErrorCode(RESPONSE_STATUS))
            .thenReturn(FORMAT_ERROR_CODE);

        UpdateConsentPsuDataResponse actualResponse = scaStartAuthorisationStage.apply(request);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getErrorCode()).isEqualTo(FORMAT_ERROR_CODE);
    }

    @Test
    public void apply_NoneAvailableScaMethods_Failure_WrongScenarioAccordingToSpecification() {
        when(request.getPsuData())
            .thenReturn(PSU_ID_DATA);

        when(aisConsentSpi.authorisePsu(SPI_CONTEXT_DATA, SPI_PSU_DATA, PASSWORD, spiAccountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(SpiAuthorisationStatus.SUCCESS));

        when(aisConsentSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, spiAccountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(NONE_SPI_SCA_METHOD));

        UpdateConsentPsuDataResponse actualResponse = scaStartAuthorisationStage.apply(request);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getPsuId()).isEqualTo(PSU_ID);
        assertThat(actualResponse.getScaStatus()).isEqualTo(FAILED_SCA_STATUS);
        assertThat(actualResponse.getErrorCode()).isEqualTo(SCA_METHOD_UNKNOWN_ERROR_CODE);
    }

    private static SpiAuthenticationObject buildSpiSmsAuthenticationObject() {
        SpiAuthenticationObject spiAuthenticationObject = new SpiAuthenticationObject();
        spiAuthenticationObject.setAuthenticationMethodId("sms");
        spiAuthenticationObject.setAuthenticationType("SMS_OTP");
        return spiAuthenticationObject;
    }

    private static SpiAuthenticationObject buildSpiPhotoAuthenticationObject() {
        SpiAuthenticationObject spiAuthenticationObject = new SpiAuthenticationObject();
        spiAuthenticationObject.setAuthenticationMethodId("photo");
        spiAuthenticationObject.setAuthenticationType("PHOTO_OTP");
        return spiAuthenticationObject;
    }

    private static Xs2aAuthenticationObject buildXs2aSmsAuthenticationObject() {
        Xs2aAuthenticationObject xs2aAuthenticationObject = new Xs2aAuthenticationObject();
        xs2aAuthenticationObject.setAuthenticationMethodId("sms");
        xs2aAuthenticationObject.setAuthenticationType("SMS_OTP");
        return xs2aAuthenticationObject;
    }

    private static Xs2aAuthenticationObject buildXs2aPhotoAuthenticationObject() {
        Xs2aAuthenticationObject xs2aAuthenticationObject = new Xs2aAuthenticationObject();
        xs2aAuthenticationObject.setAuthenticationMethodId("photo");
        xs2aAuthenticationObject.setAuthenticationType("PHOTO_OTP");
        return xs2aAuthenticationObject;
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private <T> SpiResponse<T> buildSuccessSpiResponse(T payload) {
        return SpiResponse.<T>builder()
                   .payload(payload)
                   .aspspConsentData(ASPSP_CONSENT_DATA)
                   .success();
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private <T> SpiResponse<T> buildErrorSpiResponse(T payload) {
        return SpiResponse.<T>builder()
                   .payload(payload)
                   .aspspConsentData(ASPSP_CONSENT_DATA)
                   .fail(RESPONSE_STATUS);
    }
}


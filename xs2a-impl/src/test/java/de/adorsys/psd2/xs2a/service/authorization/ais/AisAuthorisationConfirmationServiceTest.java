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

package de.adorsys.psd2.xs2a.service.authorization.ais;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationResponse;
import de.adorsys.psd2.consent.api.service.AisConsentAuthorisationServiceEncrypted;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiConfirmationCode;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiConfirmationCodeCheckingResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static de.adorsys.psd2.consent.api.CmsError.TECHNICAL_ERROR;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_403;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AisAuthorisationConfirmationServiceTest {
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";
    private final static JsonReader jsonReader = new JsonReader();

    @InjectMocks
    private AisAuthorisationConfirmationService aisAuthorisationConfirmationService;
    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    @Mock
    private Xs2aAisConsentService aisConsentService;
    @Mock
    private AisConsentSpi aisConsentSpi;
    @Mock
    private Xs2aAisConsentMapper aisConsentMapper;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private AisConsentAuthorisationServiceEncrypted aisConsentAuthorisationServiceEncrypted;
    @Mock
    private SpiAccountConsent spiAccountConsent;
    @Mock
    private SpiAspspConsentDataProvider aspspConsentDataProvider;

    @Test
    public void processAuthorisationConfirmation_success_checkOnSpi() {
        // given
        PsuIdData psuIdData = buildPsuIdData();
        UpdateConsentPsuDataReq request = buildUpdateConsentPsuDataReq();
        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse(ScaStatus.FINALISED, CONSENT_ID, AUTHORISATION_ID);
        ResponseObject<UpdateConsentPsuDataResponse> expectedResult = ResponseObject.<UpdateConsentPsuDataResponse>builder().body(response).build();

        SpiConfirmationCode spiConfirmationCode = new SpiConfirmationCode(request.getConfirmationCode());
        SpiContextData contextData = getSpiContextData();
        AccountConsent consent = createConsent();
        AisConsentAuthorizationResponse aisConsentAuthorizationResponse = getConsentAuthorisationResponse();

        when(aisConsentAuthorisationServiceEncrypted.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(CmsResponse.<AisConsentAuthorizationResponse>builder()
                            .payload(aisConsentAuthorizationResponse)
                            .build());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(consent));
        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(false);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData))
            .thenReturn(contextData);
        when(aisConsentMapper.mapToSpiAccountConsent(consent))
            .thenReturn(spiAccountConsent);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(aspspConsentDataProvider);
        when(aisConsentSpi.checkConfirmationCode(contextData, spiConfirmationCode, spiAccountConsent, aspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiConfirmationCodeCheckingResponse>builder()
                            .payload(new SpiConfirmationCodeCheckingResponse(ScaStatus.FINALISED))
                            .build());

        // when
        ResponseObject<UpdateConsentPsuDataResponse> actualResult = aisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualToComparingFieldByField(expectedResult);
        verify(aisConsentService, times(1)).updateConsentAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FINALISED);
    }

    @Test
    public void processAuthorisationConfirmation_success_checkOnXs2a() {
        // given
        UpdateConsentPsuDataReq request = buildUpdateConsentPsuDataReq();
        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse(ScaStatus.FINALISED, CONSENT_ID, AUTHORISATION_ID);
        ResponseObject<UpdateConsentPsuDataResponse> expectedResult = ResponseObject.<UpdateConsentPsuDataResponse>builder().body(response).build();
        AisConsentAuthorizationResponse aisConsentAuthorizationResponse = getConsentAuthorisationResponse();

        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(true);
        when(aisConsentAuthorisationServiceEncrypted.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(CmsResponse.<AisConsentAuthorizationResponse>builder()
                            .payload(aisConsentAuthorizationResponse)
                            .build());
        // when
        ResponseObject<UpdateConsentPsuDataResponse> actualResult = aisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualToComparingFieldByField(expectedResult);
        verify(aisConsentService, times(1)).updateConsentAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FINALISED);
    }

    @Test
    public void processAuthorisationConfirmation_failed_NoAuthorisation() {
        // given
        UpdateConsentPsuDataReq request = buildUpdateConsentPsuDataReq();
        ResponseObject<UpdateConsentPsuDataResponse> expectedResult = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                          .fail(AIS_403, of(CONSENT_UNKNOWN_403)).build();

        when(aisConsentAuthorisationServiceEncrypted.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(CmsResponse.<AisConsentAuthorizationResponse>builder()
                            .error(TECHNICAL_ERROR)
                            .build());
        // when
        ResponseObject<UpdateConsentPsuDataResponse> actualResult = aisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualToComparingFieldByField(expectedResult);
        verify(aisConsentService, times(0)).updateConsentAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FINALISED);
    }

    @Test
    public void processAuthorisationConfirmation_failed_WrongScaStatus() {
        // given
        UpdateConsentPsuDataReq request = buildUpdateConsentPsuDataReq();
        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.AIS_400)
                                      .tppMessages(of(FORMAT_ERROR_SCA_STATUS, ScaStatus.FINALISED.name(), ScaStatus.UNCONFIRMED.name(), ScaStatus.PSUAUTHENTICATED))
                                      .build();

        ResponseObject<UpdateConsentPsuDataResponse> expectedResult = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                          .fail(errorHolder)
                                                                          .build();
        AisConsentAuthorizationResponse aisConsentAuthorizationResponse = getConsentAuthorisationResponse();
        aisConsentAuthorizationResponse.setScaStatus(ScaStatus.PSUAUTHENTICATED);

        when(aisConsentAuthorisationServiceEncrypted.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(CmsResponse.<AisConsentAuthorizationResponse>builder()
                            .payload(aisConsentAuthorizationResponse)
                            .build());
        // when
        ResponseObject<UpdateConsentPsuDataResponse> actualResult = aisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualToComparingFieldByField(expectedResult);
        verify(aisConsentService, times(0)).updateConsentAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FINALISED);
    }

    @Test
    public void processAuthorisationConfirmation_failed_wrongCode() {
        // given
        UpdateConsentPsuDataReq request = buildUpdateConsentPsuDataReq();
        request.setConfirmationCode("wrong_code");

        AisConsentAuthorizationResponse aisConsentAuthorizationResponse = getConsentAuthorisationResponse();
        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.AIS_400)
                                      .tppMessages(of(ERROR_SCA_CONFIRMATION_CODE))
                                      .build();
        ResponseObject<UpdateConsentPsuDataResponse> expectedResult = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                          .fail(errorHolder)
                                                                          .build();

        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(true);
        when(aisConsentAuthorisationServiceEncrypted.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(CmsResponse.<AisConsentAuthorizationResponse>builder()
                            .payload(aisConsentAuthorizationResponse)
                            .build());
        // when
        ResponseObject<UpdateConsentPsuDataResponse> actualResult = aisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        // then
        assertThat(actualResult).isEqualToComparingFieldByField(expectedResult);
        verify(aisConsentService, times(0)).updateConsentAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FINALISED);

    }

    @Test
    public void processAuthorisationConfirmation_failed_ConsentNotFound() {
        // given
        UpdateConsentPsuDataReq request = buildUpdateConsentPsuDataReq();
        AisConsentAuthorizationResponse aisConsentAuthorizationResponse = getConsentAuthorisationResponse();
        ErrorHolder errorHolder = ErrorHolder.builder(AIS_403)
                                      .tppMessages(of(CONSENT_UNKNOWN_403))
                                      .build();
        ResponseObject<UpdateConsentPsuDataResponse> expectedResult = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                          .fail(errorHolder)
                                                                          .build();
        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(false);
        when(aisConsentAuthorisationServiceEncrypted.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(CmsResponse.<AisConsentAuthorizationResponse>builder()
                            .payload(aisConsentAuthorizationResponse)
                            .build());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.empty());

        // when
        ResponseObject<UpdateConsentPsuDataResponse> actualResult = aisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualToComparingFieldByField(expectedResult);
        verify(aisConsentService, times(0)).updateConsentAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FINALISED);
    }

    @Test
    public void processAuthorisationConfirmation_failed_errorOnSpi() {
        // given
        PsuIdData psuIdData = buildPsuIdData();
        UpdateConsentPsuDataReq request = buildUpdateConsentPsuDataReq();
        SpiResponse<SpiConfirmationCodeCheckingResponse> spiResponse = SpiResponse.<SpiConfirmationCodeCheckingResponse>builder()
                                                                           .error(new TppMessage(PSU_CREDENTIALS_INVALID))
                                                                           .build();
        ErrorHolder errorHolder = ErrorHolder.builder(AIS_403)
                                      .tppMessages(of(CONSENT_UNKNOWN_403))
                                      .build();

        ResponseObject<UpdateConsentPsuDataResponse> expectedResult = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                          .fail(errorHolder)
                                                                          .build();
        SpiConfirmationCode spiConfirmationCode = new SpiConfirmationCode(request.getConfirmationCode());
        SpiContextData contextData = getSpiContextData();
        AccountConsent consent = createConsent();
        AisConsentAuthorizationResponse aisConsentAuthorizationResponse = getConsentAuthorisationResponse();

        when(aisConsentAuthorisationServiceEncrypted.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(CmsResponse.<AisConsentAuthorizationResponse>builder()
                            .payload(aisConsentAuthorizationResponse)
                            .build());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(consent));
        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(false);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData))
            .thenReturn(contextData);
        when(aisConsentMapper.mapToSpiAccountConsent(consent))
            .thenReturn(spiAccountConsent);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(aspspConsentDataProvider);
        when(aisConsentSpi.checkConfirmationCode(contextData, spiConfirmationCode, spiAccountConsent, aspspConsentDataProvider))
            .thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS)).thenReturn(errorHolder);

        // when
        ResponseObject<UpdateConsentPsuDataResponse> actualResult = aisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualToComparingFieldByField(expectedResult);
        verify(aisConsentService, times(0)).updateConsentAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FINALISED);
    }

    private UpdateConsentPsuDataReq buildUpdateConsentPsuDataReq() {
        UpdateConsentPsuDataReq request = jsonReader.getObjectFromFile("json/service/mapper/update-consent-psu-data-req-with-password.json",
                                                                       UpdateConsentPsuDataReq.class);
        request.setConsentId(CONSENT_ID);
        request.setAuthorizationId(AUTHORISATION_ID);
        request.setPsuData(buildPsuIdData());

        return request;
    }

    private SpiContextData getSpiContextData() {
        return new SpiContextData(null, null, null, null, null);
    }

    private AccountConsent createConsent() {
        AccountConsent accountConsent = jsonReader.getObjectFromFile("json/service/account-consent.json", AccountConsent.class);

        return accountConsent;
    }

    private AisConsentAuthorizationResponse getConsentAuthorisationResponse() {
        AisConsentAuthorizationResponse authorizationResponse = new AisConsentAuthorizationResponse();
        authorizationResponse.setAuthorizationId(AUTHORISATION_ID);
        authorizationResponse.setConsentId(CONSENT_ID);
        authorizationResponse.setPsuIdData(buildPsuIdData());
        authorizationResponse.setScaStatus(ScaStatus.UNCONFIRMED);

        return authorizationResponse;
    }

    private PsuIdData buildPsuIdData() {
        return new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType","psuIpAddress");
    }
}

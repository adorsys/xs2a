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

package de.adorsys.psd2.xs2a.service.authorization.ais;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorisationsParameters;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiCheckConfirmationCodeRequest;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentConfirmationCodeValidationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import de.adorsys.psd2.xs2a.util.reader.TestSpiDataProvider;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.adorsys.psd2.consent.api.CmsError.TECHNICAL_ERROR;
import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AisAuthorisationConfirmationServiceTest {
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";
    private static final String CONFIRMATION_CODE = "12345";
    private static final String SCA_AUTHENTICATION_DATA = "54321";
    private final static JsonReader jsonReader = new JsonReader();

    @InjectMocks
    private AisAuthorisationConfirmationService aisAuthorisationConfirmationService;

    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    @Mock
    private Xs2aAuthorisationService authorisationService;
    @Mock
    private Xs2aAisConsentService aisConsentService;
    @Mock
    private AisConsentSpi aisConsentSpi;
    @Mock
    private Xs2aAisConsentMapper aisConsentMapper;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private AuthorisationServiceEncrypted authorisationServiceEncrypted;
    @Mock
    private SpiAccountConsent spiAccountConsent;
    @Mock
    private SpiAspspConsentDataProvider aspspConsentDataProvider;

    @Test
    void processAuthorisationConfirmation_success_checkOnSpi() {
        // given
        PsuIdData psuIdData = buildPsuIdData();
        ConsentAuthorisationsParameters request = buildUpdateConsentPsuDataReq();
        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse(ScaStatus.FINALISED, CONSENT_ID, AUTHORISATION_ID, psuIdData);
        ResponseObject<UpdateConsentPsuDataResponse> expectedResult = ResponseObject.<UpdateConsentPsuDataResponse>builder().body(response).build();

        SpiCheckConfirmationCodeRequest spiCheckConfirmationCodeRequest = new SpiCheckConfirmationCodeRequest(request.getConfirmationCode(), AUTHORISATION_ID);
        SpiContextData contextData = getSpiContextData();
        AisConsent consent = createConsent();
        Authorisation aisConsentAuthorizationResponse = getConsentAuthorisationResponse();

        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder()
                            .payload(aisConsentAuthorizationResponse)
                            .build());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(consent));
        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(false);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData))
            .thenReturn(contextData);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(aspspConsentDataProvider);
        when(aisConsentSpi.checkConfirmationCode(contextData, spiCheckConfirmationCodeRequest, aspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiConsentConfirmationCodeValidationResponse>builder()
                            .payload(new SpiConsentConfirmationCodeValidationResponse(ScaStatus.FINALISED, ConsentStatus.VALID))
                            .build());

        // when
        ResponseObject<UpdateConsentPsuDataResponse> actualResult = aisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualToComparingFieldByField(expectedResult);
        verify(authorisationService, times(1)).updateAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FINALISED);
    }

    @Test
    void processAuthorisationConfirmation_checkOnXs2a_success() {
        // given
        ConsentAuthorisationsParameters request = buildUpdateConsentPsuDataReq();
        request.setConfirmationCode(CONFIRMATION_CODE);
        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse(ScaStatus.FINALISED, CONSENT_ID, AUTHORISATION_ID, buildPsuIdData());
        ResponseObject<UpdateConsentPsuDataResponse> expectedResult = ResponseObject.<UpdateConsentPsuDataResponse>builder().body(response).build();
        Authorisation aisConsentAuthorizationResponse = getConsentAuthorisationResponse();
        aisConsentAuthorizationResponse.setScaAuthenticationData(SCA_AUTHENTICATION_DATA);

        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(true);
        when(aisConsentSpi.checkConfirmationCodeInternally(AUTHORISATION_ID, CONFIRMATION_CODE, SCA_AUTHENTICATION_DATA, aspspConsentDataProvider)).thenReturn(true);
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder()
                            .payload(aisConsentAuthorizationResponse)
                            .build());
        SpiContextData contextData = getSpiContextData();
        SpiConsentConfirmationCodeValidationResponse spiConsentConfirmationCodeValidationResponse = preparationsForNotifyConfirmationCodeValidation(true);
        SpiResponse<SpiConsentConfirmationCodeValidationResponse> spiResponse = SpiResponse.<SpiConsentConfirmationCodeValidationResponse>builder().payload(spiConsentConfirmationCodeValidationResponse).build();
        when(aisConsentSpi.notifyConfirmationCodeValidation(contextData, true, spiAccountConsent, aspspConsentDataProvider)).thenReturn(spiResponse);

        // when
        ResponseObject<UpdateConsentPsuDataResponse> actualResult = aisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualToComparingFieldByField(expectedResult);
        verify(authorisationService, times(1)).updateAuthorisationStatus(AUTHORISATION_ID, spiConsentConfirmationCodeValidationResponse.getScaStatus());
        verify(aisConsentService, times(1)).updateConsentStatus(CONSENT_ID, spiConsentConfirmationCodeValidationResponse.getConsentStatus());
        verify(aisConsentSpi, times(1)).checkConfirmationCodeInternally(AUTHORISATION_ID, CONFIRMATION_CODE, SCA_AUTHENTICATION_DATA, aspspConsentDataProvider);
    }

    @Test
    void processAuthorisationConfirmation_failed_NoAuthorisation() {
        // given
        ConsentAuthorisationsParameters request = buildUpdateConsentPsuDataReq();
        ResponseObject<UpdateConsentPsuDataResponse> expectedResult = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                          .fail(ErrorType.AIS_403, of(CONSENT_UNKNOWN_403)).build();

        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder()
                            .error(TECHNICAL_ERROR)
                            .build());
        // when
        ResponseObject<UpdateConsentPsuDataResponse> actualResult = aisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualToComparingFieldByField(expectedResult);
        verify(authorisationService, times(0)).updateAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FINALISED);
    }

    @Test
    void processAuthorisationConfirmation_failed_WrongScaStatus() {
        // given
        ConsentAuthorisationsParameters request = buildUpdateConsentPsuDataReq();
        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.AIS_400)
                                      .tppMessages(of(SCA_INVALID))
                                      .build();

        ResponseObject<UpdateConsentPsuDataResponse> expectedResult = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                          .fail(errorHolder)
                                                                          .build();
        Authorisation aisConsentAuthorizationResponse = getConsentAuthorisationResponse();
        aisConsentAuthorizationResponse.setScaStatus(ScaStatus.FINALISED);

        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder()
                            .payload(aisConsentAuthorizationResponse)
                            .build());
        // when
        ResponseObject<UpdateConsentPsuDataResponse> actualResult = aisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualToComparingFieldByField(expectedResult);
        verify(authorisationService, times(0)).updateAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FINALISED);
    }

    @Test
    void processAuthorisationConfirmation_checkOnXs2a_wrongCode() {
        // given
        ConsentAuthorisationsParameters request = buildUpdateConsentPsuDataReq();
        request.setConfirmationCode("wrong_code");

        Authorisation aisConsentAuthorizationResponse = getConsentAuthorisationResponse();
        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.AIS_400)
                                      .tppMessages(of(SCA_INVALID))
                                      .build();
        ResponseObject<UpdateConsentPsuDataResponse> expectedResult = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                          .fail(errorHolder)
                                                                          .build();

        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(true);
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder()
                            .payload(aisConsentAuthorizationResponse)
                            .build());
        SpiContextData contextData = getSpiContextData();
        SpiConsentConfirmationCodeValidationResponse spiConsentConfirmationCodeValidationResponse = preparationsForNotifyConfirmationCodeValidation(false);
        SpiResponse<SpiConsentConfirmationCodeValidationResponse> spiResponse = SpiResponse.<SpiConsentConfirmationCodeValidationResponse>builder().payload(spiConsentConfirmationCodeValidationResponse).build();
        when(aisConsentSpi.notifyConfirmationCodeValidation(contextData, false, spiAccountConsent, aspspConsentDataProvider)).thenReturn(spiResponse);

        // when
        ResponseObject<UpdateConsentPsuDataResponse> actualResult = aisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualToComparingFieldByField(expectedResult);
        verify(authorisationService, times(1)).updateAuthorisationStatus(AUTHORISATION_ID, spiConsentConfirmationCodeValidationResponse.getScaStatus());
        verify(aisConsentService, times(1)).updateConsentStatus(CONSENT_ID, spiConsentConfirmationCodeValidationResponse.getConsentStatus());
    }

    @Test
    void processAuthorisationConfirmation__checkOnSpi_consentNotFound() {
        // given
        ConsentAuthorisationsParameters request = buildUpdateConsentPsuDataReq();
        Authorisation aisConsentAuthorizationResponse = getConsentAuthorisationResponse();
        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.AIS_403)
                                      .tppMessages(of(CONSENT_UNKNOWN_403))
                                      .build();
        ResponseObject<UpdateConsentPsuDataResponse> expectedResult = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                          .fail(errorHolder)
                                                                          .build();
        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(false);
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder()
                            .payload(aisConsentAuthorizationResponse)
                            .build());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.empty());

        // when
        ResponseObject<UpdateConsentPsuDataResponse> actualResult = aisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualToComparingFieldByField(expectedResult);
        verify(authorisationService, never()).updateAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FINALISED);
    }

    @Test
    void processAuthorisationConfirmation_checkOnXs2a_consentNotFound() {
        // given
        ConsentAuthorisationsParameters request = buildUpdateConsentPsuDataReq();
        Authorisation aisConsentAuthorizationResponse = getConsentAuthorisationResponse();
        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.AIS_403)
                                      .tppMessages(of(CONSENT_UNKNOWN_403))
                                      .build();
        ResponseObject<UpdateConsentPsuDataResponse> expectedResult = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                          .fail(errorHolder)
                                                                          .build();

        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(true);
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder()
                            .payload(aisConsentAuthorizationResponse)
                            .build());
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.empty());

        // when
        ResponseObject<UpdateConsentPsuDataResponse> actualResult = aisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualToComparingFieldByField(expectedResult);
        verify(authorisationService, never()).updateAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FINALISED);
    }

    @Test
    void processAuthorisationConfirmation_checkOnXs2a_spiError() {
        // given
        ConsentAuthorisationsParameters request = buildUpdateConsentPsuDataReq();
        request.setConfirmationCode("wrong_code");

        Authorisation aisConsentAuthorizationResponse = getConsentAuthorisationResponse();
        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.AIS_400)
                                      .tppMessages(of(SCA_INVALID))
                                      .build();
        ResponseObject<UpdateConsentPsuDataResponse> expectedResult = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                          .fail(errorHolder)
                                                                          .build();

        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(true);
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder()
                            .payload(aisConsentAuthorizationResponse)
                            .build());
        AisConsent consent = createConsent();
        SpiContextData contextData = getSpiContextData();
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(consent));
        when(spiContextDataProvider.provideWithPsuIdData(buildPsuIdData())).thenReturn(contextData);
        when(aisConsentMapper.mapToSpiAccountConsent(consent)).thenReturn(spiAccountConsent);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID)).thenReturn(aspspConsentDataProvider);
        SpiResponse<SpiConsentConfirmationCodeValidationResponse> spiResponse = SpiResponse.<SpiConsentConfirmationCodeValidationResponse>builder().error(new TppMessage(SCA_INVALID)).build();
        when(aisConsentSpi.notifyConfirmationCodeValidation(contextData, false, spiAccountConsent, aspspConsentDataProvider)).thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS)).thenReturn(errorHolder);

        // when
        ResponseObject<UpdateConsentPsuDataResponse> actualResult = aisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualToComparingFieldByField(expectedResult);
        verify(authorisationService, never()).updateAuthorisationStatus(any(), any());
        verify(aisConsentService, never()).updateConsentStatus(any(), any());
    }

    @Test
    void processAuthorisationConfirmation_checkOnSpi_spiError() {
        // given
        PsuIdData psuIdData = buildPsuIdData();
        ConsentAuthorisationsParameters request = buildUpdateConsentPsuDataReq();
        SpiResponse<SpiConsentConfirmationCodeValidationResponse> spiResponse = SpiResponse.<SpiConsentConfirmationCodeValidationResponse>builder()
                                                                                    .error(new TppMessage(PSU_CREDENTIALS_INVALID))
                                                                                    .build();
        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.AIS_403)
                                      .tppMessages(of(CONSENT_UNKNOWN_403))
                                      .build();

        ResponseObject<UpdateConsentPsuDataResponse> expectedResult = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                          .fail(errorHolder)
                                                                          .build();
        SpiCheckConfirmationCodeRequest spiCheckConfirmationCodeRequest = new SpiCheckConfirmationCodeRequest(request.getConfirmationCode(), AUTHORISATION_ID);
        SpiContextData contextData = getSpiContextData();
        AisConsent consent = createConsent();
        Authorisation aisConsentAuthorizationResponse = getConsentAuthorisationResponse();

        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder()
                            .payload(aisConsentAuthorizationResponse)
                            .build());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(consent));
        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(false);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData))
            .thenReturn(contextData);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(aspspConsentDataProvider);
        when(aisConsentSpi.checkConfirmationCode(contextData, spiCheckConfirmationCodeRequest, aspspConsentDataProvider))
            .thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS)).thenReturn(errorHolder);

        // when
        ResponseObject<UpdateConsentPsuDataResponse> actualResult = aisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualToComparingFieldByField(expectedResult);
        verify(authorisationService, times(0)).updateAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FINALISED);
    }

    @Test
    void checkConfirmationCodeInternally() {
        aisAuthorisationConfirmationService.checkConfirmationCodeInternally(AUTHORISATION_ID, CONFIRMATION_CODE, SCA_AUTHENTICATION_DATA, aspspConsentDataProvider);
        verify(aisConsentSpi, times(1)).checkConfirmationCodeInternally(AUTHORISATION_ID, CONFIRMATION_CODE, SCA_AUTHENTICATION_DATA, aspspConsentDataProvider);
    }

    private ConsentAuthorisationsParameters buildUpdateConsentPsuDataReq() {
        ConsentAuthorisationsParameters request = jsonReader.getObjectFromFile("json/service/mapper/update-consent-psu-data-req-with-password.json",
                                                                               ConsentAuthorisationsParameters.class);
        request.setConsentId(CONSENT_ID);
        request.setAuthorizationId(AUTHORISATION_ID);
        request.setPsuData(buildPsuIdData());

        return request;
    }

    private SpiContextData getSpiContextData() {
        return TestSpiDataProvider.defaultSpiContextData();
    }

    private AisConsent createConsent() {
        return jsonReader.getObjectFromFile("json/service/ais-consent.json", AisConsent.class);
    }

    private Authorisation getConsentAuthorisationResponse() {
        Authorisation authorizationResponse = new Authorisation();
        authorizationResponse.setAuthorisationId(AUTHORISATION_ID);
        authorizationResponse.setParentId(CONSENT_ID);
        authorizationResponse.setPsuIdData(buildPsuIdData());
        authorizationResponse.setScaStatus(ScaStatus.UNCONFIRMED);

        return authorizationResponse;
    }

    private PsuIdData buildPsuIdData() {
        return new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    }

    private SpiConsentConfirmationCodeValidationResponse preparationsForNotifyConfirmationCodeValidation(boolean confirmationCodeValidationResult) {
        AisConsent consent = createConsent();
        SpiContextData contextData = getSpiContextData();

        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(consent));
        when(spiContextDataProvider.provideWithPsuIdData(buildPsuIdData())).thenReturn(contextData);
        when(aisConsentMapper.mapToSpiAccountConsent(consent)).thenReturn(spiAccountConsent);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID)).thenReturn(aspspConsentDataProvider);

        return confirmationCodeValidationResult
                   ? new SpiConsentConfirmationCodeValidationResponse(ScaStatus.FINALISED, ConsentStatus.VALID)
                   : new SpiConsentConfirmationCodeValidationResponse(ScaStatus.FAILED, ConsentStatus.REJECTED);
    }
}

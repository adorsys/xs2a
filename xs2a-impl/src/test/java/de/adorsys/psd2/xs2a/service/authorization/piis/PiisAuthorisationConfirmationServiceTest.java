/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.authorization.piis;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorisationsParameters;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAuthorizationMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPiisConsentMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiCheckConfirmationCodeRequest;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentConfirmationCodeValidationResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.psd2.xs2a.spi.domain.error.SpiMessageErrorCode;
import de.adorsys.psd2.xs2a.spi.domain.error.SpiTppMessage;
import de.adorsys.psd2.xs2a.spi.domain.piis.SpiPiisConsent;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.sca.SpiScaStatus;
import de.adorsys.psd2.xs2a.spi.service.PiisConsentSpi;
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
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_403;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.SCA_INVALID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PiisAuthorisationConfirmationServiceTest {
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";
    private static final String CONFIRMATION_CODE = "12345";
    private static final String SCA_AUTHENTICATION_DATA = "54321";
    private final static JsonReader jsonReader = new JsonReader();

    @InjectMocks
    private PiisAuthorisationConfirmationService piisAuthorisationConfirmationService;

    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    @Mock
    private Xs2aAuthorisationService authorisationService;
    @Mock
    private Xs2aPiisConsentService piisConsentService;
    @Mock
    private PiisConsentSpi piisConsentSpi;
    @Mock
    private Xs2aToSpiPiisConsentMapper piisConsentMapper;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private AuthorisationServiceEncrypted authorisationServiceEncrypted;
    @Mock
    private SpiPiisConsent spiPiisConsent;
    @Mock
    private SpiAspspConsentDataProvider aspspConsentDataProvider;
    @Mock
    private SpiToXs2aConsentMapper spiToXs2aConsentMapper;
    @Mock
    private SpiToXs2aAuthorizationMapper spiToXs2aAuthorizationMapper;

    @Test
    void processAuthorisationConfirmation_success_checkOnSpi() {
        // given
        PsuIdData psuIdData = buildPsuIdData();
        ConsentAuthorisationsParameters request = buildUpdateConsentPsuDataReq();
        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse(ScaStatus.FINALISED, CONSENT_ID, AUTHORISATION_ID, psuIdData);
        ResponseObject<UpdateConsentPsuDataResponse> expectedResult = ResponseObject.<UpdateConsentPsuDataResponse>builder().body(response).build();

        SpiCheckConfirmationCodeRequest spiCheckConfirmationCodeRequest = new SpiCheckConfirmationCodeRequest(request.getConfirmationCode(), AUTHORISATION_ID);
        SpiContextData contextData = getSpiContextData();
        PiisConsent consent = createConsent();
        Authorisation authorisationResponse = getConsentAuthorisationResponse();

        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder()
                            .payload(authorisationResponse)
                            .build());
        when(piisConsentService.getPiisConsentById(CONSENT_ID))
            .thenReturn(Optional.of(consent));
        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(false);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData))
            .thenReturn(contextData);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(aspspConsentDataProvider);
        when(piisConsentSpi.checkConfirmationCode(contextData, spiCheckConfirmationCodeRequest, aspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiConsentConfirmationCodeValidationResponse>builder()
                            .payload(new SpiConsentConfirmationCodeValidationResponse(SpiScaStatus.FINALISED, SpiConsentStatus.VALID))
                            .build());
        when(spiToXs2aAuthorizationMapper.mapToScaStatus(SpiScaStatus.FINALISED)).thenReturn(ScaStatus.FINALISED);

        // when
        ResponseObject<UpdateConsentPsuDataResponse> actualResult = piisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

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
        Authorisation authorisationResponse = getConsentAuthorisationResponse();

        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(true);
        when(piisConsentSpi.checkConfirmationCodeInternally(AUTHORISATION_ID, CONFIRMATION_CODE, SCA_AUTHENTICATION_DATA, aspspConsentDataProvider)).thenReturn(true);
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder()
                            .payload(authorisationResponse)
                            .build());
        SpiContextData contextData = getSpiContextData();
        SpiConsentConfirmationCodeValidationResponse spiConsentConfirmationCodeValidationResponse = preparationsForNotifyConfirmationCodeValidation(true);
        SpiResponse<SpiConsentConfirmationCodeValidationResponse> spiResponse = SpiResponse.<SpiConsentConfirmationCodeValidationResponse>builder().payload(spiConsentConfirmationCodeValidationResponse).build();
        when(piisConsentSpi.notifyConfirmationCodeValidation(contextData, true, spiPiisConsent, aspspConsentDataProvider)).thenReturn(spiResponse);
        when(spiToXs2aAuthorizationMapper.mapToScaStatus(SpiScaStatus.FINALISED)).thenReturn(ScaStatus.FINALISED);
        when(spiToXs2aConsentMapper.mapToConsentStatus(SpiConsentStatus.VALID)).thenReturn(ConsentStatus.VALID);

        // when
        ResponseObject<UpdateConsentPsuDataResponse> actualResult = piisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualToComparingFieldByField(expectedResult);
        verify(authorisationService, times(1)).updateAuthorisationStatus(AUTHORISATION_ID, ScaStatus.valueOf(spiConsentConfirmationCodeValidationResponse.getScaStatus().name()));
        verify(piisConsentService, times(1)).updateConsentStatus(CONSENT_ID, ConsentStatus.valueOf(spiConsentConfirmationCodeValidationResponse.getConsentStatus().name()));
        verify(piisConsentSpi, times(1)).checkConfirmationCodeInternally(AUTHORISATION_ID, CONFIRMATION_CODE, SCA_AUTHENTICATION_DATA, aspspConsentDataProvider);
    }

    @Test
    void processAuthorisationConfirmation_failed_NoAuthorisation() {
        // given
        ConsentAuthorisationsParameters request = buildUpdateConsentPsuDataReq();
        ResponseObject<UpdateConsentPsuDataResponse> expectedResult = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                          .fail(ErrorType.PIIS_403, of(CONSENT_UNKNOWN_403)).build();

        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder()
                            .error(TECHNICAL_ERROR)
                            .build());
        // when
        ResponseObject<UpdateConsentPsuDataResponse> actualResult = piisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualToComparingFieldByField(expectedResult);
        verify(authorisationService, times(0)).updateAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FINALISED);
    }

    @Test
    void processAuthorisationConfirmation_failed_WrongScaStatus() {
        // given
        ConsentAuthorisationsParameters request = buildUpdateConsentPsuDataReq();
        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIIS_400)
                                      .tppMessages(of(SCA_INVALID))
                                      .build();

        ResponseObject<UpdateConsentPsuDataResponse> expectedResult = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                          .fail(errorHolder)
                                                                          .build();
        Authorisation authorisationResponse = getConsentAuthorisationResponse();
        authorisationResponse.setScaStatus(ScaStatus.FINALISED);

        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder()
                            .payload(authorisationResponse)
                            .build());
        // when
        ResponseObject<UpdateConsentPsuDataResponse> actualResult = piisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualToComparingFieldByField(expectedResult);
        verify(authorisationService, times(0)).updateAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FINALISED);
    }

    @Test
    void processAuthorisationConfirmation_checkOnXs2a_wrongCode() {
        // given
        ConsentAuthorisationsParameters request = buildUpdateConsentPsuDataReq();
        request.setConfirmationCode("wrong_code");

        Authorisation authorisationResponse = getConsentAuthorisationResponse();
        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIIS_400)
                                      .tppMessages(of(SCA_INVALID))
                                      .build();
        ResponseObject<UpdateConsentPsuDataResponse> expectedResult = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                          .fail(errorHolder)
                                                                          .build();

        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(true);
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder()
                            .payload(authorisationResponse)
                            .build());
        when(spiToXs2aAuthorizationMapper.mapToScaStatus(SpiScaStatus.FAILED)).thenReturn(ScaStatus.FAILED);
        when(spiToXs2aConsentMapper.mapToConsentStatus(SpiConsentStatus.REJECTED)).thenReturn(ConsentStatus.REJECTED);
        SpiContextData contextData = getSpiContextData();
        SpiConsentConfirmationCodeValidationResponse spiConsentConfirmationCodeValidationResponse = preparationsForNotifyConfirmationCodeValidation(false);
        SpiResponse<SpiConsentConfirmationCodeValidationResponse> spiResponse = SpiResponse.<SpiConsentConfirmationCodeValidationResponse>builder().payload(spiConsentConfirmationCodeValidationResponse).build();
        when(piisConsentSpi.notifyConfirmationCodeValidation(contextData, false, spiPiisConsent, aspspConsentDataProvider)).thenReturn(spiResponse);

        // when
        ResponseObject<UpdateConsentPsuDataResponse> actualResult = piisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualToComparingFieldByField(expectedResult);
        verify(authorisationService, times(1)).updateAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FAILED);
        verify(piisConsentService, times(1)).updateConsentStatus(CONSENT_ID, ConsentStatus.REJECTED);
    }

    @Test
    void processAuthorisationConfirmation__checkOnSpi_consentNotFound() {
        // given
        ConsentAuthorisationsParameters request = buildUpdateConsentPsuDataReq();
        Authorisation authorisationResponse = getConsentAuthorisationResponse();
        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIIS_403)
                                      .tppMessages(of(CONSENT_UNKNOWN_403))
                                      .build();
        ResponseObject<UpdateConsentPsuDataResponse> expectedResult = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                          .fail(errorHolder)
                                                                          .build();
        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(false);
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder()
                            .payload(authorisationResponse)
                            .build());
        when(piisConsentService.getPiisConsentById(CONSENT_ID))
            .thenReturn(Optional.empty());

        // when
        ResponseObject<UpdateConsentPsuDataResponse> actualResult = piisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualToComparingFieldByField(expectedResult);
        verify(authorisationService, never()).updateAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FINALISED);
    }

    @Test
    void processAuthorisationConfirmation_checkOnXs2a_consentNotFound() {
        // given
        ConsentAuthorisationsParameters request = buildUpdateConsentPsuDataReq();
        Authorisation authorisationResponse = getConsentAuthorisationResponse();
        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIIS_403)
                                      .tppMessages(of(CONSENT_UNKNOWN_403))
                                      .build();
        ResponseObject<UpdateConsentPsuDataResponse> expectedResult = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                          .fail(errorHolder)
                                                                          .build();

        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(true);
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder()
                            .payload(authorisationResponse)
                            .build());
        when(piisConsentService.getPiisConsentById(CONSENT_ID)).thenReturn(Optional.empty());

        // when
        ResponseObject<UpdateConsentPsuDataResponse> actualResult = piisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualToComparingFieldByField(expectedResult);
        verify(authorisationService, never()).updateAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FINALISED);
    }

    @Test
    void processAuthorisationConfirmation_checkOnXs2a_spiError() {
        // given
        ConsentAuthorisationsParameters request = buildUpdateConsentPsuDataReq();
        request.setConfirmationCode("wrong_code");

        Authorisation authorisationResponse = getConsentAuthorisationResponse();
        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIIS_400)
                                      .tppMessages(of(SCA_INVALID))
                                      .build();
        ResponseObject<UpdateConsentPsuDataResponse> expectedResult = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                          .fail(errorHolder)
                                                                          .build();

        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(true);
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder()
                            .payload(authorisationResponse)
                            .build());
        PiisConsent consent = createConsent();
        SpiContextData contextData = getSpiContextData();
        when(piisConsentService.getPiisConsentById(CONSENT_ID)).thenReturn(Optional.of(consent));
        when(spiContextDataProvider.provideWithPsuIdData(buildPsuIdData())).thenReturn(contextData);
        when(piisConsentMapper.mapToSpiPiisConsent(consent)).thenReturn(spiPiisConsent);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID)).thenReturn(aspspConsentDataProvider);
        SpiResponse<SpiConsentConfirmationCodeValidationResponse> spiResponse = SpiResponse.<SpiConsentConfirmationCodeValidationResponse>builder().error(new SpiTppMessage(SpiMessageErrorCode.SCA_INVALID)).build();
        when(piisConsentSpi.notifyConfirmationCodeValidation(contextData, false, spiPiisConsent, aspspConsentDataProvider)).thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIIS)).thenReturn(errorHolder);

        // when
        ResponseObject<UpdateConsentPsuDataResponse> actualResult = piisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualToComparingFieldByField(expectedResult);
        verify(authorisationService, never()).updateAuthorisationStatus(any(), any());
        verify(piisConsentService, never()).updateConsentStatus(any(), any());
    }

    @Test
    void processAuthorisationConfirmation_checkOnSpi_spiError() {
        // given
        PsuIdData psuIdData = buildPsuIdData();
        ConsentAuthorisationsParameters request = buildUpdateConsentPsuDataReq();
        SpiResponse<SpiConsentConfirmationCodeValidationResponse> spiResponse = SpiResponse.<SpiConsentConfirmationCodeValidationResponse>builder()
                                                                                    .error(new SpiTppMessage(SpiMessageErrorCode.PSU_CREDENTIALS_INVALID))
                                                                                    .build();
        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIIS_403)
                                      .tppMessages(of(CONSENT_UNKNOWN_403))
                                      .build();

        ResponseObject<UpdateConsentPsuDataResponse> expectedResult = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                          .fail(errorHolder)
                                                                          .build();
        SpiCheckConfirmationCodeRequest spiCheckConfirmationCodeRequest = new SpiCheckConfirmationCodeRequest(request.getConfirmationCode(), AUTHORISATION_ID);
        SpiContextData contextData = getSpiContextData();
        PiisConsent consent = createConsent();
        Authorisation authorisationResponse = getConsentAuthorisationResponse();

        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder()
                            .payload(authorisationResponse)
                            .build());
        when(piisConsentService.getPiisConsentById(CONSENT_ID))
            .thenReturn(Optional.of(consent));
        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(false);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData))
            .thenReturn(contextData);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(aspspConsentDataProvider);
        when(piisConsentSpi.checkConfirmationCode(contextData, spiCheckConfirmationCodeRequest, aspspConsentDataProvider))
            .thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIIS)).thenReturn(errorHolder);

        // when
        ResponseObject<UpdateConsentPsuDataResponse> actualResult = piisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualToComparingFieldByField(expectedResult);
        verify(authorisationService, times(0)).updateAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FINALISED);
    }

    @Test
    void checkConfirmationCodeInternally() {
        piisAuthorisationConfirmationService.checkConfirmationCodeInternally(AUTHORISATION_ID, CONFIRMATION_CODE, SCA_AUTHENTICATION_DATA, aspspConsentDataProvider);
        verify(piisConsentSpi, times(1)).checkConfirmationCodeInternally(AUTHORISATION_ID, CONFIRMATION_CODE, SCA_AUTHENTICATION_DATA, aspspConsentDataProvider);
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

    private PiisConsent createConsent() {
        return jsonReader.getObjectFromFile("json/piis/piis-consent.json", PiisConsent.class);
    }

    private Authorisation getConsentAuthorisationResponse() {
        Authorisation authorizationResponse = new Authorisation();
        authorizationResponse.setAuthorisationId(AUTHORISATION_ID);
        authorizationResponse.setParentId(CONSENT_ID);
        authorizationResponse.setPsuIdData(buildPsuIdData());
        authorizationResponse.setScaStatus(ScaStatus.UNCONFIRMED);
        authorizationResponse.setScaAuthenticationData(SCA_AUTHENTICATION_DATA);

        return authorizationResponse;
    }

    private PsuIdData buildPsuIdData() {
        return new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    }

    private SpiConsentConfirmationCodeValidationResponse preparationsForNotifyConfirmationCodeValidation(boolean confirmationCodeValidationResult) {
        PiisConsent consent = createConsent();
        SpiContextData contextData = getSpiContextData();

        when(piisConsentService.getPiisConsentById(CONSENT_ID)).thenReturn(Optional.of(consent));
        when(spiContextDataProvider.provideWithPsuIdData(buildPsuIdData())).thenReturn(contextData);
        when(piisConsentMapper.mapToSpiPiisConsent(consent)).thenReturn(spiPiisConsent);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID)).thenReturn(aspspConsentDataProvider);

        return confirmationCodeValidationResult
                   ? new SpiConsentConfirmationCodeValidationResponse(SpiScaStatus.FINALISED, SpiConsentStatus.VALID)
                   : new SpiConsentConfirmationCodeValidationResponse(SpiScaStatus.FAILED, SpiConsentStatus.REJECTED);
    }
}

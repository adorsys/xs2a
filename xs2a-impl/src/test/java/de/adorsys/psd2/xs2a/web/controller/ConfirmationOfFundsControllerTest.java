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

package de.adorsys.psd2.xs2a.web.controller;

import de.adorsys.psd2.core.data.piis.PiisConsentData;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.profile.PiisConsentSupported;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.AdditionalPsuIdData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.fund.CreatePiisConsentRequest;
import de.adorsys.psd2.xs2a.service.PiisConsentService;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ResponseErrorMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.header.ConsentHeadersBuilder;
import de.adorsys.psd2.xs2a.web.header.ResponseHeaders;
import de.adorsys.psd2.xs2a.web.mapper.AuthorisationMapper;
import de.adorsys.psd2.xs2a.web.mapper.ConsentModelMapper;
import de.adorsys.psd2.xs2a.web.mapper.PiisConsentModelMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.util.StringUtils.isEmpty;

@ExtendWith(MockitoExtension.class)
class ConfirmationOfFundsControllerTest {
    private static final String CONSENT_ID = "XXXX-YYYY-XXXX-YYYY";
    private static final String AUTHORISATION_ID = "93c4a8ad-74c8-4ae7-87a8-0b495811eb1a";
    private static final String WRONG_CONSENT_ID = "YYYY-YYYY-YYYY-YYYY";
    private static final String PSU_MESSAGE_RESPONSE = "test psu message";
    private static final String CORRECT_PSU_ID = "ID 777";
    private static final String PASSWORD = "password";
    private static final Map BODY = Collections.singletonMap("psuData", Collections.singletonMap("password", PASSWORD));
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(CORRECT_PSU_ID, null, null, null, null, buildEmptyAdditionalPsuIdData());
    private static final ResponseHeaders RESPONSE_HEADERS = ResponseHeaders.builder().aspspScaApproach(ScaApproach.REDIRECT).build();
    private static final MessageError MESSAGE_ERROR_PIIS_400 = new MessageError(ErrorType.PIIS_400, of(MessageErrorCode.CONSENT_UNKNOWN_400));
    private static final MessageError MESSAGE_ERROR_PIIS_404 = new MessageError(ErrorType.PIIS_404, of(MessageErrorCode.RESOURCE_UNKNOWN_404));
    private static final MessageError MESSAGE_ERROR_PIIS_403 = new MessageError(ErrorType.PIIS_403, of(MessageErrorCode.CONSENT_UNKNOWN_403));


    @InjectMocks
    private ConfirmationOfFundsController confirmationOfFundsController;
    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;
    @Mock
    private ResponseErrorMapper responseErrorMapper;
    @Mock
    private PiisConsentService piisConsentService;
    @Mock
    private ConsentHeadersBuilder consentHeadersBuilder;
    @Mock
    private ResponseMapper responseMapper;
    @Mock
    private PiisConsentModelMapper piisConsentModelMapper;
    @Mock
    private AuthorisationMapper authorisationMapper;
    @Mock
    private ConsentModelMapper consentModelMapper;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void createConsentConfirmationOfFunds_piisConsentFromTppNotSupported() {
        //Given
        when(aspspProfileServiceWrapper.getPiisConsentSupported())
            .thenReturn(PiisConsentSupported.ASPSP_CONSENT_SUPPORTED);
        HttpStatus httpStatus = HttpStatus.METHOD_NOT_ALLOWED;
        when(responseErrorMapper.generateErrorResponse(any(MessageError.class)))
            .thenReturn(ResponseEntity.status(httpStatus).build());

        //When
        ResponseEntity responseEntity = confirmationOfFundsController.createConsentConfirmationOfFunds(null, null, null, null, null,
                                                                                                       null, null, null, null, null,
                                                                                                       null, null, null, null, null,
                                                                                                       null, null, null, null, null,
                                                                                                       null, null, null);
        //Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(httpStatus);
    }

    @Test
    void createConsentConfirmationOfFunds_failedOnCreation() {
        //Given
        when(aspspProfileServiceWrapper.getPiisConsentSupported())
            .thenReturn(PiisConsentSupported.TPP_CONSENT_SUPPORTED);
        when(piisConsentService.createPiisConsentWithResponse(any(), eq(PSU_ID_DATA), eq(false)))
            .thenReturn(ResponseObject.<Xs2aConfirmationOfFundsResponse>builder()
                            .fail(ErrorType.PIIS_400, of(MessageErrorCode.RESOURCE_UNKNOWN_400))
                            .build());
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        when(responseErrorMapper.generateErrorResponse(any(MessageError.class)))
            .thenReturn(ResponseEntity.status(httpStatus).build());

        //When
        ResponseEntity responseEntity = confirmationOfFundsController.createConsentConfirmationOfFunds(null, null, null, null, null,
                                                                                                       PSU_ID_DATA.getPsuId(), null, null, null, null,
                                                                                                       null, null, null, null, null,
                                                                                                       null, null, null, null, null,
                                                                                                       null, null, null);
        //Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(httpStatus);
    }

    @Test
    void createConsentConfirmationOfFunds_success() {
        //Given
        when(aspspProfileServiceWrapper.getPiisConsentSupported())
            .thenReturn(PiisConsentSupported.TPP_CONSENT_SUPPORTED);

        ConsentsConfirmationOfFunds consentsConfirmationOfFunds = new ConsentsConfirmationOfFunds();
        CreatePiisConsentRequest request = new CreatePiisConsentRequest(null, null, null, null, null);
        when(piisConsentModelMapper.toCreatePiisConsentRequest(consentsConfirmationOfFunds)).thenReturn(request);
        when(piisConsentService.createPiisConsentWithResponse(request, PSU_ID_DATA, false))
            .thenReturn(createConsentsConfirmationOfFunds());
        when(consentHeadersBuilder.buildCreateConsentHeaders(any(), any()))
            .thenReturn(RESPONSE_HEADERS);
        doReturn(new ResponseEntity<>(createConsentsConfirmationOfFundsResponse().getBody(), HttpStatus.CREATED))
            .when(responseMapper).created(any(), any(Function.class), eq(RESPONSE_HEADERS));

        //When
        ResponseEntity responseEntity = confirmationOfFundsController.createConsentConfirmationOfFunds(null, consentsConfirmationOfFunds, null, null, null,
                                                                                                       PSU_ID_DATA.getPsuId(), null, null, null, null,
                                                                                                       null, null, null, null, null,
                                                                                                       null, null, null, null, null,
                                                                                                       null, null, null);

        ConsentsConfirmationOfFundsResponse resp = (ConsentsConfirmationOfFundsResponse) responseEntity.getBody();
        //Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp).isNotNull();
        assertThat(resp.getConsentStatus()).hasToString(ConsentStatus.RECEIVED.getValue());
        assertThat(resp.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(resp.getPsuMessage()).isEqualTo(PSU_MESSAGE_RESPONSE);
    }

    @Test
    void getConsentConfirmationOfFunds_Failure() {
        //Given
        when(piisConsentService.getPiisConsentById(WRONG_CONSENT_ID))
            .thenReturn(getPiisConsent(WRONG_CONSENT_ID));
        when(responseErrorMapper.generateErrorResponse(MESSAGE_ERROR_PIIS_404))
            .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        //When
        ResponseEntity responseEntity = confirmationOfFundsController.getConsentConfirmationOfFunds(WRONG_CONSENT_ID, null,
                                                                                                    null, null, null, null, null, null,
                                                                                                    null, null, null, null, null,
                                                                                                    null, null);
        //Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getConsentConfirmationOfFunds_Success() {
        //Given
        when(piisConsentService.getPiisConsentById(CONSENT_ID))
            .thenReturn(getPiisConsent(CONSENT_ID));
        doReturn(new ResponseEntity<>(getConsentConfirmationOfFundsContentResponse().getBody(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());

        //When
        ResponseEntity responseEntity = confirmationOfFundsController.getConsentConfirmationOfFunds(CONSENT_ID, null,
                                                                                                    null, null, null, null, null, null,
                                                                                                    null, null, null, null, null,
                                                                                                    null, null);
        //Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isExactlyInstanceOf(ConsentConfirmationOfFundsContentResponse.class);
    }

    @Test
    void getConsentConfirmationOfFundsStatus_Failure() {
        //Given
        when(piisConsentService.getPiisConsentStatusById(WRONG_CONSENT_ID))
            .thenReturn(ResponseObject.<ConsentStatusResponse>builder()
                            .fail(MESSAGE_ERROR_PIIS_404)
                            .build());
        when(responseErrorMapper.generateErrorResponse(MESSAGE_ERROR_PIIS_404))
            .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        //When
        ResponseEntity responseEntity = confirmationOfFundsController.getConsentConfirmationOfFundsStatus(WRONG_CONSENT_ID, null,
                                                                                                          null, null, null, null, null, null,
                                                                                                          null, null, null, null, null,
                                                                                                          null, null);
        //Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getConsentConfirmationOfFundsStatus_Success() {
        //Given
        when(piisConsentService.getPiisConsentStatusById(CONSENT_ID))
            .thenReturn(ResponseObject.<ConsentStatusResponse>builder()
                            .body(new ConsentStatusResponse(ConsentStatus.RECEIVED, PSU_MESSAGE_RESPONSE))
                            .build());
        doReturn(new ResponseEntity<>(ConsentStatus.RECEIVED, HttpStatus.OK)).when(responseMapper).ok(any(), any());

        //When
        ResponseEntity responseEntity = confirmationOfFundsController.getConsentConfirmationOfFundsStatus(CONSENT_ID, null,
                                                                                                          null, null, null, null, null, null,
                                                                                                          null, null, null, null, null,
                                                                                                          null, null);
        //Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(ConsentStatus.RECEIVED);
    }

    @Test
    void deleteConsentConfirmationOfFunds_Success() {
        //Given
        when(piisConsentService.deleteAccountConsentsById(CONSENT_ID))
            .thenReturn(ResponseObject.<Void>builder().build());
        doReturn(new ResponseEntity<>(HttpStatus.OK))
            .when(responseMapper).delete(any());
        //When
        ResponseEntity responseEntity = confirmationOfFundsController.deleteConsentConfirmationOfFunds(CONSENT_ID, null, null, null, null,
                                                                                                       null, null, null, null,
                                                                                                       null, null, null, null, null, null);
        //Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deleteConsentConfirmationOfFunds_Failure() {
        //Given
        when(piisConsentService.deleteAccountConsentsById(CONSENT_ID))
            .thenReturn(ResponseObject.<Void>builder()
                            .fail(MESSAGE_ERROR_PIIS_403)
                            .build());
        when(responseErrorMapper.generateErrorResponse(MESSAGE_ERROR_PIIS_403))
            .thenReturn(new ResponseEntity<>(HttpStatus.FORBIDDEN));
        //When
        ResponseEntity responseEntity = confirmationOfFundsController.deleteConsentConfirmationOfFunds(CONSENT_ID, null, null, null, null,
                                                                                                       null, null, null, null,
                                                                                                       null, null, null, null, null, null);
        //Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getConsentAuthorisation_Success() {
        //Given
        Xs2aAuthorisationSubResources authorisation = new Xs2aAuthorisationSubResources(Collections.singletonList(AUTHORISATION_ID));
        when(piisConsentService.getConsentInitiationAuthorisations(CONSENT_ID))
            .thenReturn(ResponseObject.<Xs2aAuthorisationSubResources>builder()
                            .body(authorisation)
                            .build());
        doReturn(new ResponseEntity<>(authorisation, HttpStatus.OK)).when(responseMapper).ok(any(), any());

        //When
        ResponseEntity responseEntity = confirmationOfFundsController.getConsentAuthorisation(CONSENT_ID, null,
                                                                                              null, null, null, null, null, null,
                                                                                              null, null, null, null, null,
                                                                                              null, null);
        //Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(authorisation);
    }


    @Test
    void getConsentAuthorisation_failure() {
        //Given
        when(piisConsentService.getConsentInitiationAuthorisations(CONSENT_ID))
            .thenReturn(ResponseObject.<Xs2aAuthorisationSubResources>builder()
                            .fail(MESSAGE_ERROR_PIIS_403)
                            .build());
        when(responseErrorMapper.generateErrorResponse(MESSAGE_ERROR_PIIS_403))
            .thenReturn(new ResponseEntity<>(HttpStatus.FORBIDDEN));

        //When
        ResponseEntity responseEntity = confirmationOfFundsController.getConsentAuthorisation(CONSENT_ID, null,
                                                                                              null, null, null, null, null, null,
                                                                                              null, null, null, null, null,
                                                                                              null, null);
        //Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void startConsentAuthorisation_Success() {
        // Given
        ResponseHeaders responseHeaders = ResponseHeaders.builder().build();

        CreateConsentAuthorizationResponse expectedResponse = getCreateConsentAuthorizationResponse();
        ResponseObject<AuthorisationResponse> responseObject = ResponseObject.<AuthorisationResponse>builder()
                                                                   .body(expectedResponse)
                                                                   .build();
        doReturn(new ResponseEntity<>(getCreateConsentAuthorizationResponse(), HttpStatus.CREATED))
            .when(responseMapper).created(any(), eq(responseHeaders));
        when(authorisationMapper.mapToPasswordFromBody(BODY))
            .thenReturn(PASSWORD);
        when(authorisationMapper.mapToConsentCreateOrUpdateAuthorisationResponse(responseObject))
            .thenReturn(new StartScaprocessResponse());
        when(piisConsentService.createPiisAuthorisation(PSU_ID_DATA, CONSENT_ID, PASSWORD))
            .thenReturn(responseObject);
        when(consentHeadersBuilder.buildStartAuthorisationHeaders(any()))
            .thenReturn(responseHeaders);

        // When
        ResponseEntity responseEntity = confirmationOfFundsController.startConsentAuthorisation(null, CONSENT_ID,
                                                                                                BODY, null, null, null, CORRECT_PSU_ID, null, null,
                                                                                                null, null, null, null, null,
                                                                                                null, null, null, null, null,
                                                                                                null, null, null, null, null, null);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    void startConsentAuthorisation_Failure() {
        when(authorisationMapper.mapToPasswordFromBody(BODY))
            .thenReturn(PASSWORD);
        when(piisConsentService.createPiisAuthorisation(PSU_ID_DATA, WRONG_CONSENT_ID, PASSWORD))
            .thenReturn(ResponseObject.<AuthorisationResponse>builder()
                            .fail(MESSAGE_ERROR_PIIS_400)
                            .build());
        when(responseErrorMapper.generateErrorResponse(MESSAGE_ERROR_PIIS_400))
            .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        // When
        ResponseEntity responseEntity = confirmationOfFundsController.startConsentAuthorisation(null, WRONG_CONSENT_ID,
                                                                                                BODY, null, null, null, CORRECT_PSU_ID, null,
                                                                                                null, null, null, null, null,
                                                                                                null, null, null, null, null,
                                                                                                null, null, null, null, null, null, null);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateConsentsPsuData_success() {
        // Given
        Map body = new HashMap();
        PsuIdData psuIdData = new PsuIdData(CORRECT_PSU_ID, null, null, null, null, null);

        ConsentAuthorisationsParameters updateConsentPsuDataReq = jsonReader.getObjectFromFile("json/piis/update-consent-psu-data-req.json", ConsentAuthorisationsParameters.class);

        when(consentModelMapper.mapToUpdatePsuData(psuIdData, CONSENT_ID, AUTHORISATION_ID, body)).thenReturn(updateConsentPsuDataReq);

        UpdateConsentPsuDataResponse updateConsentPsuDataResponse = new UpdateConsentPsuDataResponse(ScaStatus.FINALISED, CONSENT_ID, AUTHORISATION_ID, psuIdData);

        ResponseObject<UpdateConsentPsuDataResponse> updateConsentPsuDataServiceResponse = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                                               .body(updateConsentPsuDataResponse)
                                                                                               .build();

        when(piisConsentService.updateConsentPsuData(updateConsentPsuDataReq)).thenReturn(updateConsentPsuDataServiceResponse);

        ResponseHeaders responseHeaders = buildScaApproachHeader();
        when(consentHeadersBuilder.buildUpdatePsuDataHeaders(AUTHORISATION_ID)).thenReturn(responseHeaders);

        doReturn(new ResponseEntity<>(updateConsentPsuDataServiceResponse.getBody(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any(), any());

        // When
        ResponseEntity responseEntity = confirmationOfFundsController.updateConsentsPsuData(null, CONSENT_ID, AUTHORISATION_ID, body, null, null, null, CORRECT_PSU_ID,
                                                                                            null, null, null, null, null, null, null,
                                                                                            null, null, null, null, null, null);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(updateConsentPsuDataResponse);
    }

    @Test
    void updateConsentsPsuData_failure() {
        // Given
        Map body = new HashMap();
        PsuIdData psuIdData = new PsuIdData(CORRECT_PSU_ID, null, null, null, null, null);

        ConsentAuthorisationsParameters updateConsentPsuDataReq = jsonReader.getObjectFromFile("json/piis/update-consent-psu-data-req.json", ConsentAuthorisationsParameters.class);

        when(consentModelMapper.mapToUpdatePsuData(psuIdData, CONSENT_ID, AUTHORISATION_ID, body)).thenReturn(updateConsentPsuDataReq);

        MessageError messageError = new MessageError(ErrorType.PIIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR));
        ResponseObject<UpdateConsentPsuDataResponse> updateConsentPsuDataServiceResponse = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                                               .fail(messageError)
                                                                                               .build();

        when(piisConsentService.updateConsentPsuData(updateConsentPsuDataReq)).thenReturn(updateConsentPsuDataServiceResponse);
        when(responseErrorMapper.generateErrorResponse(messageError))
            .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        // When
        ResponseEntity responseEntity = confirmationOfFundsController.updateConsentsPsuData(null, CONSENT_ID, AUTHORISATION_ID, body, null, null, null, CORRECT_PSU_ID,
                                                                                            null, null, null, null, null, null, null,
                                                                                            null, null, null, null, null, null);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private CreateConsentAuthorizationResponse getCreateConsentAuthorizationResponse() {
        CreateConsentAuthorizationResponse response = new CreateConsentAuthorizationResponse();
        response.setConsentId(CONSENT_ID);
        response.setAuthorisationId(AUTHORISATION_ID);
        response.setScaStatus(ScaStatus.RECEIVED);
        return response;
    }

    private ResponseObject<ConsentConfirmationOfFundsContentResponse> getConsentConfirmationOfFundsContentResponse() {

        PiisConsent piisConsent = getPiisConsent(CONSENT_ID).getBody();
        PiisConsentData consentData = piisConsent.getConsentData();

        ConsentConfirmationOfFundsContentResponse consentConfirmationOfFundsContentResponse = new ConsentConfirmationOfFundsContentResponse();
        AccountReference accountReference = new AccountReference();
        consentConfirmationOfFundsContentResponse.setAccount(accountReference);
        consentConfirmationOfFundsContentResponse.setConsentStatus(de.adorsys.psd2.model.ConsentStatus.fromValue(piisConsent.getConsentStatus().getValue()));
        consentConfirmationOfFundsContentResponse.setCardInformation(consentData.getCardInformation());
        consentConfirmationOfFundsContentResponse.setCardExpiryDate(consentData.getCardExpiryDate());
        consentConfirmationOfFundsContentResponse.setCardNumber(consentData.getCardNumber());
        consentConfirmationOfFundsContentResponse.setRegistrationInformation(consentData.getRegistrationInformation());


        return ResponseObject.<ConsentConfirmationOfFundsContentResponse>builder().body(consentConfirmationOfFundsContentResponse).build();
    }

    private ResponseObject<PiisConsent> getPiisConsent(String consentId) {
        PiisConsent piisConsent = consentId.equals(WRONG_CONSENT_ID)
                                      ? null
                                      : buildPiisConsent(consentId);
        return isEmpty(piisConsent)
                   ? ResponseObject.<PiisConsent>builder().fail(MESSAGE_ERROR_PIIS_404).build()
                   : ResponseObject.<PiisConsent>builder().body(piisConsent).build();
    }

    private PiisConsent buildPiisConsent(String consentId) {
        PiisConsent piisConsent = jsonReader.getObjectFromFile("json/service/piis-consent.json", PiisConsent.class);
        piisConsent.setId(consentId);

        return piisConsent;
    }

    private ResponseObject<ConsentsConfirmationOfFundsResponse> createConsentsConfirmationOfFundsResponse() {
        ConsentsConfirmationOfFundsResponse response = new ConsentsConfirmationOfFundsResponse();
        response.setConsentStatus(de.adorsys.psd2.model.ConsentStatus.RECEIVED);
        response.setConsentId(CONSENT_ID);
        response.setPsuMessage(PSU_MESSAGE_RESPONSE);

        return ResponseObject.<ConsentsConfirmationOfFundsResponse>builder().body(response).build();
    }

    private ResponseObject<Xs2aConfirmationOfFundsResponse> createConsentsConfirmationOfFunds() {
        Xs2aConfirmationOfFundsResponse consentResponse = new Xs2aConfirmationOfFundsResponse(ConsentStatus.RECEIVED.getValue(), ConfirmationOfFundsControllerTest.CONSENT_ID, false, null);
        consentResponse.setPsuMessage(PSU_MESSAGE_RESPONSE);
        Links links = new Links();
        links.setSelf(new HrefType("type"));
        consentResponse.setLinks(links);
        return ResponseObject.<Xs2aConfirmationOfFundsResponse>builder().body(consentResponse).build();
    }

    private static AdditionalPsuIdData buildEmptyAdditionalPsuIdData() {
        return new AdditionalPsuIdData(null, null, null, null, null, null, null, null, null);
    }

    private ResponseHeaders buildScaApproachHeader() {
        return ResponseHeaders.builder()
                   .aspspScaApproach(ScaApproach.EMBEDDED)
                   .build();
    }
}

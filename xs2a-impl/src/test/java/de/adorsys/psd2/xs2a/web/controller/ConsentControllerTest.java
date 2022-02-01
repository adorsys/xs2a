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

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.AdditionalPsuIdData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.ConsentStatusResponse;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aScaStatusResponse;
import de.adorsys.psd2.xs2a.service.ConsentService;
import de.adorsys.psd2.xs2a.service.NotificationSupportedModeService;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ResponseErrorMapper;
import de.adorsys.psd2.xs2a.web.header.ConsentHeadersBuilder;
import de.adorsys.psd2.xs2a.web.header.ResponseHeaders;
import de.adorsys.psd2.xs2a.web.mapper.AuthorisationMapper;
import de.adorsys.psd2.xs2a.web.mapper.ConsentModelMapper;
import de.adorsys.psd2.xs2a.web.mapper.TppRedirectUriMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.util.StringUtils.isEmpty;

@ExtendWith(MockitoExtension.class)
class ConsentControllerTest {
    private static final String CORRECT_PSU_ID = "ID 777";
    private static final String WRONG_PSU_ID = "ID 666";
    private static final String PASSWORD = "password";
    private static final Map BODY = Collections.singletonMap("psuData", Collections.singletonMap("password", PASSWORD));
    private static final String CONSENT_ID = "XXXX-YYYY-XXXX-YYYY";
    private static final String AUTHORISATION_ID = "2400de4c-1c74-4ca0-941d-8f56b828f31d";
    private static final String WRONG_CONSENT_ID = "YYYY-YYYY-YYYY-YYYY";
    private static final String PSU_MESSAGE_RESPONSE = "test psu message";
    private static final boolean EXPLICIT_PREFERRED = true;
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(CORRECT_PSU_ID, null, null, null, null);
    private static final PsuIdData PSU_ID_DATA_WITH_EMPTY_ADDITIONAL_DATA = new PsuIdData(CORRECT_PSU_ID, null, null, null, null, buildEmptyAdditionalPsuIdData());
    private static final PsuIdData PSU_ID_DATA_WRONG_WITH_EMPTY_ADDITIONAL_DATA = new PsuIdData(WRONG_PSU_ID, null, null, null, null, buildEmptyAdditionalPsuIdData());
    private static final UUID REQUEST_ID = UUID.fromString("ddd36e05-d67a-4830-93ad-9462f71ae1e6");
    private static final MessageError MESSAGE_ERROR_AIS_400 = new MessageError(ErrorType.AIS_400, of(MessageErrorCode.CONSENT_UNKNOWN_400));
    private static final MessageError MESSAGE_ERROR_AIS_403 = new MessageError(ErrorType.AIS_403, of(MessageErrorCode.RESOURCE_UNKNOWN_403));
    private static final MessageError MESSAGE_ERROR_AIS_404 = new MessageError(ErrorType.AIS_404, of(MessageErrorCode.RESOURCE_UNKNOWN_404));
    private static final ResponseHeaders RESPONSE_HEADERS = ResponseHeaders.builder().aspspScaApproach(ScaApproach.REDIRECT).build();
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";

    @InjectMocks
    private ConsentController consentController;

    @Mock
    private ConsentService consentService;
    @Mock
    private ResponseMapper responseMapper;
    @Mock
    private ConsentModelMapper consentModelMapper;
    @Mock
    private AuthorisationMapper authorisationMapper;
    @Mock
    private TppRedirectUriMapper tppRedirectUriMapper;
    @Mock
    private ResponseErrorMapper responseErrorMapper;
    @Mock
    private ConsentHeadersBuilder consentHeadersBuilder;
    @Mock
    private NotificationSupportedModeService notificationSupportedModeService;
    @Mock
    private RequestProviderService requestProviderService;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void createAccountConsent_Success() {
        //Given:
        when(consentService.createAccountConsentsWithResponse(any(), eq(PSU_ID_DATA_WITH_EMPTY_ADDITIONAL_DATA), eq(EXPLICIT_PREFERRED)))
            .thenReturn(createXs2aConsentResponse(CONSENT_ID));
        doReturn(new ResponseEntity<>(createConsentResponse().getBody(), HttpStatus.CREATED))
            .when(responseMapper).created(any(), any(Function.class), eq(RESPONSE_HEADERS));
        when(consentHeadersBuilder.buildCreateConsentHeaders(any(), any(), any()))
            .thenReturn(RESPONSE_HEADERS);
        Consents consents = getConsents();

        //When:
        ResponseEntity responseEntity = consentController.createConsent(null, null, consents, null, null,
                                                                        new byte[]{}, CORRECT_PSU_ID, null, null,
                                                                        null, false, null, null, null,
                                                                        EXPLICIT_PREFERRED, null, null, null, null, null,
                                                                        null, null, null, null, null,
                                                                        null, null);
        ConsentsResponse201 resp = (ConsentsResponse201) responseEntity.getBody();

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp).isNotNull();
        assertThat(resp.getConsentStatus()).hasToString(ConsentStatus.RECEIVED.getValue());
        assertThat(resp.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(resp.getPsuMessage()).isEqualTo(PSU_MESSAGE_RESPONSE);
    }

    @Test
    void createAccountConsent_WithNullInLinks() {
        //Given:
        when(consentService.createAccountConsentsWithResponse(any(), eq(PSU_ID_DATA_WITH_EMPTY_ADDITIONAL_DATA), eq(EXPLICIT_PREFERRED)))
            .thenReturn(createXs2aConsentResponseWithoutLinks());
        Consents consents = getConsents();

        //When:
        assertThrows(IllegalArgumentException.class, () -> consentController.createConsent(null, null, consents, null, null,
                                                                                           new byte[]{}, CORRECT_PSU_ID, null, null,
                                                                                           null, false, null, null, null,
                                                                                           EXPLICIT_PREFERRED, null, null, null, null, null,
                                                                                           null, null, null, null, null,
                                                                                           null, null));
    }

    @Test
    void createAccountConsent_Failure() {
        //Given:
        when(consentService.createAccountConsentsWithResponse(any(), eq(PSU_ID_DATA_WRONG_WITH_EMPTY_ADDITIONAL_DATA), eq(EXPLICIT_PREFERRED)))
            .thenReturn(createXs2aConsentResponse(null));
        when(responseErrorMapper.generateErrorResponse(MESSAGE_ERROR_AIS_404))
            .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        Consents consents = getConsents();
        //When:
        ResponseEntity responseEntity = consentController.createConsent(null, null, consents, null, null,
                                                                        new byte[]{}, WRONG_PSU_ID, null, null,
                                                                        null, false, null, null, null,
                                                                        EXPLICIT_PREFERRED, null, null, null, null, null,
                                                                        null, null, null, null, null,
                                                                        null, null);
        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getAccountConsentsStatusById_Success() {
        // Given
        when(consentService.getAccountConsentsStatusById(CONSENT_ID))
            .thenReturn(ResponseObject.<ConsentStatusResponse>builder()
                            .body(new ConsentStatusResponse(ConsentStatus.RECEIVED, PSU_MESSAGE_RESPONSE))
                            .build());

        doReturn(new ResponseEntity<>(ConsentStatus.RECEIVED, HttpStatus.OK)).when(responseMapper).ok(any(), any());

        //When:
        ResponseEntity responseEntity = consentController.getConsentStatus(CONSENT_ID, null,
                                                                           null, null, null, null, null, null,
                                                                           null, null, null, null, null,
                                                                           null, null);
        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(ConsentStatus.RECEIVED);
    }

    @Test
    void getAccountConsentsStatusById_Failure() {
        // Given
        when(consentService.getAccountConsentsStatusById(WRONG_CONSENT_ID))
            .thenReturn(ResponseObject.<ConsentStatusResponse>builder()
                            .fail(MESSAGE_ERROR_AIS_404)
                            .build());
        when(responseErrorMapper.generateErrorResponse(MESSAGE_ERROR_AIS_404))
            .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        //When:
        ResponseEntity responseEntity = consentController.getConsentStatus(WRONG_CONSENT_ID, null,
                                                                           null, null, null, null, null, null,
                                                                           null, null, null, null, null,
                                                                           null, null);
        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
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
        when(consentService.createAisAuthorisation(PSU_ID_DATA, CONSENT_ID, PASSWORD))
            .thenReturn(responseObject);
        when(consentHeadersBuilder.buildStartAuthorisationHeaders(any()))
            .thenReturn(responseHeaders);

        // When
        ResponseEntity responseEntity = consentController.startConsentAuthorisation(null, CONSENT_ID,
                                                                                    BODY, null, null, null, CORRECT_PSU_ID, null, null,
                                                                                    null, null, null, null, null, null,
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
        when(consentService.createAisAuthorisation(PSU_ID_DATA, WRONG_CONSENT_ID, PASSWORD))
            .thenReturn(ResponseObject.<AuthorisationResponse>builder()
                            .fail(MESSAGE_ERROR_AIS_400)
                            .build());
        when(responseErrorMapper.generateErrorResponse(MESSAGE_ERROR_AIS_400))
            .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        // When
        ResponseEntity responseEntity = consentController.startConsentAuthorisation(null, WRONG_CONSENT_ID,
                                                                                    BODY, null, null, null, CORRECT_PSU_ID, null,
                                                                                    null, null, null, null, null, null,
                                                                                    null, null, null, null, null,
                                                                                    null, null, null, null, null, null, null);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getAccountConsentsInformationById_Success() {
        when(consentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(getConsent(CONSENT_ID));
        doReturn(new ResponseEntity<>(getConsentInformationResponse().getBody(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());
        //When:
        ResponseEntity responseEntity = consentController.getConsentInformation(CONSENT_ID, null,
                                                                                null, null, null, null, null, null,
                                                                                null, null, null, null, null,
                                                                                null, null);
        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isExactlyInstanceOf(ConsentInformationResponse200Json.class);
    }

    @Test
    void getAccountConsentsInformationById_Failure() {
        // Given
        when(consentService.getAccountConsentById(WRONG_CONSENT_ID))
            .thenReturn(getConsent(WRONG_CONSENT_ID));
        when(responseErrorMapper.generateErrorResponse(MESSAGE_ERROR_AIS_404))
            .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        //When:
        ResponseEntity responseEntity = consentController.getConsentInformation(WRONG_CONSENT_ID, null,
                                                                                null, null, null, null, null, null,
                                                                                null, null, null, null, null,
                                                                                null, null);
        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteAccountConsent_Success() {
        // Given
        when(consentService.deleteAccountConsentsById(CONSENT_ID))
            .thenReturn(ResponseObject.<Void>builder().build());
        doReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT))
            .when(responseMapper).delete(any());

        //When:
        ResponseEntity responseEntity = consentController.deleteConsent(CONSENT_ID, null,
                                                                        null, null, null, null, null, null,
                                                                        null, null, null, null, null,
                                                                        null, null);
        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void deleteAccountConsent_Failure() {
        // Given
        when(consentService.deleteAccountConsentsById(WRONG_CONSENT_ID))
            .thenReturn(ResponseObject.<Void>builder()
                            .fail(MESSAGE_ERROR_AIS_404)
                            .build());
        when(responseErrorMapper.generateErrorResponse(MESSAGE_ERROR_AIS_404))
            .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        //When:
        ResponseEntity responseEntity = consentController.deleteConsent(WRONG_CONSENT_ID, null,
                                                                        null, null, null, null, null, null,
                                                                        null, null, null, null, null,
                                                                        null, null);
        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getConsentScaStatus_success() {
        // Given
        Xs2aScaStatusResponse xs2aScaStatusResponse = new Xs2aScaStatusResponse(ScaStatus.RECEIVED, true, "psu message", null, null);
        ResponseObject<Xs2aScaStatusResponse> responseObject = ResponseObject.<Xs2aScaStatusResponse>builder()
                                                                   .body(xs2aScaStatusResponse)
                                                                   .build();
        when(consentService.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(responseObject);
        doReturn(ResponseEntity.ok(buildReceivedScaStatusResponse()))
            .when(responseMapper).ok(eq(responseObject), any());

        ScaStatusResponse expected = buildReceivedScaStatusResponse();

        // When
        ResponseEntity actual = consentController.getConsentScaStatus(CONSENT_ID, AUTHORISATION_ID, REQUEST_ID,
                                                                      null, null, null,
                                                                      null, null,
                                                                      null, null,
                                                                      null, null,
                                                                      null, null,
                                                                      null, null);

        // Then
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual.getBody()).isEqualTo(expected);
    }

    @Test
    void getConsentScaStatus_failure() {
        // Given
        when(responseErrorMapper.generateErrorResponse(MESSAGE_ERROR_AIS_403))
            .thenReturn(new ResponseEntity<>(HttpStatus.FORBIDDEN));
        when(consentService.getConsentAuthorisationScaStatus(WRONG_CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(ResponseObject.<Xs2aScaStatusResponse>builder()
                            .fail(MESSAGE_ERROR_AIS_403)
                            .build());

        // When
        ResponseEntity actual = consentController.getConsentScaStatus(WRONG_CONSENT_ID, AUTHORISATION_ID, REQUEST_ID,
                                                                      null, null, null,
                                                                      null, null,
                                                                      null, null,
                                                                      null, null,
                                                                      null, null,
                                                                      null, null);

        // Then
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private ResponseObject<ConsentsResponse201> createConsentResponse() {
        ConsentsResponse201 response = new ConsentsResponse201();
        response.setConsentStatus(de.adorsys.psd2.model.ConsentStatus.RECEIVED);
        response.setConsentId(CONSENT_ID);
        response.setPsuMessage(PSU_MESSAGE_RESPONSE);

        return isEmpty(CONSENT_ID)
                   ? ResponseObject.<ConsentsResponse201>builder().fail(ErrorType.AIS_404, of(MessageErrorCode.RESOURCE_UNKNOWN_404)).build()
                   : ResponseObject.<ConsentsResponse201>builder().body(response).build();
    }

    private ResponseObject<CreateConsentResponse> createXs2aConsentResponse(String consentId) {
        if (isEmpty(consentId)) {
            return ResponseObject.<CreateConsentResponse>builder().fail(MESSAGE_ERROR_AIS_404).build();
        }
        CreateConsentResponse consentResponse = new CreateConsentResponse(ConsentStatus.RECEIVED.getValue(), consentId, null, null, null, false, INTERNAL_REQUEST_ID, null);
        Links links = new Links();
        links.setSelf(new HrefType("type"));
        consentResponse.setLinks(links);
        return ResponseObject.<CreateConsentResponse>builder().body(consentResponse).build();
    }

    private ResponseObject<CreateConsentResponse> createXs2aConsentResponseWithoutLinks() {
        if (isEmpty(CONSENT_ID)) {
            return ResponseObject.<CreateConsentResponse>builder().fail(MESSAGE_ERROR_AIS_404).build();
        }
        CreateConsentResponse consentResponse = new CreateConsentResponse(ConsentStatus.RECEIVED.getValue(), ConsentControllerTest.CONSENT_ID, null, null, null, false, INTERNAL_REQUEST_ID, null);
        return ResponseObject.<CreateConsentResponse>builder().body(consentResponse).build();
    }

    private ResponseObject<AisConsent> getConsent(String consentId) {
        AisConsent aisConsent = consentId.equals(WRONG_CONSENT_ID)
                                    ? null
                                    : buildAisConsent(consentId);
        return isEmpty(aisConsent)
                   ? ResponseObject.<AisConsent>builder().fail(MESSAGE_ERROR_AIS_404).build()
                   : ResponseObject.<AisConsent>builder().body(aisConsent).build();
    }

    private AisConsent buildAisConsent(String consentId) {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/ais-consent.json", AisConsent.class);
        aisConsent.setId(consentId);

        return aisConsent;
    }

    private ResponseObject<ConsentInformationResponse200Json> getConsentInformationResponse() {

        ConsentInformationResponse200Json consent = new ConsentInformationResponse200Json();
        de.adorsys.psd2.model.AccountAccess access = new de.adorsys.psd2.model.AccountAccess();
        access.setAccounts(Collections.emptyList());
        access.setBalances(Collections.emptyList());
        access.setTransactions(Collections.emptyList());
        access.setAllPsd2(de.adorsys.psd2.model.AccountAccess.AllPsd2Enum.ALLACCOUNTS);
        access.setAvailableAccounts(de.adorsys.psd2.model.AccountAccess.AvailableAccountsEnum.ALLACCOUNTS);
        consent.setAccess(access);
        return ResponseObject.<ConsentInformationResponse200Json>builder().body(consent).build();
    }

    private Consents getConsents() {
        Consents consents = new Consents();
        de.adorsys.psd2.model.AccountAccess access = new de.adorsys.psd2.model.AccountAccess();
        consents.setAccess(access);
        return consents;
    }

    private CreateConsentAuthorizationResponse getCreateConsentAuthorizationResponse() {
        CreateConsentAuthorizationResponse response = new CreateConsentAuthorizationResponse();
        response.setConsentId(CONSENT_ID);
        response.setAuthorisationId(AUTHORISATION_ID);
        response.setScaStatus(ScaStatus.RECEIVED);
        return response;
    }

    private ScaStatusResponse buildReceivedScaStatusResponse() {
        ScaStatusResponse response = new ScaStatusResponse().scaStatus(de.adorsys.psd2.model.ScaStatus.RECEIVED);
        response.setTrustedBeneficiaryFlag(true);
        return response;
    }

    private static AdditionalPsuIdData buildEmptyAdditionalPsuIdData() {
        return new AdditionalPsuIdData(null, null, null, null, null, null, null, null, null);
    }
}

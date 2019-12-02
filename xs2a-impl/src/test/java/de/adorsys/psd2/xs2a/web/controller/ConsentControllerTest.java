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

package de.adorsys.psd2.xs2a.web.controller;

import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.ConsentService;
import de.adorsys.psd2.xs2a.service.NotificationSupportedModeService;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ResponseErrorMapper;
import de.adorsys.psd2.xs2a.web.header.ConsentHeadersBuilder;
import de.adorsys.psd2.xs2a.web.header.ResponseHeaders;
import de.adorsys.psd2.xs2a.web.mapper.AuthorisationMapper;
import de.adorsys.psd2.xs2a.web.mapper.ConsentModelMapper;
import de.adorsys.psd2.xs2a.web.mapper.TppRedirectUriMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.util.StringUtils.isEmpty;

@RunWith(MockitoJUnitRunner.class)
public class ConsentControllerTest {
    private static final String CORRECT_PSU_ID = "ID 777";
    private static final String WRONG_PSU_ID = "ID 666";
    private static final String PASSWORD = "password";
    private static final Map BODY = Collections.singletonMap("psuData", Collections.singletonMap("password", PASSWORD));
    private static final String CONSENT_ID = "XXXX-YYYY-XXXX-YYYY";
    private static final String AUTHORISATION_ID = "2400de4c-1c74-4ca0-941d-8f56b828f31d";
    private static final String WRONG_CONSENT_ID = "YYYY-YYYY-YYYY-YYYY";
    private static final String PSU_MESSAGE_RESPONSE = "test psu message";
    private static final boolean EXPLICIT_PREFERRED = true;
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(CORRECT_PSU_ID, null, null, null);
    private static final PsuIdData PSU_ID_DATA_WRONG = new PsuIdData(WRONG_PSU_ID, null, null, null);
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

    @Before
    public void setUp() {
        when(consentService.createAccountConsentsWithResponse(any(), eq(PSU_ID_DATA), eq(EXPLICIT_PREFERRED))).thenReturn(createXs2aConsentResponse(CONSENT_ID));
        when(consentService.createAccountConsentsWithResponse(any(), eq(PSU_ID_DATA_WRONG), eq(EXPLICIT_PREFERRED))).thenReturn(createXs2aConsentResponse(null));
        when(consentService.getAccountConsentsStatusById(eq(CONSENT_ID))).thenReturn(ResponseObject.<ConsentStatusResponse>builder().body(new ConsentStatusResponse(ConsentStatus.RECEIVED)).build());
        when(consentService.getAccountConsentsStatusById(eq(WRONG_CONSENT_ID))).thenReturn(ResponseObject.<ConsentStatusResponse>builder().fail(MESSAGE_ERROR_AIS_404).build());
        when(consentService.getAccountConsentById(eq(CONSENT_ID))).thenReturn(getConsent(CONSENT_ID));
        when(consentService.getAccountConsentById(eq(WRONG_CONSENT_ID))).thenReturn(getConsent(WRONG_CONSENT_ID));
        when(consentService.deleteAccountConsentsById(eq(CONSENT_ID))).thenReturn(ResponseObject.<Void>builder().build());
        when(consentService.deleteAccountConsentsById(eq(WRONG_CONSENT_ID))).thenReturn(ResponseObject.<Void>builder().fail(MESSAGE_ERROR_AIS_404).build());
        when(responseErrorMapper.generateErrorResponse(MESSAGE_ERROR_AIS_403)).thenReturn(new ResponseEntity<>(HttpStatus.FORBIDDEN));
        when(responseErrorMapper.generateErrorResponse(MESSAGE_ERROR_AIS_404)).thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Test
    public void createAccountConsent_Success() {
        //Given:
        doReturn(new ResponseEntity<>(createConsentResponse().getBody(), HttpStatus.CREATED))
            .when(responseMapper).created(any(), any(Function.class), eq(RESPONSE_HEADERS));
        when(consentHeadersBuilder.buildCreateConsentHeaders(any(), any(), any())).thenReturn(RESPONSE_HEADERS);
        Consents consents = getConsents();

        //When:
        ResponseEntity responseEntity = consentController.createConsent(null, consents,
                                                                        null, null, null, CORRECT_PSU_ID, null, null,
                                                                        null, false, null, null,
                                                                        EXPLICIT_PREFERRED, null, null, null, null,
                                                                        null, null, null, null, null,
                                                                        null, null, null);
        ConsentsResponse201 resp = (ConsentsResponse201) responseEntity.getBody();

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getConsentStatus().toString()).isEqualTo(ConsentStatus.RECEIVED.getValue());
        assertThat(resp.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(resp.getPsuMessage()).isEqualTo(PSU_MESSAGE_RESPONSE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createAccountConsent_WithNullInLinks() {
        //Given:
        when(consentService.createAccountConsentsWithResponse(any(), eq(PSU_ID_DATA), eq(EXPLICIT_PREFERRED))).thenReturn(createXs2aConsentResponseWithoutLinks());
        Consents consents = getConsents();

        //When:
        consentController.createConsent(null, consents,
                                        null, null, null, CORRECT_PSU_ID, null, null,
                                        null, false, null, null,
                                        EXPLICIT_PREFERRED, null, null, null, null,
                                        null, null, null, null, null,
                                        null, null, null);
    }

    @Test
    public void createAccountConsent_Failure() {
        //Given:
        when(responseErrorMapper.generateErrorResponse(MESSAGE_ERROR_AIS_404))
            .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        Consents consents = getConsents();
        //When:
        ResponseEntity responseEntity = consentController.createConsent(null, consents,
                                                                        null, null, null, WRONG_PSU_ID, null, null,
                                                                        null, false, null, null,
                                                                        EXPLICIT_PREFERRED, null, null, null, null,
                                                                        null, null, null, null, null,
                                                                        null, null, null);
        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getAccountConsentsStatusById_Success() {
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
    public void getAccountConsentsStatusById_Failure() {
        //When:
        ResponseEntity responseEntity = consentController.getConsentStatus(WRONG_CONSENT_ID, null,
                                                                           null, null, null, null, null, null,
                                                                           null, null, null, null, null,
                                                                           null, null);
        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void startConsentAuthorisation_Success() {
        // Given
        ResponseHeaders responseHeaders = ResponseHeaders.builder().build();

        CreateConsentAuthorizationResponse expectedResponse = getCreateConsentAuthorizationResponse();
        ResponseObject<AuthorisationResponse> responseObject = ResponseObject.<AuthorisationResponse>builder()
                                                                   .body(expectedResponse)
                                                                   .build();
        doReturn(new ResponseEntity<>(getCreateConsentAuthorizationResponse(), HttpStatus.CREATED))
            .when(responseMapper).created(any(), eq(responseHeaders));
        when(authorisationMapper.mapToPasswordFromBody(BODY)).thenReturn(PASSWORD);
        when(authorisationMapper.mapToAisCreateOrUpdateAuthorisationResponse(responseObject)).thenReturn(new StartScaprocessResponse());

        when(consentService.createAisAuthorisation(PSU_ID_DATA, CONSENT_ID, PASSWORD))
            .thenReturn(responseObject);
        when(consentHeadersBuilder.buildStartConsentAuthorisationHeaders(any())).thenReturn(responseHeaders);

        // When
        ResponseEntity responseEntity = consentController.startConsentAuthorisation(null, CONSENT_ID,
                                                                                    BODY, null, null, null, CORRECT_PSU_ID, null, null,
                                                                                    null, null, null, null, null,
                                                                                    null, null, null, null, null,
                                                                                    null, null, null, null, null, null);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    public void startConsentAuthorisation_Failure() {
        when(authorisationMapper.mapToPasswordFromBody(BODY)).thenReturn(PASSWORD);
        when(consentService.createAisAuthorisation(PSU_ID_DATA, WRONG_CONSENT_ID, PASSWORD))
            .thenReturn(ResponseObject.<AuthorisationResponse>builder()
                            .fail(MESSAGE_ERROR_AIS_400)
                            .build());
        when(responseErrorMapper.generateErrorResponse(MESSAGE_ERROR_AIS_400))
            .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        // When
        ResponseEntity responseEntity = consentController.startConsentAuthorisation(null, WRONG_CONSENT_ID,
                                                                                    BODY, null, null, null, CORRECT_PSU_ID, null,
                                                                                    null, null, null, null, null,
                                                                                    null, null, null, null, null,
                                                                                    null, null, null, null, null, null, null);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getAccountConsentsInformationById_Success() {
        doReturn(new ResponseEntity<>(getConsentInformationResponse(CONSENT_ID).getBody(), HttpStatus.OK))
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
    public void getAccountConsentsInformationById_Failure() {
        //When:
        ResponseEntity responseEntity = consentController.getConsentInformation(WRONG_CONSENT_ID, null,
                                                                                null, null, null, null, null, null,
                                                                                null, null, null, null, null,
                                                                                null, null);
        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void deleteAccountConsent_Success() {
        doReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT)).when(responseMapper).delete(any());
        //When:
        ResponseEntity responseEntity = consentController.deleteConsent(CONSENT_ID, null,
                                                                        null, null, null, null, null, null,
                                                                        null, null, null, null, null,
                                                                        null, null);
        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void deleteAccountConsent_Failure() {
        //When:
        ResponseEntity responseEntity = consentController.deleteConsent(WRONG_CONSENT_ID, null,
                                                                        null, null, null, null, null, null,
                                                                        null, null, null, null, null,
                                                                        null, null);
        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getConsentScaStatus_success() {
        ResponseObject<ScaStatus> responseObject = ResponseObject.<ScaStatus>builder()
                                                       .body(ScaStatus.RECEIVED)
                                                       .build();
        when(consentService.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(responseObject);
        doReturn(ResponseEntity.ok(buildReceivedScaStatusResponse()))
            .when(responseMapper).ok(eq(responseObject), any());

        // Given
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
    public void getConsentScaStatus_failure() {
        when(consentService.getConsentAuthorisationScaStatus(WRONG_CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(buildScaStatusError());
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
        CreateConsentResponse consentResponse = new CreateConsentResponse(ConsentStatus.RECEIVED.getValue(), consentId, null, null, null, null, false, INTERNAL_REQUEST_ID, null);
        Links links = new Links();
        links.setSelf(new HrefType("type"));
        consentResponse.setLinks(links);
        return ResponseObject.<CreateConsentResponse>builder().body(consentResponse).build();
    }

    private ResponseObject<CreateConsentResponse> createXs2aConsentResponseWithoutLinks() {
        if (isEmpty(CONSENT_ID)) {
            return ResponseObject.<CreateConsentResponse>builder().fail(MESSAGE_ERROR_AIS_404).build();
        }
        CreateConsentResponse consentResponse = new CreateConsentResponse(ConsentStatus.RECEIVED.getValue(), ConsentControllerTest.CONSENT_ID, null, null, null, null, false, INTERNAL_REQUEST_ID, null);
        return ResponseObject.<CreateConsentResponse>builder().body(consentResponse).build();
    }

    private ResponseObject<AccountConsent> getConsent(String consentId) {
        AccountConsent accountConsent = consentId.equals(WRONG_CONSENT_ID)
                                            ? null
                                            : new AccountConsent(consentId, new Xs2aAccountAccess(null, null, null, null, null, null, null), new Xs2aAccountAccess(null, null, null, null, null, null, null), false, LocalDate.now(), 4, LocalDate.now(), ConsentStatus.VALID, false, false, null, null, AisConsentRequestType.GLOBAL, false, Collections.emptyList(), OffsetDateTime.now(), Collections.emptyMap(), OffsetDateTime.now());
        return isEmpty(accountConsent)
                   ? ResponseObject.<AccountConsent>builder().fail(MESSAGE_ERROR_AIS_404).build()
                   : ResponseObject.<AccountConsent>builder().body(accountConsent).build();
    }

    private ResponseObject<ConsentInformationResponse200Json> getConsentInformationResponse(String consentId) {
        if (consentId.equals(WRONG_CONSENT_ID)) {
            return ResponseObject.<ConsentInformationResponse200Json>builder()
                       .fail(MESSAGE_ERROR_AIS_404)
                       .build();
        }

        ConsentInformationResponse200Json consent = new ConsentInformationResponse200Json();
        AccountAccess access = new AccountAccess();
        access.setAccounts(Collections.emptyList());
        access.setBalances(Collections.emptyList());
        access.setTransactions(Collections.emptyList());
        access.setAllPsd2(AccountAccess.AllPsd2Enum.ALLACCOUNTS);
        access.setAvailableAccounts(AccountAccess.AvailableAccountsEnum.ALLACCOUNTS);
        consent.setAccess(access);
        return ResponseObject.<ConsentInformationResponse200Json>builder().body(consent).build();
    }

    private Consents getConsents() {
        Consents consents = new Consents();
        AccountAccess access = new AccountAccess();
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
        return new ScaStatusResponse().scaStatus(de.adorsys.psd2.model.ScaStatus.RECEIVED);
    }

    private ResponseObject<ScaStatus> buildScaStatusError() {
        return ResponseObject.<ScaStatus>builder()
                   .fail(MESSAGE_ERROR_AIS_403)
                   .build();
    }
}

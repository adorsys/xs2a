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

package de.adorsys.psd2.xs2a.web.controller;

import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.exception.MessageCategory;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.ConsentService;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.xs2a.web.mapper.AuthorisationMapper;
import de.adorsys.psd2.xs2a.web.mapper.ConsentModelMapper;
import de.adorsys.psd2.xs2a.web.mapper.TppRedirectUriMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.util.StringUtils.isEmpty;

@RunWith(MockitoJUnitRunner.class)
public class ConsentControllerTest {
    private static final String CORRECT_PSU_ID = "ID 777";
    private static final String WRONG_PSU_ID = "ID 666";
    private static final String CONSENT_ID = "XXXX-YYYY-XXXX-YYYY";
    private static final String AUTHORISATION_ID = "2400de4c-1c74-4ca0-941d-8f56b828f31d";
    private static final String WRONG_CONSENT_ID = "YYYY-YYYY-YYYY-YYYY";
    private static final boolean EXPLICIT_PREFERRED = true;
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(null, null, null, null);
    private static final UUID REQUEST_ID = UUID.fromString("ddd36e05-d67a-4830-93ad-9462f71ae1e6");

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


    @Before
    public void setUp() {
        when(consentModelMapper.mapToCreateConsentReq(any())).thenReturn(getCreateConsentReq());
        when(consentService.createAccountConsentsWithResponse(any(), eq(PSU_ID_DATA), eq(EXPLICIT_PREFERRED), any())).thenReturn(createXs2aConsentResponse(CONSENT_ID));
        when(consentService.createAccountConsentsWithResponse(any(), eq(PSU_ID_DATA), eq(EXPLICIT_PREFERRED), any())).thenReturn(createXs2aConsentResponse(null));
        when(consentService.getAccountConsentsStatusById(eq(CONSENT_ID))).thenReturn(ResponseObject.<ConsentStatusResponse>builder().body(new ConsentStatusResponse(ConsentStatus.RECEIVED)).build());
        when(consentService.getAccountConsentsStatusById(eq(WRONG_CONSENT_ID))).thenReturn(ResponseObject.<ConsentStatusResponse>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.RESOURCE_UNKNOWN_404))).build());
        when(consentService.getAccountConsentById(eq(CONSENT_ID))).thenReturn(getConsent(CONSENT_ID));
        when(consentService.getAccountConsentById(eq(WRONG_CONSENT_ID))).thenReturn(getConsent(WRONG_CONSENT_ID));
        when(consentService.deleteAccountConsentsById(eq(CONSENT_ID))).thenReturn(ResponseObject.<Void>builder().build());
        when(consentService.deleteAccountConsentsById(eq(WRONG_CONSENT_ID))).thenReturn(ResponseObject.<Void>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.RESOURCE_UNKNOWN_404))).build());
    }

    @Test
    public void createAccountConsent_Success() {
        doReturn(new ResponseEntity<>(createConsentResponse(CONSENT_ID).getBody(), HttpStatus.CREATED)).when(responseMapper).created(any(), any());
        //Given:
        Consents consents = getConsents();
        //When:
        ResponseEntity responseEntity = consentController.createConsent(null, consents,
                                                                        null, null, null, CORRECT_PSU_ID, null, null,
                                                                        null, null, null, null,
                                                                        false, null, null, null, null,
                                                                        null, null, null, null, null,
                                                                        null);
        ConsentsResponse201 resp = (ConsentsResponse201) responseEntity.getBody();

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getConsentStatus().toString()).isEqualTo(ConsentStatus.RECEIVED.getValue());
        assertThat(resp.getConsentId()).isEqualTo(CONSENT_ID);
    }

    @Test
    public void createAccountConsent_Failure() {
        doReturn(new ResponseEntity<>(createConsentResponse(null).getError(), HttpStatus.NOT_FOUND)).when(responseMapper).created(any(), any());
        //Given:
        Consents consents = getConsents();
        //When:
        ResponseEntity responseEntity = consentController.createConsent(null, consents,
                                                                        null, null, null, WRONG_PSU_ID, null, null,
                                                                        null, null, null, null,
                                                                        false, null, null, null, null,
                                                                        null, null, null, null, null,
                                                                        null);
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
        doReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND)).when(responseMapper).ok(any(), any());
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
        doReturn(new ResponseEntity<>(getCreateConsentAuthorizationResponse(CONSENT_ID), HttpStatus.CREATED))
            .when(responseMapper).created(any(), any());

        // Given
        CreateConsentAuthorizationResponse expectedResponse = getCreateConsentAuthorizationResponse(CONSENT_ID);

        // When
        ResponseEntity responseEntity = consentController.startConsentAuthorisation(CONSENT_ID, null,
                                                                                    null, null, null, null, null, null,
                                                                                    null, null, null, null, null,
                                                                                    null, null, null, null, null,
                                                                                    null);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    public void startConsentAuthorisation_Failure() {
        doReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST)).when(responseMapper).created(any(), any());

        // When
        ResponseEntity responseEntity = consentController.startConsentAuthorisation(CONSENT_ID, null,
                                                                                    null, null, null, null, null, null,
                                                                                    null, null, null, null, null,
                                                                                    null, null, null, null, null,
                                                                                    null);

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
        doReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND)).when(responseMapper).ok(any(), any());
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
        doReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND)).when(responseMapper).delete(any());
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
        doReturn(ResponseEntity.ok(buildScaStatusResponse(de.adorsys.psd2.model.ScaStatus.RECEIVED)))
            .when(responseMapper).ok(eq(responseObject), any());

        // Given
        ScaStatusResponse expected = buildScaStatusResponse(de.adorsys.psd2.model.ScaStatus.RECEIVED);

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
        when(responseMapper.ok(any(), any())).thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).build());

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

    private ResponseObject<ConsentsResponse201> createConsentResponse(String consentId) {
        ConsentsResponse201 response = new ConsentsResponse201();
        response.setConsentStatus(de.adorsys.psd2.model.ConsentStatus.RECEIVED);
        response.setConsentId(consentId);

        return isEmpty(consentId)
                   ? ResponseObject.<ConsentsResponse201>builder().fail(new MessageError(
            new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.RESOURCE_UNKNOWN_404))).build()
                   : ResponseObject.<ConsentsResponse201>builder().body(response).build();
    }


    private ResponseObject<CreateConsentResponse> createXs2aConsentResponse(String consentId) {
        return isEmpty(consentId)
                   ? ResponseObject.<CreateConsentResponse>builder().fail(new MessageError(
            new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.RESOURCE_UNKNOWN_404))).build()
                   : ResponseObject.<CreateConsentResponse>builder().body(new CreateConsentResponse(ConsentStatus.RECEIVED.getValue(), consentId, null, null, null, null)).build();
    }

    private ResponseObject<AccountConsent> getConsent(String consentId) {
        AccountConsent accountConsent = consentId.equals(WRONG_CONSENT_ID)
                                            ? null
                                            : new AccountConsent(consentId, new Xs2aAccountAccess(null, null, null, null, null), false, LocalDate.now(), 4, LocalDate.now(), ConsentStatus.VALID, false, false, null, null, AisConsentRequestType.GLOBAL);
        return isEmpty(accountConsent)
                   ? ResponseObject.<AccountConsent>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.RESOURCE_UNKNOWN_404))).build()
                   : ResponseObject.<AccountConsent>builder().body(accountConsent).build();
    }

    private ResponseObject<ConsentInformationResponse200Json> getConsentInformationResponse(String consentId) {
        if (consentId.equals(WRONG_CONSENT_ID)) {
            return ResponseObject.<ConsentInformationResponse200Json>builder()
                       .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR,
                                                                        MessageErrorCode.RESOURCE_UNKNOWN_404)))
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

    private CreateConsentReq getCreateConsentReq() {
        CreateConsentReq req = new CreateConsentReq();
        Xs2aAccountAccess access = new Xs2aAccountAccess(Collections.emptyList(), Collections.emptyList(),
                                                         Collections.emptyList(), Xs2aAccountAccessType.ALL_ACCOUNTS, Xs2aAccountAccessType.ALL_ACCOUNTS);
        req.setAccess(access);
        return req;
    }

    private Consents getConsents() {
        Consents consents = new Consents();
        AccountAccess access = new AccountAccess();
        consents.setAccess(access);
        return consents;
    }

    private CreateConsentAuthorizationResponse getCreateConsentAuthorizationResponse(String consentId) {
        CreateConsentAuthorizationResponse response = new CreateConsentAuthorizationResponse();
        response.setConsentId(consentId);
        response.setAuthorizationId(AUTHORISATION_ID);
        response.setScaStatus(ScaStatus.STARTED);
        return response;
    }

    private ScaStatusResponse buildScaStatusResponse(de.adorsys.psd2.model.ScaStatus scaStatus) {
        return new ScaStatusResponse().scaStatus(scaStatus);
    }

    private ResponseObject<ScaStatus> buildScaStatusError() {
        return ResponseObject.<ScaStatus>builder()
                   .fail(new MessageError(MessageErrorCode.RESOURCE_UNKNOWN_403))
                   .build();
    }
}

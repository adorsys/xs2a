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

package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.consent.api.ActionStatus;
import de.adorsys.aspsp.xs2a.consent.api.AisConsentRequestType;
import de.adorsys.aspsp.xs2a.consent.api.CmsConsentStatus;
import de.adorsys.aspsp.xs2a.consent.api.ConsentActionRequest;
import de.adorsys.aspsp.xs2a.consent.api.ais.*;
import de.adorsys.aspsp.xs2a.service.AisConsentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AisConsentControllerTest {
    private static final String EXISTING_CONSENT_ID = "139c5d0e-0c03-473b-b047-30fd66d869db";
    private static final String NON_EXISTING_CONSENT_ID = "139c5d0e-0c03-473b-b047-30fd66d869db";
    private static final String TPP_ID = "testTPP";
    private static final String PSU_ID = "testPSU";
    private static final LocalDate DATE = LocalDate.parse("2020-10-10");
    private static final byte[] CONSENT_DATA = "consent data".getBytes();
    private static final int FREQUENCY_PER_DAY = 4;

    @InjectMocks
    private AisConsentController aisConsentController;
    @Mock
    private AisConsentService aisConsentService;

    @Test
    public void createConsent_Success() {
        when(aisConsentService.createConsent(any())).thenReturn(Optional.of(EXISTING_CONSENT_ID));

        // Given
        CreateAisConsentRequest request = getCreateAisConsentRequest();
        CreateAisConsentResponse expectedResponse = getCreateAisConsentResponse();

        // When
        ResponseEntity<CreateAisConsentResponse> result = aisConsentController.createConsent(request);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    public void createConsent_Failure() {
        when(aisConsentService.createConsent(any())).thenReturn(Optional.empty());

        // Given
        CreateAisConsentRequest request = getCreateAisConsentRequest();

        // When
        ResponseEntity<CreateAisConsentResponse> result = aisConsentController.createConsent(request);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getBody()).isNull();
    }

    @Test
    public void saveConsentActionLog() {
        // Given
        ConsentActionRequest request = new ConsentActionRequest(TPP_ID, EXISTING_CONSENT_ID, ActionStatus.SUCCESS);

        // When
        ResponseEntity<Void> result = aisConsentController.saveConsentActionLog(request);

        // Then
        verify(aisConsentService, times(1)).checkConsentAndSaveActionLog(request);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNull();
    }

    @Test
    public void getConsentById_Success() {
        AisAccountConsent consent = getAisAccountConsent();
        when(aisConsentService.getAisAccountConsentById(any()))
            .thenReturn(Optional.of(consent));

        // When
        ResponseEntity<AisAccountConsent> result = aisConsentController.getConsentById(EXISTING_CONSENT_ID);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(getAisAccountConsent());
    }

    @Test
    public void getConsentById_Failure() {
        when(aisConsentService.getAisAccountConsentById(eq(NON_EXISTING_CONSENT_ID))).thenReturn(Optional.empty());

        // When
        ResponseEntity<AisAccountConsent> result = aisConsentController.getConsentById(NON_EXISTING_CONSENT_ID);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getBody()).isNull();
    }

    @Test
    public void updateAccountAccess_Success() {
        when(aisConsentService.updateAccountAccess(eq(EXISTING_CONSENT_ID), any()))
            .thenReturn(Optional.of(EXISTING_CONSENT_ID));

        // Given
        AisAccountAccessInfo request = getAisAccountAccessInfo();
        CreateAisConsentResponse expectedResponse = getCreateAisConsentResponse();

        // When
        ResponseEntity<CreateAisConsentResponse> result =
            aisConsentController.updateAccountAccess(EXISTING_CONSENT_ID, request);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    public void updateAccountAccess_Failure() {
        when(aisConsentService.updateAccountAccess(eq(NON_EXISTING_CONSENT_ID), any())).thenReturn(Optional.empty());

        // Given
        AisAccountAccessInfo request = getAisAccountAccessInfo();

        // When
        ResponseEntity<CreateAisConsentResponse> result =
            aisConsentController.updateAccountAccess(NON_EXISTING_CONSENT_ID, request);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isNull();
    }

    @Test
    public void getAspspConsentData_Success() {
        when(aisConsentService.getAspspConsentData(EXISTING_CONSENT_ID))
            .thenReturn(Optional.of(getAisConsentAspspDataResponse()));

        // Given
        AisConsentAspspDataResponse expectedResponse = getAisConsentAspspDataResponse();

        // When
        ResponseEntity<AisConsentAspspDataResponse> result =
            aisConsentController.getAspspConsentData(EXISTING_CONSENT_ID);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    public void getAspspConsentData_Failure() {
        when(aisConsentService.getAspspConsentData(NON_EXISTING_CONSENT_ID)).thenReturn(Optional.empty());

        // When
        ResponseEntity<AisConsentAspspDataResponse> result =
            aisConsentController.getAspspConsentData(NON_EXISTING_CONSENT_ID);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isNull();
    }

    private CreateAisConsentRequest getCreateAisConsentRequest() {
        CreateAisConsentRequest request = new CreateAisConsentRequest();
        request.setPsuId(PSU_ID);
        request.setTppId(TPP_ID);
        request.setFrequencyPerDay(FREQUENCY_PER_DAY);
        request.setAccess(getAisAccountAccessInfo());
        request.setValidUntil(DATE);
        request.setRecurringIndicator(false);
        request.setTppRedirectPreferred(false);
        request.setCombinedServiceIndicator(false);
        request.setAspspConsentData(CONSENT_DATA);
        return request;
    }

    private AisAccountAccessInfo getAisAccountAccessInfo() {
        AisAccountAccessInfo info = new AisAccountAccessInfo();
        info.setAccounts(Collections.emptyList());
        info.setBalances(Collections.emptyList());
        info.setTransactions(Collections.emptyList());
        info.setAvailableAccounts(AccountAccessType.ALL_ACCOUNTS);
        return info;
    }

    private CreateAisConsentResponse getCreateAisConsentResponse() {
        return new CreateAisConsentResponse(EXISTING_CONSENT_ID);
    }

    private AisAccountConsent getAisAccountConsent() {
        AisAccountAccess accessInfo = new AisAccountAccess(Collections.emptyList(), Collections.emptyList(),
            Collections.emptyList());
        return new AisAccountConsent(EXISTING_CONSENT_ID, accessInfo, false, DATE, FREQUENCY_PER_DAY,
            DATE, CmsConsentStatus.RECEIVED, false, false, CONSENT_DATA,
            AisConsentRequestType.GLOBAL, PSU_ID, TPP_ID);
    }

    private AisConsentAspspDataResponse getAisConsentAspspDataResponse() {
        AisConsentAspspDataResponse response = new AisConsentAspspDataResponse();
        response.setAspspConsentDataBase64(Base64.getEncoder().encodeToString(CONSENT_DATA));
        return response;
    }
}

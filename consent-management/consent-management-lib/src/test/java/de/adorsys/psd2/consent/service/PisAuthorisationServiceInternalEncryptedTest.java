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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.*;
import de.adorsys.psd2.consent.api.service.PisAuthorisationService;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PisAuthorisationServiceInternalEncryptedTest {
    private static final ScaStatus SCA_STATUS = ScaStatus.SCAMETHODSELECTED;
    private static final String ENCRYPTED_PAYMENT_ID = "encrypted payment id";
    private static final String DECRYPTED_PAYMENT_ID = "1856e4fa-8af8-427b-85ec-4caf515ce074";
    private static final String AUTHORISATION_METHOD_ID = "33346cb3-a01b-4196-a6b9-40b0e4cd2639";
    private static final String AUTHORISATION_ID = "46f2e3a7-1855-4815-8755-5ca76769a1a4";
    private static final PsuIdData PSU_DATA = new PsuIdData(null, null, null, null, null);
    private static final String TPP_REDIRECT_URI = "request/redirect_uri";
    private static final String TPP_NOK_REDIRECT_URI = "request/nok_redirect_uri";
    private static final TppRedirectUri TPP_REDIRECT_URIs = new TppRedirectUri(TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI);
    private static final CreatePisAuthorisationRequest CREATE_PIS_AUTHORISATION_REQUEST = new CreatePisAuthorisationRequest(PaymentAuthorisationType.CREATED, PSU_DATA, ScaApproach.REDIRECT, TPP_REDIRECT_URIs);
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";
    private static final String CANCELLATION_INTERNAL_REQUEST_ID = "5b8d8b12-9363-4d9e-9b7e-2219cbcfc311";

    @InjectMocks
    private PisAuthorisationServiceInternalEncrypted pisAuthorisationServiceInternalEncrypted;
    @Mock
    private PisAuthorisationService pisAuthorisationService;
    @Mock
    private SecurityDataService securityDataService;

    @Test
    void createAuthorization_success() {
        // Given
        CreatePisAuthorisationResponse expected = buildCreatePisAuthorisationResponse();
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID))
            .thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(pisAuthorisationService.createAuthorization(DECRYPTED_PAYMENT_ID, CREATE_PIS_AUTHORISATION_REQUEST))
            .thenReturn(CmsResponse.<CreatePisAuthorisationResponse>builder()
                            .payload(buildCreatePisAuthorisationResponse())
                            .build());

        // When
        CmsResponse<CreatePisAuthorisationResponse> actual =
            pisAuthorisationServiceInternalEncrypted.createAuthorization(ENCRYPTED_PAYMENT_ID, CREATE_PIS_AUTHORISATION_REQUEST);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(expected, actual.getPayload());
        verify(pisAuthorisationService, times(1))
            .createAuthorization(DECRYPTED_PAYMENT_ID, CREATE_PIS_AUTHORISATION_REQUEST);
    }

    @Test
    void createAuthorization_technicalError() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.empty());

        CmsResponse<CreatePisAuthorisationResponse> actual =
            pisAuthorisationServiceInternalEncrypted.createAuthorization(ENCRYPTED_PAYMENT_ID, CREATE_PIS_AUTHORISATION_REQUEST);

        assertTrue(actual.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
    }

    @Test
    void createAuthorizationCancellation_success() {
        // Given
        CreatePisAuthorisationResponse expected = buildCreatePisAuthorisationResponse();
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID))
            .thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(pisAuthorisationService.createAuthorizationCancellation(DECRYPTED_PAYMENT_ID, CREATE_PIS_AUTHORISATION_REQUEST))
            .thenReturn(CmsResponse.<CreatePisAuthorisationResponse>builder()
                            .payload(buildCreatePisAuthorisationResponse())
                            .build());

        // When
        CmsResponse<CreatePisAuthorisationResponse> actual =
            pisAuthorisationServiceInternalEncrypted.createAuthorizationCancellation(ENCRYPTED_PAYMENT_ID, CREATE_PIS_AUTHORISATION_REQUEST);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(expected, actual.getPayload());
        verify(pisAuthorisationService, times(1))
            .createAuthorizationCancellation(DECRYPTED_PAYMENT_ID, CREATE_PIS_AUTHORISATION_REQUEST);
    }

    @Test
    void createAuthorizationCancellation_technicalError() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.empty());

        CmsResponse<CreatePisAuthorisationResponse> actual =
            pisAuthorisationServiceInternalEncrypted.createAuthorizationCancellation(ENCRYPTED_PAYMENT_ID, CREATE_PIS_AUTHORISATION_REQUEST);

        assertTrue(actual.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
    }

    @Test
    void updatePisAuthorisation_success() {
        // Given
        UpdatePisCommonPaymentPsuDataRequest request = buildUpdatePisCommonPaymentPsuDataRequest();
        UpdatePisCommonPaymentPsuDataResponse expected = buildUpdatePisCommonPaymentPsuDataResponse();
        when(pisAuthorisationService.updatePisAuthorisation(AUTHORISATION_ID, buildUpdatePisCommonPaymentPsuDataRequest()))
            .thenReturn(CmsResponse.<UpdatePisCommonPaymentPsuDataResponse>builder()
                            .payload(buildUpdatePisCommonPaymentPsuDataResponse())
                            .build());

        // When
        CmsResponse<UpdatePisCommonPaymentPsuDataResponse> actual = pisAuthorisationServiceInternalEncrypted.updatePisAuthorisation(AUTHORISATION_ID, request);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(expected, actual.getPayload());
        verify(pisAuthorisationService, times(1))
            .updatePisAuthorisation(AUTHORISATION_ID, request);
    }

    @Test
    void updatePisCancellationAuthorisation_success() {
        // Given
        UpdatePisCommonPaymentPsuDataRequest request = buildUpdatePisCommonPaymentPsuDataRequest();
        UpdatePisCommonPaymentPsuDataResponse expected = buildUpdatePisCommonPaymentPsuDataResponse();

        when(pisAuthorisationService.updatePisCancellationAuthorisation(AUTHORISATION_ID, buildUpdatePisCommonPaymentPsuDataRequest()))
            .thenReturn(CmsResponse.<UpdatePisCommonPaymentPsuDataResponse>builder()
                            .payload(buildUpdatePisCommonPaymentPsuDataResponse())
                            .build());

        // When
        CmsResponse<UpdatePisCommonPaymentPsuDataResponse> actual =
            pisAuthorisationServiceInternalEncrypted.updatePisCancellationAuthorisation(AUTHORISATION_ID, request);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(expected, actual.getPayload());
        verify(pisAuthorisationService, times(1))
            .updatePisCancellationAuthorisation(AUTHORISATION_ID, request);
    }

    @Test
    void getPisAuthorisationById_success() {
        // Given
        GetPisAuthorisationResponse expected = buildGetPisAuthorisationResponse();
        when(pisAuthorisationService.getPisAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<GetPisAuthorisationResponse>builder()
                            .payload(buildGetPisAuthorisationResponse())
                            .build());

        // When
        CmsResponse<GetPisAuthorisationResponse> actual =
            pisAuthorisationServiceInternalEncrypted.getPisAuthorisationById(AUTHORISATION_ID);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(expected, actual.getPayload());
        verify(pisAuthorisationService, times(1)).getPisAuthorisationById(AUTHORISATION_ID);
    }

    @Test
    void getPisCancellationAuthorisationById_success() {
        // Given
        GetPisAuthorisationResponse expected = buildGetPisAuthorisationResponse();
        when(pisAuthorisationService.getPisCancellationAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<GetPisAuthorisationResponse>builder()
                            .payload(buildGetPisAuthorisationResponse())
                            .build());

        // When
        CmsResponse<GetPisAuthorisationResponse> actual =
            pisAuthorisationServiceInternalEncrypted.getPisCancellationAuthorisationById(AUTHORISATION_ID);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(expected, actual.getPayload());
        verify(pisAuthorisationService, times(1))
            .getPisCancellationAuthorisationById(AUTHORISATION_ID);
    }

    @Test
    void getAuthorisationsByPaymentId_success() {
        // Given
        List<String> expected = buildPaymentAuthorisations();
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID))
            .thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(pisAuthorisationService.getAuthorisationsByPaymentId(DECRYPTED_PAYMENT_ID, PaymentAuthorisationType.CREATED))
            .thenReturn(CmsResponse.<List<String>>builder()
                            .payload(buildPaymentAuthorisations())
                            .build());

        // When
        CmsResponse<List<String>> actual = pisAuthorisationServiceInternalEncrypted.getAuthorisationsByPaymentId(ENCRYPTED_PAYMENT_ID,
                                                                                                                 PaymentAuthorisationType.CREATED);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(expected, actual.getPayload());
        verify(pisAuthorisationService, times(1))
            .getAuthorisationsByPaymentId(DECRYPTED_PAYMENT_ID, PaymentAuthorisationType.CREATED);
    }

    @Test
    void getAuthorisationsByPaymentId_technicalError() {
        // Given
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.empty());

        // When
        CmsResponse<List<String>> actual = pisAuthorisationServiceInternalEncrypted.getAuthorisationsByPaymentId(ENCRYPTED_PAYMENT_ID,
                                                                                                                 PaymentAuthorisationType.CREATED);

        // Then
        assertTrue(actual.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());

        verify(pisAuthorisationService, never()).getAuthorisationsByPaymentId(DECRYPTED_PAYMENT_ID, PaymentAuthorisationType.CREATED);
    }

    @Test
    void getAuthorisationScaStatus_success() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID))
            .thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(pisAuthorisationService.getAuthorisationScaStatus(DECRYPTED_PAYMENT_ID, AUTHORISATION_ID, PaymentAuthorisationType.CREATED))
            .thenReturn(CmsResponse.<ScaStatus>builder()
                            .payload(SCA_STATUS)
                            .build());
        // When
        CmsResponse<ScaStatus> actual = pisAuthorisationServiceInternalEncrypted.getAuthorisationScaStatus(ENCRYPTED_PAYMENT_ID, AUTHORISATION_ID, PaymentAuthorisationType.CREATED);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(SCA_STATUS, actual.getPayload());
        verify(pisAuthorisationService, times(1)).getAuthorisationScaStatus(DECRYPTED_PAYMENT_ID, AUTHORISATION_ID, PaymentAuthorisationType.CREATED);
    }

    @Test
    void getAuthorisationScaStatus_technicalError() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.empty());
        // When
        CmsResponse<ScaStatus> actual = pisAuthorisationServiceInternalEncrypted.getAuthorisationScaStatus(ENCRYPTED_PAYMENT_ID, AUTHORISATION_ID, PaymentAuthorisationType.CREATED);

        // Then
        assertTrue(actual.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());

        verify(pisAuthorisationService, never()).getAuthorisationScaStatus(DECRYPTED_PAYMENT_ID, AUTHORISATION_ID, PaymentAuthorisationType.CREATED);
    }

    @Test
    void getAuthorisationScaApproach() {
        when(pisAuthorisationService.getAuthorisationScaApproach(AUTHORISATION_ID, PaymentAuthorisationType.CREATED))
            .thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                            .payload(new AuthorisationScaApproachResponse(ScaApproach.EMBEDDED))
                            .build());

        CmsResponse<AuthorisationScaApproachResponse> actual = pisAuthorisationServiceInternalEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID, PaymentAuthorisationType.CREATED);

        assertTrue(actual.isSuccessful());

        assertEquals(ScaApproach.EMBEDDED, actual.getPayload().getScaApproach());
        verify(pisAuthorisationService, times(1)).getAuthorisationScaApproach(eq(AUTHORISATION_ID), eq(PaymentAuthorisationType.CREATED));
    }

    @Test
    void updatePisAuthorisationStatus() {
        pisAuthorisationServiceInternalEncrypted.updatePisAuthorisationStatus(AUTHORISATION_ID, ScaStatus.RECEIVED);

        verify(pisAuthorisationService).updatePisAuthorisationStatus(AUTHORISATION_ID, ScaStatus.RECEIVED);
    }

    @Test
    void isAuthenticationMethodDecoupled() {
        pisAuthorisationServiceInternalEncrypted.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHORISATION_METHOD_ID);

        verify(pisAuthorisationService).isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHORISATION_METHOD_ID);
    }

    @Test
    void saveAuthenticationMethods() {
        pisAuthorisationServiceInternalEncrypted.saveAuthenticationMethods(AUTHORISATION_ID, Collections.emptyList());

        verify(pisAuthorisationService).saveAuthenticationMethods(AUTHORISATION_ID, Collections.emptyList());
    }

    @Test
    void updateScaApproach() {
        pisAuthorisationServiceInternalEncrypted.updateScaApproach(AUTHORISATION_ID, ScaApproach.EMBEDDED);

        verify(pisAuthorisationService).updateScaApproach(AUTHORISATION_ID, ScaApproach.EMBEDDED);
    }

    private CreatePisAuthorisationResponse buildCreatePisAuthorisationResponse() {
        return new CreatePisAuthorisationResponse(AUTHORISATION_ID, ScaStatus.RECEIVED, INTERNAL_REQUEST_ID, CANCELLATION_INTERNAL_REQUEST_ID, null);
    }

    private List<String> buildPaymentAuthorisations() {
        return Collections.singletonList(AUTHORISATION_ID);
    }

    private GetPisAuthorisationResponse buildGetPisAuthorisationResponse() {
        return new GetPisAuthorisationResponse();
    }

    private UpdatePisCommonPaymentPsuDataRequest buildUpdatePisCommonPaymentPsuDataRequest() {
        UpdatePisCommonPaymentPsuDataRequest request = new UpdatePisCommonPaymentPsuDataRequest();
        request.setAuthorizationId(AUTHORISATION_ID);
        return request;
    }

    private UpdatePisCommonPaymentPsuDataResponse buildUpdatePisCommonPaymentPsuDataResponse() {
        return new UpdatePisCommonPaymentPsuDataResponse(SCA_STATUS);
    }
}

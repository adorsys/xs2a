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


package de.adorsys.psd2.xs2a.service.authorization.pis;

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.service.PisAuthorisationServiceEncrypted;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationChainResponsibilityService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.web.mapper.TppRedirectUriMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PisAuthorisationServiceTest {
    private static final String PAYMENT_ID = "c713a32c-15ff-4f90-afa0-34a500359844";
    private static final String WRONG_PAYMENT_ID = "wrong payment id";
    private static final String AUTHORISATION_ID = "ad746cb3-a01b-4196-a6b9-40b0e4cd2350";
    private static final String WRONG_AUTHORISATION_ID = "wrong authorisation id";
    private static final String CANCELLATION_AUTHORISATION_ID = "dd5d766f-eeb7-4efe-b730-24d5ed53f537";
    private static final String WRONG_CANCELLATION_AUTHORISATION_ID = "wrong cancellation authorisation id";
    private static final String TPP_REDIRECT_URI = "request/redirect_uri";
    private static final String TPP_NOK_REDIRECT_URI = "request/nok_redirect_uri";
    private static final TppRedirectUri TPP_REDIRECT_URIs = new TppRedirectUri(TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI);

    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final List<String> SOME_LIST = Collections.emptyList();
    private static final ScaApproach SCA_APPROACH = ScaApproach.OAUTH;
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    private static final CreatePisAuthorisationRequest CREATE_PIS_AUTHORISATION_REQUEST_CANCELLED = buildCreatePisAuthorisationRequestCancelled();
    private static final CreatePisAuthorisationRequest CREATE_PIS_AUTHORISATION_REQUEST_CREATED = buildCreatePisAuthorisationRequestCreated();
    private static final CreatePisAuthorisationResponse CREATE_PIS_AUTHORISATION_RESPONSE = new CreatePisAuthorisationResponse(AUTHORISATION_ID, SCA_STATUS, null, null, null);
    private static final Xs2aUpdatePisCommonPaymentPsuDataRequest XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST = buildXs2aUpdatePisCommonPaymentPsuDataRequest();
    private static final GetPisAuthorisationResponse GET_PIS_AUTHORISATION_RESPONSE = buildGetPisAuthorisationResponse();
    private static final Xs2aUpdatePisCommonPaymentPsuDataResponse STAGE_RESPONSE = new Xs2aUpdatePisCommonPaymentPsuDataResponse(SCA_STATUS, PAYMENT_ID, AUTHORISATION_ID, PSU_ID_DATA);

    @InjectMocks
    private PisAuthorisationService pisAuthorisationService;

    @Mock
    private PisAuthorisationServiceEncrypted pisAuthorisationServiceEncrypted;
    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private TppRedirectUriMapper tppRedirectUriMapper;
    @Mock
    private AuthorisationChainResponsibilityService authorisationChainResponsibilityService;


    @Before
    public void setUp() {
        when(pisAuthorisationServiceEncrypted.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID, PaymentAuthorisationType.CREATED))
            .thenReturn(buildSuccessfulGetAuthorisationStatusResponse(SCA_STATUS));
        when(pisAuthorisationServiceEncrypted.getAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_AUTHORISATION_ID, PaymentAuthorisationType.CREATED))
            .thenReturn(buildErrorfulGetAuthorisationStatusResponse());
        when(pisAuthorisationServiceEncrypted.getAuthorisationScaStatus(PAYMENT_ID, CANCELLATION_AUTHORISATION_ID, PaymentAuthorisationType.CANCELLED))
            .thenReturn(buildSuccessfulGetAuthorisationStatusResponse(SCA_STATUS));
        when(pisAuthorisationServiceEncrypted.getAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_CANCELLATION_AUTHORISATION_ID, PaymentAuthorisationType.CANCELLED))
            .thenReturn(buildErrorfulGetAuthorisationStatusResponse());
        when(requestProviderService.getRequestId()).thenReturn(UUID.randomUUID());
    }

    @Test
    public void createPisAuthorisation_success() {
        // Given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(SCA_APPROACH);
        when(pisAuthorisationServiceEncrypted.createAuthorization(PAYMENT_ID, CREATE_PIS_AUTHORISATION_REQUEST_CREATED))
            .thenReturn(CmsResponse.<CreatePisAuthorisationResponse>builder().payload(CREATE_PIS_AUTHORISATION_RESPONSE).build());
        when(requestProviderService.getTppRedirectURI())
            .thenReturn(TPP_REDIRECT_URI);
        when(requestProviderService.getTppNokRedirectURI())
            .thenReturn(TPP_NOK_REDIRECT_URI);
        when(tppRedirectUriMapper.mapToTppRedirectUri(TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI))
            .thenReturn(new TppRedirectUri(TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI));

        // When
        CreatePisAuthorisationResponse actualResponse = pisAuthorisationService.createPisAuthorisation(PAYMENT_ID, PSU_ID_DATA);

        // Then
        assertThat(actualResponse).isEqualTo(CREATE_PIS_AUTHORISATION_RESPONSE);
    }

    @Test
    public void createPisAuthorisation_wrongCreatePisAuthResponse_fail() {
        // Given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(SCA_APPROACH);
        when(pisAuthorisationServiceEncrypted.createAuthorization(any(), any()))
            .thenReturn(CmsResponse.<CreatePisAuthorisationResponse>builder().error(CmsError.TECHNICAL_ERROR).build());
        when(requestProviderService.getTppRedirectURI())
            .thenReturn(TPP_REDIRECT_URI);
        when(requestProviderService.getTppNokRedirectURI())
            .thenReturn(TPP_NOK_REDIRECT_URI);

        // When
        CreatePisAuthorisationResponse actualResponse = pisAuthorisationService.createPisAuthorisation(WRONG_PAYMENT_ID, PSU_ID_DATA);

        // Then
        assertThat(actualResponse).isNull();
    }

    @Test
    public void updatePisAuthorisation_success() {
        ArgumentCaptor<AuthorisationProcessorRequest> authorisationProcessorRequestCaptor = ArgumentCaptor.forClass(AuthorisationProcessorRequest.class);
        // Given
        when(pisAuthorisationServiceEncrypted.getPisAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<GetPisAuthorisationResponse>builder().payload(GET_PIS_AUTHORISATION_RESPONSE).build());
        when(authorisationChainResponsibilityService.apply(authorisationProcessorRequestCaptor.capture())).thenReturn(STAGE_RESPONSE);

        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisAuthorisationService.updatePisAuthorisation(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST, SCA_APPROACH);

        // Then
        assertThat(actualResponse).isEqualTo(STAGE_RESPONSE);

        assertThat(authorisationProcessorRequestCaptor.getValue().getServiceType()).isEqualTo(ServiceType.PIS);
        assertThat(authorisationProcessorRequestCaptor.getValue().getPaymentAuthorisationType()).isEqualTo(PaymentAuthorisationType.CREATED);
        assertThat(authorisationProcessorRequestCaptor.getValue().getUpdateAuthorisationRequest()).isEqualTo(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST);
        assertThat(authorisationProcessorRequestCaptor.getValue().getAuthorisation()).isEqualTo(GET_PIS_AUTHORISATION_RESPONSE);
    }

    @Test
    public void updatePisCancellationAuthorisation_success() {
        ArgumentCaptor<AuthorisationProcessorRequest> authorisationProcessorRequestCaptor = ArgumentCaptor.forClass(AuthorisationProcessorRequest.class);

        // Given
        when(pisAuthorisationServiceEncrypted.getPisCancellationAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<GetPisAuthorisationResponse>builder().payload(GET_PIS_AUTHORISATION_RESPONSE).build());
        when(authorisationChainResponsibilityService.apply(authorisationProcessorRequestCaptor.capture())).thenReturn(STAGE_RESPONSE);

        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisAuthorisationService.updatePisCancellationAuthorisation(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST, SCA_APPROACH);

        // Then
        assertThat(actualResponse).isEqualTo(STAGE_RESPONSE);

        assertThat(authorisationProcessorRequestCaptor.getValue().getServiceType()).isEqualTo(ServiceType.PIS);
        assertThat(authorisationProcessorRequestCaptor.getValue().getPaymentAuthorisationType()).isEqualTo(PaymentAuthorisationType.CANCELLED);
        assertThat(authorisationProcessorRequestCaptor.getValue().getUpdateAuthorisationRequest()).isEqualTo(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST);
        assertThat(authorisationProcessorRequestCaptor.getValue().getAuthorisation()).isEqualTo(GET_PIS_AUTHORISATION_RESPONSE);
    }

    @Test
    public void createPisAuthorisationCancellation_success() {
        // Given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(SCA_APPROACH);
        when(pisAuthorisationServiceEncrypted.createAuthorizationCancellation(PAYMENT_ID, CREATE_PIS_AUTHORISATION_REQUEST_CANCELLED))
            .thenReturn(CmsResponse.<CreatePisAuthorisationResponse>builder().payload(CREATE_PIS_AUTHORISATION_RESPONSE).build());
        when(requestProviderService.getTppRedirectURI())
            .thenReturn(TPP_REDIRECT_URI);
        when(requestProviderService.getTppNokRedirectURI())
            .thenReturn(TPP_NOK_REDIRECT_URI);
        when(tppRedirectUriMapper.mapToTppRedirectUri(TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI))
            .thenReturn(new TppRedirectUri(TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI));

        // When
        CreatePisAuthorisationResponse actualResponse = pisAuthorisationService.createPisAuthorisationCancellation(PAYMENT_ID, PSU_ID_DATA);

        // Then
        assertThat(actualResponse).isEqualTo(CREATE_PIS_AUTHORISATION_RESPONSE);
    }

    @Test
    public void createPisAuthorisationCancellation_wrongId_fail() {
        // Given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(SCA_APPROACH);
        when(pisAuthorisationServiceEncrypted.createAuthorizationCancellation(any(), any()))
            .thenReturn(CmsResponse.<CreatePisAuthorisationResponse>builder().error(CmsError.TECHNICAL_ERROR).build());
        when(requestProviderService.getTppRedirectURI())
            .thenReturn(TPP_REDIRECT_URI);
        when(requestProviderService.getTppNokRedirectURI())
            .thenReturn(TPP_NOK_REDIRECT_URI);

        // When
        CreatePisAuthorisationResponse actualResponse = pisAuthorisationService.createPisAuthorisationCancellation(WRONG_PAYMENT_ID, PSU_ID_DATA);

        // Then
        assertThat(actualResponse).isNull();
    }

    @Test
    public void getCancellationAuthorisationSubResources_success() {
        // Given
        when(pisAuthorisationServiceEncrypted.getAuthorisationsByPaymentId(PAYMENT_ID, PaymentAuthorisationType.CANCELLED))
            .thenReturn(CmsResponse.<List<String>>builder().payload(SOME_LIST).build());

        // When
        Optional<List<String>> actualResponse = pisAuthorisationService.getCancellationAuthorisationSubResources(PAYMENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse).isEqualTo(Optional.of(SOME_LIST));
    }

    @Test
    public void getCancellationAuthorisationSubResources_wrongPaymentId_fail() {
        // Given
        when(pisAuthorisationServiceEncrypted.getAuthorisationsByPaymentId(WRONG_PAYMENT_ID, PaymentAuthorisationType.CANCELLED))
            .thenReturn(CmsResponse.<List<String>>builder().error(CmsError.TECHNICAL_ERROR).build());

        // When
        Optional<List<String>> actualResponse = pisAuthorisationService.getCancellationAuthorisationSubResources(WRONG_PAYMENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void getAuthorisationSubResources_success() {
        // Given
        when(pisAuthorisationServiceEncrypted.getAuthorisationsByPaymentId(PAYMENT_ID, PaymentAuthorisationType.CREATED))
            .thenReturn(CmsResponse.<List<String>>builder().payload(SOME_LIST).build());

        // When
        Optional<List<String>> actualResponse = pisAuthorisationService.getAuthorisationSubResources(PAYMENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse).isEqualTo(Optional.of(SOME_LIST));
    }

    @Test
    public void getAuthorisationSubResources_wrongPaymentId_fail() {
        // Given
        when(pisAuthorisationServiceEncrypted.getAuthorisationsByPaymentId(WRONG_PAYMENT_ID, PaymentAuthorisationType.CREATED))
            .thenReturn(CmsResponse.<List<String>>builder().error(CmsError.TECHNICAL_ERROR).build());

        // When
        Optional<List<String>> actualResponse = pisAuthorisationService.getAuthorisationSubResources(WRONG_PAYMENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void getAuthorisationScaStatus_success() {
        // When
        Optional<ScaStatus> actual = pisAuthorisationService.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(SCA_STATUS, actual.get());
    }

    @Test
    public void getAuthorisationScaStatus_failure_wrongIds() {
        // When
        Optional<ScaStatus> actual = pisAuthorisationService.getAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertFalse(actual.isPresent());
    }

    @Test
    public void getCancellationAuthorisationScaStatus_success() {
        // When
        Optional<ScaStatus> actual = pisAuthorisationService.getCancellationAuthorisationScaStatus(PAYMENT_ID, CANCELLATION_AUTHORISATION_ID);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(SCA_STATUS, actual.get());
    }

    @Test
    public void getCancellationAuthorisationScaStatus_failure_wrongIds() {
        // When
        Optional<ScaStatus> actual = pisAuthorisationService.getCancellationAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_CANCELLATION_AUTHORISATION_ID);

        // Then
        assertFalse(actual.isPresent());
    }

    private static CreatePisAuthorisationRequest buildCreatePisAuthorisationRequestCancelled() {
        return new CreatePisAuthorisationRequest(PaymentAuthorisationType.CANCELLED, PSU_ID_DATA, SCA_APPROACH, TPP_REDIRECT_URIs);
    }

    private static CreatePisAuthorisationRequest buildCreatePisAuthorisationRequestCreated() {
        return new CreatePisAuthorisationRequest(PaymentAuthorisationType.CREATED, PSU_ID_DATA, SCA_APPROACH, TPP_REDIRECT_URIs);
    }

    private static Xs2aUpdatePisCommonPaymentPsuDataRequest buildXs2aUpdatePisCommonPaymentPsuDataRequest() {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
        request.setAuthorisationId(AUTHORISATION_ID);
        return request;
    }

    private static GetPisAuthorisationResponse buildGetPisAuthorisationResponse() {
        GetPisAuthorisationResponse response = new GetPisAuthorisationResponse();
        response.setScaStatus(ScaStatus.RECEIVED);
        return response;
    }

    private CmsResponse<ScaStatus> buildSuccessfulGetAuthorisationStatusResponse(ScaStatus scaStatus) {
        return CmsResponse.<ScaStatus>builder()
                   .payload(scaStatus)
                   .build();
    }

    private CmsResponse<ScaStatus> buildErrorfulGetAuthorisationStatusResponse() {
        return CmsResponse.<ScaStatus>builder()
                   .error(CmsError.TECHNICAL_ERROR)
                   .build();
    }
}

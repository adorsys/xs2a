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


package de.adorsys.psd2.xs2a.service.authorization.pis;

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.authorisation.*;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreateAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationChainResponsibilityService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.web.mapper.TppRedirectUriMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PisAuthorisationServiceTest {
    private static final String PAYMENT_ID = "c713a32c-15ff-4f90-afa0-34a500359844";
    private static final String WRONG_PAYMENT_ID = "wrong payment id";
    private static final String AUTHORISATION_ID = "ad746cb3-a01b-4196-a6b9-40b0e4cd2350";
    private static final String TPP_REDIRECT_URI = "request/redirect_uri";
    private static final String TPP_NOK_REDIRECT_URI = "request/nok_redirect_uri";

    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final ScaApproach SCA_APPROACH = ScaApproach.REDIRECT;
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    private static final CreateAuthorisationResponse CREATE_PIS_AUTHORISATION_RESPONSE = new CreateAuthorisationResponse(AUTHORISATION_ID, SCA_STATUS, null, null, SCA_APPROACH);
    private static final PaymentAuthorisationParameters XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST = buildXs2aUpdatePisCommonPaymentPsuDataRequest();
    private static final Authorisation GET_PIS_AUTHORISATION_RESPONSE = buildGetPisAuthorisationResponse();
    private static final Xs2aUpdatePisCommonPaymentPsuDataResponse STAGE_RESPONSE = new Xs2aUpdatePisCommonPaymentPsuDataResponse(
        SCA_STATUS, PAYMENT_ID, AUTHORISATION_ID, PSU_ID_DATA, null);

    @InjectMocks
    private PisAuthorisationService pisAuthorisationService;

    @Mock
    private AuthorisationServiceEncrypted authorisationServiceEncrypted;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private TppRedirectUriMapper tppRedirectUriMapper;
    @Mock
    private AuthorisationChainResponsibilityService authorisationChainResponsibilityService;
    @Mock
    private Xs2aPisCommonPaymentMapper pisCommonPaymentMapper;

    @Test
    void createPisAuthorisation_success() {
        // Given
        ArgumentCaptor<PisAuthorisationParentHolder> authorisationParentHolderCaptor = ArgumentCaptor.forClass(PisAuthorisationParentHolder.class);

        when(authorisationServiceEncrypted.createAuthorisation(authorisationParentHolderCaptor.capture(), any(CreateAuthorisationRequest.class)))
            .thenReturn(CmsResponse.<CreateAuthorisationResponse>builder().payload(CREATE_PIS_AUTHORISATION_RESPONSE).build());
        when(requestProviderService.getTppRedirectURI()).thenReturn(TPP_REDIRECT_URI);
        when(requestProviderService.getTppNokRedirectURI()).thenReturn(TPP_NOK_REDIRECT_URI);
        when(tppRedirectUriMapper.mapToTppRedirectUri(TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI))
            .thenReturn(new TppRedirectUri(TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI));
        Xs2aCreateAuthorisationRequest xs2aCreateAuthorisationRequest = Xs2aCreateAuthorisationRequest.builder()
                                                                            .paymentId(PAYMENT_ID)
                                                                            .psuData(PSU_ID_DATA)
                                                                            .scaStatus(SCA_STATUS)
                                                                            .scaApproach(SCA_APPROACH)
                                                                            .authorisationId(AUTHORISATION_ID)
                                                                            .build();
        // When
        CreateAuthorisationResponse actualResponse = pisAuthorisationService.createPisAuthorisation(xs2aCreateAuthorisationRequest);

        // Then
        assertThat(actualResponse).isEqualTo(CREATE_PIS_AUTHORISATION_RESPONSE);

        assertThat(authorisationParentHolderCaptor.getValue().getParentId()).isEqualTo(PAYMENT_ID);
        assertThat(authorisationParentHolderCaptor.getValue().getAuthorisationType()).isEqualTo(AuthorisationType.PIS_CREATION);
    }

    @Test
    void createPisAuthorisation_wrongCreatePisAuthResponse_fail() {
        // Given
        ArgumentCaptor<PisAuthorisationParentHolder> authorisationParentHolderCaptor = ArgumentCaptor.forClass(PisAuthorisationParentHolder.class);

        when(authorisationServiceEncrypted.createAuthorisation(authorisationParentHolderCaptor.capture(), any(CreateAuthorisationRequest.class)))
            .thenReturn(CmsResponse.<CreateAuthorisationResponse>builder().error(CmsError.TECHNICAL_ERROR).build());
        when(requestProviderService.getTppRedirectURI())
            .thenReturn(TPP_REDIRECT_URI);
        when(requestProviderService.getTppNokRedirectURI())
            .thenReturn(TPP_NOK_REDIRECT_URI);
        Xs2aCreateAuthorisationRequest xs2aCreateAuthorisationRequest = Xs2aCreateAuthorisationRequest.builder()
                                                                            .paymentId(WRONG_PAYMENT_ID)
                                                                            .psuData(PSU_ID_DATA)
                                                                            .scaStatus(SCA_STATUS)
                                                                            .scaApproach(SCA_APPROACH)
                                                                            .authorisationId(AUTHORISATION_ID)
                                                                            .build();
        // When
        CreateAuthorisationResponse actualResponse = pisAuthorisationService.createPisAuthorisation(xs2aCreateAuthorisationRequest);

        // Then
        assertThat(actualResponse).isNull();

        assertThat(authorisationParentHolderCaptor.getValue().getParentId()).isEqualTo(WRONG_PAYMENT_ID);
        assertThat(authorisationParentHolderCaptor.getValue().getAuthorisationType()).isEqualTo(AuthorisationType.PIS_CREATION);
    }

    @Test
    void updatePisAuthorisation_success() {
        ArgumentCaptor<AuthorisationProcessorRequest> authorisationProcessorRequestCaptor = ArgumentCaptor.forClass(AuthorisationProcessorRequest.class);
        // Given
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder().payload(GET_PIS_AUTHORISATION_RESPONSE).build());
        when(authorisationChainResponsibilityService.apply(authorisationProcessorRequestCaptor.capture())).thenReturn(STAGE_RESPONSE);

        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisAuthorisationService.updatePisAuthorisation(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST, SCA_APPROACH);

        // Then
        assertThat(actualResponse).isEqualTo(STAGE_RESPONSE);

        assertThat(authorisationProcessorRequestCaptor.getValue().getServiceType()).isEqualTo(ServiceType.PIS);
        assertThat(authorisationProcessorRequestCaptor.getValue().getUpdateAuthorisationRequest()).isEqualTo(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST);
        assertThat(authorisationProcessorRequestCaptor.getValue().getAuthorisation()).isEqualTo(GET_PIS_AUTHORISATION_RESPONSE);
    }

    @Test
    void updatePisCancellationAuthorisation_success() {
        ArgumentCaptor<AuthorisationProcessorRequest> authorisationProcessorRequestCaptor = ArgumentCaptor.forClass(AuthorisationProcessorRequest.class);

        // Given
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder().payload(GET_PIS_AUTHORISATION_RESPONSE).build());
        when(authorisationChainResponsibilityService.apply(authorisationProcessorRequestCaptor.capture())).thenReturn(STAGE_RESPONSE);

        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisAuthorisationService.updatePisCancellationAuthorisation(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST, SCA_APPROACH);

        // Then
        assertThat(actualResponse).isEqualTo(STAGE_RESPONSE);

        assertThat(authorisationProcessorRequestCaptor.getValue().getServiceType()).isEqualTo(ServiceType.PIS);
        assertThat(authorisationProcessorRequestCaptor.getValue().getUpdateAuthorisationRequest()).isEqualTo(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST);
        assertThat(authorisationProcessorRequestCaptor.getValue().getAuthorisation()).isEqualTo(GET_PIS_AUTHORISATION_RESPONSE);
    }

    @Test
    void createPisAuthorisationCancellation_success() {
        // Given
        ArgumentCaptor<PisCancellationAuthorisationParentHolder> authorisationParentHolderCaptor = ArgumentCaptor.forClass(PisCancellationAuthorisationParentHolder.class);

        when(authorisationServiceEncrypted.createAuthorisation(authorisationParentHolderCaptor.capture(), any(CreateAuthorisationRequest.class)))
            .thenReturn(CmsResponse.<CreateAuthorisationResponse>builder().payload(CREATE_PIS_AUTHORISATION_RESPONSE).build());
        when(requestProviderService.getTppRedirectURI()).thenReturn(TPP_REDIRECT_URI);
        when(requestProviderService.getTppNokRedirectURI()).thenReturn(TPP_NOK_REDIRECT_URI);
        when(tppRedirectUriMapper.mapToTppRedirectUri(TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI))
            .thenReturn(new TppRedirectUri(TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI));
        Xs2aCreateAuthorisationRequest xs2aCreateAuthorisationRequest = Xs2aCreateAuthorisationRequest.builder()
                                                                            .paymentId(PAYMENT_ID)
                                                                            .psuData(PSU_ID_DATA)
                                                                            .scaStatus(SCA_STATUS)
                                                                            .scaApproach(SCA_APPROACH)
                                                                            .authorisationId(AUTHORISATION_ID)
                                                                            .build();
        // When
        CreateAuthorisationResponse actualResponse = pisAuthorisationService.createPisAuthorisationCancellation(xs2aCreateAuthorisationRequest);

        // Then
        assertThat(actualResponse).isEqualTo(CREATE_PIS_AUTHORISATION_RESPONSE);
        assertThat(authorisationParentHolderCaptor.getValue().getParentId()).isEqualTo(PAYMENT_ID);
        assertThat(authorisationParentHolderCaptor.getValue().getAuthorisationType()).isEqualTo(AuthorisationType.PIS_CANCELLATION);
    }

    @Test
    void createPisAuthorisationCancellation_wrongId_fail() {
        // Given
        when(authorisationServiceEncrypted.createAuthorisation(any(), any()))
            .thenReturn(CmsResponse.<CreateAuthorisationResponse>builder().error(CmsError.TECHNICAL_ERROR).build());
        when(requestProviderService.getTppRedirectURI())
            .thenReturn(TPP_REDIRECT_URI);
        when(requestProviderService.getTppNokRedirectURI())
            .thenReturn(TPP_NOK_REDIRECT_URI);
        Xs2aCreateAuthorisationRequest xs2aCreateAuthorisationRequest = Xs2aCreateAuthorisationRequest.builder()
                                                                            .paymentId(WRONG_PAYMENT_ID)
                                                                            .psuData(PSU_ID_DATA)
                                                                            .scaStatus(SCA_STATUS)
                                                                            .scaApproach(SCA_APPROACH)
                                                                            .authorisationId(AUTHORISATION_ID)
                                                                            .build();
        // When
        CreateAuthorisationResponse actualResponse = pisAuthorisationService.createPisAuthorisationCancellation(xs2aCreateAuthorisationRequest);

        // Then
        assertThat(actualResponse).isNull();
    }

    @Test
    void getCancellationAuthorisationSubResources() {
        ArgumentCaptor<PisCancellationAuthorisationParentHolder> authorisationParentHolderCaptor = ArgumentCaptor.forClass(PisCancellationAuthorisationParentHolder.class);
        when(authorisationServiceEncrypted.getAuthorisationsByParentId(any(PisCancellationAuthorisationParentHolder.class)))
            .thenReturn(CmsResponse.<List<String>>builder().payload(Collections.emptyList()).build());

        Assertions.assertEquals(Optional.of(Collections.emptyList()), pisAuthorisationService.getCancellationAuthorisationSubResources(PAYMENT_ID));

        verify(authorisationServiceEncrypted, times(1)).getAuthorisationsByParentId(authorisationParentHolderCaptor.capture());
        Assertions.assertEquals(PAYMENT_ID, authorisationParentHolderCaptor.getValue().getParentId());
    }

    @Test
    void getCancellationAuthorisationSubResources_error() {
        ArgumentCaptor<PisCancellationAuthorisationParentHolder> authorisationParentHolderCaptor = ArgumentCaptor.forClass(PisCancellationAuthorisationParentHolder.class);
        when(authorisationServiceEncrypted.getAuthorisationsByParentId(any(PisCancellationAuthorisationParentHolder.class)))
            .thenReturn(CmsResponse.<List<String>>builder().error(CmsError.TECHNICAL_ERROR).build());

        Assertions.assertEquals(Optional.empty(), pisAuthorisationService.getCancellationAuthorisationSubResources(PAYMENT_ID));

        verify(authorisationServiceEncrypted, times(1)).getAuthorisationsByParentId(authorisationParentHolderCaptor.capture());
        Assertions.assertEquals(PAYMENT_ID, authorisationParentHolderCaptor.getValue().getParentId());
    }

    @Test
    void getAuthorisationSubResources() {
        ArgumentCaptor<PisAuthorisationParentHolder> authorisationParentHolderCaptor = ArgumentCaptor.forClass(PisAuthorisationParentHolder.class);
        when(authorisationServiceEncrypted.getAuthorisationsByParentId(any(PisAuthorisationParentHolder.class)))
            .thenReturn(CmsResponse.<List<String>>builder().payload(Collections.emptyList()).build());

        Assertions.assertEquals(Optional.of(Collections.emptyList()), pisAuthorisationService.getAuthorisationSubResources(PAYMENT_ID));

        verify(authorisationServiceEncrypted, times(1)).getAuthorisationsByParentId(authorisationParentHolderCaptor.capture());
        Assertions.assertEquals(PAYMENT_ID, authorisationParentHolderCaptor.getValue().getParentId());
    }

    @Test
    void getAuthorisationSubResources_error() {
        ArgumentCaptor<PisAuthorisationParentHolder> authorisationParentHolderCaptor = ArgumentCaptor.forClass(PisAuthorisationParentHolder.class);
        when(authorisationServiceEncrypted.getAuthorisationsByParentId(any(PisAuthorisationParentHolder.class)))
            .thenReturn(CmsResponse.<List<String>>builder().error(CmsError.TECHNICAL_ERROR).build());

        Assertions.assertEquals(Optional.empty(), pisAuthorisationService.getAuthorisationSubResources(PAYMENT_ID));

        verify(authorisationServiceEncrypted, times(1)).getAuthorisationsByParentId(authorisationParentHolderCaptor.capture());
        Assertions.assertEquals(PAYMENT_ID, authorisationParentHolderCaptor.getValue().getParentId());
    }

    @Test
    void getAuthorisationScaStatus() {
        ArgumentCaptor<PisAuthorisationParentHolder> authorisationParentHolderCaptor = ArgumentCaptor.forClass(PisAuthorisationParentHolder.class);
        when(authorisationServiceEncrypted.getAuthorisationScaStatus(eq(AUTHORISATION_ID), any(PisAuthorisationParentHolder.class)))
            .thenReturn(CmsResponse.<ScaStatus>builder().payload(ScaStatus.PSUIDENTIFIED).build());

        Assertions.assertEquals(Optional.of(ScaStatus.PSUIDENTIFIED), pisAuthorisationService.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID));

        verify(authorisationServiceEncrypted, times(1)).getAuthorisationScaStatus(eq(AUTHORISATION_ID), authorisationParentHolderCaptor.capture());
        Assertions.assertEquals(PAYMENT_ID, authorisationParentHolderCaptor.getValue().getParentId());
    }

    @Test
    void getAuthorisationScaStatus_error() {
        ArgumentCaptor<PisAuthorisationParentHolder> authorisationParentHolderCaptor = ArgumentCaptor.forClass(PisAuthorisationParentHolder.class);
        when(authorisationServiceEncrypted.getAuthorisationScaStatus(eq(AUTHORISATION_ID), any(PisAuthorisationParentHolder.class)))
            .thenReturn(CmsResponse.<ScaStatus>builder().error(CmsError.TECHNICAL_ERROR).build());

        Assertions.assertEquals(Optional.empty(), pisAuthorisationService.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID));

        verify(authorisationServiceEncrypted, times(1)).getAuthorisationScaStatus(eq(AUTHORISATION_ID), authorisationParentHolderCaptor.capture());
        Assertions.assertEquals(PAYMENT_ID, authorisationParentHolderCaptor.getValue().getParentId());
    }

    @Test
    void getCancellationAuthorisationScaStatus() {
        ArgumentCaptor<PisCancellationAuthorisationParentHolder> authorisationParentHolderCaptor = ArgumentCaptor.forClass(PisCancellationAuthorisationParentHolder.class);
        when(authorisationServiceEncrypted.getAuthorisationScaStatus(eq(AUTHORISATION_ID), any(PisCancellationAuthorisationParentHolder.class)))
            .thenReturn(CmsResponse.<ScaStatus>builder().payload(ScaStatus.PSUIDENTIFIED).build());

        Assertions.assertEquals(Optional.of(ScaStatus.PSUIDENTIFIED), pisAuthorisationService.getCancellationAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID));

        verify(authorisationServiceEncrypted, times(1)).getAuthorisationScaStatus(eq(AUTHORISATION_ID), authorisationParentHolderCaptor.capture());
        Assertions.assertEquals(PAYMENT_ID, authorisationParentHolderCaptor.getValue().getParentId());
    }

    @Test
    void getCancellationAuthorisationScaStatus_error() {
        ArgumentCaptor<PisCancellationAuthorisationParentHolder> authorisationParentHolderCaptor = ArgumentCaptor.forClass(PisCancellationAuthorisationParentHolder.class);
        when(authorisationServiceEncrypted.getAuthorisationScaStatus(eq(AUTHORISATION_ID), any(PisCancellationAuthorisationParentHolder.class)))
            .thenReturn(CmsResponse.<ScaStatus>builder().error(CmsError.TECHNICAL_ERROR).build());

        Assertions.assertEquals(Optional.empty(), pisAuthorisationService.getCancellationAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID));

        verify(authorisationServiceEncrypted, times(1)).getAuthorisationScaStatus(eq(AUTHORISATION_ID), authorisationParentHolderCaptor.capture());
        Assertions.assertEquals(PAYMENT_ID, authorisationParentHolderCaptor.getValue().getParentId());
    }

    @Test
    void getAuthorisationScaApproach() {
        AuthorisationScaApproachResponse payload = new AuthorisationScaApproachResponse(ScaApproach.DECOUPLED);
        when(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder().payload(payload).build());

        Assertions.assertEquals(Optional.of(payload), pisAuthorisationService.getAuthorisationScaApproach(AUTHORISATION_ID));

        verify(authorisationServiceEncrypted, times(1)).getAuthorisationScaApproach(AUTHORISATION_ID);
    }

    @Test
    void getAuthorisationScaApproach_error() {
        when(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        Assertions.assertEquals(Optional.empty(), pisAuthorisationService.getAuthorisationScaApproach(AUTHORISATION_ID));

        verify(authorisationServiceEncrypted, times(1)).getAuthorisationScaApproach(AUTHORISATION_ID);
    }

    @Test
    void updateAuthorisation() {
        PaymentAuthorisationParameters request = new PaymentAuthorisationParameters();
        request.setAuthorisationId(AUTHORISATION_ID);
        AuthorisationProcessorResponse response = new AuthorisationProcessorResponse();
        UpdateAuthorisationRequest updateAuthorisationRequest = new UpdateAuthorisationRequest();

        when(pisCommonPaymentMapper.mapToUpdateAuthorisationRequest(response, AuthorisationType.PIS_CREATION)).thenReturn(updateAuthorisationRequest);

        pisAuthorisationService.updateAuthorisation(request, response);

        verify(authorisationServiceEncrypted, times(1)).updateAuthorisation(AUTHORISATION_ID, updateAuthorisationRequest);
        verify(pisCommonPaymentMapper, times(1)).mapToUpdateAuthorisationRequest(response, AuthorisationType.PIS_CREATION);
    }

    @Test
    void updateAuthorisation_hasError() {
        PaymentAuthorisationParameters request = new PaymentAuthorisationParameters();
        request.setAuthorisationId(AUTHORISATION_ID);
        AuthorisationProcessorResponse response = new AuthorisationProcessorResponse();
        response.setErrorHolder(ErrorHolder.builder(ErrorType.AIS_400).build());

        pisAuthorisationService.updateAuthorisation(request, response);

        verify(authorisationServiceEncrypted, never()).updateAuthorisation(any(), any());
        verify(pisCommonPaymentMapper, never()).mapToUpdateAuthorisationRequest(any(), any());
    }

    @Test
    void updateCancellationAuthorisation() {
        PaymentAuthorisationParameters request = new PaymentAuthorisationParameters();
        request.setAuthorisationId(AUTHORISATION_ID);
        AuthorisationProcessorResponse response = new AuthorisationProcessorResponse();
        UpdateAuthorisationRequest updateAuthorisationRequest = new UpdateAuthorisationRequest();

        when(pisCommonPaymentMapper.mapToUpdateAuthorisationRequest(response, AuthorisationType.PIS_CANCELLATION)).thenReturn(updateAuthorisationRequest);

        pisAuthorisationService.updateCancellationAuthorisation(request, response);

        verify(authorisationServiceEncrypted, times(1)).updateAuthorisation(AUTHORISATION_ID, updateAuthorisationRequest);
        verify(pisCommonPaymentMapper, times(1)).mapToUpdateAuthorisationRequest(response, AuthorisationType.PIS_CANCELLATION);
    }

    @Test
    void updateCancellationAuthorisation_hasError() {
        PaymentAuthorisationParameters request = new PaymentAuthorisationParameters();
        request.setAuthorisationId(AUTHORISATION_ID);
        AuthorisationProcessorResponse response = new AuthorisationProcessorResponse();
        response.setErrorHolder(ErrorHolder.builder(ErrorType.AIS_400).build());

        pisAuthorisationService.updateCancellationAuthorisation(request, response);

        verify(authorisationServiceEncrypted, never()).updateAuthorisation(any(), any());
        verify(pisCommonPaymentMapper, never()).mapToUpdateAuthorisationRequest(any(), any());
    }

    private static PaymentAuthorisationParameters buildXs2aUpdatePisCommonPaymentPsuDataRequest() {
        PaymentAuthorisationParameters request = new PaymentAuthorisationParameters();
        request.setAuthorisationId(AUTHORISATION_ID);
        return request;
    }

    private static Authorisation buildGetPisAuthorisationResponse() {
        Authorisation response = new Authorisation();
        response.setScaStatus(ScaStatus.RECEIVED);
        return response;
    }
}

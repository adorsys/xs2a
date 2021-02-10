/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthorisationSubResources;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPaymentCancellationAuthorisationSubResource;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aPisCommonPaymentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmbeddedPisScaAuthorisationServiceTest {
    private static final String PAYMENT_ID = "c713a32c-15ff-4f90-afa0-34a500359844";
    private static final String WRONG_PAYMENT_ID = "wrong payment id";
    private static final String AUTHORISATION_ID = "ad746cb3-a01b-4196-a6b9-40b0e4cd2350";
    private static final String WRONG_AUTHORISATION_ID = "wrong authorisation id";
    private static final String CANCELLATION_AUTHORISATION_ID = "dd5d766f-eeb7-4efe-b730-24d5ed53f537";
    private static final String WRONG_CANCELLATION_AUTHORISATION_ID = "wrong cancellation authorisation id";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final List<String> STRING_LIST = Collections.singletonList(PAYMENT_ID);
    private static final Xs2aAuthorisationSubResources XS2A_AUTHORISATION_SUB_RESOURCES = new Xs2aAuthorisationSubResources(STRING_LIST);
    private static final Xs2aUpdatePisCommonPaymentPsuDataRequest XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
    private static final Xs2aPaymentCancellationAuthorisationSubResource XS2A_PAYMENT_CANCELLATION_AUTHORISATION_SUB_RESOURCE = new Xs2aPaymentCancellationAuthorisationSubResource(STRING_LIST);
    private static final PaymentType PAYMENT_TYPE = PaymentType.SINGLE;
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("Test psuId", null, null, null, null);
    private static final Xs2aUpdatePisCommonPaymentPsuDataResponse XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_RESPONSE = new Xs2aUpdatePisCommonPaymentPsuDataResponse(
        SCA_STATUS, PAYMENT_ID, AUTHORISATION_ID, PSU_ID_DATA, null);
    private static final CreateAuthorisationResponse CREATE_PIS_AUTHORISATION_RESPONSE = new CreateAuthorisationResponse(AUTHORISATION_ID, SCA_STATUS, null, null);
    private static final CreateAuthorisationResponse WRONG_CREATE_PIS_AUTHORISATION_RESPONSE = new CreateAuthorisationResponse(WRONG_AUTHORISATION_ID, SCA_STATUS, null, null);
    private static final Xs2aCreatePisCancellationAuthorisationResponse XS2A_CREATE_PIS_CANCELLATION_AUTHORISATION_RESPONSE = new Xs2aCreatePisCancellationAuthorisationResponse(CREATE_PIS_AUTHORISATION_RESPONSE.getAuthorizationId(), ScaStatus.RECEIVED, PAYMENT_TYPE, null);
    private static final Xs2aCreatePisAuthorisationResponse XS2A_CREATE_PIS_AUTHORISATION_RESPONSE = new Xs2aCreatePisAuthorisationResponse(AUTHORISATION_ID, SCA_STATUS, PAYMENT_TYPE, null, null, null);

    @InjectMocks
    private EmbeddedPisScaAuthorisationService embeddedPisScaAuthorisationService;

    @Mock
    private PisAuthorisationService pisAuthorisationService;
    @Mock
    private Xs2aPisCommonPaymentMapper pisCommonPaymentMapper;

    @Test
    void createCommonPaymentAuthorisation_success() {
        // Given
        when(pisAuthorisationService.createPisAuthorisation(PAYMENT_ID, PSU_ID_DATA))
            .thenReturn(CREATE_PIS_AUTHORISATION_RESPONSE);
        when(pisCommonPaymentMapper.mapToXsa2CreatePisAuthorisationResponse(CREATE_PIS_AUTHORISATION_RESPONSE, PAYMENT_TYPE))
            .thenReturn(Optional.of(XS2A_CREATE_PIS_AUTHORISATION_RESPONSE));

        // When
        Optional<Xs2aCreatePisAuthorisationResponse> actualResponse = embeddedPisScaAuthorisationService.createCommonPaymentAuthorisation(PAYMENT_ID, PAYMENT_TYPE, PSU_ID_DATA);

        // Then
        assertThat(actualResponse).isPresent().contains(XS2A_CREATE_PIS_AUTHORISATION_RESPONSE);
    }

    @Test
    void createCommonPaymentAuthorisation_wrongId_fail() {
        // Given
        when(pisAuthorisationService.createPisAuthorisation(WRONG_PAYMENT_ID, PSU_ID_DATA))
            .thenReturn(WRONG_CREATE_PIS_AUTHORISATION_RESPONSE);
        when(pisCommonPaymentMapper.mapToXsa2CreatePisAuthorisationResponse(WRONG_CREATE_PIS_AUTHORISATION_RESPONSE, PAYMENT_TYPE))
            .thenReturn(Optional.empty());

        // When
        Optional<Xs2aCreatePisAuthorisationResponse> actualResponse = embeddedPisScaAuthorisationService.createCommonPaymentAuthorisation(WRONG_PAYMENT_ID, PAYMENT_TYPE, PSU_ID_DATA);

        // Then
        assertThat(actualResponse).isNotPresent();
    }

    @Test
    void updateCommonPaymentPsuData_success() {
        // Given
        when(pisAuthorisationService.updatePisAuthorisation(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST, ScaApproach.EMBEDDED))
            .thenReturn(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_RESPONSE);

        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = embeddedPisScaAuthorisationService.updateCommonPaymentPsuData(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST);

        // Then
        assertThat(actualResponse).isEqualTo(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_RESPONSE);
    }

    @Test
    void updateCommonPaymentPsuData_fail() {
        // Given
        Xs2aUpdatePisCommonPaymentPsuDataResponse errorResponse = buildErrorXs2aUpdatePisCommonPaymentPsuDataResponse();
        when(pisAuthorisationService.updatePisAuthorisation(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST, ScaApproach.EMBEDDED))
            .thenReturn(errorResponse);

        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = embeddedPisScaAuthorisationService.updateCommonPaymentPsuData(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST);

        // Then
        assertThat(actualResponse).isEqualTo(errorResponse);
    }

    @Test
    void createCommonPaymentCancellationAuthorisation_success() {
        // Given
        when(pisAuthorisationService.createPisAuthorisationCancellation(PAYMENT_ID, PSU_ID_DATA))
            .thenReturn(CREATE_PIS_AUTHORISATION_RESPONSE);
        when(pisCommonPaymentMapper.mapToXs2aCreatePisCancellationAuthorisationResponse(CREATE_PIS_AUTHORISATION_RESPONSE, PAYMENT_TYPE))
            .thenReturn(Optional.of(XS2A_CREATE_PIS_CANCELLATION_AUTHORISATION_RESPONSE));

        // When
        Optional<Xs2aCreatePisCancellationAuthorisationResponse> actualResponse = embeddedPisScaAuthorisationService.createCommonPaymentCancellationAuthorisation(PAYMENT_ID, PAYMENT_TYPE, PSU_ID_DATA);

        // Then
        assertThat(actualResponse).isPresent().contains(XS2A_CREATE_PIS_CANCELLATION_AUTHORISATION_RESPONSE);
    }

    @Test
    void createCommonPaymentCancellationAuthorisation_wrongId_fail() {
        // Given
        when(pisAuthorisationService.createPisAuthorisationCancellation(WRONG_PAYMENT_ID, PSU_ID_DATA))
            .thenReturn(WRONG_CREATE_PIS_AUTHORISATION_RESPONSE);
        when(pisCommonPaymentMapper.mapToXs2aCreatePisCancellationAuthorisationResponse(WRONG_CREATE_PIS_AUTHORISATION_RESPONSE, PAYMENT_TYPE))
            .thenReturn(Optional.empty());

        // When
        Optional<Xs2aCreatePisCancellationAuthorisationResponse> actualResponse = embeddedPisScaAuthorisationService.createCommonPaymentCancellationAuthorisation(WRONG_PAYMENT_ID, PAYMENT_TYPE, PSU_ID_DATA);

        // Then
        assertThat(actualResponse).isNotPresent();
    }

    @Test
    void getCancellationAuthorisationSubResources_success() {
        // Given
        when(pisAuthorisationService.getCancellationAuthorisationSubResources(PAYMENT_ID))
            .thenReturn(Optional.of(STRING_LIST));

        // When
        Optional<Xs2aPaymentCancellationAuthorisationSubResource> actualResponse = embeddedPisScaAuthorisationService.getCancellationAuthorisationSubResources(PAYMENT_ID);

        // Then
        assertThat(actualResponse).isPresent().contains(XS2A_PAYMENT_CANCELLATION_AUTHORISATION_SUB_RESOURCE);
    }

    @Test
    void getCancellationAuthorisationSubResources_wrongPaymentId_fail() {
        // Given
        when(pisAuthorisationService.getCancellationAuthorisationSubResources(WRONG_PAYMENT_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<Xs2aPaymentCancellationAuthorisationSubResource> actualResponse = embeddedPisScaAuthorisationService.getCancellationAuthorisationSubResources(WRONG_PAYMENT_ID);

        // Then
        assertThat(actualResponse).isNotPresent();
    }

    @Test
    void updateCommonPaymentCancellationPsuData_success() {
        // Given
        when(pisAuthorisationService.updatePisCancellationAuthorisation(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST, ScaApproach.EMBEDDED))
            .thenReturn(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_RESPONSE);
        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = embeddedPisScaAuthorisationService.updateCommonPaymentCancellationPsuData(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST);

        // Then
        assertThat(actualResponse).isEqualTo(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_RESPONSE);
    }

    @Test
    void updateCommonPaymentCancellationPsuData_fail() {
        // Given
        Xs2aUpdatePisCommonPaymentPsuDataResponse errorResponse = buildErrorXs2aUpdatePisCommonPaymentPsuDataResponse();
        when(pisAuthorisationService.updatePisCancellationAuthorisation(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST, ScaApproach.EMBEDDED))
            .thenReturn(errorResponse);
        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = embeddedPisScaAuthorisationService.updateCommonPaymentCancellationPsuData(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST);

        // Then
        assertThat(actualResponse).isEqualTo(errorResponse);
    }

    @Test
    void getAuthorisationSubResources_success() {
        // Given
        when(pisAuthorisationService.getAuthorisationSubResources(PAYMENT_ID))
            .thenReturn(Optional.of(STRING_LIST));

        // When
        Optional<Xs2aAuthorisationSubResources> actualResponse = embeddedPisScaAuthorisationService.getAuthorisationSubResources(PAYMENT_ID);

        // Then
        assertThat(actualResponse).isPresent().contains(XS2A_AUTHORISATION_SUB_RESOURCES);
    }

    @Test
    void getAuthorisationSubResources_wrongPaymentId_fail() {
        // Given
        when(pisAuthorisationService.getAuthorisationSubResources(WRONG_PAYMENT_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<Xs2aAuthorisationSubResources> actualResponse = embeddedPisScaAuthorisationService.getAuthorisationSubResources(WRONG_PAYMENT_ID);

        // Then
        assertThat(actualResponse).isNotPresent();
    }

    @Test
    void getAuthorisationScaStatus_success() {
        // Given
        when(pisAuthorisationService.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(ScaStatus.RECEIVED));

        // When
        Optional<ScaStatus> actual = embeddedPisScaAuthorisationService.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID);

        // Then
        assertThat(actual).isPresent().contains(ScaStatus.RECEIVED);
    }

    @Test
    void getAuthorisationScaStatus_wrongIds_failure() {
        // Given
        when(pisAuthorisationService.getAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<ScaStatus> actual = embeddedPisScaAuthorisationService.getAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertThat(actual).isNotPresent();
    }

    @Test
    void getCancellationAuthorisationScaStatus_success() {
        // Given
        when(pisAuthorisationService.getCancellationAuthorisationScaStatus(PAYMENT_ID, CANCELLATION_AUTHORISATION_ID))
            .thenReturn(Optional.of(SCA_STATUS));

        // When
        Optional<ScaStatus> actual = embeddedPisScaAuthorisationService.getCancellationAuthorisationScaStatus(PAYMENT_ID, CANCELLATION_AUTHORISATION_ID);

        // Then
        assertThat(actual).isPresent().contains(SCA_STATUS);
    }

    @Test
    void getCancellationAuthorisationScaStatus_wrongIds_failure() {
        // Given
        when(pisAuthorisationService.getCancellationAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_CANCELLATION_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<ScaStatus> actual = embeddedPisScaAuthorisationService.getCancellationAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_CANCELLATION_AUTHORISATION_ID);

        // Then
        assertThat(actual).isNotPresent();
    }

    @Test
    void getScaApproachServiceType_success() {
        //When
        ScaApproach actualResponse = embeddedPisScaAuthorisationService.getScaApproachServiceType();

        //Then
        assertThat(actualResponse).isEqualTo(ScaApproach.EMBEDDED);
    }

    @Test
    void updateAuthorisation() {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
        AuthorisationProcessorResponse response = new AuthorisationProcessorResponse();

        embeddedPisScaAuthorisationService.updateAuthorisation(request, response);
        verify(pisAuthorisationService, times(1)).updateAuthorisation(request, response);
    }

    @Test
    void updateCancellationAuthorisation() {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
        AuthorisationProcessorResponse response = new AuthorisationProcessorResponse();

        embeddedPisScaAuthorisationService.updateCancellationAuthorisation(request, response);
        verify(pisAuthorisationService, times(1)).updateCancellationAuthorisation(request, response);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse buildErrorXs2aUpdatePisCommonPaymentPsuDataResponse() {
        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_NO_PSU))
                                      .build();
        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, PAYMENT_ID, AUTHORISATION_ID, PSU_ID_DATA);

    }
}

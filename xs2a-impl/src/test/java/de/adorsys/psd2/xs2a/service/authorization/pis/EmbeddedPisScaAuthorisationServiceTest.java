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

package de.adorsys.psd2.xs2a.service.authorization.pis;

import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthorisationSubResources;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPaymentCancellationAuthorisationSubResource;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EmbeddedPisScaAuthorisationServiceTest {
    private static final String PAYMENT_ID = "c713a32c-15ff-4f90-afa0-34a500359844";
    private static final String WRONG_PAYMENT_ID = "wrong payment id";
    private static final String AUTHORISATION_ID = "ad746cb3-a01b-4196-a6b9-40b0e4cd2350";
    private static final String WRONG_AUTHORISATION_ID = "wrong authorisation id";
    private static final String CANCELLATION_AUTHORISATION_ID = "dd5d766f-eeb7-4efe-b730-24d5ed53f537";
    private static final String WRONG_CANCELLATION_AUTHORISATION_ID = "wrong cancellation authorisation id";
    private static final String MESSAGE_ERROR_NO_PSU = "Please provide the PSU identification data";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final List<String> STRING_LIST = Collections.singletonList(PAYMENT_ID);
    private static final Xs2aAuthorisationSubResources XS2A_AUTHORISATION_SUB_RESOURCES = new Xs2aAuthorisationSubResources(STRING_LIST);
    private static final Xs2aUpdatePisCommonPaymentPsuDataRequest XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
    private static final Xs2aUpdatePisCommonPaymentPsuDataResponse XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_RESPONSE = new Xs2aUpdatePisCommonPaymentPsuDataResponse();
    private static final Xs2aPaymentCancellationAuthorisationSubResource XS2A_PAYMENT_CANCELLATION_AUTHORISATION_SUB_RESOURCE = new Xs2aPaymentCancellationAuthorisationSubResource(STRING_LIST);
    private static final PaymentType PAYMENT_TYPE = PaymentType.SINGLE;
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("Test psuId", null, null, null);
    private static final CreatePisAuthorisationResponse CREATE_PIS_AUTHORISATION_RESPONSE = new CreatePisAuthorisationResponse(AUTHORISATION_ID);
    private static final CreatePisAuthorisationResponse WRONG_CREATE_PIS_AUTHORISATION_RESPONSE = new CreatePisAuthorisationResponse(WRONG_AUTHORISATION_ID);
    private static final Xs2aCreatePisCancellationAuthorisationResponse XS2A_CREATE_PIS_CANCELLATION_AUTHORISATION_RESPONSE = new Xs2aCreatePisCancellationAuthorisationResponse(CREATE_PIS_AUTHORISATION_RESPONSE.getAuthorizationId(), ScaStatus.STARTED, PAYMENT_TYPE);
    private static final Xs2aCreatePisAuthorisationResponse XS2A_CREATE_PIS_AUTHORISATION_RESPONSE = new Xs2aCreatePisAuthorisationResponse(AUTHORISATION_ID, SCA_STATUS, PAYMENT_TYPE);

    @InjectMocks
    private EmbeddedPisScaAuthorisationService embeddedPisScaAuthorisationService;

    @Mock
    private PisAuthorisationService pisAuthorisationService;
    @Mock
    private Xs2aPisCommonPaymentMapper pisCommonPaymentMapper;

    @Test
    public void createCommonPaymentAuthorisation_success() {
        // Given
        when(pisAuthorisationService.createPisAuthorisation(PAYMENT_ID, PSU_ID_DATA))
            .thenReturn(CREATE_PIS_AUTHORISATION_RESPONSE);
        when(pisCommonPaymentMapper.mapToXsa2CreatePisAuthorisationResponse(CREATE_PIS_AUTHORISATION_RESPONSE, PAYMENT_TYPE))
            .thenReturn(Optional.of(XS2A_CREATE_PIS_AUTHORISATION_RESPONSE));

        // When
        Optional<Xs2aCreatePisAuthorisationResponse> actualResponse = embeddedPisScaAuthorisationService.createCommonPaymentAuthorisation(PAYMENT_ID, PAYMENT_TYPE, PSU_ID_DATA);

        // Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse).isEqualTo(Optional.of(XS2A_CREATE_PIS_AUTHORISATION_RESPONSE));
    }

    @Test
    public void createCommonPaymentAuthorisation_wrongId_fail() {
        // Given
        when(pisAuthorisationService.createPisAuthorisation(WRONG_PAYMENT_ID, PSU_ID_DATA))
            .thenReturn(WRONG_CREATE_PIS_AUTHORISATION_RESPONSE);
        when(pisCommonPaymentMapper.mapToXsa2CreatePisAuthorisationResponse(WRONG_CREATE_PIS_AUTHORISATION_RESPONSE, PAYMENT_TYPE))
            .thenReturn(Optional.empty());

        // When
        Optional<Xs2aCreatePisAuthorisationResponse> actualResponse = embeddedPisScaAuthorisationService.createCommonPaymentAuthorisation(WRONG_PAYMENT_ID, PAYMENT_TYPE, PSU_ID_DATA);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void updateCommonPaymentPsuData_success() {
        // Given
        when(pisAuthorisationService.updatePisAuthorisation(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST, ScaApproach.EMBEDDED))
            .thenReturn(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_RESPONSE);

        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = embeddedPisScaAuthorisationService.updateCommonPaymentPsuData(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST);

        // Then
        assertThat(actualResponse).isEqualTo(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_RESPONSE);
    }

    @Test
    public void updateCommonPaymentPsuData_fail() {
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
    public void createCommonPaymentCancellationAuthorisation_success() {
        // Given
        when(pisAuthorisationService.createPisAuthorisationCancellation(PAYMENT_ID, PSU_ID_DATA))
            .thenReturn(CREATE_PIS_AUTHORISATION_RESPONSE);
        when(pisCommonPaymentMapper.mapToXs2aCreatePisCancellationAuthorisationResponse(CREATE_PIS_AUTHORISATION_RESPONSE, PAYMENT_TYPE))
            .thenReturn(Optional.of(XS2A_CREATE_PIS_CANCELLATION_AUTHORISATION_RESPONSE));

        // When
        Optional<Xs2aCreatePisCancellationAuthorisationResponse> actualResponse = embeddedPisScaAuthorisationService.createCommonPaymentCancellationAuthorisation(PAYMENT_ID, PAYMENT_TYPE, PSU_ID_DATA);

        // Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse).isEqualTo(Optional.of(XS2A_CREATE_PIS_CANCELLATION_AUTHORISATION_RESPONSE));
    }

    @Test
    public void createCommonPaymentCancellationAuthorisation_wrongId_fail() {
        // Given
        when(pisAuthorisationService.createPisAuthorisationCancellation(WRONG_PAYMENT_ID, PSU_ID_DATA))
            .thenReturn(WRONG_CREATE_PIS_AUTHORISATION_RESPONSE);
        when(pisCommonPaymentMapper.mapToXs2aCreatePisCancellationAuthorisationResponse(WRONG_CREATE_PIS_AUTHORISATION_RESPONSE, PAYMENT_TYPE))
            .thenReturn(Optional.empty());

        // When
        Optional<Xs2aCreatePisCancellationAuthorisationResponse> actualResponse = embeddedPisScaAuthorisationService.createCommonPaymentCancellationAuthorisation(WRONG_PAYMENT_ID, PAYMENT_TYPE, PSU_ID_DATA);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void getCancellationAuthorisationSubResources_success() {
        // Given
        when(pisAuthorisationService.getCancellationAuthorisationSubResources(PAYMENT_ID))
            .thenReturn(Optional.of(STRING_LIST));

        // When
        Optional<Xs2aPaymentCancellationAuthorisationSubResource> actualResponse = embeddedPisScaAuthorisationService.getCancellationAuthorisationSubResources(PAYMENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse).isEqualTo(Optional.of(XS2A_PAYMENT_CANCELLATION_AUTHORISATION_SUB_RESOURCE));
    }

    @Test
    public void getCancellationAuthorisationSubResources_wrongPaymentId_fail() {
        // Given
        when(pisAuthorisationService.getCancellationAuthorisationSubResources(WRONG_PAYMENT_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<Xs2aPaymentCancellationAuthorisationSubResource> actualResponse = embeddedPisScaAuthorisationService.getCancellationAuthorisationSubResources(WRONG_PAYMENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void updateCommonPaymentCancellationPsuData_success() {
        // Given
        when(pisAuthorisationService.updatePisCancellationAuthorisation(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST, ScaApproach.EMBEDDED))
            .thenReturn(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_RESPONSE);
        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = embeddedPisScaAuthorisationService.updateCommonPaymentCancellationPsuData(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST);

        // Then
        assertThat(actualResponse).isEqualTo(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_RESPONSE);
    }

    @Test
    public void updateCommonPaymentCancellationPsuData_fail() {
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
    public void getAuthorisationSubResources_success() {
        // Given
        when(pisAuthorisationService.getAuthorisationSubResources(PAYMENT_ID))
            .thenReturn(Optional.of(STRING_LIST));

        // When
        Optional<Xs2aAuthorisationSubResources> actualResponse = embeddedPisScaAuthorisationService.getAuthorisationSubResources(PAYMENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse).isEqualTo(Optional.of(XS2A_AUTHORISATION_SUB_RESOURCES));
    }

    @Test
    public void getAuthorisationSubResources_wrongPaymentId_fail() {
        // Given
        when(pisAuthorisationService.getAuthorisationSubResources(WRONG_PAYMENT_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<Xs2aAuthorisationSubResources> actualResponse = embeddedPisScaAuthorisationService.getAuthorisationSubResources(WRONG_PAYMENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void getAuthorisationScaStatus_success() {
        // Given
        when(pisAuthorisationService.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(ScaStatus.RECEIVED));

        // When
        Optional<ScaStatus> actual = embeddedPisScaAuthorisationService.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(ScaStatus.RECEIVED, actual.get());
    }

    @Test
    public void getAuthorisationScaStatus_wrongIds_failure() {
        // Given
        when(pisAuthorisationService.getAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<ScaStatus> actual = embeddedPisScaAuthorisationService.getAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertFalse(actual.isPresent());
    }

    @Test
    public void getCancellationAuthorisationScaStatus_success() {
        // Given
        when(pisAuthorisationService.getCancellationAuthorisationScaStatus(PAYMENT_ID, CANCELLATION_AUTHORISATION_ID))
            .thenReturn(Optional.of(SCA_STATUS));

        // When
        Optional<ScaStatus> actual = embeddedPisScaAuthorisationService.getCancellationAuthorisationScaStatus(PAYMENT_ID, CANCELLATION_AUTHORISATION_ID);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(SCA_STATUS, actual.get());
    }

    @Test
    public void getCancellationAuthorisationScaStatus_wrongIds_failure() {
        // Given
        when(pisAuthorisationService.getCancellationAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_CANCELLATION_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<ScaStatus> actual = embeddedPisScaAuthorisationService.getCancellationAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_CANCELLATION_AUTHORISATION_ID);

        // Then
        assertFalse(actual.isPresent());
    }

    @Test
    public void getScaApproachServiceType_success() {
        //When
        ScaApproach actualResponse = embeddedPisScaAuthorisationService.getScaApproachServiceType();

        //Then
        assertThat(actualResponse).isEqualTo(ScaApproach.EMBEDDED);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse buildErrorXs2aUpdatePisCommonPaymentPsuDataResponse() {
        ErrorHolder errorHolder = ErrorHolder.builder(MessageErrorCode.FORMAT_ERROR)
                                      .errorType(ErrorType.PIS_400)
                                      .messages(Collections.singletonList(MESSAGE_ERROR_NO_PSU))
                                      .build();
        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, PAYMENT_ID, AUTHORISATION_ID, PSU_ID_DATA);

    }
}

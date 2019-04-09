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

import de.adorsys.psd2.consent.api.CmsAuthorisationType;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.config.factory.PisScaStageAuthorisationFactory;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.pis.stage.initiation.PisScaStartAuthorisationStage;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisCommonPaymentMapper;
import org.junit.Before;
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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PisAuthorisationServiceTest {
    private static final String PAYMENT_ID = "c713a32c-15ff-4f90-afa0-34a500359844";
    private static final String WRONG_PAYMENT_ID = "wrong payment id";
    private static final String AUTHORISATION_ID = "ad746cb3-a01b-4196-a6b9-40b0e4cd2350";
    private static final String WRONG_AUTHORISATION_ID = "wrong authorisation id";
    private static final String CANCELLATION_AUTHORISATION_ID = "dd5d766f-eeb7-4efe-b730-24d5ed53f537";
    private static final String WRONG_CANCELLATION_AUTHORISATION_ID = "wrong cancellation authorisation id";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final List<String> SOME_LIST = Collections.emptyList();
    private static final ScaApproach SCA_APPROACH = ScaApproach.OAUTH;
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType");
    private static final CreatePisAuthorisationRequest CREATE_PIS_AUTHORISATION_REQUEST_CANCELLED = buildCreatePisAuthorisationRequestCancelled();
    private static final CreatePisAuthorisationRequest CREATE_PIS_AUTHORISATION_REQUEST_CREATED = buildCreatePisAuthorisationRequestCreated();
    private static final CreatePisAuthorisationResponse CREATE_PIS_AUTHORISATION_RESPONSE = new CreatePisAuthorisationResponse(AUTHORISATION_ID);
    private static final Xs2aUpdatePisCommonPaymentPsuDataRequest XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST = buildXs2aUpdatePisCommonPaymentPsuDataRequest();
    private static final GetPisAuthorisationResponse GET_PIS_AUTHORISATION_RESPONSE = buildGetPisAuthorisationResponse();
    private static final Xs2aUpdatePisCommonPaymentPsuDataResponse STAGE_RESPONSE = new Xs2aUpdatePisCommonPaymentPsuDataResponse();
    private static final UpdatePisCommonPaymentPsuDataRequest UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST = new UpdatePisCommonPaymentPsuDataRequest();

    @InjectMocks
    private PisAuthorisationService pisAuthorisationService;

    @Mock
    private PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;
    @Mock
    private PisScaStageAuthorisationFactory pisScaStageAuthorisationFactory;
    @Mock
    private Xs2aPisCommonPaymentMapper pisCommonPaymentMapper;
    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private PisScaStartAuthorisationStage pisScaStartAuthorisationStage;

    @Before
    public void setUp() {
        when(pisCommonPaymentServiceEncrypted.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID, CmsAuthorisationType.CREATED))
            .thenReturn(Optional.of(SCA_STATUS));
        when(pisCommonPaymentServiceEncrypted.getAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_AUTHORISATION_ID, CmsAuthorisationType.CREATED))
            .thenReturn(Optional.empty());
        when(pisCommonPaymentServiceEncrypted.getAuthorisationScaStatus(PAYMENT_ID, CANCELLATION_AUTHORISATION_ID, CmsAuthorisationType.CANCELLED))
            .thenReturn(Optional.of(SCA_STATUS));
        when(pisCommonPaymentServiceEncrypted.getAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_CANCELLATION_AUTHORISATION_ID, CmsAuthorisationType.CANCELLED))
            .thenReturn(Optional.empty());
    }

    @Test
    public void createPisAuthorisation_success() {
        // Given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(SCA_APPROACH);
        when(pisCommonPaymentServiceEncrypted.createAuthorization(PAYMENT_ID, CREATE_PIS_AUTHORISATION_REQUEST_CREATED))
            .thenReturn(Optional.of(CREATE_PIS_AUTHORISATION_RESPONSE));

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
        when(pisCommonPaymentServiceEncrypted.createAuthorization(WRONG_PAYMENT_ID, CREATE_PIS_AUTHORISATION_REQUEST_CREATED))
            .thenReturn(Optional.empty());

        // When
        CreatePisAuthorisationResponse actualResponse = pisAuthorisationService.createPisAuthorisation(WRONG_PAYMENT_ID, PSU_ID_DATA);

        // Then
        assertThat(actualResponse).isNull();
    }

    @Test
    public void updatePisAuthorisation_success() {
        // Given
        when(pisCommonPaymentServiceEncrypted.getPisAuthorisationById(AUTHORISATION_ID))
            .thenReturn(Optional.of(GET_PIS_AUTHORISATION_RESPONSE));
        when(pisScaStageAuthorisationFactory.getService(any(String.class)))
            .thenReturn(pisScaStartAuthorisationStage);
        when(pisScaStartAuthorisationStage.apply(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST, GET_PIS_AUTHORISATION_RESPONSE))
            .thenReturn(STAGE_RESPONSE);
        when(pisCommonPaymentMapper.mapToCmsUpdateCommonPaymentPsuDataReq(STAGE_RESPONSE))
            .thenReturn(UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST);

        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisAuthorisationService.updatePisAuthorisation(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST, SCA_APPROACH);

        // Then
        assertThat(actualResponse).isEqualTo(STAGE_RESPONSE);
    }

    @Test
    public void updatePisCancellationAuthorisation_success() {
        // Given
        when(pisCommonPaymentServiceEncrypted.getPisCancellationAuthorisationById(AUTHORISATION_ID))
            .thenReturn(Optional.of(GET_PIS_AUTHORISATION_RESPONSE));
        when(pisScaStageAuthorisationFactory.getService(any(String.class)))
            .thenReturn(pisScaStartAuthorisationStage);
        when(pisScaStartAuthorisationStage.apply(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST, GET_PIS_AUTHORISATION_RESPONSE))
            .thenReturn(STAGE_RESPONSE);
        when(pisCommonPaymentMapper.mapToCmsUpdateCommonPaymentPsuDataReq(STAGE_RESPONSE))
            .thenReturn(UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST);

        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisAuthorisationService.updatePisCancellationAuthorisation(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST, SCA_APPROACH);

        // Then
        assertThat(actualResponse).isEqualTo(STAGE_RESPONSE);
    }

    @Test
    public void createPisAuthorisationCancellation_success() {
        // Given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(SCA_APPROACH);
        when(pisCommonPaymentServiceEncrypted.createAuthorizationCancellation(PAYMENT_ID, CREATE_PIS_AUTHORISATION_REQUEST_CANCELLED))
            .thenReturn(Optional.of(CREATE_PIS_AUTHORISATION_RESPONSE));

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
        when(pisCommonPaymentServiceEncrypted.createAuthorizationCancellation(WRONG_PAYMENT_ID, CREATE_PIS_AUTHORISATION_REQUEST_CANCELLED))
            .thenReturn(Optional.empty());

        // When
        CreatePisAuthorisationResponse actualResponse = pisAuthorisationService.createPisAuthorisationCancellation(WRONG_PAYMENT_ID, PSU_ID_DATA);

        // Then
        assertThat(actualResponse).isNull();
    }

    @Test
    public void getCancellationAuthorisationSubResources_success() {
        // Given
        when(pisCommonPaymentServiceEncrypted.getAuthorisationsByPaymentId(PAYMENT_ID, CmsAuthorisationType.CANCELLED))
            .thenReturn(Optional.of(SOME_LIST));

        // When
        Optional<List<String>> actualResponse = pisAuthorisationService.getCancellationAuthorisationSubResources(PAYMENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse).isEqualTo(Optional.of(SOME_LIST));
    }

    @Test
    public void getCancellationAuthorisationSubResources_wrongPaymentId_fail() {
        // Given
        when(pisCommonPaymentServiceEncrypted.getAuthorisationsByPaymentId(WRONG_PAYMENT_ID, CmsAuthorisationType.CANCELLED))
            .thenReturn(Optional.empty());

        // When
        Optional<List<String>> actualResponse = pisAuthorisationService.getCancellationAuthorisationSubResources(WRONG_PAYMENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void getAuthorisationSubResources_success() {
        // Given
        when(pisCommonPaymentServiceEncrypted.getAuthorisationsByPaymentId(PAYMENT_ID, CmsAuthorisationType.CREATED))
            .thenReturn(Optional.of(SOME_LIST));

        // When
        Optional<List<String>> actualResponse = pisAuthorisationService.getAuthorisationSubResources(PAYMENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse).isEqualTo(Optional.of(SOME_LIST));
    }

    @Test
    public void getAuthorisationSubResources_wrongPaymentId_fail() {
        // Given
        when(pisCommonPaymentServiceEncrypted.getAuthorisationsByPaymentId(WRONG_PAYMENT_ID, CmsAuthorisationType.CREATED))
            .thenReturn(Optional.empty());

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
        return new CreatePisAuthorisationRequest(CmsAuthorisationType.CANCELLED, PSU_ID_DATA, SCA_APPROACH);
    }

    private static CreatePisAuthorisationRequest buildCreatePisAuthorisationRequestCreated() {
        return new CreatePisAuthorisationRequest(CmsAuthorisationType.CREATED, PSU_ID_DATA, SCA_APPROACH);
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
}

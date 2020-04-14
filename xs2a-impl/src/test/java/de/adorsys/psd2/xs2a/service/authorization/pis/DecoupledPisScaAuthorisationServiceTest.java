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
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthorisationSubResources;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPaymentCancellationAuthorisationSubResource;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisCommonPaymentMapper;
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
class DecoupledPisScaAuthorisationServiceTest {
    private static final String PAYMENT_ID = "c713a32c-15ff-4f90-afa0-34a500359844";
    private static final String AUTHORISATION_ID = "ad746cb3-a01b-4196-a6b9-40b0e4cd2350";
    private static final String CANCELLATION_AUTHORISATION_ID = "dd5d766f-eeb7-4efe-b730-24d5ed53f537";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("psu Id", "psuId Type", "psu Corporate Id", "psuCorporate Id Type", "psuIp Address");

    @InjectMocks
    private DecoupledPisScaAuthorisationService decoupledPisScaAuthorisationService;
    @Mock
    private PisAuthorisationService authorisationService;
    @Mock
    private Xs2aPisCommonPaymentMapper pisCommonPaymentMapper;

    @Test
    void getScaApproachServiceType() {
        assertEquals(ScaApproach.DECOUPLED, decoupledPisScaAuthorisationService.getScaApproachServiceType());
    }

    @Test
    void createCommonPaymentAuthorisation() {
        CreateAuthorisationResponse authorisationResponse = new CreateAuthorisationResponse(AUTHORISATION_ID, SCA_STATUS, "2344565", PSU_ID_DATA);
        when(authorisationService.createPisAuthorisation(PAYMENT_ID, PSU_ID_DATA)).thenReturn(authorisationResponse);

        decoupledPisScaAuthorisationService.createCommonPaymentAuthorisation(PAYMENT_ID, PaymentType.SINGLE, PSU_ID_DATA);

        verify(authorisationService, times(1)).createPisAuthorisation(PAYMENT_ID, PSU_ID_DATA);
        verify(pisCommonPaymentMapper, times(1)).mapToXsa2CreatePisAuthorisationResponse(authorisationResponse, PaymentType.SINGLE);
    }

    @Test
    void updateCommonPaymentPsuData() {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = new Xs2aUpdatePisCommonPaymentPsuDataRequest();

        decoupledPisScaAuthorisationService.updateCommonPaymentPsuData(request);
        verify(authorisationService, times(1)).updatePisAuthorisation(request, ScaApproach.DECOUPLED);
    }

    @Test
    void updateAuthorisation() {
        UpdateAuthorisationRequest request = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
        AuthorisationProcessorResponse response = new AuthorisationProcessorResponse();

        decoupledPisScaAuthorisationService.updateAuthorisation(request, response);
        verify(authorisationService, times(1)).updateAuthorisation(request, response);
    }

    @Test
    void updateCancellationAuthorisation() {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
        AuthorisationProcessorResponse response = new AuthorisationProcessorResponse();

        decoupledPisScaAuthorisationService.updateCancellationAuthorisation(request, response);
        verify(authorisationService, times(1)).updateCancellationAuthorisation(request, response);
    }

    @Test
    void createCommonPaymentCancellationAuthorisation() {
        CreateAuthorisationResponse authorisationResponse = new CreateAuthorisationResponse(AUTHORISATION_ID, SCA_STATUS, "2344565", PSU_ID_DATA);
        when(authorisationService.createPisAuthorisationCancellation(PAYMENT_ID, PSU_ID_DATA)).thenReturn(authorisationResponse);

        decoupledPisScaAuthorisationService.createCommonPaymentCancellationAuthorisation(PAYMENT_ID, PaymentType.SINGLE, PSU_ID_DATA);

        verify(authorisationService, times(1)).createPisAuthorisationCancellation(PAYMENT_ID, PSU_ID_DATA);
        verify(pisCommonPaymentMapper, times(1)).mapToXs2aCreatePisCancellationAuthorisationResponse(authorisationResponse, PaymentType.SINGLE);
    }

    @Test
    void updateCommonPaymentCancellationPsuData() {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = new Xs2aUpdatePisCommonPaymentPsuDataRequest();

        decoupledPisScaAuthorisationService.updateCommonPaymentCancellationPsuData(request);
        verify(authorisationService, times(1)).updatePisCancellationAuthorisation(request, ScaApproach.DECOUPLED);
    }

    @Test
    void getAuthorisationSubResources() {
        List<String> authorisationIds = Collections.singletonList(AUTHORISATION_ID);
        when(authorisationService.getAuthorisationSubResources(PAYMENT_ID)).thenReturn(Optional.of(authorisationIds));

        Optional<Xs2aAuthorisationSubResources> actual = decoupledPisScaAuthorisationService.getAuthorisationSubResources(PAYMENT_ID);

        assertTrue(actual.isPresent());
        assertEquals(1, actual.get().getAuthorisationIds().size());
        assertTrue(actual.get().getAuthorisationIds().contains(AUTHORISATION_ID));
    }

    @Test
    void getCancellationAuthorisationSubResources() {
        List<String> authorisationIds = Collections.singletonList(AUTHORISATION_ID);
        when(authorisationService.getCancellationAuthorisationSubResources(PAYMENT_ID)).thenReturn(Optional.of(authorisationIds));

        Optional<Xs2aPaymentCancellationAuthorisationSubResource> actual = decoupledPisScaAuthorisationService.getCancellationAuthorisationSubResources(PAYMENT_ID);

        assertTrue(actual.isPresent());
        assertEquals(1, actual.get().getAuthorisationIds().size());
        assertTrue(actual.get().getAuthorisationIds().contains(AUTHORISATION_ID));
    }

    @Test
    void getAuthorisationScaStatus_success() {
        // Given
        when(authorisationService.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(ScaStatus.RECEIVED));

        // When
        Optional<ScaStatus> actual = decoupledPisScaAuthorisationService.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(ScaStatus.RECEIVED, actual.get());
    }

    @Test
    void getCancellationAuthorisationScaStatus_success() {
        // Given
        when(authorisationService.getCancellationAuthorisationScaStatus(PAYMENT_ID, CANCELLATION_AUTHORISATION_ID))
            .thenReturn(Optional.of(SCA_STATUS));

        // When
        Optional<ScaStatus> actual = decoupledPisScaAuthorisationService.getCancellationAuthorisationScaStatus(PAYMENT_ID, CANCELLATION_AUTHORISATION_ID);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(SCA_STATUS, actual.get());
    }
}

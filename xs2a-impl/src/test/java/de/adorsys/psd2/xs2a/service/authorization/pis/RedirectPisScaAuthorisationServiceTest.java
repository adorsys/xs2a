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

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthorisationSubResources;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPaymentCancellationAuthorisationSubResource;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisCommonPaymentMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.Mapper;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.asm.tree.analysis.Value;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RedirectPisScaAuthorisationServiceTest {
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

    @InjectMocks
    private RedirectPisScaAuthorisationService redirectPisScaAuthorisationService;
    @Mock
    private PisAuthorisationService pisAuthorisationService;
    @Mock
    private Xs2aPisCommonPaymentMapper pisCommonPaymentMapper;



    @Test
    public void getCancellationAuthorisationSubResources_success() {
        // Given
        when(pisAuthorisationService.getCancellationAuthorisationSubResources(PAYMENT_ID))
            .thenReturn(Optional.of(STRING_LIST));
        // When
        Optional<Xs2aPaymentCancellationAuthorisationSubResource> actualResponse = redirectPisScaAuthorisationService.getCancellationAuthorisationSubResources(PAYMENT_ID);
        // Then
        assertTrue(actualResponse.isPresent());
    }

    @Test
    public void updateCommonPaymentCancellationPsuData_success() {
        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = redirectPisScaAuthorisationService.updateCommonPaymentCancellationPsuData(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST);
        // Then
        assertNull(actualResponse);
    }

    @Test // NOT FINISHED
    public void getAuthorisationSubResources_success() {
        // Given
        when(pisAuthorisationService.getAuthorisationSubResources(PAYMENT_ID).map(Xs2aAuthorisationSubResources::new))
            .thenReturn(Optional.of(XS2A_AUTHORISATION_SUB_RESOURCES));
//        when(pisAuthorisationService.getAuthorisationSubResources(PAYMENT_ID))
//            .thenReturn(Optional.of(STRING_LIST));

        // When
        Optional<Xs2aAuthorisationSubResources> actual = redirectPisScaAuthorisationService.getAuthorisationSubResources(PAYMENT_ID);

        // Then
        assertTrue(actual.isPresent());
        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(Optional.of(Xs2aAuthorisationSubResources.class));

    }
    // ============================================================================================

    @Test
    public void getAuthorisationScaStatus_success() {
        // Given
        when(pisAuthorisationService.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(SCA_STATUS));

        // When
        Optional<ScaStatus> actual = redirectPisScaAuthorisationService.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(SCA_STATUS, actual.get());
    }

    @Test
    public void getAuthorisationScaStatus_failure_wrongIds() {
        // Given
        when(pisAuthorisationService.getAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<ScaStatus> actual = redirectPisScaAuthorisationService.getAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertFalse(actual.isPresent());
    }

    @Test
    public void getCancellationAuthorisationScaStatus_success() {
        // Given
        when(pisAuthorisationService.getCancellationAuthorisationScaStatus(PAYMENT_ID, CANCELLATION_AUTHORISATION_ID))
            .thenReturn(Optional.of(SCA_STATUS));

        // When
        Optional<ScaStatus> actual = redirectPisScaAuthorisationService.getCancellationAuthorisationScaStatus(PAYMENT_ID, CANCELLATION_AUTHORISATION_ID);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(SCA_STATUS, actual.get());
    }

    @Test
    public void getCancellationAuthorisationScaStatus_failure_wrongIds() {
        // Given
        when(pisAuthorisationService.getCancellationAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_CANCELLATION_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<ScaStatus> actual = redirectPisScaAuthorisationService.getCancellationAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_CANCELLATION_AUTHORISATION_ID);

        // Then
        assertFalse(actual.isPresent());
    }

    @Test
    public void getScaApproachServiceType_test() {
        //When
        ScaApproach actualResponse = redirectPisScaAuthorisationService.getScaApproachServiceType();
        //Then
        assertThat(actualResponse).isNotNull();
    }
}

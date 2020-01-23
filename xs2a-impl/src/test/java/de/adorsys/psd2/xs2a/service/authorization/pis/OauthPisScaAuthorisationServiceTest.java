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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OauthPisScaAuthorisationServiceTest {
    private static final String PAYMENT_ID = "c713a32c-15ff-4f90-afa0-34a500359844";
    private static final String AUTHORISATION_ID = "ad746cb3-a01b-4196-a6b9-40b0e4cd2350";
    private static final String CANCELLATION_AUTHORISATION_ID = "dd5d766f-eeb7-4efe-b730-24d5ed53f537";
    private static final PaymentType PAYMENT_TYPE = PaymentType.SINGLE;
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("Test psuId", null, null, null);
    private static final Xs2aUpdatePisCommonPaymentPsuDataRequest XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST = new Xs2aUpdatePisCommonPaymentPsuDataRequest();

    @InjectMocks
    private OauthPisScaAuthorisationService oauthPisScaAuthorisationService;

    @Test
    void createCommonPaymentAuthorisation_success() {
        // When
        Optional<Xs2aCreatePisAuthorisationResponse> actualResponse = oauthPisScaAuthorisationService.createCommonPaymentAuthorisation(PAYMENT_ID, PAYMENT_TYPE, PSU_ID_DATA);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void updateCommonPaymentPsuData_success() {
        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = oauthPisScaAuthorisationService.updateCommonPaymentPsuData(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST);

        // Then
        assertThat(actualResponse).isNull();
    }

    @Test
    void createCommonPaymentCancellationAuthorisation_success() {
        // When
        Optional<Xs2aCreatePisCancellationAuthorisationResponse> actualResponse = oauthPisScaAuthorisationService.createCommonPaymentCancellationAuthorisation(PAYMENT_ID, PAYMENT_TYPE, PSU_ID_DATA);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void getCancellationAuthorisationSubResources_success() {
        // When
        Optional<Xs2aPaymentCancellationAuthorisationSubResource> actualResponse = oauthPisScaAuthorisationService.getCancellationAuthorisationSubResources(PAYMENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void updateCommonPaymentCancellationPsuData_success() {
        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = oauthPisScaAuthorisationService.updateCommonPaymentCancellationPsuData(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST);

        // Then
        assertThat(actualResponse).isNull();
    }

    @Test
    void getAuthorisationSubResources_success() {
        // When
        Optional<Xs2aAuthorisationSubResources> actualResponse = oauthPisScaAuthorisationService.getAuthorisationSubResources(PAYMENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void getAuthorisationScaStatus_success() {
        // When
        Optional<ScaStatus> actualResponse = oauthPisScaAuthorisationService.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void getCancellationAuthorisationScaStatus_success() {
        // When
        Optional<ScaStatus> actualResponse = oauthPisScaAuthorisationService.getCancellationAuthorisationScaStatus(PAYMENT_ID, CANCELLATION_AUTHORISATION_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void getScaApproachServiceType_success() {
        //When
        ScaApproach actualResponse = oauthPisScaAuthorisationService.getScaApproachServiceType();

        //Then
        assertThat(actualResponse).isNotNull();
    }
}

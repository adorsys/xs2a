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

import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedirectPisScaAuthorisationServiceTest {
    private static final String PAYMENT_ID = "c713a32c-15ff-4f90-afa0-34a500359844";
    private static final String WRONG_PAYMENT_ID = "wrong payment id";
    private static final String AUTHORISATION_ID = "ad746cb3-a01b-4196-a6b9-40b0e4cd2350";
    private static final String WRONG_AUTHORISATION_ID = "wrong authorisation id";
    private static final String CANCELLATION_AUTHORISATION_ID = "dd5d766f-eeb7-4efe-b730-24d5ed53f537";
    private static final String WRONG_CANCELLATION_AUTHORISATION_ID = "wrong cancellation authorisation id";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final ScaApproach SCA_APPROACH = ScaApproach.REDIRECT;
    private static final List<String> STRING_LIST = Collections.singletonList(PAYMENT_ID);
    private static final Xs2aAuthorisationSubResources XS2A_AUTHORISATION_SUB_RESOURCES = new Xs2aAuthorisationSubResources(STRING_LIST);
    private static final PaymentAuthorisationParameters XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST = new PaymentAuthorisationParameters();
    private static final Xs2aPaymentCancellationAuthorisationSubResource XS2A_PAYMENT_CANCELLATION_AUTHORISATION_SUB_RESOURCE = new Xs2aPaymentCancellationAuthorisationSubResource(STRING_LIST);
    private static final PaymentType PAYMENT_TYPE = PaymentType.SINGLE;
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("Test psuId", null, null, null, null);
    private static final CreateAuthorisationResponse CREATE_PIS_AUTHORISATION_RESPONSE = new CreateAuthorisationResponse(AUTHORISATION_ID, SCA_STATUS, null, null, SCA_APPROACH);
    private static final CreateAuthorisationResponse WRONG_CREATE_PIS_AUTHORISATION_RESPONSE = new CreateAuthorisationResponse(WRONG_AUTHORISATION_ID, SCA_STATUS, null, null, SCA_APPROACH);
    private static final Xs2aCreatePisCancellationAuthorisationResponse XS2A_CREATE_PIS_CANCELLATION_AUTHORISATION_RESPONSE = new Xs2aCreatePisCancellationAuthorisationResponse(CREATE_PIS_AUTHORISATION_RESPONSE.getAuthorizationId(), ScaStatus.RECEIVED, PAYMENT_TYPE, null);
    private static final Xs2aUpdatePisCommonPaymentPsuDataResponse XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_RESPONSE = new Xs2aUpdatePisCommonPaymentPsuDataResponse();
    private static final Xs2aCreatePisAuthorisationResponse XS2A_CREATE_PIS_AUTHORISATION_RESPONSE = new Xs2aCreatePisAuthorisationResponse(AUTHORISATION_ID, SCA_STATUS, PAYMENT_TYPE, null, null, null, null, SCA_APPROACH);

    @InjectMocks
    private RedirectPisScaAuthorisationService redirectPisScaAuthorisationService;
    @Mock
    private PisAuthorisationService pisAuthorisationService;
    @Mock
    private Xs2aPisCommonPaymentMapper pisCommonPaymentMapper;
    @Mock
    private PisAuthorisationConfirmationService pisAuthorisationConfirmationService;

    @Test
    void createCommonPaymentAuthorisation_success() {
        // Given
        Xs2aCreateAuthorisationRequest xs2aCreateAuthorisationRequest = Xs2aCreateAuthorisationRequest.builder()
                                                                            .paymentId(PAYMENT_ID)
                                                                            .psuData(PSU_ID_DATA)
                                                                            .scaStatus(SCA_STATUS)
                                                                            .scaApproach(SCA_APPROACH)
                                                                            .authorisationId(AUTHORISATION_ID)
                                                                            .build();
        when(pisAuthorisationService.createPisAuthorisation(xs2aCreateAuthorisationRequest))
            .thenReturn(CREATE_PIS_AUTHORISATION_RESPONSE);
        when(pisCommonPaymentMapper.mapToXsa2CreatePisAuthorisationResponse(CREATE_PIS_AUTHORISATION_RESPONSE, PAYMENT_TYPE))
            .thenReturn(Optional.of(XS2A_CREATE_PIS_AUTHORISATION_RESPONSE));

        // When
        Optional<Xs2aCreatePisAuthorisationResponse> actualResponse = redirectPisScaAuthorisationService.createCommonPaymentAuthorisation(xs2aCreateAuthorisationRequest, PAYMENT_TYPE);

        // Then
        assertTrue(actualResponse.isPresent());
        assertThat(actualResponse).isEqualTo(Optional.of(XS2A_CREATE_PIS_AUTHORISATION_RESPONSE));
    }

    @Test
    void createCommonPaymentAuthorisation_wrongId_fail() {
        // Given
        Xs2aCreateAuthorisationRequest xs2aCreateAuthorisationRequest = Xs2aCreateAuthorisationRequest.builder()
                                                                            .paymentId(WRONG_PAYMENT_ID)
                                                                            .psuData(PSU_ID_DATA)
                                                                            .scaStatus(SCA_STATUS)
                                                                            .scaApproach(SCA_APPROACH)
                                                                            .authorisationId(AUTHORISATION_ID)
                                                                            .build();
        when(pisAuthorisationService.createPisAuthorisation(xs2aCreateAuthorisationRequest))
            .thenReturn(WRONG_CREATE_PIS_AUTHORISATION_RESPONSE);
        when(pisCommonPaymentMapper.mapToXsa2CreatePisAuthorisationResponse(WRONG_CREATE_PIS_AUTHORISATION_RESPONSE, PAYMENT_TYPE))
            .thenReturn(Optional.empty());

        // When
        Optional<Xs2aCreatePisAuthorisationResponse> actualResponse = redirectPisScaAuthorisationService.createCommonPaymentAuthorisation(xs2aCreateAuthorisationRequest, PAYMENT_TYPE);

        // Then
        assertThat(actualResponse).isNotPresent();
    }

    @Test
    void updateCommonPaymentPsuData_success() {
        // Given
        when(pisAuthorisationConfirmationService.processAuthorisationConfirmation(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST))
            .thenReturn(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_RESPONSE);
        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = redirectPisScaAuthorisationService.updateCommonPaymentPsuData(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST);

        // Then
        assertThat(actualResponse).isEqualTo(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_RESPONSE);
    }

    @Test
    void updateCommonPaymentPsuData_fail() {
        // Given
        Xs2aUpdatePisCommonPaymentPsuDataResponse errorResponse = buildErrorXs2aUpdatePisCommonPaymentPsuDataResponse();
        when(pisAuthorisationConfirmationService.processAuthorisationConfirmation(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST))
            .thenReturn(errorResponse);

        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = redirectPisScaAuthorisationService.updateCommonPaymentPsuData(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST);

        // Then
        assertThat(actualResponse).isEqualTo(errorResponse);
    }

    @Test
    void createCommonPaymentCancellationAuthorisation_success() {
        // Given
        Xs2aCreateAuthorisationRequest xs2aCreateAuthorisationRequest = Xs2aCreateAuthorisationRequest.builder()
                                                                            .paymentId(PAYMENT_ID)
                                                                            .psuData(PSU_ID_DATA)
                                                                            .scaStatus(SCA_STATUS)
                                                                            .scaApproach(SCA_APPROACH)
                                                                            .authorisationId(AUTHORISATION_ID)
                                                                            .build();
        when(pisAuthorisationService.createPisAuthorisationCancellation(xs2aCreateAuthorisationRequest))
            .thenReturn(CREATE_PIS_AUTHORISATION_RESPONSE);
        when(pisCommonPaymentMapper.mapToXs2aCreatePisCancellationAuthorisationResponse(CREATE_PIS_AUTHORISATION_RESPONSE, PAYMENT_TYPE))
            .thenReturn(Optional.of(XS2A_CREATE_PIS_CANCELLATION_AUTHORISATION_RESPONSE));

        // When
        Optional<Xs2aCreatePisCancellationAuthorisationResponse> actualResponse = redirectPisScaAuthorisationService.createCommonPaymentCancellationAuthorisation(xs2aCreateAuthorisationRequest, PAYMENT_TYPE);

        // Then
        assertTrue(actualResponse.isPresent());
        assertThat(actualResponse).isEqualTo(Optional.of(XS2A_CREATE_PIS_CANCELLATION_AUTHORISATION_RESPONSE));
    }

    @Test
    void createCommonPaymentCancellationAuthorisation_fail() {
        // Given
        Xs2aCreateAuthorisationRequest xs2aCreateAuthorisationRequest = Xs2aCreateAuthorisationRequest.builder()
                                                                            .paymentId(WRONG_PAYMENT_ID)
                                                                            .psuData(PSU_ID_DATA)
                                                                            .scaStatus(SCA_STATUS)
                                                                            .scaApproach(SCA_APPROACH)
                                                                            .authorisationId(AUTHORISATION_ID)
                                                                            .build();
        when(pisAuthorisationService.createPisAuthorisationCancellation(xs2aCreateAuthorisationRequest))
            .thenReturn(WRONG_CREATE_PIS_AUTHORISATION_RESPONSE);
        when(pisCommonPaymentMapper.mapToXs2aCreatePisCancellationAuthorisationResponse(WRONG_CREATE_PIS_AUTHORISATION_RESPONSE, PAYMENT_TYPE))
            .thenReturn(Optional.empty());

        // When
        Optional<Xs2aCreatePisCancellationAuthorisationResponse> actualResponse = redirectPisScaAuthorisationService.createCommonPaymentCancellationAuthorisation(xs2aCreateAuthorisationRequest, PAYMENT_TYPE);

        // Then
        assertThat(actualResponse).isNotPresent();
    }

    @Test
    void getCancellationAuthorisationSubResources_success() {
        // Given
        when(pisAuthorisationService.getCancellationAuthorisationSubResources(PAYMENT_ID))
            .thenReturn(Optional.of(STRING_LIST));

        // When
        Optional<Xs2aPaymentCancellationAuthorisationSubResource> actualResponse = redirectPisScaAuthorisationService.getCancellationAuthorisationSubResources(PAYMENT_ID);

        // Then
        assertTrue(actualResponse.isPresent());
        assertThat(actualResponse).isEqualTo(Optional.of(XS2A_PAYMENT_CANCELLATION_AUTHORISATION_SUB_RESOURCE));
    }

    @Test
    void getCancellationAuthorisationSubResources_wrongPaymentId_fail() {
        // Given
        when(pisAuthorisationService.getCancellationAuthorisationSubResources(WRONG_PAYMENT_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<Xs2aPaymentCancellationAuthorisationSubResource> actualResponse = redirectPisScaAuthorisationService.getCancellationAuthorisationSubResources(WRONG_PAYMENT_ID);

        // Then
        assertThat(actualResponse).isNotPresent();
    }

    @Test
    void updateCommonPaymentCancellationPsuData_success() {
        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = redirectPisScaAuthorisationService.updateCommonPaymentCancellationPsuData(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST);

        // Then
        assertNull(actualResponse);
    }

    @Test
    void getAuthorisationSubResources_success() {
        // Given
        when(pisAuthorisationService.getAuthorisationSubResources(PAYMENT_ID))
            .thenReturn(Optional.of(STRING_LIST));

        // When
        Optional<Xs2aAuthorisationSubResources> actualResponse = redirectPisScaAuthorisationService.getAuthorisationSubResources(PAYMENT_ID);

        // Then
        assertTrue(actualResponse.isPresent());
        assertThat(actualResponse).isEqualTo(Optional.of(XS2A_AUTHORISATION_SUB_RESOURCES));
    }

    @Test
    void getAuthorisationSubResources_wrongPaymentId_fail() {
        // Given
        when(pisAuthorisationService.getAuthorisationSubResources(WRONG_PAYMENT_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<Xs2aAuthorisationSubResources> actualResponse = redirectPisScaAuthorisationService.getAuthorisationSubResources(WRONG_PAYMENT_ID);

        // Then
        assertThat(actualResponse).isNotPresent();
    }

    @Test
    void getAuthorisationScaStatus_success() {
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
    void getAuthorisationScaStatus_wrongIds_fail() {
        // Given
        when(pisAuthorisationService.getAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<ScaStatus> actual = redirectPisScaAuthorisationService.getAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertFalse(actual.isPresent());
    }

    @Test
    void getCancellationAuthorisationScaStatus_success() {
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
    void getCancellationAuthorisationScaStatus_wrongIds_fail() {
        // Given
        when(pisAuthorisationService.getCancellationAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_CANCELLATION_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<ScaStatus> actual = redirectPisScaAuthorisationService.getCancellationAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_CANCELLATION_AUTHORISATION_ID);

        // Then
        assertFalse(actual.isPresent());
    }

    @Test
    void getScaApproachServiceType_success() {
        // When
        ScaApproach actualResponse = redirectPisScaAuthorisationService.getScaApproachServiceType();

        // Then
        assertThat(actualResponse).isEqualTo(ScaApproach.REDIRECT);
    }


    private Xs2aUpdatePisCommonPaymentPsuDataResponse buildErrorXs2aUpdatePisCommonPaymentPsuDataResponse() {
        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_NO_PSU))
                                      .build();
        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, PAYMENT_ID, AUTHORISATION_ID, PSU_ID_DATA);

    }
}

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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationChainResponsibilityService;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.piis.AbstractPiisAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.piis.PiisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import de.adorsys.psd2.xs2a.service.event.EventTypeService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.validator.ConsentEndpointAccessCheckerService;
import de.adorsys.psd2.xs2a.service.validator.piis.dto.CreatePiisConsentAuthorisationObject;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PiisConsentAuthorisationServiceTest {
    private static final String PASSWORD = "password";
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String WRONG_CONSENT_ID = "wrong_consent_id";
    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";
    private static final String CORRECT_PSU_ID = "marion.mueller";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(CORRECT_PSU_ID, null, null, null, null);

    private static final MessageError CONSENT_INVALID_401_ERROR =
        new MessageError(ErrorType.PIIS_401, TppMessageInformation.of(MessageErrorCode.CONSENT_INVALID));
    private static final MessageError CONSENT_UNKNOWN_403_ERROR =
        new MessageError(ErrorType.PIIS_403, TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_403));
    private static final MessageError GET_AUTHORISATIONS_MESSAGE_ERROR = new MessageError(ErrorType.PIIS_403, TppMessageInformation.of(MessageErrorCode.FORBIDDEN));
    private static final MessageError RESOURCE_UNKNOWN_MESSAGE_ERROR = new MessageError(ErrorType.PIIS_404, TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404));

    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final ScaApproach SCA_APPROACH = ScaApproach.EMBEDDED;
    private static final String PSU_MESSAGE = "psu message";
    private static final Set<TppMessageInformation> TEST_TPP_MESSAGES = Collections.singleton(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR));

    @InjectMocks
    private PiisConsentAuthorisationService service;

    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private Xs2aPiisConsentService xs2aPiisConsentService;
    @Mock
    private PiisScaAuthorisationServiceResolver piisScaAuthorisationServiceResolver;
    @Mock
    private ConfirmationOfFundsConsentValidationService confirmationOfFundsConsentValidationService;
    @Mock
    private AbstractPiisAuthorizationService redirectPiisAuthorizationService;
    @Mock
    private Xs2aAuthorisationService xs2aAuthorisationService;
    @Mock
    private PsuIdDataAuthorisationService psuIdDataAuthorisationService;
    @Mock
    private ConsentEndpointAccessCheckerService endpointAccessCheckerService;
    @Mock
    private LoggingContextService loggingContextService;
    @Mock
    private AbstractPiisAuthorizationService piisAuthorizationService;
    @Mock
    private AuthorisationChainResponsibilityService authorisationChainResponsibilityService;
    @Mock
    private EventTypeService eventTypeService;
    @Mock
    private ScaApproachResolver scaApproachResolver;

    private final JsonReader jsonReader = new JsonReader();
    private PiisConsent piisConsent;
    private CreatePiisConsentAuthorisationObject createPiisConsentAuthorisationObject;

    @BeforeEach
    void setUp() {
        piisConsent = jsonReader.getObjectFromFile("json/service/piis-consent.json", PiisConsent.class);
        createPiisConsentAuthorisationObject = new CreatePiisConsentAuthorisationObject(piisConsent, PSU_ID_DATA);
    }

    @Test
    void createPiisAuthorisation_withUnknownConsent_shouldReturnConsentUnknownError() {
        // Given
        when(xs2aPiisConsentService.getPiisConsentById(WRONG_CONSENT_ID)).thenReturn(Optional.empty());
        // When
        ResponseObject<AuthorisationResponse> response = service.createPiisAuthorisation(PSU_ID_DATA, WRONG_CONSENT_ID, "");
        // Then
        assertThat(response.getError()).isEqualTo(CONSENT_UNKNOWN_403_ERROR);
    }

    @Test
    void createPiisAuthorisation_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID)).thenReturn(Optional.of(piisConsent));
        when(confirmationOfFundsConsentValidationService.validateConsentAuthorisationOnCreate(any(CreatePiisConsentAuthorisationObject.class)))
            .thenReturn(ValidationResult.invalid(CONSENT_INVALID_401_ERROR));
        // When
        ResponseObject<AuthorisationResponse> actualResponse = service.createPiisAuthorisation(PSU_ID_DATA, CONSENT_ID, PASSWORD);
        // Then
        verify(xs2aPiisConsentService, times(1)).getPiisConsentById(CONSENT_ID);
        verify(confirmationOfFundsConsentValidationService, times(1)).validateConsentAuthorisationOnCreate(createPiisConsentAuthorisationObject);
        assertValidationError(actualResponse, CONSENT_INVALID_401_ERROR);
    }

    @Test
    void createPiisAuthorisation_failedOnCreation_shouldReturnError() {
        // Given
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID)).thenReturn(Optional.of(piisConsent));
        when(confirmationOfFundsConsentValidationService.validateConsentAuthorisationOnCreate(any(CreatePiisConsentAuthorisationObject.class)))
            .thenReturn(ValidationResult.valid());
        when(piisScaAuthorisationServiceResolver.getService())
            .thenReturn(redirectPiisAuthorizationService);
        when(scaApproachResolver.resolveScaApproach()).thenReturn(SCA_APPROACH);
        CreateConsentAuthorisationProcessorResponse response = new CreateConsentAuthorisationProcessorResponse(SCA_STATUS, SCA_APPROACH, PSU_MESSAGE, TEST_TPP_MESSAGES, CONSENT_ID, PSU_ID_DATA);
        when(authorisationChainResponsibilityService.apply(any())).thenReturn(response);
        when(redirectPiisAuthorizationService.createConsentAuthorization(any()))
            .thenReturn(Optional.empty());
        // When
        ResponseObject<AuthorisationResponse> actualResponse = service.createPiisAuthorisation(PSU_ID_DATA, CONSENT_ID, PASSWORD);
        // Then
        verify(xs2aPiisConsentService, times(1)).getPiisConsentById(CONSENT_ID);
        verify(confirmationOfFundsConsentValidationService, times(1)).validateConsentAuthorisationOnCreate(createPiisConsentAuthorisationObject);
        assertValidationError(actualResponse, CONSENT_UNKNOWN_403_ERROR);
    }

    @Test
    void createPiisAuthorisation_success() {
        // Given
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID)).thenReturn(Optional.of(piisConsent));
        when(confirmationOfFundsConsentValidationService.validateConsentAuthorisationOnCreate(any(CreatePiisConsentAuthorisationObject.class)))
            .thenReturn(ValidationResult.valid());

        when(piisScaAuthorisationServiceResolver.getService(AUTHORISATION_ID)).thenReturn(piisAuthorizationService);
        when(piisScaAuthorisationServiceResolver.getService()).thenReturn(piisAuthorizationService);

        CreateConsentAuthorizationResponse createConsentAuthorizationResponse = buildCreateConsentAuthorizationResponse();
        when(piisAuthorizationService.createConsentAuthorization(any()))
            .thenReturn(Optional.of(createConsentAuthorizationResponse));

        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, false)).thenReturn(true);
        when(confirmationOfFundsConsentValidationService.validateConsentPsuDataOnUpdate(piisConsent, buildUpdateConsentPsuDataReq())).thenReturn(ValidationResult.valid());
        when(piisAuthorizationService.getConsentAuthorizationById(AUTHORISATION_ID)).thenReturn(Optional.of(buildAuthorisation()));
        when(scaApproachResolver.resolveScaApproach()).thenReturn(SCA_APPROACH);
        CreateConsentAuthorisationProcessorResponse response = new CreateConsentAuthorisationProcessorResponse(SCA_STATUS, SCA_APPROACH, PSU_MESSAGE, TEST_TPP_MESSAGES, CONSENT_ID, PSU_ID_DATA);
        when(authorisationChainResponsibilityService.apply(any())).thenReturn(response, buildUpdateConsentPsuDataResponse());
        // When
        ResponseObject<AuthorisationResponse> actualResponse = service.createPiisAuthorisation(PSU_ID_DATA, CONSENT_ID, PASSWORD);
        // Then
        verify(xs2aPiisConsentService, times(2)).getPiisConsentById(CONSENT_ID);
        verify(confirmationOfFundsConsentValidationService, times(1)).validateConsentAuthorisationOnCreate(createPiisConsentAuthorisationObject);
        assertEquals(buildUpdateConsentPsuDataResponse(), actualResponse.getBody());
    }

    @Test
    void getConsentInitiationAuthorisations_withUnknownConsent_shouldReturnConsentUnknownError() {
        //Given
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID)).thenReturn(Optional.empty());
        //When
        ResponseObject<Xs2aAuthorisationSubResources> xs2aAuthorisationSubResourcesResponseObject = service.getConsentInitiationAuthorisations(CONSENT_ID);
        //Then
        verify(xs2aEventService, atLeastOnce()).recordConsentTppRequest(CONSENT_ID, EventType.GET_PIIS_CONSENT_AUTHORISATION_REQUEST_RECEIVED);
        verify(xs2aPiisConsentService, atLeastOnce()).getPiisConsentById(CONSENT_ID);
        assertThat(xs2aAuthorisationSubResourcesResponseObject.getError()).isEqualTo(CONSENT_UNKNOWN_403_ERROR);
    }

    @Test
    void getConsentInitiationAuthorisations_validationError_shouldReturnForbiddenError() {
        //Given
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID)).thenReturn(Optional.of(piisConsent));
        when(confirmationOfFundsConsentValidationService.validateConsentAuthorisationOnGettingById(piisConsent))
            .thenReturn(ValidationResult.invalid(GET_AUTHORISATIONS_MESSAGE_ERROR));
        //When
        ResponseObject<Xs2aAuthorisationSubResources> xs2aAuthorisationSubResourcesResponseObject = service.getConsentInitiationAuthorisations(CONSENT_ID);
        //Then
        verify(xs2aEventService, atLeastOnce()).recordConsentTppRequest(CONSENT_ID, EventType.GET_PIIS_CONSENT_AUTHORISATION_REQUEST_RECEIVED);
        verify(xs2aPiisConsentService, atLeastOnce()).getPiisConsentById(CONSENT_ID);
        verify(confirmationOfFundsConsentValidationService, atLeastOnce()).validateConsentAuthorisationOnGettingById(piisConsent);
        assertValidationError(xs2aAuthorisationSubResourcesResponseObject, GET_AUTHORISATIONS_MESSAGE_ERROR);
    }

    @Test
    void getConsentInitiationAuthorisations_getAuthorisationsFailed_shouldReturnResourceUnknownError() {
        //Given
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID)).thenReturn(Optional.of(piisConsent));
        when(confirmationOfFundsConsentValidationService.validateConsentAuthorisationOnGettingById(piisConsent))
            .thenReturn(ValidationResult.valid());
        when(xs2aAuthorisationService.getAuthorisationSubResources(CONSENT_ID, AuthorisationType.CONSENT))
            .thenReturn(Optional.empty());
        //When
        ResponseObject<Xs2aAuthorisationSubResources> xs2aAuthorisationSubResourcesResponseObject = service.getConsentInitiationAuthorisations(CONSENT_ID);
        //Then
        verify(xs2aEventService, atLeastOnce()).recordConsentTppRequest(CONSENT_ID, EventType.GET_PIIS_CONSENT_AUTHORISATION_REQUEST_RECEIVED);
        verify(xs2aPiisConsentService, atLeastOnce()).getPiisConsentById(CONSENT_ID);
        verify(confirmationOfFundsConsentValidationService, atLeastOnce()).validateConsentAuthorisationOnGettingById(piisConsent);
        verify(xs2aAuthorisationService, atLeastOnce()).getAuthorisationSubResources(CONSENT_ID, AuthorisationType.CONSENT);
        assertValidationError(xs2aAuthorisationSubResourcesResponseObject, RESOURCE_UNKNOWN_MESSAGE_ERROR);
    }

    @Test
    void getConsentInitiationAuthorisations_success() {
        //Given
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID)).thenReturn(Optional.of(piisConsent));
        when(confirmationOfFundsConsentValidationService.validateConsentAuthorisationOnGettingById(piisConsent))
            .thenReturn(ValidationResult.valid());
        List<String> authorisations = Collections.singletonList(AUTHORISATION_ID);
        when(xs2aAuthorisationService.getAuthorisationSubResources(CONSENT_ID, AuthorisationType.CONSENT))
            .thenReturn(Optional.of(authorisations));
        //When
        ResponseObject<Xs2aAuthorisationSubResources> xs2aAuthorisationSubResourcesResponseObject = service.getConsentInitiationAuthorisations(CONSENT_ID);
        //Then
        verify(xs2aEventService, atLeastOnce()).recordConsentTppRequest(CONSENT_ID, EventType.GET_PIIS_CONSENT_AUTHORISATION_REQUEST_RECEIVED);
        verify(xs2aPiisConsentService, atLeastOnce()).getPiisConsentById(CONSENT_ID);
        verify(confirmationOfFundsConsentValidationService, atLeastOnce()).validateConsentAuthorisationOnGettingById(piisConsent);
        verify(xs2aAuthorisationService, atLeastOnce()).getAuthorisationSubResources(CONSENT_ID, AuthorisationType.CONSENT);
        assertEquals(authorisations, xs2aAuthorisationSubResourcesResponseObject.getBody().getAuthorisationIds());
    }

    @Test
    void getConsentAuthorisationScaStatus_withUnknownConsent_shouldReturnConsentUnknownError() {
        //Given
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID)).thenReturn(Optional.empty());
        //When
        ResponseObject<ConfirmationOfFundsConsentScaStatus> confirmationOfFundsConsentScaStatusResponseObject = service.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);
        //Then
        verify(xs2aEventService, atLeastOnce()).recordConsentTppRequest(CONSENT_ID, EventType.GET_PIIS_CONSENT_SCA_STATUS_REQUEST_RECEIVED);
        verify(xs2aPiisConsentService, atLeastOnce()).getPiisConsentById(CONSENT_ID);
        assertThat(confirmationOfFundsConsentScaStatusResponseObject.getError()).isEqualTo(CONSENT_UNKNOWN_403_ERROR);
    }

    @Test
    void getConsentAuthorisationScaStatus_validationError_shouldReturnForbiddenError() {
        //Given
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID)).thenReturn(Optional.of(piisConsent));
        when(confirmationOfFundsConsentValidationService.validateConsentAuthorisationScaStatus(piisConsent, AUTHORISATION_ID))
            .thenReturn(ValidationResult.invalid(GET_AUTHORISATIONS_MESSAGE_ERROR));
        //When
        ResponseObject<ConfirmationOfFundsConsentScaStatus> confirmationOfFundsConsentScaStatusResponseObject = service.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);
        //Then
        verify(xs2aEventService, atLeastOnce()).recordConsentTppRequest(CONSENT_ID, EventType.GET_PIIS_CONSENT_SCA_STATUS_REQUEST_RECEIVED);
        verify(xs2aPiisConsentService, atLeastOnce()).getPiisConsentById(CONSENT_ID);
        verify(confirmationOfFundsConsentValidationService, atLeastOnce()).validateConsentAuthorisationScaStatus(piisConsent, AUTHORISATION_ID);
        assertValidationError(confirmationOfFundsConsentScaStatusResponseObject, GET_AUTHORISATIONS_MESSAGE_ERROR);
    }

    @Test
    void getConsentAuthorisationScaStatus_success() {
        //Given
        ConfirmationOfFundsConsentScaStatus consentScaStatus = new ConfirmationOfFundsConsentScaStatus(PSU_ID_DATA, piisConsent, ScaStatus.RECEIVED);
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID)).thenReturn(Optional.of(piisConsent));
        when(confirmationOfFundsConsentValidationService.validateConsentAuthorisationScaStatus(piisConsent, AUTHORISATION_ID))
            .thenReturn(ValidationResult.valid());
        when(piisScaAuthorisationServiceResolver.getService(AUTHORISATION_ID)).thenReturn(redirectPiisAuthorizationService);
        when(redirectPiisAuthorizationService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(ScaStatus.RECEIVED));
        when(psuIdDataAuthorisationService.getPsuIdData(AUTHORISATION_ID, Collections.singletonList(PSU_ID_DATA))).thenReturn(PSU_ID_DATA);
        //When
        ResponseObject<ConfirmationOfFundsConsentScaStatus> confirmationOfFundsConsentScaStatusResponseObject = service.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);
        //Then
        verify(xs2aEventService, atLeastOnce()).recordConsentTppRequest(CONSENT_ID, EventType.GET_PIIS_CONSENT_SCA_STATUS_REQUEST_RECEIVED);
        verify(xs2aPiisConsentService, atLeastOnce()).getPiisConsentById(CONSENT_ID);
        verify(confirmationOfFundsConsentValidationService, atLeastOnce()).validateConsentAuthorisationScaStatus(piisConsent, AUTHORISATION_ID);
        assertFalse(confirmationOfFundsConsentScaStatusResponseObject.hasError());
        assertEquals(consentScaStatus, confirmationOfFundsConsentScaStatusResponseObject.getBody());
    }

    private CreateConsentAuthorizationResponse buildCreateConsentAuthorizationResponse() {
        CreateConsentAuthorizationResponse createConsentAuthorizationResponse = new CreateConsentAuthorizationResponse();
        createConsentAuthorizationResponse.setAuthorisationId(AUTHORISATION_ID);
        createConsentAuthorizationResponse.setScaStatus(ScaStatus.RECEIVED);
        createConsentAuthorizationResponse.setPsuIdData(PSU_ID_DATA);
        return createConsentAuthorizationResponse;
    }

    private void assertValidationError(ResponseObject<?> response, MessageError messageError) {
        assertThat(response).isNotNull();
        assertThat(response.hasError()).isTrue();
        assertThat(response.getError()).isEqualTo(messageError);
    }

    private ConsentAuthorisationsParameters buildUpdateConsentPsuDataReq() {
        ConsentAuthorisationsParameters updatePsuData = new ConsentAuthorisationsParameters();
        updatePsuData.setPsuData(PSU_ID_DATA);
        updatePsuData.setConsentId(CONSENT_ID);
        updatePsuData.setAuthorizationId(AUTHORISATION_ID);
        updatePsuData.setPassword(PASSWORD);
        return updatePsuData;
    }

    private Authorisation buildAuthorisation() {
        return jsonReader.getObjectFromFile("json/consent-authorisation.json", Authorisation.class);
    }

    private UpdateConsentPsuDataResponse buildUpdateConsentPsuDataResponse() {
        return new UpdateConsentPsuDataResponse(ScaStatus.RECEIVED, CONSENT_ID, AUTHORISATION_ID, PSU_ID_DATA);
    }
}

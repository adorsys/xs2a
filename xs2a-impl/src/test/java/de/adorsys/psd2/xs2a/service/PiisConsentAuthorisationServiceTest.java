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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.ConfirmationOfFundsConsentScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthorisationSubResources;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationChainResponsibilityService;
import de.adorsys.psd2.xs2a.service.authorization.piis.PiisAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.piis.PiisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.authorization.piis.RedirectPiisAuthorizationService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.validator.ConsentEndpointAccessCheckerService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PiisConsentAuthorisationServiceTest {
    private static final String PASSWORD = "password";
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String WRONG_CONSENT_ID = "wrong_consent_id";
    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";
    private static final String WRONG_AUTHORISATION_ID = "wrong authorisation id";
    private static final String CORRECT_PSU_ID = "marion.mueller";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(CORRECT_PSU_ID, null, null, null, null);
    private static final PsuIdData PSU_ID_DATA_EMPTY = new PsuIdData(null, null, null, null, null);
    private static final boolean CONFIRMATION_CODE_RECEIVED_FALSE = false;
    private static final String SCA_AUTHENTICATION_DATA = "some_confirmation_code";

    private static final MessageError CONSENT_INVALID_401_ERROR =
        new MessageError(ErrorType.PIIS_401, TppMessageInformation.of(MessageErrorCode.CONSENT_INVALID));
    private static final MessageError CONSENT_UNKNOWN_403_ERROR =
        new MessageError(ErrorType.PIIS_403, TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_403));
    private static final MessageError GET_AUTHORISATIONS_MESSAGE_ERROR = new MessageError(ErrorType.PIIS_403, TppMessageInformation.of(MessageErrorCode.FORBIDDEN));
    private static final MessageError RESOURCE_UNKNOWN_MESSAGE_ERROR = new MessageError(ErrorType.PIIS_404, TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404));

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
    private RedirectPiisAuthorizationService redirectPiisAuthorizationService;
    @Mock
    private Xs2aAuthorisationService xs2aAuthorisationService;
    @Mock
    private PsuIdDataAuthorisationService psuIdDataAuthorisationService;
    @Mock
    private ConsentEndpointAccessCheckerService endpointAccessCheckerService;
    @Mock
    private LoggingContextService loggingContextService;
    @Mock
    private PiisAuthorizationService piisAuthorizationService;
    @Mock
    private AuthorisationChainResponsibilityService authorisationChainResponsibilityService;

    private JsonReader jsonReader = new JsonReader();
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
        when(redirectPiisAuthorizationService.createConsentAuthorization(any(), anyString()))
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
        when(piisAuthorizationService.createConsentAuthorization(any(), anyString()))
            .thenReturn(Optional.of(createConsentAuthorizationResponse));

        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, false)).thenReturn(true);
        when(confirmationOfFundsConsentValidationService.validateConsentPsuDataOnUpdate(piisConsent, buildUpdateConsentPsuDataReq())).thenReturn(ValidationResult.valid());
        when(piisAuthorizationService.getPiisConsentAuthorizationById(AUTHORISATION_ID)).thenReturn(Optional.of(buildAuthorisation()));
        when(authorisationChainResponsibilityService.apply(any())).thenReturn(buildUpdateConsentPsuDataResponse());
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

    private UpdateConsentPsuDataReq buildUpdateConsentPsuDataReq() {
        UpdateConsentPsuDataReq updatePsuData = new UpdateConsentPsuDataReq();
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

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

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.domain.MessageCategory;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationChainResponsibilityService;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisAuthorisationConfirmationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.authorization.ais.RedirectAisAuthorizationService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.event.EventAuthorisationType;
import de.adorsys.psd2.xs2a.service.event.EventTypeService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.validator.ConsentEndpointAccessCheckerService;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.CreateConsentAuthorisationObject;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsentAuthorisationServiceTest {
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
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(MessageErrorCode.CONSENT_INVALID));
    private static final MessageError CONSENT_UNKNOWN_403_ERROR =
        new MessageError(ErrorType.AIS_403, TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_403));

    @InjectMocks
    private ConsentAuthorisationService service;

    @Mock
    private Xs2aAuthorisationService authorisationService;
    @Mock
    private Xs2aAisConsentService aisConsentService;
    @Mock
    private AisScaAuthorisationServiceResolver aisScaAuthorisationServiceResolver;
    @Mock
    private ConsentEndpointAccessCheckerService endpointAccessCheckerService;
    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private ConsentValidationService consentValidationService;
    @Mock
    private AuthorisationChainResponsibilityService authorisationChainResponsibilityService;
    @Mock
    private LoggingContextService loggingContextService;
    @Mock
    private RedirectAisAuthorizationService redirectAisAuthorizationService;
    @Mock
    private AisAuthorisationConfirmationService aisAuthorisationConfirmationService;
    @Mock
    private PsuIdDataAuthorisationService psuIdDataAuthorisationService;
    @Mock
    private EventTypeService eventTypeService;

    private JsonReader jsonReader = new JsonReader();
    private AisConsent aisConsent;
    private CreateConsentAuthorisationObject createConsentAuthorisationObject;

    @BeforeEach
    void setUp() {
        aisConsent = jsonReader.getObjectFromFile("json/service/ais-consent.json", AisConsent.class);
        createConsentAuthorisationObject = new CreateConsentAuthorisationObject(aisConsent, PSU_ID_DATA);
    }

    @Test
    void getConsentAuthorisationScaStatus_failure() {
        // When
        ResponseObject<ConsentScaStatus> actual = service.getConsentAuthorisationScaStatus(CONSENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertTrue(actual.hasError());
        assertNull(actual.getBody());
    }

    @Test
    void getConsentAuthorisationScaStatus_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentAuthorisationScaStatus(aisConsent, AUTHORISATION_ID))
            .thenReturn(ValidationResult.invalid(CONSENT_INVALID_401_ERROR));

        // When
        ResponseObject<ConsentScaStatus> actualResponse = service.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        verify(consentValidationService).validateConsentAuthorisationScaStatus(aisConsent, AUTHORISATION_ID);
        assertValidationError(actualResponse);
    }

    @Test
    void getConsentAuthorisationScaStatus_withUnknownConsent_shouldReturnConsentUnknownError() {
        // Given
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID)).thenReturn(Optional.empty());

        // When
        ResponseObject<ConsentScaStatus> response = service.getConsentAuthorisationScaStatus(WRONG_CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertThat(response.getError()).isEqualTo(CONSENT_UNKNOWN_403_ERROR);
    }

    @Test
    void getConsentAuthorisationScaStatus_shouldRecordStatusInLoggingContext() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentAuthorisationScaStatus(aisConsent, AUTHORISATION_ID)).thenReturn(ValidationResult.valid());
        when(aisScaAuthorisationServiceResolver.getService(AUTHORISATION_ID)).thenReturn(redirectAisAuthorizationService);
        when(redirectAisAuthorizationService.getAuthorisationScaStatus(any(), any()))
            .thenReturn(Optional.of(ScaStatus.RECEIVED));

        when(psuIdDataAuthorisationService.getPsuIdData(AUTHORISATION_ID, Collections.singletonList(PSU_ID_DATA))).thenReturn(PSU_ID_DATA);
        ArgumentCaptor<ConsentStatus> consentStatusArgumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);
        ArgumentCaptor<ScaStatus> scaStatusArgumentCaptor = ArgumentCaptor.forClass(ScaStatus.class);

        // When
        service.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        verify(aisConsentService, times(1)).getAccountConsentById(CONSENT_ID);
        verify(consentValidationService, times(1)).validateConsentAuthorisationScaStatus(aisConsent, AUTHORISATION_ID);
        verify(aisScaAuthorisationServiceResolver, times(1)).getService(AUTHORISATION_ID);
        verify(loggingContextService, times(1)).storeConsentStatus(consentStatusArgumentCaptor.capture());
        verify(loggingContextService, times(1)).storeScaStatus(scaStatusArgumentCaptor.capture());
        assertThat(consentStatusArgumentCaptor.getValue()).isEqualTo(ConsentStatus.VALID);
        assertThat(scaStatusArgumentCaptor.getValue()).isEqualTo(ScaStatus.RECEIVED);
    }

    @Test
    void getConsentAuthorisationScaStatus_success_shouldRecordEvent() {
        // Given
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        service.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        verify(xs2aEventService, times(1)).recordConsentTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_CONSENT_SCA_STATUS_REQUEST_RECEIVED);
    }

    @Test
    void getConsentAuthorisationScaStatus_success() {
        // Given
        ConsentScaStatus consentScaStatus = new ConsentScaStatus(PSU_ID_DATA, aisConsent, ScaStatus.RECEIVED);

        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentAuthorisationScaStatus(aisConsent, AUTHORISATION_ID)).thenReturn(ValidationResult.valid());
        when(aisScaAuthorisationServiceResolver.getService(AUTHORISATION_ID)).thenReturn(redirectAisAuthorizationService);
        when(redirectAisAuthorizationService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(ScaStatus.RECEIVED));
        when(psuIdDataAuthorisationService.getPsuIdData(AUTHORISATION_ID, Collections.singletonList(PSU_ID_DATA))).thenReturn(PSU_ID_DATA);

        // When
        ResponseObject<ConsentScaStatus> actual = service.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertFalse(actual.hasError());
        assertEquals(consentScaStatus, actual.getBody());
    }

    @Test
    void getConsentAuthorisationScaStatus_scaStatusIsEmpty_resourceUnknown() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentAuthorisationScaStatus(aisConsent, AUTHORISATION_ID)).thenReturn(ValidationResult.valid());
        when(aisScaAuthorisationServiceResolver.getService(AUTHORISATION_ID)).thenReturn(redirectAisAuthorizationService);
        when(redirectAisAuthorizationService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseObject<ConsentScaStatus> actual = service.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertTrue(actual.hasError());
        assertEquals(MessageErrorCode.RESOURCE_UNKNOWN_403, actual.getError().getTppMessage().getMessageErrorCode());
    }

    @Test
    void getConsentInitiationAuthorisations_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentAuthorisationOnGettingById(aisConsent))
            .thenReturn(ValidationResult.invalid(CONSENT_INVALID_401_ERROR));

        // When
        ResponseObject<Xs2aAuthorisationSubResources> actualResponse = service.getConsentInitiationAuthorisations(CONSENT_ID);

        // Then
        verify(aisConsentService, times(1)).getAccountConsentById(CONSENT_ID);
        verify(consentValidationService, times(1)).validateConsentAuthorisationOnGettingById(aisConsent);
        assertValidationError(actualResponse);
    }

    @Test
    void getConsentInitiationAuthorisations_withUnknownConsent_shouldReturnConsentUnknownError() {
        // Given
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID)).thenReturn(Optional.empty());

        // When
        ResponseObject<Xs2aAuthorisationSubResources> response = service.getConsentInitiationAuthorisations(WRONG_CONSENT_ID);

        // Then
        assertThat(response.getError()).isEqualTo(CONSENT_UNKNOWN_403_ERROR);
    }

    @Test
    void getConsentInitiationAuthorisations_shouldRecordStatusInLoggingContext() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentAuthorisationOnGettingById(aisConsent))
            .thenReturn(ValidationResult.valid());

        // When
        service.getConsentInitiationAuthorisations(CONSENT_ID);

        ArgumentCaptor<ConsentStatus> consentStatusArgumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);

        // Then
        verify(loggingContextService).storeConsentStatus(consentStatusArgumentCaptor.capture());
    }

    @Test
    void updateConsentPsuData_withInvalidRequest_shouldReturnValidationError() {
        // Given
        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq(CONSENT_ID);

        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentPsuDataOnUpdate(aisConsent, updateConsentPsuDataReq))
            .thenReturn(ValidationResult.invalid(CONSENT_INVALID_401_ERROR));

        // When
        ResponseObject<UpdateConsentPsuDataResponse> actualResponse = service.updateConsentPsuData(updateConsentPsuDataReq);

        // Then
        verify(consentValidationService).validateConsentPsuDataOnUpdate(aisConsent, updateConsentPsuDataReq);

        assertValidationError(actualResponse);
    }

    @Test
    void updateConsentPsuData_consentIsExpired() {
        // Given
        AisConsent expiredAisConsent = mock(AisConsent.class);
        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq(CONSENT_ID);

        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(expiredAisConsent));
        when(consentValidationService.validateConsentPsuDataOnUpdate(expiredAisConsent, updateConsentPsuDataReq))
            .thenReturn(ValidationResult.valid());
        when(expiredAisConsent.isExpired()).thenReturn(true);

        // When
        ResponseObject<UpdateConsentPsuDataResponse> actualResponse = service.updateConsentPsuData(updateConsentPsuDataReq);

        // Then
        verify(consentValidationService).validateConsentPsuDataOnUpdate(expiredAisConsent, updateConsentPsuDataReq);

        assertTrue(actualResponse.hasError());
        assertEquals(MessageErrorCode.CONSENT_EXPIRED, actualResponse.getError().getTppMessage().getMessageErrorCode());
    }

    @Test
    void updateConsentPsuData_Success_ShouldRecordEvent() {
        // Given
        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq(CONSENT_ID);
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);
        when(eventTypeService.getEventType(updateConsentPsuDataReq, EventAuthorisationType.AIS))
            .thenReturn(EventType.UPDATE_AIS_CONSENT_PSU_DATA_IDENTIFICATION_REQUEST_RECEIVED);
        // When
        service.updateConsentPsuData(updateConsentPsuDataReq);

        // Then
        verify(xs2aEventService, times(1)).recordConsentTppRequest(eq(CONSENT_ID), argumentCaptor.capture(), any());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.UPDATE_AIS_CONSENT_PSU_DATA_IDENTIFICATION_REQUEST_RECEIVED);
    }

    @Test
    void updateConsentPsuData_Failure_EndpointIsNotAccessible() {
        // Given
        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq(CONSENT_ID);
        when(eventTypeService.getEventType(updateConsentPsuDataReq, EventAuthorisationType.AIS))
            .thenReturn(EventType.UPDATE_AIS_CONSENT_PSU_DATA_IDENTIFICATION_REQUEST_RECEIVED);
        doNothing()
            .when(xs2aEventService).recordConsentTppRequest(CONSENT_ID, EventType.UPDATE_AIS_CONSENT_PSU_DATA_IDENTIFICATION_REQUEST_RECEIVED, updateConsentPsuDataReq);
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(aisConsent));

        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(false);

        // When
        ResponseObject<UpdateConsentPsuDataResponse> actualResponse = service.updateConsentPsuData(updateConsentPsuDataReq);

        // Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getErrorType()).isEqualTo(ErrorType.AIS_403);
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.SERVICE_BLOCKED);
        assertThat(actualResponse.getError().getTppMessage().getCategory()).isEqualTo(MessageCategory.ERROR);
    }

    @Test
    void updateConsentPsuData_withUnknownConsent_shouldReturnConsentUnknownError() {
        // Given
        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq(WRONG_CONSENT_ID);
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID)).thenReturn(Optional.empty());

        // When
        ResponseObject<UpdateConsentPsuDataResponse> response = service.updateConsentPsuData(updateConsentPsuDataReq);

        // Then
        assertThat(response.getError()).isEqualTo(CONSENT_UNKNOWN_403_ERROR);
    }

    @Test
    void updateConsentPsuData_shouldRecordStatusInLoggingContext() {
        // Given
        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq(CONSENT_ID);

        when(aisScaAuthorisationServiceResolver.getService(AUTHORISATION_ID)).thenReturn(redirectAisAuthorizationService);
        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentPsuDataOnUpdate(aisConsent, updateConsentPsuDataReq)).thenReturn(ValidationResult.valid());

        Authorisation authorisation = new Authorisation();
        when(redirectAisAuthorizationService.getConsentAuthorizationById(AUTHORISATION_ID))
            .thenReturn(Optional.of(authorisation));

        when(authorisationChainResponsibilityService.apply(any())).thenReturn(new UpdateConsentPsuDataResponse(ScaStatus.RECEIVED, CONSENT_ID, AUTHORISATION_ID, PSU_ID_DATA));

        ArgumentCaptor<ConsentStatus> consentStatusArgumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);
        ArgumentCaptor<ScaStatus> scaStatusArgumentCaptor = ArgumentCaptor.forClass(ScaStatus.class);

        // When
        service.updateConsentPsuData(updateConsentPsuDataReq);

        // Then
        verify(loggingContextService).storeConsentStatus(consentStatusArgumentCaptor.capture());
        verify(loggingContextService).storeScaStatus(scaStatusArgumentCaptor.capture());
        assertThat(consentStatusArgumentCaptor.getValue()).isEqualTo(ConsentStatus.VALID);
        assertThat(scaStatusArgumentCaptor.getValue()).isEqualTo(ScaStatus.RECEIVED);
    }

    @Test
    void updateConsentPsuData_authorisationNotFoundError() {
        // Given
        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq(CONSENT_ID);

        when(aisScaAuthorisationServiceResolver.getService(AUTHORISATION_ID)).thenReturn(redirectAisAuthorizationService);
        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentPsuDataOnUpdate(aisConsent, updateConsentPsuDataReq)).thenReturn(ValidationResult.valid());

        when(redirectAisAuthorizationService.getConsentAuthorizationById(AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseObject<UpdateConsentPsuDataResponse> actualResponseObject = service.updateConsentPsuData(updateConsentPsuDataReq);

        // Then
        assertTrue(actualResponseObject.hasError());
        assertEquals(MessageErrorCode.CONSENT_UNKNOWN_403, actualResponseObject.getError().getTppMessage().getMessageErrorCode());
    }

    @Test
    void updateConsentPsuData_psuCredentialsInvalid_updateScaStatusAsFailed() {
        // Given
        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq(CONSENT_ID);

        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentPsuDataOnUpdate(aisConsent, updateConsentPsuDataReq))
            .thenReturn(ValidationResult.invalid(ErrorType.AIS_400, MessageErrorCode.PSU_CREDENTIALS_INVALID));

        // When
        ResponseObject<UpdateConsentPsuDataResponse> actualResponseObject = service.updateConsentPsuData(updateConsentPsuDataReq);

        // Then
        verify(authorisationService, times(1)).updateAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FAILED);
        assertTrue(actualResponseObject.hasError());
        assertEquals(MessageErrorCode.PSU_CREDENTIALS_INVALID, actualResponseObject.getError().getTppMessage().getMessageErrorCode());
    }

    @Test
    void createConsentAuthorizationWithResponse_Success_ShouldRecordEvent() {
        // Given
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        service.createAisAuthorisation(PSU_ID_DATA, CONSENT_ID, PASSWORD);

        // Then
        verify(xs2aEventService, times(1)).recordConsentTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.START_AIS_CONSENT_AUTHORISATION_REQUEST_RECEIVED);
    }

    @Test
    void createConsentAuthorizationWithResponse_Success_WithoutPsuIdHeader() {
        // Given
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        service.createAisAuthorisation(PSU_ID_DATA_EMPTY, CONSENT_ID, PASSWORD);

        // Then
        verify(xs2aEventService, times(1)).recordConsentTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.START_AIS_CONSENT_AUTHORISATION_REQUEST_RECEIVED);
    }

    @Test
    void createConsentAuthorisationWithResponse_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentAuthorisationOnCreate(any(CreateConsentAuthorisationObject.class)))
            .thenReturn(ValidationResult.invalid(CONSENT_INVALID_401_ERROR));

        // When
        ResponseObject<AuthorisationResponse> actualResponse = service.createAisAuthorisation(PSU_ID_DATA, CONSENT_ID, PASSWORD);

        // Then
        verify(aisConsentService, times(1)).getAccountConsentById(CONSENT_ID);
        verify(consentValidationService, times(1)).validateConsentAuthorisationOnCreate(createConsentAuthorisationObject);
        assertValidationError(actualResponse);
    }

    @Test
    void createAisAuthorisation_PSU_ID_empty_shouldRecordStatusInLoggingContext() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentAuthorisationOnCreate(any(CreateConsentAuthorisationObject.class)))
            .thenReturn(ValidationResult.valid());

        when(aisScaAuthorisationServiceResolver.getService()).thenReturn(redirectAisAuthorizationService);
        CreateConsentAuthorizationResponse createConsentAuthorizationResponse = new CreateConsentAuthorizationResponse();
        createConsentAuthorizationResponse.setScaStatus(ScaStatus.RECEIVED);
        when(redirectAisAuthorizationService.createConsentAuthorization(any(), anyString()))
            .thenReturn(Optional.of(createConsentAuthorizationResponse));
        ArgumentCaptor<ConsentStatus> consentStatusArgumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);
        ArgumentCaptor<ScaStatus> scaStatusArgumentCaptor = ArgumentCaptor.forClass(ScaStatus.class);

        // When
        service.createAisAuthorisation(PSU_ID_DATA_EMPTY, CONSENT_ID, PASSWORD);

        // Then
        verify(loggingContextService).storeScaStatus(scaStatusArgumentCaptor.capture());
        assertThat(scaStatusArgumentCaptor.getValue()).isEqualTo(ScaStatus.RECEIVED);
        verify(loggingContextService).storeConsentStatus(consentStatusArgumentCaptor.capture());
        assertThat(consentStatusArgumentCaptor.getValue()).isEqualTo(ConsentStatus.VALID);
    }

    @Test
    void createAisAuthorisation_shouldRecordStatusInLoggingContext() {
        // Given
        when(aisScaAuthorisationServiceResolver.getService()).thenReturn(redirectAisAuthorizationService);
        CreateConsentAuthorizationResponse createConsentAuthorizationResponse = new CreateConsentAuthorizationResponse();
        createConsentAuthorizationResponse.setAuthorisationId(AUTHORISATION_ID);
        createConsentAuthorizationResponse.setScaStatus(ScaStatus.RECEIVED);
        createConsentAuthorizationResponse.setPsuIdData(PSU_ID_DATA);
        when(redirectAisAuthorizationService.createConsentAuthorization(any(), anyString()))
            .thenReturn(Optional.of(createConsentAuthorizationResponse));
        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);
        when(aisScaAuthorisationServiceResolver.getService(AUTHORISATION_ID)).thenReturn(redirectAisAuthorizationService);
        when(redirectAisAuthorizationService.getConsentAuthorizationById(AUTHORISATION_ID))
            .thenReturn(Optional.of(new Authorisation()));

        UpdateConsentPsuDataReq updatePsuData = buildUpdateConsentPsuDataReq(CONSENT_ID);
        updatePsuData.setPassword(PASSWORD);
        UpdateConsentPsuDataResponse updateConsentPsuDataResponse = new UpdateConsentPsuDataResponse(ScaStatus.RECEIVED, CONSENT_ID, AUTHORISATION_ID, PSU_ID_DATA);
        updateConsentPsuDataResponse.setScaStatus(ScaStatus.RECEIVED);

        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentAuthorisationOnCreate(any(CreateConsentAuthorisationObject.class)))
            .thenReturn(ValidationResult.valid());
        when(consentValidationService.validateConsentPsuDataOnUpdate(aisConsent, updatePsuData))
            .thenReturn(ValidationResult.valid());
        when(authorisationChainResponsibilityService.apply(any())).thenReturn(new UpdateConsentPsuDataResponse(ScaStatus.RECEIVED, CONSENT_ID, AUTHORISATION_ID, PSU_ID_DATA));

        ArgumentCaptor<ConsentStatus> consentStatusArgumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);
        ArgumentCaptor<ScaStatus> scaStatusArgumentCaptor = ArgumentCaptor.forClass(ScaStatus.class);

        // When
        service.createAisAuthorisation(PSU_ID_DATA, CONSENT_ID, PASSWORD);

        // Then
        verify(aisConsentService, times(2)).getAccountConsentById(CONSENT_ID);
        verify(consentValidationService, times(1)).validateConsentAuthorisationOnCreate(createConsentAuthorisationObject);
        verify(consentValidationService, times(1)).validateConsentPsuDataOnUpdate(aisConsent, updatePsuData);
        verify(loggingContextService).storeScaStatus(scaStatusArgumentCaptor.capture());
        assertThat(scaStatusArgumentCaptor.getValue()).isEqualTo(ScaStatus.RECEIVED);
        verify(loggingContextService, times(2)).storeConsentStatus(consentStatusArgumentCaptor.capture());
        assertThat(consentStatusArgumentCaptor.getValue()).isEqualTo(ConsentStatus.VALID);
    }

    @Test
    void createAisAuthorisation_updateConsentPsuDataWithError() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentAuthorisationOnCreate(any(CreateConsentAuthorisationObject.class)))
            .thenReturn(ValidationResult.valid());

        when(aisScaAuthorisationServiceResolver.getService()).thenReturn(redirectAisAuthorizationService);
        CreateConsentAuthorizationResponse createConsentAuthorizationResponse = new CreateConsentAuthorizationResponse();
        createConsentAuthorizationResponse.setAuthorisationId(AUTHORISATION_ID);
        createConsentAuthorizationResponse.setScaStatus(ScaStatus.RECEIVED);
        createConsentAuthorizationResponse.setPsuIdData(PSU_ID_DATA);
        when(redirectAisAuthorizationService.createConsentAuthorization(any(), anyString()))
            .thenReturn(Optional.of(createConsentAuthorizationResponse));

        // When
        ResponseObject<AuthorisationResponse> responseObject = service.createAisAuthorisation(PSU_ID_DATA, CONSENT_ID, PASSWORD);

        // Then
        verify(aisConsentService, times(2)).getAccountConsentById(CONSENT_ID);

        assertTrue(responseObject.hasError());
    }

    @Test
    void createAisAuthorisation_accountConsentIsExpired() {
        // Given
        AisConsent expiredAccountConsent = mock(AisConsent.class);
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(expiredAccountConsent));
        when(consentValidationService.validateConsentAuthorisationOnCreate(any(CreateConsentAuthorisationObject.class)))
            .thenReturn(ValidationResult.valid());
        when(expiredAccountConsent.isExpired()).thenReturn(true);

        // When
        ResponseObject<AuthorisationResponse> responseObject = service.createAisAuthorisation(PSU_ID_DATA, CONSENT_ID, PASSWORD);

        // Then
        verify(aisConsentService, times(1)).getAccountConsentById(CONSENT_ID);

        assertTrue(responseObject.hasError());
        assertEquals(MessageErrorCode.CONSENT_EXPIRED, responseObject.getError().getTppMessage().getMessageErrorCode());
    }

    @Test
    void createAisAuthorisation_withUnknownConsent_shouldReturnConsentUnknownError() {
        // Given
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID)).thenReturn(Optional.empty());

        // When
        ResponseObject<AuthorisationResponse> response = service.createAisAuthorisation(PSU_ID_DATA, WRONG_CONSENT_ID, "");

        // Then
        assertThat(response.getError()).isEqualTo(CONSENT_UNKNOWN_403_ERROR);
    }

    @Test
    void updateConsentPsuData_Success_Redirect_ShouldRecordEvent() {
        // Given
        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq(CONSENT_ID);
        Authorisation authorisation = new Authorisation();
        authorisation.setChosenScaApproach(ScaApproach.REDIRECT);
        authorisation.setScaAuthenticationData(SCA_AUTHENTICATION_DATA);
        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse(ScaStatus.FINALISED, CONSENT_ID, AUTHORISATION_ID, PSU_ID_DATA);
        ResponseObject<UpdateConsentPsuDataResponse> expectedResult = ResponseObject.<UpdateConsentPsuDataResponse>builder().body(response).build();
        when(aisScaAuthorisationServiceResolver.getService(AUTHORISATION_ID)).thenReturn(redirectAisAuthorizationService);
        when(redirectAisAuthorizationService.getConsentAuthorizationById(AUTHORISATION_ID))
            .thenReturn(Optional.of(authorisation));
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentPsuDataOnUpdate(aisConsent, updateConsentPsuDataReq)).thenReturn(ValidationResult.valid());
        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);
        when(aisAuthorisationConfirmationService.processAuthorisationConfirmation(updateConsentPsuDataReq))
            .thenReturn(expectedResult);
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);
        when(eventTypeService.getEventType(updateConsentPsuDataReq, EventAuthorisationType.AIS))
            .thenReturn(EventType.UPDATE_AIS_CONSENT_PSU_DATA_IDENTIFICATION_REQUEST_RECEIVED);

        // When
        ResponseObject<UpdateConsentPsuDataResponse> actualResult = service.updateConsentPsuData(updateConsentPsuDataReq);

        // Then
        verify(xs2aEventService, times(1)).recordConsentTppRequest(eq(CONSENT_ID), argumentCaptor.capture(), any());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.UPDATE_AIS_CONSENT_PSU_DATA_IDENTIFICATION_REQUEST_RECEIVED);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void getConsentInitiationAuthorisation() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(aisConsent));
        when(consentValidationService.validateConsentAuthorisationOnGettingById(aisConsent))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAuthorisationSubResources(anyString()))
            .thenReturn(Optional.of(Collections.singletonList(CONSENT_ID)));
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        ResponseObject<Xs2aAuthorisationSubResources> paymentInitiationAuthorisation = service.getConsentInitiationAuthorisations(CONSENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordConsentTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_CONSENT_AUTHORISATION_REQUEST_RECEIVED);

        assertThat(paymentInitiationAuthorisation.getBody()).isNotNull();
        List<String> authorisationIds = paymentInitiationAuthorisation.getBody().getAuthorisationIds();
        assertFalse(authorisationIds.isEmpty());
        assertThat(authorisationIds.get(0)).isEqualTo(CONSENT_ID);
    }

    private void assertValidationError(ResponseObject<?> response) {
        assertThat(response).isNotNull();
        assertThat(response.hasError()).isTrue();
        assertThat(response.getError()).isEqualTo(CONSENT_INVALID_401_ERROR);
    }

    private UpdateConsentPsuDataReq buildUpdateConsentPsuDataReq(String consentId) {
        UpdateConsentPsuDataReq request = new UpdateConsentPsuDataReq();
        request.setConsentId(consentId);
        request.setAuthorizationId(ConsentAuthorisationServiceTest.AUTHORISATION_ID);
        request.setPsuData(PSU_ID_DATA);
        return request;
    }
}

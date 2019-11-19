/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.exception.MessageCategory;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationChainResponsibilityService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.authorization.ais.RedirectAisAuthorizationService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.LoggingContextService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.AisEndpointAccessCheckerService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.CreateConsentAuthorisationObject;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConsentAuthorisationServiceTest {

    private static final String PASSWORD = "password";
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String WRONG_CONSENT_ID = "wrong_consent_id";
    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";
    private static final String WRONG_AUTHORISATION_ID = "wrong authorisation id";
    private static final String CORRECT_PSU_ID = "123456789";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(CORRECT_PSU_ID, null, null, null);
    private static final PsuIdData PSU_ID_DATA_EMPTY = new PsuIdData(null, null, null, null);
    private static final MessageError VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(MessageErrorCode.CONSENT_INVALID));
    private static final MessageError CONSENT_UNKNOWN_403_ERROR =
        new MessageError(ErrorType.AIS_403, TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_403));

    @InjectMocks
    private ConsentAuthorisationService service;

    @Mock
    private Xs2aAisConsentService aisConsentService;
    @Mock
    private AisScaAuthorisationServiceResolver aisScaAuthorisationServiceResolver;
    @Mock
    private AisEndpointAccessCheckerService endpointAccessCheckerService;
    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private ConsentValidationService consentValidationService;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private AuthorisationChainResponsibilityService authorisationChainResponsibilityService;
    @Mock
    private LoggingContextService loggingContextService;
    @Mock
    private RedirectAisAuthorizationService redirectAisAuthorizationService;

    private JsonReader jsonReader = new JsonReader();
    private AccountConsent accountConsent;
    private CreateConsentAuthorisationObject createConsentAuthorisationObject;

    @Before
    public void setUp() {
        accountConsent = jsonReader.getObjectFromFile("json/service/account-consent.json", AccountConsent.class);
        createConsentAuthorisationObject = new CreateConsentAuthorisationObject(accountConsent, PSU_ID_DATA);
    }

    @Test
    public void getConsentAuthorisationScaStatus_failure() {
        // Given

        // When
        ResponseObject<ScaStatus> actual = service.getConsentAuthorisationScaStatus(CONSENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertTrue(actual.hasError());
        assertNull(actual.getBody());
    }

    @Test
    public void getConsentAuthorisationScaStatus_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(accountConsent));
        when(consentValidationService.validateConsentAuthorisationScaStatus(accountConsent, AUTHORISATION_ID))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<ScaStatus> actualResponse = service.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        verify(consentValidationService).validateConsentAuthorisationScaStatus(accountConsent, AUTHORISATION_ID);
        assertValidationError(actualResponse);
    }

    @Test
    public void getConsentAuthorisationScaStatus_withUnknownConsent_shouldReturnConsentUnknownError() {
        // Given
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID)).thenReturn(Optional.empty());

        // When
        ResponseObject response = service.getConsentAuthorisationScaStatus(WRONG_CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertThat(response.getError()).isEqualTo(CONSENT_UNKNOWN_403_ERROR);
    }

    @Test
    public void getConsentAuthorisationScaStatus_shouldRecordStatusInLoggingContext() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(accountConsent));
        when(consentValidationService.validateConsentAuthorisationScaStatus(accountConsent, AUTHORISATION_ID)).thenReturn(ValidationResult.valid());
        when(aisScaAuthorisationServiceResolver.getServiceInitiation(AUTHORISATION_ID)).thenReturn(redirectAisAuthorizationService);
        when(redirectAisAuthorizationService.getAuthorisationScaStatus(any(), any()))
            .thenReturn(Optional.of(ScaStatus.RECEIVED));
        ArgumentCaptor<ConsentStatus> consentStatusArgumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);
        ArgumentCaptor<ScaStatus> scaStatusArgumentCaptor = ArgumentCaptor.forClass(ScaStatus.class);

        // When
        service.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        verify(aisConsentService, times(1)).getAccountConsentById(CONSENT_ID);
        verify(consentValidationService, times(1)).validateConsentAuthorisationScaStatus(accountConsent, AUTHORISATION_ID);
        verify(aisScaAuthorisationServiceResolver, times(1)).getServiceInitiation(AUTHORISATION_ID);
        verify(loggingContextService, times(1)).storeConsentStatus(consentStatusArgumentCaptor.capture());
        verify(loggingContextService, times(1)).storeScaStatus(scaStatusArgumentCaptor.capture());
        assertThat(consentStatusArgumentCaptor.getValue()).isEqualTo(ConsentStatus.VALID);
        assertThat(scaStatusArgumentCaptor.getValue()).isEqualTo(ScaStatus.RECEIVED);
    }

    @Test
    public void getConsentAuthorisationScaStatus_success_shouldRecordEvent() {
        // Given
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        service.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_CONSENT_SCA_STATUS_REQUEST_RECEIVED);
    }

    @Test
    public void getConsentAuthorisationScaStatus_success() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(accountConsent));
        when(consentValidationService.validateConsentAuthorisationScaStatus(accountConsent, AUTHORISATION_ID)).thenReturn(ValidationResult.valid());
        when(aisScaAuthorisationServiceResolver.getServiceInitiation(AUTHORISATION_ID)).thenReturn(redirectAisAuthorizationService);
        when(redirectAisAuthorizationService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(ScaStatus.RECEIVED));

        // When
        ResponseObject<ScaStatus> actual = service.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertFalse(actual.hasError());
        assertEquals(ScaStatus.RECEIVED, actual.getBody());
    }

    @Test
    public void getConsentAuthorisationScaStatus_scaStatusIsEmpty_resourceUnknown() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(accountConsent));
        when(consentValidationService.validateConsentAuthorisationScaStatus(accountConsent, AUTHORISATION_ID)).thenReturn(ValidationResult.valid());
        when(aisScaAuthorisationServiceResolver.getServiceInitiation(AUTHORISATION_ID)).thenReturn(redirectAisAuthorizationService);
        when(redirectAisAuthorizationService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseObject<ScaStatus> actual = service.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertTrue(actual.hasError());
        assertEquals(MessageErrorCode.RESOURCE_UNKNOWN_403, actual.getError().getTppMessage().getMessageErrorCode());
    }

    @Test
    public void getConsentInitiationAuthorisations_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(accountConsent));
        when(consentValidationService.validateConsentAuthorisationOnGettingById(accountConsent))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aAuthorisationSubResources> actualResponse = service.getConsentInitiationAuthorisations(CONSENT_ID);

        // Then
        verify(aisConsentService, times(1)).getAccountConsentById(CONSENT_ID);
        verify(consentValidationService, times(1)).validateConsentAuthorisationOnGettingById(accountConsent);
        assertValidationError(actualResponse);
    }

    @Test
    public void getConsentInitiationAuthorisations_withUnknownConsent_shouldReturnConsentUnknownError() {
        // Given
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID)).thenReturn(Optional.empty());

        // When
        ResponseObject response = service.getConsentInitiationAuthorisations(WRONG_CONSENT_ID);

        // Then
        assertThat(response.getError()).isEqualTo(CONSENT_UNKNOWN_403_ERROR);
    }

    @Test
    public void getConsentInitiationAuthorisations_shouldRecordStatusInLoggingContext() {
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(accountConsent));
        when(consentValidationService.validateConsentAuthorisationOnGettingById(accountConsent))
            .thenReturn(ValidationResult.valid());

        service.getConsentInitiationAuthorisations(CONSENT_ID);

        ArgumentCaptor<ConsentStatus> consentStatusArgumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);

        verify(loggingContextService).storeConsentStatus(consentStatusArgumentCaptor.capture());
    }

    @Test
    public void updateConsentPsuData_withInvalidRequest_shouldReturnValidationError() {
        // Given
        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq(CONSENT_ID);

        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(true);
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(accountConsent));
        when(consentValidationService.validateConsentPsuDataOnUpdate(accountConsent, updateConsentPsuDataReq))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<UpdateConsentPsuDataResponse> actualResponse = service.updateConsentPsuData(updateConsentPsuDataReq);

        // Then
        verify(consentValidationService).validateConsentPsuDataOnUpdate(accountConsent, updateConsentPsuDataReq);

        assertValidationError(actualResponse);
    }

    @Test
    public void updateConsentPsuData_consentIsExpired() {
        // Given
        AccountConsent expiredAccountConsent = mock(AccountConsent.class);
        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq(CONSENT_ID);

        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(true);
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(expiredAccountConsent));
        when(consentValidationService.validateConsentPsuDataOnUpdate(expiredAccountConsent, updateConsentPsuDataReq))
            .thenReturn(ValidationResult.valid());
        when(expiredAccountConsent.isExpired()).thenReturn(true);

        // When
        ResponseObject<UpdateConsentPsuDataResponse> actualResponse = service.updateConsentPsuData(updateConsentPsuDataReq);

        // Then
        verify(consentValidationService).validateConsentPsuDataOnUpdate(expiredAccountConsent, updateConsentPsuDataReq);

        assertTrue(actualResponse.hasError());
        assertEquals(MessageErrorCode.CONSENT_EXPIRED, actualResponse.getError().getTppMessage().getMessageErrorCode());
    }

    @Test
    public void updateConsentPsuData_Success_ShouldRecordEvent() {
        // Given
        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(true);

        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq(CONSENT_ID);
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        service.updateConsentPsuData(updateConsentPsuDataReq);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture(), any());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.UPDATE_AIS_CONSENT_PSU_DATA_REQUEST_RECEIVED);
    }

    @Test
    public void updateConsentPsuData_Failure_EndpointIsNotAccessible() {
        // Given
        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq(CONSENT_ID);

        doNothing()
            .when(xs2aEventService).recordAisTppRequest(CONSENT_ID, EventType.UPDATE_AIS_CONSENT_PSU_DATA_REQUEST_RECEIVED, updateConsentPsuDataReq);

        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONSENT_ID))
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
    public void updateConsentPsuData_withUnknownConsent_shouldReturnConsentUnknownError() {
        // Given
        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq(WRONG_CONSENT_ID);
        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, WRONG_CONSENT_ID))
            .thenReturn(true);
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseObject response = service.updateConsentPsuData(updateConsentPsuDataReq);

        // Then
        assertThat(response.getError()).isEqualTo(CONSENT_UNKNOWN_403_ERROR);
    }

    @Test
    public void updateConsentPsuData_shouldRecordStatusInLoggingContext() {
        // Given
        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq(CONSENT_ID);

        when(aisScaAuthorisationServiceResolver.getServiceInitiation(AUTHORISATION_ID)).thenReturn(redirectAisAuthorizationService);
        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(true);
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(accountConsent));
        when(consentValidationService.validateConsentPsuDataOnUpdate(accountConsent, updateConsentPsuDataReq)).thenReturn(ValidationResult.valid());

        AccountConsentAuthorization authorization = new AccountConsentAuthorization();
        when(redirectAisAuthorizationService.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(Optional.of(authorization));

        when(authorisationChainResponsibilityService.apply(any())).thenReturn(new UpdateConsentPsuDataResponse(ScaStatus.RECEIVED, CONSENT_ID, AUTHORISATION_ID));

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
    public void updateConsentPsuData_authorisationNotFoundError() {
        // Given
        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq(CONSENT_ID);

        when(aisScaAuthorisationServiceResolver.getServiceInitiation(AUTHORISATION_ID)).thenReturn(redirectAisAuthorizationService);
        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(true);
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(accountConsent));
        when(consentValidationService.validateConsentPsuDataOnUpdate(accountConsent, updateConsentPsuDataReq)).thenReturn(ValidationResult.valid());

        when(redirectAisAuthorizationService.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseObject<UpdateConsentPsuDataResponse> actualResponseObject = service.updateConsentPsuData(updateConsentPsuDataReq);

        // Then
        assertTrue(actualResponseObject.hasError());
        assertEquals(MessageErrorCode.CONSENT_UNKNOWN_403, actualResponseObject.getError().getTppMessage().getMessageErrorCode());
    }

    @Test
    public void updateConsentPsuData_psuCredentialsInvalid_updateScaStatusAsFailed() {
        // Given
        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq(CONSENT_ID);

        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(true);
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(accountConsent));
        when(consentValidationService.validateConsentPsuDataOnUpdate(accountConsent, updateConsentPsuDataReq))
            .thenReturn(ValidationResult.invalid(ErrorType.AIS_400, MessageErrorCode.PSU_CREDENTIALS_INVALID));

        // When
        ResponseObject<UpdateConsentPsuDataResponse> actualResponseObject = service.updateConsentPsuData(updateConsentPsuDataReq);

        // Then
        verify(aisConsentService, times(1)).updateConsentAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FAILED);
        assertTrue(actualResponseObject.hasError());
        assertEquals(MessageErrorCode.PSU_CREDENTIALS_INVALID, actualResponseObject.getError().getTppMessage().getMessageErrorCode());
    }

    @Test
    public void createConsentAuthorizationWithResponse_Success_ShouldRecordEvent() {
        // Given
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        service.createAisAuthorisation(PSU_ID_DATA, CONSENT_ID, PASSWORD);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.START_AIS_CONSENT_AUTHORISATION_REQUEST_RECEIVED);
    }

    @Test
    public void createConsentAuthorizationWithResponse_Success_WithoutPsuIdHeader() {
        // Given
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        service.createAisAuthorisation(PSU_ID_DATA_EMPTY, CONSENT_ID, PASSWORD);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.START_AIS_CONSENT_AUTHORISATION_REQUEST_RECEIVED);
    }

    @Test
    public void createConsentAuthorisationWithResponse_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(accountConsent));
        when(consentValidationService.validateConsentAuthorisationOnCreate(any(CreateConsentAuthorisationObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<AuthorisationResponse> actualResponse = service.createAisAuthorisation(PSU_ID_DATA, CONSENT_ID, PASSWORD);

        // Then
        verify(aisConsentService, times(1)).getAccountConsentById(CONSENT_ID);
        verify(consentValidationService, times(1)).validateConsentAuthorisationOnCreate(createConsentAuthorisationObject);
        assertValidationError(actualResponse);
    }

    @Test
    public void createAisAuthorisation_PSU_ID_empty_shouldRecordStatusInLoggingContext() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(accountConsent));
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
    public void createAisAuthorisation_shouldRecordStatusInLoggingContext() {
        // Given
        when(aisScaAuthorisationServiceResolver.getService()).thenReturn(redirectAisAuthorizationService);
        CreateConsentAuthorizationResponse createConsentAuthorizationResponse = new CreateConsentAuthorizationResponse();
        createConsentAuthorizationResponse.setAuthorisationId(AUTHORISATION_ID);
        createConsentAuthorizationResponse.setScaStatus(ScaStatus.RECEIVED);
        createConsentAuthorizationResponse.setPsuIdData(PSU_ID_DATA);
        when(redirectAisAuthorizationService.createConsentAuthorization(any(), anyString()))
            .thenReturn(Optional.of(createConsentAuthorizationResponse));
        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(true);
        when(aisScaAuthorisationServiceResolver.getServiceInitiation(AUTHORISATION_ID)).thenReturn(redirectAisAuthorizationService);
        when(redirectAisAuthorizationService.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(Optional.of(new AccountConsentAuthorization()));

        UpdateConsentPsuDataReq updatePsuData = buildUpdateConsentPsuDataReq(CONSENT_ID);
        updatePsuData.setPassword(PASSWORD);
        UpdateConsentPsuDataResponse updateConsentPsuDataResponse = new UpdateConsentPsuDataResponse(ScaStatus.RECEIVED, CONSENT_ID, AUTHORISATION_ID);
        updateConsentPsuDataResponse.setScaStatus(ScaStatus.RECEIVED);

        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(accountConsent));
        when(consentValidationService.validateConsentAuthorisationOnCreate(any(CreateConsentAuthorisationObject.class)))
            .thenReturn(ValidationResult.valid());
        when(consentValidationService.validateConsentPsuDataOnUpdate(accountConsent, updatePsuData))
            .thenReturn(ValidationResult.valid());
        when(authorisationChainResponsibilityService.apply(any())).thenReturn(new UpdateConsentPsuDataResponse(ScaStatus.RECEIVED, CONSENT_ID, AUTHORISATION_ID));

        ArgumentCaptor<ConsentStatus> consentStatusArgumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);
        ArgumentCaptor<ScaStatus> scaStatusArgumentCaptor = ArgumentCaptor.forClass(ScaStatus.class);

        // When
        service.createAisAuthorisation(PSU_ID_DATA, CONSENT_ID, PASSWORD);

        // Then
        verify(aisConsentService, times(2)).getAccountConsentById(CONSENT_ID);
        verify(consentValidationService, times(1)).validateConsentAuthorisationOnCreate(createConsentAuthorisationObject);
        verify(consentValidationService, times(1)).validateConsentPsuDataOnUpdate(accountConsent, updatePsuData);
        verify(loggingContextService).storeScaStatus(scaStatusArgumentCaptor.capture());
        assertThat(scaStatusArgumentCaptor.getValue()).isEqualTo(ScaStatus.RECEIVED);
        verify(loggingContextService, times(2)).storeConsentStatus(consentStatusArgumentCaptor.capture());
        assertThat(consentStatusArgumentCaptor.getValue()).isEqualTo(ConsentStatus.VALID);
    }

    @Test
    public void createAisAuthorisation_updateConsentPsuDataWithError() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(accountConsent));
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
        verify(aisConsentService, times(1)).getAccountConsentById(CONSENT_ID);

        assertTrue(responseObject.hasError());
    }

    @Test
    public void createAisAuthorisation_accountConsentIsExpired() {
        // Given
        AccountConsent expiredAccountConsent = mock(AccountConsent.class);
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
    public void createAisAuthorisation_withUnknownConsent_shouldReturnConsentUnknownError() {
        // Given
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID)).thenReturn(Optional.empty());

        // When
        ResponseObject response = service.createAisAuthorisation(PSU_ID_DATA, WRONG_CONSENT_ID, "");

        // Then
        assertThat(response.getError()).isEqualTo(CONSENT_UNKNOWN_403_ERROR);
    }

    public void getConsentInitiationAuthorisation() {
        // Given
        when(aisConsentService.getAuthorisationSubResources(anyString()))
            .thenReturn(Optional.of(Collections.singletonList(CONSENT_ID)));
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        ResponseObject<Xs2aAuthorisationSubResources> paymentInitiationAuthorisation = service.getConsentInitiationAuthorisations(CONSENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_CONSENT_AUTHORISATION_REQUEST_RECEIVED);

        assertThat(paymentInitiationAuthorisation.getBody()).isNotNull();
        List<String> authorisationIds = paymentInitiationAuthorisation.getBody().getAuthorisationIds();
        assertFalse(authorisationIds.isEmpty());
        assertThat(authorisationIds.get(0)).isEqualTo(CONSENT_ID);
    }

    private void assertValidationError(ResponseObject response) {
        assertThat(response).isNotNull();
        assertThat(response.hasError()).isTrue();
        assertThat(response.getError()).isEqualTo(VALIDATION_ERROR);
    }

    private UpdateConsentPsuDataReq buildUpdateConsentPsuDataReq(String consentId) {
        return buildUpdateConsentPsuDataReq(consentId, AUTHORISATION_ID);
    }

    private UpdateConsentPsuDataReq buildUpdateConsentPsuDataReq(String consentId, String authorisationId) {
        UpdateConsentPsuDataReq request = new UpdateConsentPsuDataReq();
        request.setConsentId(consentId);
        request.setAuthorizationId(authorisationId);
        request.setPsuData(PSU_ID_DATA);
        return request;
    }
}

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
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.service.authorization.ais.PiisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.authorization.ais.RedirectPiisAuthorizationService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.piis.dto.CreatePiisConsentAuthorisationObject;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        when(piisScaAuthorisationServiceResolver.getService())
            .thenReturn(redirectPiisAuthorizationService);
        CreateConsentAuthorizationResponse createConsentAuthorizationResponse = buildCreateConsentAuthorizationResponse();
        when(redirectPiisAuthorizationService.createConsentAuthorization(any(), anyString()))
            .thenReturn(Optional.of(createConsentAuthorizationResponse));
        // When
        ResponseObject<AuthorisationResponse> actualResponse = service.createPiisAuthorisation(PSU_ID_DATA, CONSENT_ID, PASSWORD);
        // Then
        verify(xs2aPiisConsentService, times(1)).getPiisConsentById(CONSENT_ID);
        verify(confirmationOfFundsConsentValidationService, times(1)).validateConsentAuthorisationOnCreate(createPiisConsentAuthorisationObject);
        assertEquals(createConsentAuthorizationResponse, actualResponse.getBody());
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
}

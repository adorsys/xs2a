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

package de.adorsys.psd2.xs2a.service.validator.piis;


import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.piis.PiisConsent;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.fund.PiisConsentValidationResult;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.tpp.PiisTppInfoValidator;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400_INCORRECT_CERTIFICATE;
import static de.adorsys.psd2.xs2a.core.piis.PiisConsentTppAccessType.ALL_TPP;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIIS_400;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PiisConsentValidationTest {
    private static final String CREATE_PIIS_CONSENT_JSON_PATH = "json/service/validator/tpp/piis-consent-request.json";
    private static final String CREATE_WRONG_PIIS_CONSENT_JSON_PATH = "json/service/validator/tpp/wrong-piis-consent-request.json";
    private static final String AUTHORISATION_NUMBER = "12345987";
    private static final String DIFFERENT_AUTHORISATION_NUMBER = "different authorisation number";
    private static final UUID X_REQUEST_ID = UUID.fromString("1af360bc-13cb-40ab-9aa0-cc0d6af4510c");

    private JsonReader jsonReader = new JsonReader();

    @Mock
    private PiisTppInfoValidator piisTppInfoValidator;
    @Mock
    private RequestProviderService requestProviderService;

    @InjectMocks
    private PiisConsentValidation piisConsentValidation;

    @Before
    public void setUp() {
        when(piisTppInfoValidator.validateTpp(DIFFERENT_AUTHORISATION_NUMBER))
            .thenReturn(ValidationResult.invalid(PIIS_400, CONSENT_UNKNOWN_400_INCORRECT_CERTIFICATE));

        when(piisTppInfoValidator.validateTpp(AUTHORISATION_NUMBER)).thenReturn(ValidationResult.valid());
        when(requestProviderService.getRequestId()).thenReturn(X_REQUEST_ID);
    }

    @Test
    public void validatePiisConsentData_validList_successful() {
        // Given
        PiisConsent validConsent = buildConsent();
        PiisConsent wrongConsent = buildWrongPiisConsent();

        List<PiisConsent> piisConsents = new ArrayList<>();
        piisConsents.add(validConsent);
        piisConsents.add(wrongConsent);

        // When
        PiisConsentValidationResult validationResult = piisConsentValidation.validatePiisConsentData(piisConsents);

        // Then
        assertNotNull(validationResult);
        assertFalse(validationResult.hasError());
        assertEquals(validationResult.getConsent(), validConsent);
    }

    @Test
    public void validatePiisConsentData_differentTpp_shouldReturnException() {
        // Given
        PiisConsent validConsent = buildConsent();
        PiisConsent wrongConsent = buildWrongPiisConsent();
        validConsent.setTppAuthorisationNumber(DIFFERENT_AUTHORISATION_NUMBER);

        List<PiisConsent> piisConsents = new ArrayList<>();
        piisConsents.add(validConsent);
        piisConsents.add(wrongConsent);

        // When
        PiisConsentValidationResult validationResult = piisConsentValidation.validatePiisConsentData(piisConsents);

        // Then
        assertThatErrorIs(validationResult, CONSENT_UNKNOWN_400_INCORRECT_CERTIFICATE);
    }

    @Test
    public void validatePiisConsentData_allTppConsent_successful() {
        // Given
        PiisConsent validConsent = buildConsent();
        validConsent.setTppAccessType(ALL_TPP);

        List<PiisConsent> piisConsents = new ArrayList<>();
        piisConsents.add(validConsent);

        // When
        PiisConsentValidationResult validationResult = piisConsentValidation.validatePiisConsentData(piisConsents);

        // Then
        assertNotNull(validationResult);
        assertFalse(validationResult.hasError());
        assertEquals(validationResult.getConsent(), validConsent);
    }

    @Test
    public void validatePiisConsentData_allTppConsent_shouldReturnException() {
        // Given
        PiisConsent validConsent = buildConsent();
        validConsent.setTppAccessType(null);

        List<PiisConsent> piisConsents = new ArrayList<>();
        piisConsents.add(validConsent);

        // When
        PiisConsentValidationResult validationResult = piisConsentValidation.validatePiisConsentData(piisConsents);

        // Then
        assertThatErrorIs(validationResult, MessageErrorCode.CONSENT_UNKNOWN_400_NULL_ACCESS_TYPE);
    }

    @Test
    public void validatePiisConsentData_wrongList_shouldReturnError() {
        // Given
        PiisConsent wrongConsent = buildWrongPiisConsent();

        List<PiisConsent> piisConsents = new ArrayList<>();
        piisConsents.add(wrongConsent);

        // When
        PiisConsentValidationResult validationResult = piisConsentValidation.validatePiisConsentData(piisConsents);

        // Then
        assertThatErrorIs(validationResult, MessageErrorCode.CONSENT_UNKNOWN_400_NULL_ACCESS_TYPE);
    }

    @Test
    public void validatePiisConsentData_emptyList_shouldReturnError() {
        // Given
        List<PiisConsent> piisConsents = new ArrayList<>();

        // When
        PiisConsentValidationResult validationResult = piisConsentValidation.validatePiisConsentData(piisConsents);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.hasError());
        assertEquals(MessageErrorCode.NO_PIIS_ACTIVATION, validationResult.getErrorHolder().getTppMessageInformationList().iterator().next().getMessageErrorCode());
        assertEquals(ErrorType.PIIS_400, validationResult.getErrorHolder().getErrorType());
    }

    private PiisConsent buildWrongPiisConsent() {
        return jsonReader.getObjectFromFile(CREATE_WRONG_PIIS_CONSENT_JSON_PATH, PiisConsent.class);
    }

    private PiisConsent buildConsent() {
        return jsonReader.getObjectFromFile(CREATE_PIIS_CONSENT_JSON_PATH, PiisConsent.class);
    }

    private void assertThatErrorIs(PiisConsentValidationResult validationResult, MessageErrorCode consentUnknown400) {
        assertNotNull(validationResult);
        assertTrue(validationResult.hasError());
        assertEquals(validationResult.getErrorHolder().getTppMessageInformationList().iterator().next().getMessageErrorCode(), consentUnknown400);
        assertEquals(validationResult.getErrorHolder().getErrorType(), ErrorType.PIIS_400);
    }
}

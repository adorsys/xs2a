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

package de.adorsys.psd2.xs2a.service.validator.piis;

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationServiceType;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.service.validator.PiisPsuDataUpdateAuthorisationCheckerValidator;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationStageCheckValidator;
import de.adorsys.psd2.xs2a.service.validator.piis.dto.UpdatePiisConsentPsuDataRequestObject;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.RESOURCE_UNKNOWN_403;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePiisConsentPsuDataValidatorTest {
    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";
    private static final ValidationResult INVALID = ValidationResult.invalid(ErrorType.PIIS_403, RESOURCE_UNKNOWN_403);

    @InjectMocks
    private UpdatePiisConsentPsuDataValidator updatePiisConsentPsuDataValidator;
    @Mock
    private PiisAuthorisationValidator piisAuthorisationValidator;
    @Mock
    private PiisPsuDataUpdateAuthorisationCheckerValidator piisPsuDataUpdateAuthorisationCheckerValidator;
    @Mock
    private PiisAuthorisationStatusValidator piisAuthorisationStatusValidator;
    @Mock
    private AuthorisationStageCheckValidator authorisationStageCheckValidator;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void executeBusinessValidation_valid() {
        // Given
        PiisConsent piisConsent = jsonReader.getObjectFromFile("json/piis/piis-consent.json", PiisConsent.class);
        UpdateConsentPsuDataReq updateConsentPsuDataReq = jsonReader.getObjectFromFile("json/piis/update-consent-psu-data-req.json", UpdateConsentPsuDataReq.class);
        UpdatePiisConsentPsuDataRequestObject input = new UpdatePiisConsentPsuDataRequestObject(piisConsent, updateConsentPsuDataReq);
        PsuIdData psuIdData = jsonReader.getObjectFromFile("json/piis/psu-data.json", PsuIdData.class);

        when(piisAuthorisationValidator.validate(AUTHORISATION_ID, piisConsent)).thenReturn(ValidationResult.valid());
        when(piisPsuDataUpdateAuthorisationCheckerValidator.validate(psuIdData, psuIdData)).thenReturn(ValidationResult.valid());
        when(piisAuthorisationStatusValidator.validate(ScaStatus.RECEIVED, false)).thenReturn(ValidationResult.valid());
        when(authorisationStageCheckValidator.validate(updateConsentPsuDataReq, ScaStatus.RECEIVED, AuthorisationServiceType.PIIS)).thenReturn(ValidationResult.valid());

        // When
        ValidationResult actual = updatePiisConsentPsuDataValidator.executeBusinessValidation(input);

        // Then
        assertThat(actual.isValid()).isTrue();
    }

    @Test
    void executeBusinessValidation_invalidStage() {
        // Given
        PiisConsent piisConsent = jsonReader.getObjectFromFile("json/piis/piis-consent.json", PiisConsent.class);
        UpdateConsentPsuDataReq updateConsentPsuDataReq = jsonReader.getObjectFromFile("json/piis/update-consent-psu-data-req.json", UpdateConsentPsuDataReq.class);
        UpdatePiisConsentPsuDataRequestObject input = new UpdatePiisConsentPsuDataRequestObject(piisConsent, updateConsentPsuDataReq);
        PsuIdData psuIdData = jsonReader.getObjectFromFile("json/piis/psu-data.json", PsuIdData.class);

        when(piisAuthorisationValidator.validate(AUTHORISATION_ID, piisConsent)).thenReturn(ValidationResult.valid());
        when(piisPsuDataUpdateAuthorisationCheckerValidator.validate(psuIdData, psuIdData)).thenReturn(ValidationResult.valid());
        when(piisAuthorisationStatusValidator.validate(ScaStatus.RECEIVED, false)).thenReturn(ValidationResult.valid());
        when(authorisationStageCheckValidator.validate(updateConsentPsuDataReq, ScaStatus.RECEIVED, AuthorisationServiceType.PIIS)).thenReturn(INVALID);

        // When
        ValidationResult actual = updatePiisConsentPsuDataValidator.executeBusinessValidation(input);

        // Then
        assertThat(actual.isValid()).isFalse();
    }

    @Test
    void executeBusinessValidation_invalidAuthStatus() {
        // Given
        PiisConsent piisConsent = jsonReader.getObjectFromFile("json/piis/piis-consent.json", PiisConsent.class);
        UpdateConsentPsuDataReq updateConsentPsuDataReq = jsonReader.getObjectFromFile("json/piis/update-consent-psu-data-req.json", UpdateConsentPsuDataReq.class);
        UpdatePiisConsentPsuDataRequestObject input = new UpdatePiisConsentPsuDataRequestObject(piisConsent, updateConsentPsuDataReq);
        PsuIdData psuIdData = jsonReader.getObjectFromFile("json/piis/psu-data.json", PsuIdData.class);

        when(piisAuthorisationValidator.validate(AUTHORISATION_ID, piisConsent)).thenReturn(ValidationResult.valid());
        when(piisPsuDataUpdateAuthorisationCheckerValidator.validate(psuIdData, psuIdData)).thenReturn(ValidationResult.valid());
        when(piisAuthorisationStatusValidator.validate(ScaStatus.RECEIVED, false)).thenReturn(INVALID);

        // When
        ValidationResult actual = updatePiisConsentPsuDataValidator.executeBusinessValidation(input);

        // Then
        assertThat(actual.isValid()).isFalse();
    }

    @Test
    void executeBusinessValidation_invalidPsu() {
        // Given
        PiisConsent piisConsent = jsonReader.getObjectFromFile("json/piis/piis-consent.json", PiisConsent.class);
        UpdateConsentPsuDataReq updateConsentPsuDataReq = jsonReader.getObjectFromFile("json/piis/update-consent-psu-data-req.json", UpdateConsentPsuDataReq.class);
        UpdatePiisConsentPsuDataRequestObject input = new UpdatePiisConsentPsuDataRequestObject(piisConsent, updateConsentPsuDataReq);
        PsuIdData psuIdData = jsonReader.getObjectFromFile("json/piis/psu-data.json", PsuIdData.class);

        when(piisAuthorisationValidator.validate(AUTHORISATION_ID, piisConsent)).thenReturn(ValidationResult.valid());
        when(piisPsuDataUpdateAuthorisationCheckerValidator.validate(psuIdData, psuIdData)).thenReturn(INVALID);

        // When
        ValidationResult actual = updatePiisConsentPsuDataValidator.executeBusinessValidation(input);

        // Then
        assertThat(actual.isValid()).isFalse();
    }

    @Test
    void executeBusinessValidation_invalidAuth() {
        // Given
        PiisConsent piisConsent = jsonReader.getObjectFromFile("json/piis/piis-consent.json", PiisConsent.class);
        UpdateConsentPsuDataReq updateConsentPsuDataReq = jsonReader.getObjectFromFile("json/piis/update-consent-psu-data-req.json", UpdateConsentPsuDataReq.class);
        UpdatePiisConsentPsuDataRequestObject input = new UpdatePiisConsentPsuDataRequestObject(piisConsent, updateConsentPsuDataReq);

        when(piisAuthorisationValidator.validate(AUTHORISATION_ID, piisConsent)).thenReturn(INVALID);

        // When
        ValidationResult actual = updatePiisConsentPsuDataValidator.executeBusinessValidation(input);

        // Then
        assertThat(actual.isValid()).isFalse();
    }

    @Test
    void executeBusinessValidation_EmptyAuth() {
        // Given
        PiisConsent piisConsent = jsonReader.getObjectFromFile("json/piis/piis-consent-invalid.json", PiisConsent.class);
        UpdateConsentPsuDataReq updateConsentPsuDataReq = jsonReader.getObjectFromFile("json/piis/update-consent-psu-data-req.json", UpdateConsentPsuDataReq.class);
        UpdatePiisConsentPsuDataRequestObject input = new UpdatePiisConsentPsuDataRequestObject(piisConsent, updateConsentPsuDataReq);

        when(piisAuthorisationValidator.validate(AUTHORISATION_ID, piisConsent)).thenReturn(ValidationResult.valid());

        // When
        ValidationResult actual = updatePiisConsentPsuDataValidator.executeBusinessValidation(input);

        // Then
        assertThat(actual.isValid()).isFalse();
    }
}

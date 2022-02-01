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

package de.adorsys.psd2.xs2a.service.validator.piis;

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationServiceType;
import de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorisationsParameters;
import de.adorsys.psd2.xs2a.service.validator.PiisPsuDataUpdateAuthorisationCheckerValidator;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationStageCheckValidator;
import de.adorsys.psd2.xs2a.service.validator.piis.dto.UpdatePiisConsentPsuDataRequestObject;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.RESOURCE_UNKNOWN_403;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
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

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void executeBusinessValidation_valid() {
        // Given
        PiisConsent piisConsent = jsonReader.getObjectFromFile("json/piis/piis-consent.json", PiisConsent.class);
        ConsentAuthorisationsParameters updateConsentPsuDataReq = jsonReader.getObjectFromFile("json/piis/update-consent-psu-data-req.json", ConsentAuthorisationsParameters.class);
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
        ConsentAuthorisationsParameters updateConsentPsuDataReq = jsonReader.getObjectFromFile("json/piis/update-consent-psu-data-req.json", ConsentAuthorisationsParameters.class);
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
        ConsentAuthorisationsParameters updateConsentPsuDataReq = jsonReader.getObjectFromFile("json/piis/update-consent-psu-data-req.json", ConsentAuthorisationsParameters.class);
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
        ConsentAuthorisationsParameters updateConsentPsuDataReq = jsonReader.getObjectFromFile("json/piis/update-consent-psu-data-req.json", ConsentAuthorisationsParameters.class);
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
        ConsentAuthorisationsParameters updateConsentPsuDataReq = jsonReader.getObjectFromFile("json/piis/update-consent-psu-data-req.json", ConsentAuthorisationsParameters.class);
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
        ConsentAuthorisationsParameters updateConsentPsuDataReq = jsonReader.getObjectFromFile("json/piis/update-consent-psu-data-req.json", ConsentAuthorisationsParameters.class);
        UpdatePiisConsentPsuDataRequestObject input = new UpdatePiisConsentPsuDataRequestObject(piisConsent, updateConsentPsuDataReq);

        when(piisAuthorisationValidator.validate(AUTHORISATION_ID, piisConsent)).thenReturn(ValidationResult.valid());

        // When
        ValidationResult actual = updatePiisConsentPsuDataValidator.executeBusinessValidation(input);

        // Then
        assertThat(actual.isValid()).isFalse();
    }

    @Test
    void buildWarningMessages() {
        // Given
        PiisConsent piisConsent =
            jsonReader.getObjectFromFile("json/piis/piis-consent-invalid.json", PiisConsent.class);
        ConsentAuthorisationsParameters updateConsentPsuDataReq =
            jsonReader.getObjectFromFile("json/piis/update-consent-psu-data-req.json", ConsentAuthorisationsParameters.class);
        UpdatePiisConsentPsuDataRequestObject input =
            new UpdatePiisConsentPsuDataRequestObject(piisConsent, updateConsentPsuDataReq);

        //When
        Set<TppMessageInformation> actual = updatePiisConsentPsuDataValidator.buildWarningMessages(input);

        //Then
        assertThat(actual).isEmpty();
        verifyNoInteractions(piisAuthorisationValidator);
        verifyNoInteractions(piisPsuDataUpdateAuthorisationCheckerValidator);
        verifyNoInteractions(piisAuthorisationStatusValidator);
        verifyNoInteractions(authorisationStageCheckValidator);
    }
}


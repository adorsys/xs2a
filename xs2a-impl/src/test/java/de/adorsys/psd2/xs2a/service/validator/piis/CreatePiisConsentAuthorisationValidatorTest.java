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
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.authorisation.ConsentAuthorization;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationPsuDataChecker;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationStatusChecker;
import de.adorsys.psd2.xs2a.service.validator.piis.dto.CreatePiisConsentAuthorisationObject;
import de.adorsys.psd2.xs2a.service.validator.tpp.PiisConsentTppInfoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Collections;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePiisConsentAuthorisationValidatorTest {
    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");

    private static final MessageError TPP_VALIDATION_ERROR =
        new MessageError(ErrorType.PIIS_401, TppMessageInformation.of(UNAUTHORIZED));

    private static final MessageError PSU_CREDENTIALS_ERROR =
        new MessageError(ErrorType.PIIS_401, TppMessageInformation.of(PSU_CREDENTIALS_INVALID));

    private static final MessageError STATUS_INVALID_ERROR =
        new MessageError(ErrorType.PIIS_409, TppMessageInformation.of(STATUS_INVALID));

    private static final PsuIdData PSU_DATA = new PsuIdData("111", null, null, null, null);
    private static final PsuIdData EMPTY_PSU_DATA = new PsuIdData(null, null, null, null, null);
    private static final PsuIdData NEW_PSU_DATA = new PsuIdData("new PSU data", null, null, null, null);

    @Mock
    private PiisConsentTppInfoValidator piisConsentTppInfoValidator;
    @Mock
    private AuthorisationPsuDataChecker authorisationPsuDataChecker;
    @Mock
    private AuthorisationStatusChecker aisAuthorisationStatusChecker;

    @InjectMocks
    private CreatePiisConsentAuthorisationValidator createPiisConsentAuthorisationValidator;

    @BeforeEach
    void setUp() {
        // Inject pisTppInfoValidator via setter
        createPiisConsentAuthorisationValidator.setPiisConsentTppInfoValidator(piisConsentTppInfoValidator);
    }

    @Test
    void validate_withValidConsentObject_shouldReturnValid() {
        // Given
        PiisConsent piisConsent = buildPiisConsent(TPP_INFO);
        when(piisConsentTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = createPiisConsentAuthorisationValidator.validate(new CreatePiisConsentAuthorisationObject(piisConsent, EMPTY_PSU_DATA));

        // Then
        verify(piisConsentTppInfoValidator).validateTpp(piisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withDifferentPsuIdInConsent_shouldReturnPsuCredentialsInvalidError() {
        // Given
        PiisConsent piisConsent = buildPiisConsentWithPsuIdData(false);
        when(authorisationPsuDataChecker.isPsuDataWrong(anyBoolean(), any(), any())).thenReturn(true);
        when(piisConsentTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = createPiisConsentAuthorisationValidator.validate(new CreatePiisConsentAuthorisationObject(piisConsent, NEW_PSU_DATA));

        // Then
        verify(piisConsentTppInfoValidator).validateTpp(piisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(PSU_CREDENTIALS_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withDifferentPsuIdInConsent_multilevelSca_shouldReturnValid() {
        // Given
        PiisConsent piisConsent = buildPiisConsentWithPsuIdData(true);
        when(authorisationPsuDataChecker.isPsuDataWrong(anyBoolean(), any(), any())).thenReturn(false);
        when(piisConsentTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = createPiisConsentAuthorisationValidator.validate(new CreatePiisConsentAuthorisationObject(piisConsent, NEW_PSU_DATA));

        // Then
        verify(piisConsentTppInfoValidator).validateTpp(piisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withFinalisedAuthorisation_shouldReturnStatusInvalidError() {
        // Given
        PiisConsent piisConsent = buildPiisConsentWithPsuIdDataAndAuthorisation();
        CreatePiisConsentAuthorisationObject createPiisAuthorisationPO = new CreatePiisConsentAuthorisationObject(piisConsent, PSU_DATA);
        when(aisAuthorisationStatusChecker.isFinalised(any(PsuIdData.class), anyList(), eq(AuthorisationType.CONSENT))).thenReturn(true);

        when(piisConsentTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = createPiisConsentAuthorisationValidator.validate(createPiisAuthorisationPO);

        // Then
        verify(piisConsentTppInfoValidator).validateTpp(createPiisAuthorisationPO.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(STATUS_INVALID_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidTppInConsent_shouldReturnTppValidationError() {
        // Given
        PiisConsent piisConsent = buildPiisConsent(INVALID_TPP_INFO);
        when(piisConsentTppInfoValidator.validateTpp(INVALID_TPP_INFO)).thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = createPiisConsentAuthorisationValidator.validate(new CreatePiisConsentAuthorisationObject(piisConsent, EMPTY_PSU_DATA));

        // Then
        verify(piisConsentTppInfoValidator).validateTpp(piisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }

    private PiisConsent buildPiisConsent(TppInfo tppInfo) {
        PiisConsent piisConsent = new PiisConsent(ConsentType.PIIS_TPP);
        ConsentTppInformation consentTppInformation = new ConsentTppInformation();
        consentTppInformation.setTppInfo(tppInfo);
        piisConsent.setConsentTppInformation(consentTppInformation);
        piisConsent.setAuthorisations(Collections.emptyList());
        piisConsent.setPsuIdDataList(Collections.emptyList());
        return piisConsent;
    }

    private PiisConsent buildPiisConsentWithPsuIdData(boolean isMultilevelSca) {
        PiisConsent piisConsent = new PiisConsent(ConsentType.PIIS_TPP);
        ConsentTppInformation consentTppInformation = new ConsentTppInformation();
        consentTppInformation.setTppInfo(CreatePiisConsentAuthorisationValidatorTest.TPP_INFO);
        piisConsent.setConsentTppInformation(consentTppInformation);
        piisConsent.setMultilevelScaRequired(isMultilevelSca);
        piisConsent.setPsuIdDataList(Collections.singletonList(EMPTY_PSU_DATA));
        piisConsent.setFrequencyPerDay(0);
        piisConsent.setUsages(Collections.emptyMap());
        piisConsent.setCreationTimestamp(OffsetDateTime.now());
        piisConsent.setAuthorisations(Collections.emptyList());
        return piisConsent;
    }

    private PiisConsent buildPiisConsentWithPsuIdDataAndAuthorisation() {
        ConsentAuthorization authorisation = new ConsentAuthorization();
        authorisation.setScaStatus(ScaStatus.FINALISED);
        authorisation.setPsuIdData(PSU_DATA);
        PiisConsent piisConsent = new PiisConsent(ConsentType.PIIS_TPP);
        piisConsent.setAuthorisations(Collections.singletonList(authorisation));
        piisConsent.setPsuIdDataList(Collections.singletonList(PSU_DATA));

        ConsentTppInformation consentTppInformation = new ConsentTppInformation();
        consentTppInformation.setTppInfo(TPP_INFO);
        piisConsent.setConsentTppInformation(consentTppInformation);
        piisConsent.setFrequencyPerDay(0);
        piisConsent.setUsages(Collections.emptyMap());
        piisConsent.setCreationTimestamp(OffsetDateTime.now());

        return piisConsent;
    }

}

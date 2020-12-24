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

package de.adorsys.psd2.xs2a.service.validator.ais.consent;

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.authorisation.ConsentAuthorization;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.CreateConsentAuthorisationObject;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationPsuDataChecker;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationStatusChecker;
import de.adorsys.psd2.xs2a.service.validator.tpp.AisConsentTppInfoValidator;
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
class CreateConsentAuthorisationValidatorTest {
    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");

    private static final MessageError TPP_VALIDATION_ERROR =
        new MessageError(ErrorType.PIS_401, TppMessageInformation.of(UNAUTHORIZED));

    private static final MessageError PSU_CREDENTIALS_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(PSU_CREDENTIALS_INVALID));

    private static final MessageError STATUS_INVALID_ERROR =
        new MessageError(ErrorType.AIS_409, TppMessageInformation.of(STATUS_INVALID));

    private static final PsuIdData PSU_DATA = new PsuIdData("111", null, null, null, null);
    private static final PsuIdData EMPTY_PSU_DATA = new PsuIdData(null, null, null, null, null);
    private static final PsuIdData NEW_PSU_DATA = new PsuIdData("new PSU data", null, null, null, null);

    @Mock
    private AisConsentTppInfoValidator aisConsentTppInfoValidator;
    @Mock
    private AuthorisationPsuDataChecker authorisationPsuDataChecker;
    @Mock
    private AuthorisationStatusChecker aisAuthorisationStatusChecker;

    @InjectMocks
    private CreateConsentAuthorisationValidator createConsentAuthorisationValidator;

    @BeforeEach
    void setUp() {
        // Inject pisTppInfoValidator via setter
        createConsentAuthorisationValidator.setAisConsentTppInfoValidator(aisConsentTppInfoValidator);
    }

    @Test
    void validate_withValidConsentObject_shouldReturnValid() {
        // Given
        AisConsent accountConsent = buildAccountConsent(TPP_INFO);
        when(aisConsentTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = createConsentAuthorisationValidator.validate(new CreateConsentAuthorisationObject(accountConsent, EMPTY_PSU_DATA));

        // Then
        verify(aisConsentTppInfoValidator).validateTpp(accountConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withDifferentPsuIdInConsent_shouldReturnPsuCredentialsInvalidError() {
        // Given
        AisConsent accountConsent = buildAccountConsentWithPsuIdData(false);
        when(authorisationPsuDataChecker.isPsuDataWrong(anyBoolean(), any(), any())).thenReturn(true);
        when(aisConsentTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = createConsentAuthorisationValidator.validate(new CreateConsentAuthorisationObject(accountConsent, NEW_PSU_DATA));

        // Then
        verify(aisConsentTppInfoValidator).validateTpp(accountConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(PSU_CREDENTIALS_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withDifferentPsuIdInConsent_multilevelSca_shouldReturnValid() {
        // Given
        AisConsent accountConsent = buildAccountConsentWithPsuIdData(true);
        when(authorisationPsuDataChecker.isPsuDataWrong(anyBoolean(), any(), any())).thenReturn(false);
        when(aisConsentTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = createConsentAuthorisationValidator.validate(new CreateConsentAuthorisationObject(accountConsent, NEW_PSU_DATA));

        // Then
        verify(aisConsentTppInfoValidator).validateTpp(accountConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withFinalisedAuthorisation_shouldReturnStatusInvalidError() {
        // Given
        AisConsent accountConsent = buildAccountConsentWithPsuIdDataAndAuthorisation();
        CreateConsentAuthorisationObject createPisAuthorisationPO = new CreateConsentAuthorisationObject(accountConsent, PSU_DATA);
        when(aisAuthorisationStatusChecker.isFinalised(any(PsuIdData.class), anyList(), eq(AuthorisationType.CONSENT))).thenReturn(true);

        when(aisConsentTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = createConsentAuthorisationValidator.validate(createPisAuthorisationPO);

        // Then
        verify(aisConsentTppInfoValidator).validateTpp(createPisAuthorisationPO.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(STATUS_INVALID_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidTppInConsent_shouldReturnTppValidationError() {
        // Given
        AisConsent accountConsent = buildAccountConsent(INVALID_TPP_INFO);
        when(aisConsentTppInfoValidator.validateTpp(INVALID_TPP_INFO)).thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = createConsentAuthorisationValidator.validate(new CreateConsentAuthorisationObject(accountConsent, EMPTY_PSU_DATA));

        // Then
        verify(aisConsentTppInfoValidator).validateTpp(accountConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }

    private AisConsent buildAccountConsent(TppInfo tppInfo) {
        AisConsent aisConsent = new AisConsent();
        ConsentTppInformation consentTppInformation = new ConsentTppInformation();
        consentTppInformation.setTppInfo(tppInfo);
        aisConsent.setConsentTppInformation(consentTppInformation);
        aisConsent.setAuthorisations(Collections.emptyList());
        aisConsent.setPsuIdDataList(Collections.emptyList());
        return aisConsent;
    }

    private AisConsent buildAccountConsentWithPsuIdData(boolean isMultilevelSca) {
        AisConsent aisConsent = new AisConsent();
        ConsentTppInformation consentTppInformation = new ConsentTppInformation();
        consentTppInformation.setTppInfo(CreateConsentAuthorisationValidatorTest.TPP_INFO);
        aisConsent.setConsentTppInformation(consentTppInformation);
        aisConsent.setMultilevelScaRequired(isMultilevelSca);
        aisConsent.setPsuIdDataList(Collections.singletonList(EMPTY_PSU_DATA));
        aisConsent.setFrequencyPerDay(0);
        aisConsent.setUsages(Collections.emptyMap());
        aisConsent.setCreationTimestamp(OffsetDateTime.now());
        aisConsent.setAuthorisations(Collections.emptyList());
        return aisConsent;
    }

    private AisConsent buildAccountConsentWithPsuIdDataAndAuthorisation() {
        ConsentAuthorization authorisation = new ConsentAuthorization();
        authorisation.setScaStatus(ScaStatus.FINALISED);
        authorisation.setPsuIdData(PSU_DATA);
        AisConsent aisConsent = new AisConsent();
        aisConsent.setAuthorisations(Collections.singletonList(authorisation));
        aisConsent.setPsuIdDataList(Collections.singletonList(PSU_DATA));

        ConsentTppInformation consentTppInformation = new ConsentTppInformation();
        consentTppInformation.setTppInfo(TPP_INFO);
        aisConsent.setConsentTppInformation(consentTppInformation);
        aisConsent.setFrequencyPerDay(0);
        aisConsent.setUsages(Collections.emptyMap());
        aisConsent.setCreationTimestamp(OffsetDateTime.now());

        return aisConsent;
    }

}

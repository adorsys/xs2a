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

package de.adorsys.psd2.xs2a.service.validator.ais.consent;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsentAuthorization;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.PsuDataUpdateAuthorisationChecker;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.UpdateConsentPsuDataRequestObject;
import de.adorsys.psd2.xs2a.service.validator.tpp.AisConsentTppInfoValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.STATUS_INVALID;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.UNAUTHORIZED;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateConsentPsuDataValidatorTest {
    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final ScaStatus INVALID_SCA_STATUS = ScaStatus.FAILED;
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("psu-id", null, null, null);

    private static final MessageError TPP_VALIDATION_ERROR =
        new MessageError(ErrorType.PIS_401, TppMessageInformation.of(UNAUTHORIZED));
    private static final MessageError STATUS_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_409, TppMessageInformation.of(STATUS_INVALID));

    @Mock
    private AisConsentTppInfoValidator aisConsentTppInfoValidator;
    @Mock
    private AisAuthorisationStatusValidator aisAuthorisationStatusValidator;
    @Mock
    private PsuDataUpdateAuthorisationChecker psuDataUpdateAuthorisationChecker;

    @InjectMocks
    private UpdateConsentPsuDataValidator updateConsentPsuDataValidator;

    @Before
    public void setUp() {
        // Inject pisTppInfoValidator via setter
        updateConsentPsuDataValidator.setAisConsentTppInfoValidator(aisConsentTppInfoValidator);

        when(aisConsentTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(aisConsentTppInfoValidator.validateTpp(INVALID_TPP_INFO))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));
        when(psuDataUpdateAuthorisationChecker.canPsuUpdateAuthorisation(PSU_ID_DATA, null))
            .thenReturn(true);
    }

    @Test
    public void validate_withValidConsentObject_shouldReturnValid() {
        // Given
        when(aisAuthorisationStatusValidator.validate(SCA_STATUS)).thenReturn(ValidationResult.valid());
        AccountConsent accountConsent = buildAccountConsent(TPP_INFO);
        AccountConsentAuthorization authorisation = buildAccountConsentAuthorization(SCA_STATUS);

        // When
        ValidationResult validationResult = updateConsentPsuDataValidator.validate(new UpdateConsentPsuDataRequestObject(accountConsent, authorisation, PSU_ID_DATA));

        // Then
        verify(aisConsentTppInfoValidator).validateTpp(accountConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validate_withInvalidTppInConsent_shouldReturnTppValidationError() {
        // Given
        AccountConsent accountConsent = buildAccountConsent(INVALID_TPP_INFO);
        AccountConsentAuthorization authorisation = buildAccountConsentAuthorization(SCA_STATUS);

        // When
        ValidationResult validationResult = updateConsentPsuDataValidator.validate(new UpdateConsentPsuDataRequestObject(accountConsent, authorisation, PSU_ID_DATA));

        // Then
        verify(aisConsentTppInfoValidator).validateTpp(accountConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validate_withFailedAuthorisation_shouldReturnError() {
        // Given
        AccountConsent accountConsent = buildAccountConsent(TPP_INFO);
        AccountConsentAuthorization authorisation = buildAccountConsentAuthorization(INVALID_SCA_STATUS);
        when(aisAuthorisationStatusValidator.validate(INVALID_SCA_STATUS))
            .thenReturn(ValidationResult.invalid(STATUS_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = updateConsentPsuDataValidator.validate(new UpdateConsentPsuDataRequestObject(accountConsent, authorisation, PSU_ID_DATA));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(STATUS_VALIDATION_ERROR, validationResult.getMessageError());
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }

    private AccountConsent buildAccountConsent(TppInfo tppInfo) {
        return new AccountConsent("id", null, false, null, 0,
                                  null, null, false, false,
                                  Collections.emptyList(), tppInfo, null, false,
                                  Collections.emptyList(), null, Collections.emptyMap());
    }

    private AccountConsentAuthorization buildAccountConsentAuthorization(ScaStatus scaStatus) {
        AccountConsentAuthorization authorisation = new AccountConsentAuthorization();
        authorisation.setScaStatus(scaStatus);
        return authorisation;
    }
}

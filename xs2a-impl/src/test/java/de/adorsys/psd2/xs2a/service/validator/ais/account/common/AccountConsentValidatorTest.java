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

package de.adorsys.psd2.xs2a.service.validator.ais.account.common;

import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.Collections;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class AccountConsentValidatorTest {
    private static final String AUTHORISATION_NUMBER = "authorisation number";
    private static final MessageError AIS_CONSENT_EXPIRED_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(CONSENT_EXPIRED));
    private static final MessageError AIS_CONSENT_INVALID_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(CONSENT_INVALID));
    private static final MessageError AIS_CONSENT_ACCESS_EXCEEDED_ERROR =
        new MessageError(ErrorType.AIS_429, TppMessageInformation.of(ACCESS_EXCEEDED));

    @InjectMocks
    private AccountConsentValidator accountConsentValidator;

    @Test
    public void testValidate_shouldReturnValid() {
        // Given
        AccountConsent accountConsent = buildAccountConsentValid();

        // When
        ValidationResult actual = accountConsentValidator.validate(accountConsent);

        // Then
        assertTrue(actual.isValid());
    }

    @Test
    public void testValidateExpired_shouldReturnExpiredError() {
        // Given
        AccountConsent accountConsent = buildAccountConsentExpired();

        // When
        ValidationResult actual = accountConsentValidator.validate(accountConsent);

        // Then
        assertTrue(actual.isNotValid());
        assertEquals(AIS_CONSENT_EXPIRED_ERROR, actual.getMessageError());
    }

    @Test
    public void testValidateInvalid_shouldReturnInvalidError() {
        // Given
        AccountConsent accountConsent = buildAccountConsentInvalid();

        // When
        ValidationResult actual = accountConsentValidator.validate(accountConsent);

        // Then
        assertTrue(actual.isNotValid());
        assertEquals(AIS_CONSENT_INVALID_ERROR, actual.getMessageError());
    }
    @Test
    public void testValidateAccessExceeded_shouldReturnExceededError() {
        // Given
        AccountConsent accountConsent = buildAccountConsentAccessExceeded();

        // When
        ValidationResult actual = accountConsentValidator.validate(accountConsent);

        // Then
        assertTrue(actual.isNotValid());
        assertEquals(AIS_CONSENT_ACCESS_EXCEEDED_ERROR, actual.getMessageError());
    }

    private AccountConsent buildAccountConsentValid() {
        return new AccountConsent("id", null, false, LocalDate.now().plusYears(1), 0,
                                  null, ConsentStatus.VALID, false, false,
                                  Collections.emptyList(), buildTppInfo(), null, false,
                                  Collections.emptyList(), null, 10);
    }

    private AccountConsent buildAccountConsentExpired() {
        return new AccountConsent("id", null, false, LocalDate.now().minusDays(1), 0,
                                  null, ConsentStatus.VALID, false, false,
                                  Collections.emptyList(), buildTppInfo(), null, false,
                                  Collections.emptyList(), null, 10);
    }

    private AccountConsent buildAccountConsentInvalid() {
        return new AccountConsent("id", null, false, LocalDate.now().plusYears(1), 0,
                                  null, ConsentStatus.RECEIVED, false, false,
                                  Collections.emptyList(), buildTppInfo(), null, false,
                                  Collections.emptyList(), null, 10);
    }

    private AccountConsent buildAccountConsentAccessExceeded() {
        return new AccountConsent("id", null, false, LocalDate.now().plusYears(1), 0,
                                  null, ConsentStatus.VALID, false, false,
                                  Collections.emptyList(), buildTppInfo(), null, false,
                                  Collections.emptyList(), null, 0);
    }

    private static TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(AUTHORISATION_NUMBER);
        return tppInfo;
    }
}

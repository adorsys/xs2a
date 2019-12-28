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
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccountConsentValidatorTest {
    private static final String AUTHORISATION_NUMBER = "authorisation number";
    private static final String REQUEST_URI = "/accounts";

    private static final MessageError AIS_CONSENT_EXPIRED_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(CONSENT_EXPIRED));
    private static final MessageError AIS_CONSENT_INVALID_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(CONSENT_INVALID));
    private static final MessageError AIS_CONSENT_ACCESS_EXCEEDED_ERROR =
        new MessageError(ErrorType.AIS_429, TppMessageInformation.of(ACCESS_EXCEEDED));
    private static final MessageError AIS_CONSENT_INVALID_REVOKED_BY_PSU =
        new MessageError((ErrorType.AIS_401), TppMessageInformation.of(CONSENT_INVALID_REVOKED));

    @InjectMocks
    private AccountConsentValidator accountConsentValidator;
    @Mock
    private RequestProviderService requestProviderService;

    @Test
    public void testValidate_shouldReturnValid() {
        // Given
        AccountConsent accountConsent = buildAccountConsentValid();

        // When
        ValidationResult actual = accountConsentValidator.validate(accountConsent, REQUEST_URI);

        // Then
        assertTrue(actual.isValid());
    }

    @Test
    public void testValidateInvalid_shouldReturnInvalidErrorWithTextConsentRevoked() {
        // Given
        AccountConsent accountConsent = buildAccountConsentInvalidRevokedByPsu();

        // When
        ValidationResult actual = accountConsentValidator.validate(accountConsent, REQUEST_URI);

        // Then
        assertTrue(actual.isNotValid());
        assertEquals(AIS_CONSENT_INVALID_REVOKED_BY_PSU, actual.getMessageError());
    }

    @Test
    public void testValidateExpired_shouldReturnExpiredError() {
        // Given
        AccountConsent accountConsent = buildAccountConsentExpired();

        // When
        ValidationResult actual = accountConsentValidator.validate(accountConsent, REQUEST_URI);

        // Then
        assertTrue(actual.isNotValid());
        assertEquals(AIS_CONSENT_EXPIRED_ERROR, actual.getMessageError());
    }

    @Test
    public void testValidateInvalid_shouldReturnInvalidError() {
        // Given
        AccountConsent accountConsent = buildAccountConsentInvalid();

        // When
        ValidationResult actual = accountConsentValidator.validate(accountConsent, REQUEST_URI);

        // Then
        assertTrue(actual.isNotValid());
        assertEquals(AIS_CONSENT_INVALID_ERROR, actual.getMessageError());
    }

    @Test
    public void testValidateAccessExceeded_shouldReturnExceededError() {
        // Given
        when(requestProviderService.isRequestFromPsu()).thenReturn(false);
        AccountConsent accountConsent = buildAccountConsentAccessExceeded();

        // When
        ValidationResult actual = accountConsentValidator.validate(accountConsent, REQUEST_URI);

        // Then
        assertTrue(actual.isNotValid());
        assertEquals(AIS_CONSENT_ACCESS_EXCEEDED_ERROR, actual.getMessageError());
    }

    @Test
    public void testValidateAccessExceeded_shouldReturnValid_UsageCounterMapNotContainsRequestUri() {
        // Given
        when(requestProviderService.isRequestFromPsu()).thenReturn(false);
        AccountConsent accountConsent = buildAccountConsentAccessExceededIsOneAccessType();

        // When
        ValidationResult actual = accountConsentValidator.validate(accountConsent, REQUEST_URI);

        // Then
        assertTrue(actual.isValid());
    }


    @Test
    public void testValidateAccessExceeded_shouldReturnValid() {
        // Given
        when(requestProviderService.isRequestFromPsu()).thenReturn(true);
        AccountConsent accountConsent = buildAccountConsentAccessExceeded();

        // When
        ValidationResult actual = accountConsentValidator.validate(accountConsent, REQUEST_URI);

        // Then
        assertTrue(actual.isValid());
    }

    @Test
    public void testValidateAccessExceeded_oneOff_shouldReturnExceededError() {
        // Given
        when(requestProviderService.isRequestFromPsu()).thenReturn(false);
        AccountConsent accountConsent = buildOneOffAccountConsentAccessExceeded();

        // When
        ValidationResult actual = accountConsentValidator.validate(accountConsent, REQUEST_URI);

        // Then
        assertTrue(actual.isNotValid());
        assertEquals(AIS_CONSENT_ACCESS_EXCEEDED_ERROR, actual.getMessageError());
    }

    private AccountConsent buildAccountConsentValid() {
        return new AccountConsent("id", null, null, false, LocalDate.now().plusYears(1), 0,
                                  null, ConsentStatus.VALID, false, false,
                                  Collections.emptyList(), buildTppInfo(), null, false,
                                  Collections.emptyList(), null, Collections.singletonMap(REQUEST_URI, 10), OffsetDateTime.now());
    }

    private AccountConsent buildAccountConsentExpired() {
        return new AccountConsent("id", null, null, false, LocalDate.now().minusDays(1), 0,
                                  null, ConsentStatus.VALID, false, false,
                                  Collections.emptyList(), buildTppInfo(), null, false,
                                  Collections.emptyList(), null, Collections.singletonMap(REQUEST_URI, 10), OffsetDateTime.now());
    }

    private AccountConsent buildAccountConsentInvalid() {
        return new AccountConsent("id", null, null, false, LocalDate.now().plusYears(1), 0,
                                  null, ConsentStatus.RECEIVED, false, false,
                                  Collections.emptyList(), buildTppInfo(), null, false,
                                  Collections.emptyList(), null, Collections.singletonMap(REQUEST_URI, 10), OffsetDateTime.now());
    }

    private AccountConsent buildAccountConsentInvalidRevokedByPsu() {
        return new AccountConsent("id", null, null, false, LocalDate.now().plusYears(1), 0,
                                  null, ConsentStatus.REVOKED_BY_PSU, false, false,
                                  Collections.emptyList(), buildTppInfo(), null, false,
                                  Collections.emptyList(), null, Collections.singletonMap(REQUEST_URI, 10), OffsetDateTime.now());
    }

    private AccountConsent buildOneOffAccountConsentAccessExceeded() {
        return new AccountConsent("id", null, null, false, LocalDate.now().plusYears(1), 1,
                                  null, ConsentStatus.VALID, false, false,
                                  Collections.emptyList(), buildTppInfo(), null, false,
                                  Collections.emptyList(), null, Collections.singletonMap(REQUEST_URI, 0), OffsetDateTime.now());
    }

    private AccountConsent buildAccountConsentAccessExceeded() {
        return new AccountConsent("id", null, null, true, LocalDate.now().plusYears(1), 0,
                                  null, ConsentStatus.VALID, false, false,
                                  Collections.emptyList(), buildTppInfo(), null, false,
                                  Collections.emptyList(), null, Collections.singletonMap(REQUEST_URI, 0), OffsetDateTime.now());
    }

    private AccountConsent buildAccountConsentAccessExceededIsOneAccessType() {
        return new AccountConsent("id", null, null, false, LocalDate.now().plusYears(1), 0,
                                  null, ConsentStatus.VALID, false, false,
                                  Collections.emptyList(), buildTppInfo(), null, false,
                                  Collections.emptyList(), null, Collections.emptyMap(), OffsetDateTime.now());
    }

    private static TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(AUTHORISATION_NUMBER);
        return tppInfo;
    }
}

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

package de.adorsys.psd2.xs2a.service.validator.ais.account.common;

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountConsentValidatorTest {
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

    private JsonReader jsonReader = new JsonReader();

    @Test
    void testValidate_shouldReturnValid() {
        // Given
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/ais-consent.json", AisConsent.class);
        aisConsent.setUsages(Collections.singletonMap(REQUEST_URI, 10));

        // When
        ValidationResult actual = accountConsentValidator.validate(aisConsent, REQUEST_URI);

        // Then
        assertTrue(actual.isValid());
    }

    @Test
    void testValidateInvalid_shouldReturnInvalidErrorWithTextConsentRevoked() {
        // Given
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/ais-consent-revoked-by-psu.json", AisConsent.class);

        // When
        ValidationResult actual = accountConsentValidator.validate(aisConsent, REQUEST_URI);

        // Then
        assertTrue(actual.isNotValid());
        assertEquals(AIS_CONSENT_INVALID_REVOKED_BY_PSU, actual.getMessageError());
    }

    @Test
    void testValidateExpired_shouldReturnExpiredError() {
        // Given
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/ais-consent-expired.json", AisConsent.class);

        // When
        ValidationResult actual = accountConsentValidator.validate(aisConsent, REQUEST_URI);

        // Then
        assertTrue(actual.isNotValid());
        assertEquals(AIS_CONSENT_EXPIRED_ERROR, actual.getMessageError());
    }

    @Test
    void testValidateInvalid_shouldReturnInvalidError() {
        // Given
        AisConsent aisConsent = buildAccountConsentInvalid();

        // When
        ValidationResult actual = accountConsentValidator.validate(aisConsent, REQUEST_URI);

        // Then
        assertTrue(actual.isNotValid());
        assertEquals(AIS_CONSENT_INVALID_ERROR, actual.getMessageError());
    }

    @Test
    void testValidateAccessExceeded_shouldReturnExceededError() {
        // Given
        when(requestProviderService.isRequestFromPsu())
            .thenReturn(false);
        AisConsent aisConsent = buildAccountConsentAccessExceeded();

        // When
        ValidationResult actual = accountConsentValidator.validate(aisConsent, REQUEST_URI);

        // Then
        assertTrue(actual.isNotValid());
        assertEquals(AIS_CONSENT_ACCESS_EXCEEDED_ERROR, actual.getMessageError());
    }

    @Test
    void testValidateAccessExceeded_shouldReturnValid_UsageCounterMapNotContainsRequestUri() {
        // Given
        when(requestProviderService.isRequestFromPsu())
            .thenReturn(false);
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/ais-consent-one-access-type.json", AisConsent.class);

        // When
        ValidationResult actual = accountConsentValidator.validate(aisConsent, REQUEST_URI);

        // Then
        assertTrue(actual.isValid());
    }


    @Test
    void testValidateAccessExceeded_shouldReturnValid() {
        // Given
        when(requestProviderService.isRequestFromPsu()).thenReturn(true);
        AisConsent aisConsent = buildAccountConsentAccessExceeded();

        // When
        ValidationResult actual = accountConsentValidator.validate(aisConsent, REQUEST_URI);

        // Then
        assertTrue(actual.isValid());
    }

    @Test
    void testValidateAccessExceeded_oneOff_shouldReturnExceededError() {
        // Given
        when(requestProviderService.isRequestFromPsu()).thenReturn(false);
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/ais-consent.json", AisConsent.class);
        aisConsent.setUsages(Collections.singletonMap(REQUEST_URI, 0));

        // When
        ValidationResult actual = accountConsentValidator.validate(aisConsent, REQUEST_URI);

        // Then
        assertTrue(actual.isNotValid());
        assertEquals(AIS_CONSENT_ACCESS_EXCEEDED_ERROR, actual.getMessageError());
    }

    private AisConsent buildAccountConsentInvalid() {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setId("id");
        aisConsent.setTppAccountAccesses(AccountAccess.EMPTY_ACCESS);
        aisConsent.setAspspAccountAccesses(AccountAccess.EMPTY_ACCESS);
        aisConsent.setConsentData(new AisConsentData(null, null, null, false));
        aisConsent.setValidUntil(LocalDate.now().plusYears(1));
        aisConsent.setConsentStatus(ConsentStatus.RECEIVED);
        return aisConsent;
    }

    private AisConsent buildAccountConsentAccessExceeded() {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setValidUntil(LocalDate.now().plusYears(1));
        aisConsent.setUsages(Collections.singletonMap(REQUEST_URI, 0));
        aisConsent.setConsentStatus(ConsentStatus.VALID);
        aisConsent.setRecurringIndicator(true);
        return aisConsent;
    }
}

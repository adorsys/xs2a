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

package de.adorsys.psd2.xs2a.service.validator.ais.account;

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.GetTrustedBeneficiariesListConsentObject;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_401;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_405;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_EXPIRED;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.SERVICE_INVALID_405;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTrustedBeneficiariesListValidatorTest {
    private static final String ACCOUNT_ID = "accountId";
    private static final String REQUEST_URI = "requestUri";

    @InjectMocks
    private GetTrustedBeneficiariesListValidator getTrustedBeneficiariesListValidator;
    @Mock
    private AccountConsentValidator accountConsentValidator;
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;

    private JsonReader jsonReader = new JsonReader();

    private  AisConsent aisConsent;
    private GetTrustedBeneficiariesListConsentObject getTrustedBeneficiariesListConsentObject;

    @BeforeEach
    void setUp() {
        aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/ais-consent-all-available-accounts.json", AisConsent.class);
        getTrustedBeneficiariesListConsentObject = new GetTrustedBeneficiariesListConsentObject(aisConsent, ACCOUNT_ID, REQUEST_URI);
    }

    @Test
    void executeBusinessValidation_consentExpired() {
        // Given
        ValidationResult expected = ValidationResult.invalid(AIS_401, CONSENT_EXPIRED);

        when(accountConsentValidator.validate(aisConsent, REQUEST_URI)).thenReturn(ValidationResult.invalid(AIS_401, CONSENT_EXPIRED));

        // When
        ValidationResult actual = getTrustedBeneficiariesListValidator.executeBusinessValidation(getTrustedBeneficiariesListConsentObject);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.isNotValid()).isTrue();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void executeBusinessValidation_serviceInvalid() {
        // Given
        ValidationResult expected = ValidationResult.invalid(AIS_405, SERVICE_INVALID_405);

        when(accountConsentValidator.validate(aisConsent, REQUEST_URI)).thenReturn(ValidationResult.valid());
        when(aspspProfileService.isTrustedBeneficiariesSupported()).thenReturn(false);

        // When
        ValidationResult actual = getTrustedBeneficiariesListValidator.executeBusinessValidation(getTrustedBeneficiariesListConsentObject);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.isNotValid()).isTrue();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void executeBusinessValidation_validGlobal() {
        // Given
        aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/ais-consent-all-accounts.json", AisConsent.class);
        getTrustedBeneficiariesListConsentObject = new GetTrustedBeneficiariesListConsentObject(aisConsent, ACCOUNT_ID, REQUEST_URI);

        when(accountConsentValidator.validate(aisConsent, REQUEST_URI)).thenReturn(ValidationResult.valid());
        when(aspspProfileService.isTrustedBeneficiariesSupported()).thenReturn(true);

        // When
        ValidationResult actual = getTrustedBeneficiariesListValidator.executeBusinessValidation(getTrustedBeneficiariesListConsentObject);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.isValid()).isTrue();
    }

    @Test
    void executeBusinessValidation_consentInvalidAllAvailable() {
        // Given
        ValidationResult expected = ValidationResult.invalid(AIS_401, MessageErrorCode.CONSENT_INVALID);

        when(accountConsentValidator.validate(aisConsent, REQUEST_URI)).thenReturn(ValidationResult.valid());
        when(aspspProfileService.isTrustedBeneficiariesSupported()).thenReturn(true);

        // When
        ValidationResult actual = getTrustedBeneficiariesListValidator.executeBusinessValidation(getTrustedBeneficiariesListConsentObject);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.isNotValid()).isTrue();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void executeBusinessValidation_consentInvalidDedicated() {
        // Given
        ValidationResult expected = ValidationResult.invalid(AIS_401, MessageErrorCode.CONSENT_INVALID);
        aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/ais-consent-dedicated.json", AisConsent.class);
        getTrustedBeneficiariesListConsentObject = new GetTrustedBeneficiariesListConsentObject(aisConsent, ACCOUNT_ID, REQUEST_URI);

        when(accountConsentValidator.validate(aisConsent, REQUEST_URI)).thenReturn(ValidationResult.valid());
        when(aspspProfileService.isTrustedBeneficiariesSupported()).thenReturn(true);

        // When
        ValidationResult actual = getTrustedBeneficiariesListValidator.executeBusinessValidation(getTrustedBeneficiariesListConsentObject);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.isNotValid()).isTrue();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void executeBusinessValidation_valid() {
        // Given
        aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/ais-consent-dedicated-with-beneficiaries.json", AisConsent.class);
        getTrustedBeneficiariesListConsentObject = new GetTrustedBeneficiariesListConsentObject(aisConsent, ACCOUNT_ID, REQUEST_URI);

        when(accountConsentValidator.validate(aisConsent, REQUEST_URI)).thenReturn(ValidationResult.valid());
        when(aspspProfileService.isTrustedBeneficiariesSupported()).thenReturn(true);

        // When
        ValidationResult actual = getTrustedBeneficiariesListValidator.executeBusinessValidation(getTrustedBeneficiariesListConsentObject);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.isValid()).isTrue();
    }
}

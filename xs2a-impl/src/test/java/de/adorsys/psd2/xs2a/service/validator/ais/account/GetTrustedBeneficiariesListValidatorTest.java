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

package de.adorsys.psd2.xs2a.service.validator.ais.account;

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
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

import java.util.Set;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_401;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_405;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_EXPIRED;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.SERVICE_INVALID_405;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
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

    private final JsonReader jsonReader = new JsonReader();

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

    @Test
    void buildWarningMessages() {
        // Given
        aisConsent =
            jsonReader.getObjectFromFile("json/service/validator/ais/account/ais-consent-dedicated-with-beneficiaries.json", AisConsent.class);
        getTrustedBeneficiariesListConsentObject =
            new GetTrustedBeneficiariesListConsentObject(aisConsent, ACCOUNT_ID, REQUEST_URI);

        //When
        Set<TppMessageInformation> actual =
            getTrustedBeneficiariesListValidator.buildWarningMessages(getTrustedBeneficiariesListConsentObject);

        //Then
        assertThat(actual).isEmpty();
        verifyNoInteractions(accountConsentValidator);
        verifyNoInteractions(aspspProfileService);
    }
}

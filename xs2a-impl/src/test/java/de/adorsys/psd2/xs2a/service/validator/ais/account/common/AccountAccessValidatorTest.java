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

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_INVALID;
import static org.assertj.core.api.Assertions.assertThat;

class AccountAccessValidatorTest {

    private static final MessageError AIS_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(CONSENT_INVALID));

    private AccountAccessValidator accountAccessValidator;

    private JsonReader jsonReader;
    private AisConsent aisConsent;

    @BeforeEach
    void setUp() {
        accountAccessValidator = new AccountAccessValidator();
        jsonReader = new JsonReader();
    }

    @ParameterizedTest
    @MethodSource("params")
    void testValidate_allAccountConsentWithBalances(String path, boolean withBalance) {
        aisConsent = jsonReader.getObjectFromFile(path, AisConsent.class);

        ValidationResult actual = accountAccessValidator.validate(aisConsent, withBalance);

        assertThat(actual.isValid()).isTrue();
    }

    private static Stream<Arguments> params() {
        String xs2aAccountConsentAllAccsBalancePath =
            "json/service/validator/ais/account/xs2a-account-consent-all-available-accounts-with-balance.json";
        String xs2aAccountConsentAllAccsBalanceOwnerNamePath =
            "json/service/validator/ais/account/xs2a-account-consent-all-available-accounts-with-balance_with_owner_name.json";
        String xs2aAccountConsentPath = "json/service/validator/ais/account/xs2a-account-consent.json";

        return Stream.of(Arguments.arguments(xs2aAccountConsentAllAccsBalancePath, true),
                         Arguments.arguments(xs2aAccountConsentAllAccsBalanceOwnerNamePath, true),
                         Arguments.arguments(xs2aAccountConsentPath, false)
        );
    }

    @Test
    void testValidate_withBalanceAndNullBalances_shouldReturnError() {
        // Given
        aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-consent.json", AisConsent.class);
        aisConsent.getAccess().getBalances().clear();

        // When
        ValidationResult actual = accountAccessValidator.validate(aisConsent, true);

        // Then
        assertThat(actual.isNotValid()).isTrue();
        assertThat(actual.getMessageError()).isEqualTo(AIS_VALIDATION_ERROR);
    }

    @Test
    void testValidate_globalConsent_withBalanceAndNullBalances_shouldReturnError() {
        // Given
        aisConsent =
            jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-consent-global.json",
                AisConsent.class);
        assertThat(aisConsent.getAccess().getBalances()).isNull();

        // When
        ValidationResult actual = accountAccessValidator.validate(aisConsent, true);

        // Then
        assertThat(actual.isValid()).isTrue();
    }

    @Test
    void testValidate_globalConsent_withoutBalanceAndNullBalances_shouldReturnError() {
        // Given
        aisConsent =
            jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-consent-global.json",
                AisConsent.class);
        assertThat(aisConsent.getAccess().getBalances()).isNull();

        // When
        ValidationResult actual = accountAccessValidator.validate(aisConsent, false);

        // Then
        assertThat(actual.isValid()).isTrue();
    }
}

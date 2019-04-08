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

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.CONSENT_INVALID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class AccountAccessValidatorTest {

    private static final String AUTHORISATION_NUMBER = "authorisation number";
    private static final MessageError AIS_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(CONSENT_INVALID));

    @InjectMocks
    private AccountAccessValidator accountAccessValidator;

    @Test
    public void testValidate_withoutBalance_shouldReturnValid() {
        // Given
        AccountConsent accountConsent = buildAccountConsent();

        // When
        ValidationResult actual = accountAccessValidator.validate(accountConsent, false);

        // Then
        assertTrue(actual.isValid());
    }

    @Test
    public void testValidate_withBalanceAndEmptyAccess_shouldReturnError() {
        // Given
        AccountConsent accountConsent = buildAccountConsentEmptyAccesses();

        // When
        ValidationResult actual = accountAccessValidator.validate(accountConsent, true);

        // Then
        assertTrue(actual.isNotValid());
        assertEquals(AIS_VALIDATION_ERROR, actual.getMessageError());
    }

    @Test
    public void testValidate_withBalanceAndNullBalances_shouldReturnError() {
        // Given
        AccountConsent accountConsent = buildAccountConsent();

        // When
        ValidationResult actual = accountAccessValidator.validate(accountConsent, true);

        // Then
        assertTrue(actual.isNotValid());
        assertEquals(AIS_VALIDATION_ERROR, actual.getMessageError());
    }


    private AccountConsent buildAccountConsent() {
        return new AccountConsent("id", null, false, null, 0,
                                  null, null, false, false,
                                  Collections.emptyList(), buildTppInfo(), null, false,
                                  Collections.emptyList(), null, 0);
    }

    private AccountConsent buildAccountConsentEmptyAccesses() {
        return new AccountConsent("id", new Xs2aAccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, null), false, null, 0,
                                  null, null, false, false,
                                  Collections.emptyList(), buildTppInfo(), null, false,
                                  Collections.emptyList(), null, 0);
    }

    private static TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(AUTHORISATION_NUMBER);
        return tppInfo;
    }
}

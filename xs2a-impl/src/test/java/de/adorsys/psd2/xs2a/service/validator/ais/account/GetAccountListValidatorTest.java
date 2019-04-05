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

package de.adorsys.psd2.xs2a.service.validator.ais.account;

import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountAccessValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.tpp.AisTppInfoValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.CONSENT_INVALID;
import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.UNAUTHORIZED;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GetAccountListValidatorTest {
    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");

    private static final MessageError TPP_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(UNAUTHORIZED));
    private static final MessageError ACCESS_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(CONSENT_INVALID));

    @Mock
    private AccountConsentValidator accountConsentValidator;
    @Mock
    private AisTppInfoValidator aisTppInfoValidator;
    @Mock
    private AccountAccessValidator accountAccessValidator;

    @InjectMocks
    private GetAccountListValidator getAccountListValidator;

    @Before
    public void setUp() {
        // Inject pisTppInfoValidator via setter
        getAccountListValidator.setPisTppInfoValidator(aisTppInfoValidator);

        when(aisTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(aisTppInfoValidator.validateTpp(INVALID_TPP_INFO))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));
    }

    @Test
    public void validate_withValidConsentObject_shouldReturnValid() {
        // Given
        Xs2aAccountAccess accountAccess = buildXs2aAccountAccess();
        AccountConsent accountConsent = buildAccountConsent(accountAccess, TPP_INFO);
        when(accountConsentValidator.validate(accountConsent))
            .thenReturn(ValidationResult.valid());
        when(accountAccessValidator.validate(accountConsent, accountConsent.isWithBalance()))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = getAccountListValidator.validate(new GetAccountListConsentObject(accountConsent, false));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validate_withBalanceRequestAndValidAccess_shouldReturnValid() {
        // Given
        Xs2aAccountAccess accessWithBalances = buildXs2aAccountAccess(true);
        AccountConsent accountConsent = buildAccountConsent(accessWithBalances, TPP_INFO);
        when(accountConsentValidator.validate(accountConsent))
            .thenReturn(ValidationResult.valid());
        when(accountAccessValidator.validate(accountConsent, accountConsent.isWithBalance()))
            .thenReturn(ValidationResult.valid());
        when(accountAccessValidator.validate(any(), anyBoolean()))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = getAccountListValidator.validate(new GetAccountListConsentObject(accountConsent, true));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validate_withInvalidTppInConsent_shouldReturnTppValidationError() {
        // Given
        Xs2aAccountAccess accountAccess = buildXs2aAccountAccess();
        AccountConsent accountConsent = buildAccountConsent(accountAccess, INVALID_TPP_INFO);

        // When
        ValidationResult validationResult = getAccountListValidator.validate(new GetAccountListConsentObject(accountConsent, false));

        // Then
        verify(aisTppInfoValidator).validateTpp(accountConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validate_withBalanceRequestAndNoBalanceAccessInConsent_shouldReturnAccessValidationError() {
        // Given
        Xs2aAccountAccess accountAccess = buildXs2aAccountAccess();
        AccountConsent accountConsent = buildAccountConsent(accountAccess, TPP_INFO);
        when(accountConsentValidator.validate(accountConsent))
            .thenReturn(ValidationResult.valid());
        when(accountAccessValidator.validate(any(), anyBoolean()))
            .thenReturn(ValidationResult.invalid(ACCESS_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = getAccountListValidator.validate(new GetAccountListConsentObject(accountConsent, true));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(ACCESS_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validate_withBalanceRequestAndNullAccess_shouldReturnAccessValidationError() {
        // Given
        AccountConsent accountConsent = buildAccountConsent(null, TPP_INFO);
        when(accountConsentValidator.validate(accountConsent))
            .thenReturn(ValidationResult.valid());
        when(accountAccessValidator.validate(any(), anyBoolean()))
            .thenReturn(ValidationResult.invalid(ACCESS_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = getAccountListValidator.validate(new GetAccountListConsentObject(accountConsent, true));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(ACCESS_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validate_withInvalidTppInConsentAndInvalidAccess_shouldReturnTppValidationErrorFirst() {
        // Given
        Xs2aAccountAccess accountAccess = buildXs2aAccountAccess();
        AccountConsent accountConsent = buildAccountConsent(accountAccess, INVALID_TPP_INFO);

        // When
        ValidationResult validationResult = getAccountListValidator.validate(new GetAccountListConsentObject(accountConsent, true));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }

    private AccountConsent buildAccountConsent(Xs2aAccountAccess xs2aAccountAccess, TppInfo tppInfo) {
        return new AccountConsent("id", xs2aAccountAccess, false, null, 0,
                                  null, null, false, false,
                                  Collections.emptyList(), tppInfo, null, false,
                                  Collections.emptyList(), null, 0);
    }

    private Xs2aAccountAccess buildXs2aAccountAccess() {
        return buildXs2aAccountAccess(false);
    }

    private Xs2aAccountAccess buildXs2aAccountAccess(boolean withBalancesAccess) {
        List<AccountReference> accountReferences = Collections.singletonList(new AccountReference());
        List<AccountReference> balances = withBalancesAccess
                                              ? accountReferences
                                              : Collections.emptyList();

        return new Xs2aAccountAccess(accountReferences, balances, accountReferences, null, null);
    }
}

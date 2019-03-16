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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.Collections;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.FORMAT_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateConsentRequestValidatorTest {
    @InjectMocks
    private CreateConsentRequestValidator createConsentRequestValidator;
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private ScaApproachResolver scaApproachResolver;

    @Before
    public void setUp() {
        when(aspspProfileService.getAllPsd2Support()).thenReturn(false);
        when(aspspProfileService.isBankOfferedConsentSupported()).thenReturn(true);
        when(aspspProfileService.isAvailableAccountsConsentSupported()).thenReturn(true);
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
    }

    @Test
    public void validateRequestSuccess_RecurringIndicatorTrue() {
        //Given
        CreateConsentReq createConsentReq = buildCreateConsentReq(true, 1);
        //When
        ValidationResult validationResult = createConsentRequestValidator.validateRequest(createConsentReq);
        //Then
        assertValidationResultValid(validationResult);
    }

    @Test
    public void validateRequestSuccess_RecurringIndicatorFalse() {
        //Given
        CreateConsentReq createConsentReq = buildCreateConsentReq(false, 1);
        //When
        ValidationResult validationResult = createConsentRequestValidator.validateRequest(createConsentReq);
        //Then
        assertValidationResultValid(validationResult);
    }

    @Test
    public void validateRequestFail_RecurringIndicatorTrueFrequencyPerDayMinus() {
        //Given
        CreateConsentReq createConsentReq = buildCreateConsentReq(true, -1);
        //When
        ValidationResult validationResult = createConsentRequestValidator.validateRequest(createConsentReq);
        //Then
        assertValidationResultNotValid(validationResult);
    }

    @Test
    public void validateRequestFail_RecurringIndicatorTrueFrequencyPerDayZero() {
        //Given
        CreateConsentReq createConsentReq = buildCreateConsentReq(true, 0);
        //When
        ValidationResult validationResult = createConsentRequestValidator.validateRequest(createConsentReq);
        //Then
        assertValidationResultNotValid(validationResult);
    }

    @Test
    public void validateRequestFail_RecurringIndicatorFalseFrequencyPerDayMinus() {
        //Given
        CreateConsentReq createConsentReq = buildCreateConsentReq(false, -1);
        //When
        ValidationResult validationResult = createConsentRequestValidator.validateRequest(createConsentReq);
        //Then
        assertValidationResultNotValid(validationResult);
    }

    @Test
    public void validateRequestFail_RecurringIndicatorFalseFrequencyPerDayZero() {
        //Given
        CreateConsentReq createConsentReq = buildCreateConsentReq(false, 0);
        //When
        ValidationResult validationResult = createConsentRequestValidator.validateRequest(createConsentReq);
        //Then
        assertValidationResultNotValid(validationResult);
    }

    @Test
    public void validateRequestFail_RecurringIndicatorFalseFrequencyPerDayMoreThanOne() {
        //Given
        CreateConsentReq createConsentReq = buildCreateConsentReq(false, 2);
        //When
        ValidationResult validationResult = createConsentRequestValidator.validateRequest(createConsentReq);
        //Then
        assertValidationResultNotValid(validationResult);
    }

    @NotNull
    private CreateConsentReq buildCreateConsentReq(boolean recurringIndicator, int frequencyPerDay) {
        CreateConsentReq createConsentReq = new CreateConsentReq();
        createConsentReq.setValidUntil(LocalDate.now().plusDays(1));
        createConsentReq.setRecurringIndicator(recurringIndicator);
        createConsentReq.setFrequencyPerDay(frequencyPerDay);
        Xs2aAccountAccess accountAccess = new Xs2aAccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), AccountAccessType.ALL_ACCOUNTS, null);
        createConsentReq.setAccess(accountAccess);
        return createConsentReq;
    }

    private void assertValidationResultValid(ValidationResult validationResult) {
        assertThat(validationResult.isValid()).isTrue();
        assertThat(validationResult.getMessageError()).isNull();
    }

    private void assertValidationResultNotValid(ValidationResult validationResult) {
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isNotNull();
        assertThat(validationResult.getMessageError().getErrorType()).isEqualTo(ErrorType.AIS_400);
        assertThat(validationResult.getMessageError().getTppMessage().getMessageErrorCode()).isEqualTo(FORMAT_ERROR);
    }
}

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

import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.PsuDataInInitialRequestValidator;
import de.adorsys.psd2.xs2a.service.validator.SupportedAccountReferenceValidator;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.CreateConsentRequestObject;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Currency;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.FORMAT_ERROR;
import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.SESSIONS_NOT_SUPPORTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateConsentRequestValidatorTest {
    private static final MessageError COMBINED_SERVICE_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(SESSIONS_NOT_SUPPORTED));
    private static final MessageError PSU_DATA_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(FORMAT_ERROR));
    private static final MessageError SUPPORTED_ACCOUNT_REFERENCE_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(FORMAT_ERROR));
    private static final PsuIdData EMPTY_PSU_DATA = new PsuIdData(null, null, null, null);

    @InjectMocks
    private CreateConsentRequestValidator createConsentRequestValidator;
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private PsuDataInInitialRequestValidator psuDataInInitialRequestValidator;
    @Mock
    private SupportedAccountReferenceValidator supportedAccountReferenceValidator;

    @Before
    public void setUp() {
        when(aspspProfileService.getAllPsd2Support()).thenReturn(false);
        when(aspspProfileService.isBankOfferedConsentSupported()).thenReturn(true);
        when(aspspProfileService.isAvailableAccountsConsentSupported()).thenReturn(true);
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.valid());
        when(supportedAccountReferenceValidator.validate(anyCollectionOf(AccountReference.class)))
            .thenReturn(ValidationResult.valid());
    }

    @Test
    public void validate_withInvalidPsuData_shouldReturnErrorFromValidator() {
        //Given
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.invalid(PSU_DATA_VALIDATION_ERROR));
        CreateConsentReq createConsentReq = buildCreateConsentReqWithCombinedServiceIndicator(false);

        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));

        //Then
        verify(psuDataInInitialRequestValidator).validate(EMPTY_PSU_DATA);
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(PSU_DATA_VALIDATION_ERROR);
    }

    @Test
    public void validate_withUnsupportedAccountReference_shouldReturnErrorFromValidator() {
        //Given
        when(supportedAccountReferenceValidator.validate(anyCollectionOf(AccountReference.class)))
            .thenReturn(ValidationResult.invalid(SUPPORTED_ACCOUNT_REFERENCE_VALIDATION_ERROR));

        AccountReference accountReference = new AccountReference(AccountReferenceType.IBAN, "some iban", Currency.getInstance("EUR"));
        Xs2aAccountAccess xs2aAccountAccess = new Xs2aAccountAccess(Collections.singletonList(accountReference), Collections.emptyList(), Collections.emptyList(), null, null);
        CreateConsentReq createConsentReq = buildCreateConsentReqWithAccess(xs2aAccountAccess);

        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));

        //Then
        verify(supportedAccountReferenceValidator).validate(Collections.singleton(accountReference));
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(SUPPORTED_ACCOUNT_REFERENCE_VALIDATION_ERROR);
    }

    @Test
    public void validateSuccess_RecurringIndicatorTrue() {
        //Given
        CreateConsentReq createConsentReq = buildCreateConsentReq(true, 1);
        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));
        //Then
        assertValidationResultValid(validationResult);
    }

    @Test
    public void validateSuccess_RecurringIndicatorFalse() {
        //Given
        CreateConsentReq createConsentReq = buildCreateConsentReq(false, 1);
        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));
        //Then
        assertValidationResultValid(validationResult);
    }

    @Test
    public void validateSuccess_ValidUntilToday() {
        //Given
        CreateConsentReq createConsentReq = buildCreateConsentReq(true, 1, LocalDate.now());
        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));
        //Then
        assertValidationResultValid(validationResult);
    }

    @Test
    public void validateSuccess_FlagsAndAccessesEmpty() {
        //Given
        CreateConsentReq createConsentReq = buildCreateConsentReqWithoutFlagsAndAccesses(true, 1);
        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));
        //Then
        assertValidationResultValid(validationResult);
    }

    @Test
    public void validateSuccess_FlagsPresentAccessesEmpty() {
        //Given
        CreateConsentReq createConsentReq = buildCreateConsentReq(true, 1);
        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));
        //Then
        assertValidationResultValid(validationResult);
    }

    @Test
    public void validate_withSupportedCombinedServiceIndicator_shouldReturnValid() {
        //Given
        when(aspspProfileService.isCombinedServiceIndicator()).thenReturn(true);
        CreateConsentReq createConsentReq = buildCreateConsentReqWithCombinedServiceIndicator(true);

        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));

        //Then
        assertValidationResultValid(validationResult);
    }

    @Test
    public void validate_withoutSupportedCombinedServiceIndicator_shouldReturnValid() {
        //Given
        when(aspspProfileService.isCombinedServiceIndicator()).thenReturn(true);
        CreateConsentReq createConsentReq = buildCreateConsentReqWithCombinedServiceIndicator(false);

        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));

        //Then
        assertValidationResultValid(validationResult);
    }

    @Test
    public void validate_withoutNotSupportedCombinedServiceIndicator_shouldReturnValid() {
        //Given
        when(aspspProfileService.isCombinedServiceIndicator()).thenReturn(false);
        CreateConsentReq createConsentReq = buildCreateConsentReqWithCombinedServiceIndicator(false);

        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));

        //Then
        assertValidationResultValid(validationResult);
    }

    @Test
    public void validate_withNotSupportedCombinedServiceIndicator_shouldReturnFormatError() {
        //Given
        when(aspspProfileService.isCombinedServiceIndicator()).thenReturn(false);
        CreateConsentReq createConsentReq = buildCreateConsentReqWithCombinedServiceIndicator(true);

        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));

        //Then
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(COMBINED_SERVICE_VALIDATION_ERROR);
    }

    private CreateConsentReq buildCreateConsentReqWithAccess(Xs2aAccountAccess xs2aAccountAccess) {
        CreateConsentReq createConsentReq = buildCreateConsentReqWithCombinedServiceIndicator(false);
        createConsentReq.setAccess(xs2aAccountAccess);
        return createConsentReq;
    }

    private CreateConsentReq buildCreateConsentReqWithCombinedServiceIndicator(boolean combinedServiceIndicator) {
        CreateConsentReq createConsentReq = buildCreateConsentReq(true, 2);
        createConsentReq.setCombinedServiceIndicator(combinedServiceIndicator);
        return createConsentReq;
    }

    private CreateConsentReq buildCreateConsentReq(boolean recurringIndicator, int frequencyPerDay) {
        return buildCreateConsentReq(recurringIndicator, frequencyPerDay, LocalDate.now().plusDays(1));
    }

    @NotNull
    private CreateConsentReq buildCreateConsentReq(boolean recurringIndicator, int frequencyPerDay, LocalDate validUntil) {
        CreateConsentReq createConsentReq = new CreateConsentReq();
        createConsentReq.setValidUntil(validUntil);
        createConsentReq.setRecurringIndicator(recurringIndicator);
        createConsentReq.setFrequencyPerDay(frequencyPerDay);
        Xs2aAccountAccess accountAccess = new Xs2aAccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), AccountAccessType.ALL_ACCOUNTS, null);
        createConsentReq.setAccess(accountAccess);
        return createConsentReq;
    }

    private CreateConsentReq buildCreateConsentReqWithoutFlagsAndAccesses(boolean recurringIndicator, int frequencyPerDay) {
        CreateConsentReq createConsentReq = buildCreateConsentReq(recurringIndicator, frequencyPerDay);
        Xs2aAccountAccess accountAccess = new Xs2aAccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, null);
        createConsentReq.setAccess(accountAccess);
        return createConsentReq;
    }

    private void assertValidationResultValid(ValidationResult validationResult) {
        assertThat(validationResult.isValid()).isTrue();
        assertThat(validationResult.getMessageError()).isNull();
    }
}

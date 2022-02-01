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

package de.adorsys.psd2.xs2a.service.validator.ais.consent;

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.PsuDataInInitialRequestValidator;
import de.adorsys.psd2.xs2a.service.validator.SupportedAccountReferenceValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.CreateConsentRequestObject;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Currency;
import java.util.Set;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateConsentRequestValidatorTest {
    private static final MessageError COMBINED_SERVICE_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(SESSIONS_NOT_SUPPORTED));
    private static final MessageError PSU_DATA_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(FORMAT_ERROR));
    private static final MessageError SUPPORTED_ACCOUNT_REFERENCE_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(FORMAT_ERROR));
    private static final PsuIdData EMPTY_PSU_DATA = new PsuIdData(null, null, null, null, null);

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

    @Test
    void validate_withInvalidPsuData_shouldReturnErrorFromValidator() {
        //Given
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.invalid(PSU_DATA_VALIDATION_ERROR));
        CreateConsentReq createConsentReq = buildCreateConsentReqWithCombinedServiceIndicator(false, AccountAccessType.ALL_ACCOUNTS, null, null);

        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));

        //Then
        verify(psuDataInInitialRequestValidator).validate(EMPTY_PSU_DATA);
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(PSU_DATA_VALIDATION_ERROR);
    }

    @Test
    void validate_withUnsupportedAccountReference_shouldReturnErrorFromValidator() {
        //Given
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.valid());
        when(supportedAccountReferenceValidator.validate(anyCollection()))
            .thenReturn(ValidationResult.invalid(SUPPORTED_ACCOUNT_REFERENCE_VALIDATION_ERROR));

        AccountReference accountReference = buildAccountReference();
        AccountAccess accountAccess = new AccountAccess(Collections.singletonList(accountReference), Collections.emptyList(), Collections.emptyList(), null);
        CreateConsentReq createConsentReq = buildCreateConsentReqWithAccess(accountAccess, AccountAccessType.ALL_ACCOUNTS, null, null);

        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));

        //Then
        verify(supportedAccountReferenceValidator).validate(Collections.singleton(accountReference));
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(SUPPORTED_ACCOUNT_REFERENCE_VALIDATION_ERROR);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void validateSuccess_RecurringIndicator(boolean recurringIndicator) {
        //Given
        CreateConsentReq createConsentReq = buildCreateConsentReq(recurringIndicator, 1, AccountAccessType.ALL_ACCOUNTS, null, null);
        when(aspspProfileService.isAvailableAccountsConsentSupported()).thenReturn(true);
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.valid());
        when(supportedAccountReferenceValidator.validate(anyCollection()))
            .thenReturn(ValidationResult.valid());

        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));
        //Then
        assertValidationResultValid(validationResult);
    }

    @Test
    void validateSuccess_ValidUntilToday() {
        //Given
        CreateConsentReq createConsentReq = buildCreateConsentReq(true, 1, LocalDate.now(), AccountAccessType.ALL_ACCOUNTS, null, null);
        when(aspspProfileService.isAvailableAccountsConsentSupported()).thenReturn(true);
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.valid());
        when(supportedAccountReferenceValidator.validate(anyCollection()))
            .thenReturn(ValidationResult.valid());

        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));
        //Then
        assertValidationResultValid(validationResult);
    }

    @Test
    void validateSuccess_FlagsAndAccessesEmpty() {
        //Given
        CreateConsentReq createConsentReq = buildCreateConsentReqWithoutFlagsAndAccesses(true, 1, null, null, null);
        when(aspspProfileService.isBankOfferedConsentSupported()).thenReturn(true);
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.valid());
        when(supportedAccountReferenceValidator.validate(anyCollection()))
            .thenReturn(ValidationResult.valid());

        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));
        //Then
        assertValidationResultValid(validationResult);
    }

    @Test
    void validate_withSupportedCombinedServiceIndicator_shouldReturnValid() {
        //Given
        when(aspspProfileService.isAisPisSessionsSupported()).thenReturn(true);
        CreateConsentReq createConsentReq = buildCreateConsentReqWithCombinedServiceIndicator(true, AccountAccessType.ALL_ACCOUNTS, null, null);
        when(aspspProfileService.isAvailableAccountsConsentSupported()).thenReturn(true);
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.valid());
        when(supportedAccountReferenceValidator.validate(anyCollection()))
            .thenReturn(ValidationResult.valid());

        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));

        //Then
        assertValidationResultValid(validationResult);
    }

    @Test
    void validate_withoutSupportedCombinedServiceIndicator_shouldReturnValid() {
        //Given
        CreateConsentReq createConsentReq = buildCreateConsentReqWithCombinedServiceIndicator(false, AccountAccessType.ALL_ACCOUNTS, null, null);
        when(aspspProfileService.isAvailableAccountsConsentSupported()).thenReturn(true);
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.valid());
        when(supportedAccountReferenceValidator.validate(anyCollection()))
            .thenReturn(ValidationResult.valid());

        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));

        //Then
        assertValidationResultValid(validationResult);
    }

    @Test
    void validate_withoutNotSupportedCombinedServiceIndicator_shouldReturnValid() {
        //Given
        CreateConsentReq createConsentReq = buildCreateConsentReqWithCombinedServiceIndicator(false, AccountAccessType.ALL_ACCOUNTS, null, null);
        when(aspspProfileService.isAvailableAccountsConsentSupported()).thenReturn(true);
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.valid());
        when(supportedAccountReferenceValidator.validate(anyCollection()))
            .thenReturn(ValidationResult.valid());

        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));

        //Then
        assertValidationResultValid(validationResult);
    }

    @Test
    void validate_withNotSupportedCombinedServiceIndicator_shouldReturnFormatError() {
        //Given
        when(aspspProfileService.isAisPisSessionsSupported()).thenReturn(false);
        CreateConsentReq createConsentReq = buildCreateConsentReqWithCombinedServiceIndicator(true, AccountAccessType.ALL_ACCOUNTS, null, null);

        when(aspspProfileService.isAvailableAccountsConsentSupported()).thenReturn(true);
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.valid());
        when(supportedAccountReferenceValidator.validate(anyCollection()))
            .thenReturn(ValidationResult.valid());

        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));

        //Then
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(COMBINED_SERVICE_VALIDATION_ERROR);
    }

    @Test
    void validate_bankOfferedConsentNotSupported() {
        //Given
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class))).thenReturn(ValidationResult.valid());
        when(supportedAccountReferenceValidator.validate(anyCollection())).thenReturn(ValidationResult.valid());

        CreateConsentReq createConsentReq = buildCreateConsentReq(null, null, null);

        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));

        //Then
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult).isEqualTo(ValidationResult.invalid(ErrorType.AIS_400, SERVICE_INVALID_400));
    }

    @Test
    void validate_bankOfferedConsentNotSupported_EmbeddedApproach() {
        //Given
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class))).thenReturn(ValidationResult.valid());
        when(supportedAccountReferenceValidator.validate(anyCollection())).thenReturn(ValidationResult.valid());
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);

        CreateConsentReq createConsentReq = buildCreateConsentReq(null, null, null);

        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));

        //Then
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult).isEqualTo(ValidationResult.invalid(ErrorType.AIS_400, SERVICE_INVALID_400));
    }

    @Test
    void validate_globalConsentNotSupported() {
        //Given
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class))).thenReturn(ValidationResult.valid());
        when(supportedAccountReferenceValidator.validate(anyCollection())).thenReturn(ValidationResult.valid());

        CreateConsentReq createConsentReq = buildCreateConsentReq(null, AccountAccessType.ALL_ACCOUNTS, null);

        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));

        //Then
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult).isEqualTo(ValidationResult.invalid(ErrorType.AIS_400, SERVICE_INVALID_400_FOR_GLOBAL_CONSENT));
    }

    @Test
    void validate_globalWithOwnerNameConsentNotSupported() {
        //Given
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class))).thenReturn(ValidationResult.valid());
        when(supportedAccountReferenceValidator.validate(anyCollection())).thenReturn(ValidationResult.valid());

        CreateConsentReq createConsentReq = buildCreateConsentReq(null, AccountAccessType.ALL_ACCOUNTS_WITH_OWNER_NAME, null);

        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));

        //Then
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult).isEqualTo(ValidationResult.invalid(ErrorType.AIS_400, SERVICE_INVALID_400_FOR_GLOBAL_CONSENT));
    }

    @Test
    void validate_AvailableAccountsConsentNotSupported() {
        //Given
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class))).thenReturn(ValidationResult.valid());
        when(supportedAccountReferenceValidator.validate(anyCollection())).thenReturn(ValidationResult.valid());

        CreateConsentReq createConsentReq = buildCreateConsentReq(AccountAccessType.ALL_ACCOUNTS, null, null);

        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));

        //Then
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult).isEqualTo(ValidationResult.invalid(ErrorType.AIS_400, SERVICE_INVALID_400));
    }

    @Test
    void validate_AvailableAccountsWithOwnerNameConsentNotSupported() {
        //Given
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class))).thenReturn(ValidationResult.valid());
        when(supportedAccountReferenceValidator.validate(anyCollection())).thenReturn(ValidationResult.valid());

        CreateConsentReq createConsentReq = buildCreateConsentReq(AccountAccessType.ALL_ACCOUNTS_WITH_OWNER_NAME, null, null);

        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));

        //Then
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult).isEqualTo(ValidationResult.invalid(ErrorType.AIS_400, SERVICE_INVALID_400));
    }

    @Test
    void validate_AvailableAccountsWithBalanceConsentNotSupported() {
        //Given
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class))).thenReturn(ValidationResult.valid());
        when(supportedAccountReferenceValidator.validate(anyCollection())).thenReturn(ValidationResult.valid());

        CreateConsentReq createConsentReq = buildCreateConsentReq(null, null, AccountAccessType.ALL_ACCOUNTS);

        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));

        //Then
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult).isEqualTo(ValidationResult.invalid(ErrorType.AIS_400, SERVICE_INVALID_400));
    }

    @Test
    void validate_AvailableAccountsWithBalanceWithOwnerNameConsentNotSupported() {
        //Given
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class))).thenReturn(ValidationResult.valid());
        when(supportedAccountReferenceValidator.validate(anyCollection())).thenReturn(ValidationResult.valid());

        CreateConsentReq createConsentReq = buildCreateConsentReq(null, null, AccountAccessType.ALL_ACCOUNTS_WITH_OWNER_NAME);

        //When
        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA));

        //Then
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult).isEqualTo(ValidationResult.invalid(ErrorType.AIS_400, SERVICE_INVALID_400));
    }

    @Test
    void buildWarningMessages() {
        //Given
        CreateConsentReq createConsentReq =
            buildCreateConsentReqWithCombinedServiceIndicator(false, AccountAccessType.ALL_ACCOUNTS, null, null);
        CreateConsentRequestObject createConsentRequestObject = new CreateConsentRequestObject(createConsentReq, EMPTY_PSU_DATA);

        //When
        Set<TppMessageInformation> actual =
            createConsentRequestValidator.buildWarningMessages(createConsentRequestObject);

        //Then
        assertThat(actual).isEmpty();
        verifyNoInteractions(aspspProfileService);
        verifyNoInteractions(scaApproachResolver);
        verifyNoInteractions(psuDataInInitialRequestValidator);
        verifyNoInteractions(supportedAccountReferenceValidator);
    }

    private AccountReference buildAccountReference() {
        return new AccountReference(AccountReferenceType.IBAN, "some iban", Currency.getInstance("EUR"));
    }

    private CreateConsentReq buildCreateConsentReqWithAccess(AccountAccess accountAccess, AccountAccessType availableAccounts, AccountAccessType allPsd2, AccountAccessType availableAccountsWithBalance) {
        CreateConsentReq createConsentReq = buildCreateConsentReqWithCombinedServiceIndicator(false, availableAccounts, allPsd2, availableAccountsWithBalance);
        createConsentReq.setAccess(accountAccess);
        return createConsentReq;
    }

    private CreateConsentReq buildCreateConsentReqWithCombinedServiceIndicator(boolean combinedServiceIndicator, AccountAccessType availableAccounts, AccountAccessType allPsd2, AccountAccessType availableAccountsWithBalance) {
        CreateConsentReq createConsentReq = buildCreateConsentReq(true, 2, availableAccounts, allPsd2, availableAccountsWithBalance);
        createConsentReq.setCombinedServiceIndicator(combinedServiceIndicator);
        return createConsentReq;
    }

    private CreateConsentReq buildCreateConsentReq(boolean recurringIndicator, int frequencyPerDay, AccountAccessType availableAccounts, AccountAccessType allPsd2, AccountAccessType availableAccountsWithBalance) {
        return buildCreateConsentReq(recurringIndicator, frequencyPerDay, LocalDate.now().plusDays(1), availableAccounts, allPsd2, availableAccountsWithBalance);
    }

    @NotNull
    private CreateConsentReq buildCreateConsentReq(boolean recurringIndicator, int frequencyPerDay, LocalDate validUntil, AccountAccessType availableAccounts, AccountAccessType allPsd2, AccountAccessType availableAccountsWithBalance) {
        CreateConsentReq createConsentReq = new CreateConsentReq();
        createConsentReq.setValidUntil(validUntil);
        createConsentReq.setRecurringIndicator(recurringIndicator);
        createConsentReq.setFrequencyPerDay(frequencyPerDay);
        AccountAccess accountAccess = new AccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);
        createConsentReq.setAccess(accountAccess);
        createConsentReq.setAvailableAccounts(availableAccounts);
        createConsentReq.setAllPsd2(allPsd2);
        createConsentReq.setAvailableAccountsWithBalance(availableAccountsWithBalance);
        return createConsentReq;
    }

    private CreateConsentReq buildCreateConsentReqWithoutFlagsAndAccesses(boolean recurringIndicator, int frequencyPerDay, AccountAccessType availableAccounts, AccountAccessType allPsd2, AccountAccessType availableAccountsWithBalance) {
        CreateConsentReq createConsentReq = buildCreateConsentReq(recurringIndicator, frequencyPerDay, availableAccounts, allPsd2, availableAccountsWithBalance);
        AccountAccess accountAccess = new AccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);
        createConsentReq.setAccess(accountAccess);
        return createConsentReq;
    }

    private void assertValidationResultValid(ValidationResult validationResult) {
        assertThat(validationResult.isValid()).isTrue();
        assertThat(validationResult.getMessageError()).isNull();
    }

    private void assertValidationResultConsentInvalid(ValidationResult validationResult) {
        assertThat(validationResult.isValid()).isFalse();
        MessageError messageError = validationResult.getMessageError();
        assertNotNull(messageError);
        assertThat(messageError.getTppMessage().getMessageErrorCode()).isEqualTo(CONSENT_INVALID);
    }

    private CreateConsentReq buildCreateConsentReq(AccountAccessType availableAccounts, AccountAccessType allPsd2, AccountAccessType availableAccountsWithBalance) {
        return buildCreateConsentReq(false, 0, availableAccounts, allPsd2, availableAccountsWithBalance);
    }

}

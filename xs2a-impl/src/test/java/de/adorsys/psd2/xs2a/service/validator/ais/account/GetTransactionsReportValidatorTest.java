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

import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountReferenceAccessValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.PermittedAccountReferenceValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.TransactionReportAcceptHeaderValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.TransactionsReportByPeriodObject;
import de.adorsys.psd2.xs2a.service.validator.tpp.AisAccountTppInfoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import java.util.Collections;

import static de.adorsys.psd2.xs2a.core.ais.BookingStatus.PENDING;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetTransactionsReportValidatorTest {
    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");
    private static final String ACCOUNT_ID = "account id";
    private static final String REQUEST_URI = "/accounts";
    private static final boolean WITH_BALANCE = false;
    private static final String ENTRY_REFERENCE_FROM = "";
    private static final Boolean DELTA_LIST = Boolean.FALSE;
    private static final BookingStatus BOOKING_STATUS = BookingStatus.BOOKED;

    private static final MessageError TPP_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(UNAUTHORIZED));

    private static final MessageError PERMITTED_ACCOUNT_REFERENCE_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(CONSENT_INVALID));

    private static final MessageError ENTRY_REFERENCE_FROM_PARAMETER_NOT_SUPPORTED_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(PARAMETER_NOT_SUPPORTED_ENTRY_REFERENCE_FROM));

    private static final MessageError DELTA_LIST_PARAMETER_NOT_SUPPORTED_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(PARAMETER_NOT_SUPPORTED_DELTA_LIST));

    private static final MessageError ONE_DELTA_REPORT_PARAMETER_CAN_BE_PRESENT_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(FORMAT_ERROR_MULTIPLE_DELTA_REPORT));

    private static final MessageError BOOKING_STATUS_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(PARAMETER_NOT_SUPPORTED_BOOKING_STATUS, PENDING.getValue()));

    private static final MessageError REQUESTED_FORMATS_INVALID_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(REQUESTED_FORMATS_INVALID));

    @InjectMocks
    private GetTransactionsReportValidator getTransactionsReportValidator;

    @Mock
    private AccountConsentValidator accountConsentValidator;
    @Mock
    private AisAccountTppInfoValidator aisAccountTppInfoValidator;
    @Mock
    private AccountReferenceAccessValidator accountReferenceAccessValidator;
    @Mock
    private PermittedAccountReferenceValidator permittedAccountReferenceValidator;
    @Mock
    private TransactionReportAcceptHeaderValidator transactionReportAcceptHeaderValidator;

    @Mock
    private AccountReference accountReference;
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;

    @BeforeEach
    void setUp() {
        // Inject pisTppInfoValidator via setter
        getTransactionsReportValidator.setAisAccountTppInfoValidator(aisAccountTppInfoValidator);
    }

    @Test
    void validate_withInvalidAccountReference_shouldReturnInvalid() {
        // Given
        AccountConsent accountConsent = buildAccountConsent(TPP_INFO);
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported()).thenReturn(false);
        when(aspspProfileService.isEntryReferenceFromSupported()).thenReturn(false);

        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_JSON_VALUE)).thenReturn(ValidationResult.valid());
        when(accountReferenceAccessValidator.validate(accountConsent.getAccess(), accountConsent.getAccess().getTransactions(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS)).thenReturn(ValidationResult.valid());
        when(permittedAccountReferenceValidator.validate(accountConsent, ACCOUNT_ID, WITH_BALANCE))
            .thenReturn(ValidationResult.invalid(PERMITTED_ACCOUNT_REFERENCE_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = getTransactionsReportValidator.validate(new TransactionsReportByPeriodObject(accountConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, ENTRY_REFERENCE_FROM, DELTA_LIST, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS));

        // Then
        verify(permittedAccountReferenceValidator).validate(accountConsent, ACCOUNT_ID, WITH_BALANCE);

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(PERMITTED_ACCOUNT_REFERENCE_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withValidConsentObject_shouldReturnValid() {
        // Given
        AccountConsent accountConsent = buildAccountConsent(TPP_INFO);

        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported()).thenReturn(false);
        when(aspspProfileService.isEntryReferenceFromSupported()).thenReturn(false);
        when(aspspProfileService.getAvailableBookingStatuses()).thenReturn(Collections.singletonList(BOOKING_STATUS));
        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_JSON_VALUE)).thenReturn(ValidationResult.valid());
        when(accountReferenceAccessValidator.validate(accountConsent.getAccess(), accountConsent.getAccess().getTransactions(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS)).thenReturn(ValidationResult.valid());
        when(permittedAccountReferenceValidator.validate(accountConsent, ACCOUNT_ID, WITH_BALANCE))
            .thenReturn(ValidationResult.valid());
        when(accountConsentValidator.validate(accountConsent, REQUEST_URI))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = getTransactionsReportValidator.validate(new TransactionsReportByPeriodObject(accountConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, ENTRY_REFERENCE_FROM, DELTA_LIST, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(accountConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withAcceptHeader_shouldReturnInvalid() {
        // Given
        AccountConsent accountConsent = buildAccountConsent(TPP_INFO);

        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());
        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_ATOM_XML_VALUE))
            .thenReturn(ValidationResult.invalid(REQUESTED_FORMATS_INVALID_ERROR));

        // When
        ValidationResult validationResult = getTransactionsReportValidator.validate(
            new TransactionsReportByPeriodObject(accountConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, ENTRY_REFERENCE_FROM, Boolean.TRUE, MediaType.APPLICATION_ATOM_XML_VALUE, BOOKING_STATUS));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(accountConsent.getTppInfo());
        verify(permittedAccountReferenceValidator, never()).validate(accountConsent, ACCOUNT_ID, WITH_BALANCE);

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(REQUESTED_FORMATS_INVALID_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidAccountReferenceAccess_error() {
        // Given
        AccountConsent accountConsent = buildAccountConsent(TPP_INFO);
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported()).thenReturn(false);
        when(aspspProfileService.isEntryReferenceFromSupported()).thenReturn(false);

        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_JSON_VALUE)).thenReturn(ValidationResult.valid());
        when(accountReferenceAccessValidator.validate(accountConsent.getAccess(), accountConsent.getAccess().getTransactions(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS))
            .thenReturn(ValidationResult.invalid(ErrorType.AIS_401, CONSENT_INVALID));

        // When
        ValidationResult validationResult = getTransactionsReportValidator.validate(new TransactionsReportByPeriodObject(accountConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, ENTRY_REFERENCE_FROM, DELTA_LIST, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(accountConsent.getTppInfo());

        assertNotNull(validationResult);
        assertFalse(validationResult.isValid());

        verify(permittedAccountReferenceValidator, never()).validate(any(AccountConsent.class), anyString(), anyBoolean());
        verify(accountConsentValidator, never()).validate(any(AccountConsent.class), anyString());
    }

    @Test
    void validate_withInvalidTppInConsent_shouldReturnTppValidationError() {
        // Given
        AccountConsent accountConsent = buildAccountConsent(INVALID_TPP_INFO);
        when(aisAccountTppInfoValidator.validateTpp(INVALID_TPP_INFO)).thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = getTransactionsReportValidator.validate(new TransactionsReportByPeriodObject(accountConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, ENTRY_REFERENCE_FROM, DELTA_LIST, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(accountConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withDeltaListNoSupported_shouldReturnInvalid() {
        // Given
        AccountConsent accountConsent = buildAccountConsent(TPP_INFO);
        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_JSON_VALUE)).thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported()).thenReturn(false);
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());
        when(aspspProfileService.isEntryReferenceFromSupported()).thenReturn(false);

        // When
        ValidationResult validationResult = getTransactionsReportValidator.validate(new TransactionsReportByPeriodObject(accountConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, ENTRY_REFERENCE_FROM, Boolean.TRUE, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(accountConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(DELTA_LIST_PARAMETER_NOT_SUPPORTED_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withEntryReferenceFromNoSupported_shouldReturnInvalid() {
        // Given
        AccountConsent accountConsent = buildAccountConsent(TPP_INFO);
        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_JSON_VALUE)).thenReturn(ValidationResult.valid());
        when(aspspProfileService.isEntryReferenceFromSupported()).thenReturn(false);
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported()).thenReturn(false);

        // When
        ValidationResult validationResult = getTransactionsReportValidator.validate(new TransactionsReportByPeriodObject(accountConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, "777", DELTA_LIST, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(accountConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(ENTRY_REFERENCE_FROM_PARAMETER_NOT_SUPPORTED_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withDeltaListAndEntryReferenceFromNoSupported_shouldReturnInvalid() {
        // Given
        AccountConsent accountConsent = buildAccountConsent(TPP_INFO);
        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_JSON_VALUE)).thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported()).thenReturn(false);
        when(aspspProfileService.isEntryReferenceFromSupported()).thenReturn(false);
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = getTransactionsReportValidator.validate(new TransactionsReportByPeriodObject(accountConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, "777", Boolean.TRUE, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(accountConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        MessageError messageError = new MessageError(ErrorType.AIS_400,
                                                     ENTRY_REFERENCE_FROM_PARAMETER_NOT_SUPPORTED_ERROR.getTppMessage(),
                                                     DELTA_LIST_PARAMETER_NOT_SUPPORTED_ERROR.getTppMessage());
        assertEquals(messageError, validationResult.getMessageError());
    }

    @Test
    void validate_withOneDeltaReportParameterCanBePresent_shouldReturnInvalid() {
        // Given
        AccountConsent accountConsent = buildAccountConsent(TPP_INFO);
        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_JSON_VALUE)).thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported()).thenReturn(true);
        when(aspspProfileService.isEntryReferenceFromSupported()).thenReturn(true);
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = getTransactionsReportValidator.validate(new TransactionsReportByPeriodObject(accountConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, "777", Boolean.TRUE, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(accountConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(ONE_DELTA_REPORT_PARAMETER_CAN_BE_PRESENT_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withNotSupportedBookingStatus_shouldReturnBookingStatusValidationError() {
        // Given
        AccountConsent accountConsent = buildAccountConsent(TPP_INFO);
        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_JSON_VALUE)).thenReturn(ValidationResult.valid());
        when(accountReferenceAccessValidator.validate(accountConsent.getAccess(), accountConsent.getAccess().getTransactions(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS)).thenReturn(ValidationResult.valid());
        when(permittedAccountReferenceValidator.validate(accountConsent, ACCOUNT_ID, WITH_BALANCE))
            .thenReturn(ValidationResult.valid());
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported()).thenReturn(false);
        when(aspspProfileService.isEntryReferenceFromSupported()).thenReturn(false);
        when(aspspProfileService.getAvailableBookingStatuses()).thenReturn(Collections.singletonList(BOOKING_STATUS));

        // When
        ValidationResult validationResult = getTransactionsReportValidator.validate(new TransactionsReportByPeriodObject(accountConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, ENTRY_REFERENCE_FROM, DELTA_LIST, MediaType.APPLICATION_JSON_VALUE, PENDING));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(accountConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(BOOKING_STATUS_VALIDATION_ERROR, validationResult.getMessageError());
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }

    private AccountConsent buildAccountConsent(TppInfo tppInfo) {
        return new AccountConsent("id", buildXs2aAccountAccess(), buildXs2aAccountAccess(), false, null, null, 0,
                                  null, null, false, false,
                                  Collections.emptyList(), tppInfo, AisConsentRequestType.DEDICATED_ACCOUNTS, false,
                                  Collections.emptyList(), null, Collections.emptyMap());
    }

    private Xs2aAccountAccess buildXs2aAccountAccess() {
        return new Xs2aAccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.singletonList(accountReference), null, null, null);
    }
}

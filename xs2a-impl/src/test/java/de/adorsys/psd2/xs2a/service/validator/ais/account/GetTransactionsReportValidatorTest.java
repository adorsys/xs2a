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
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.OauthConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountReferenceAccessValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.PermittedAccountReferenceValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.TransactionReportAcceptHeaderValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.TransactionsReportByPeriodObject;
import de.adorsys.psd2.xs2a.service.validator.tpp.AisAccountTppInfoValidator;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static de.adorsys.psd2.xs2a.core.ais.BookingStatus.PENDING;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
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
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private OauthConsentValidator oauthConsentValidator;

    private final JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        // Inject pisTppInfoValidator via setter
        getTransactionsReportValidator.setAisAccountTppInfoValidator(aisAccountTppInfoValidator);
    }

    @Test
    void validate_withInvalidAccountReference_shouldReturnInvalid() {
        // Given
        AisConsent aisConsent = buildAccountConsent(TPP_INFO);

        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported())
            .thenReturn(false);
        when(aspspProfileService.isEntryReferenceFromSupported())
            .thenReturn(false);
        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_JSON_VALUE))
            .thenReturn(ValidationResult.valid());
        when(accountReferenceAccessValidator.validate(aisConsent, aisConsent.getAccess().getTransactions(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS))
            .thenReturn(ValidationResult.valid());
        when(permittedAccountReferenceValidator.validate(aisConsent, ACCOUNT_ID, WITH_BALANCE))
            .thenReturn(ValidationResult.invalid(PERMITTED_ACCOUNT_REFERENCE_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = getTransactionsReportValidator.validate(new TransactionsReportByPeriodObject(aisConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, ENTRY_REFERENCE_FROM, DELTA_LIST, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS, LocalDate.now()));

        // Then
        verify(permittedAccountReferenceValidator).validate(aisConsent, ACCOUNT_ID, WITH_BALANCE);

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(PERMITTED_ACCOUNT_REFERENCE_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withValidConsentObject_shouldReturnValid() {
        // Given
        AisConsent aisConsent = buildAccountConsent(TPP_INFO);

        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported())
            .thenReturn(false);
        when(aspspProfileService.isEntryReferenceFromSupported())
            .thenReturn(false);
        when(aspspProfileService.getAvailableBookingStatuses())
            .thenReturn(Collections.singletonList(BOOKING_STATUS));
        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_JSON_VALUE))
            .thenReturn(ValidationResult.valid());
        when(accountReferenceAccessValidator.validate(aisConsent, aisConsent.getAccess().getTransactions(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS))
            .thenReturn(ValidationResult.valid());
        when(permittedAccountReferenceValidator.validate(aisConsent, ACCOUNT_ID, WITH_BALANCE))
            .thenReturn(ValidationResult.valid());
        when(accountConsentValidator.validate(aisConsent, REQUEST_URI))
            .thenReturn(ValidationResult.valid());
        when(oauthConsentValidator.validate(aisConsent))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = getTransactionsReportValidator.validate(new TransactionsReportByPeriodObject(aisConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, ENTRY_REFERENCE_FROM, DELTA_LIST, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS, LocalDate.now()));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withValidConsentObject_oauth_error() {
        // Given
        AisConsent aisConsent = buildAccountConsent(TPP_INFO);

        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported())
            .thenReturn(false);
        when(aspspProfileService.isEntryReferenceFromSupported())
            .thenReturn(false);
        when(aspspProfileService.getAvailableBookingStatuses())
            .thenReturn(Collections.singletonList(BOOKING_STATUS));
        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_JSON_VALUE))
            .thenReturn(ValidationResult.valid());
        when(accountReferenceAccessValidator.validate(aisConsent, aisConsent.getAccess().getTransactions(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS))
            .thenReturn(ValidationResult.valid());
        when(permittedAccountReferenceValidator.validate(aisConsent, ACCOUNT_ID, WITH_BALANCE))
            .thenReturn(ValidationResult.valid());
        when(oauthConsentValidator.validate(aisConsent))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = getTransactionsReportValidator.validate(new TransactionsReportByPeriodObject(aisConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, ENTRY_REFERENCE_FROM, DELTA_LIST, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS, LocalDate.now()));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
    }

    @Test
    void validate_withValidConsentObject_invalidConsent() {
        // Given
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/ais-consent-with-masked-pan-and-consentData.json", AisConsent.class);

        // When
        ValidationResult validationResult = getTransactionsReportValidator.executeBusinessValidation(new TransactionsReportByPeriodObject(aisConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, ENTRY_REFERENCE_FROM, DELTA_LIST, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS, LocalDate.now()));

        // Then
        verifyNoInteractions(permittedAccountReferenceValidator, transactionReportAcceptHeaderValidator, accountReferenceAccessValidator, oauthConsentValidator);

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
    }

    @Test
    void validate_withAcceptHeader_shouldReturnInvalid() {
        // Given
        AisConsent aisConsent = buildAccountConsent(TPP_INFO);

        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_ATOM_XML_VALUE))
            .thenReturn(ValidationResult.invalid(REQUESTED_FORMATS_INVALID_ERROR));

        // When
        ValidationResult validationResult = getTransactionsReportValidator.validate(
            new TransactionsReportByPeriodObject(aisConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, ENTRY_REFERENCE_FROM, Boolean.TRUE, MediaType.APPLICATION_ATOM_XML_VALUE, BOOKING_STATUS, LocalDate.now()));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());
        verify(permittedAccountReferenceValidator, never()).validate(aisConsent, ACCOUNT_ID, WITH_BALANCE);

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(REQUESTED_FORMATS_INVALID_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidAccountReferenceAccess_error() {
        // Given
        AisConsent aisConsent = buildAccountConsent(TPP_INFO);

        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported())
            .thenReturn(false);
        when(aspspProfileService.isEntryReferenceFromSupported())
            .thenReturn(false);
        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_JSON_VALUE))
            .thenReturn(ValidationResult.valid());
        when(accountReferenceAccessValidator.validate(aisConsent, aisConsent.getAccess().getTransactions(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS))
            .thenReturn(ValidationResult.invalid(ErrorType.AIS_401, CONSENT_INVALID));

        // When
        ValidationResult validationResult = getTransactionsReportValidator.validate(new TransactionsReportByPeriodObject(aisConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, ENTRY_REFERENCE_FROM, DELTA_LIST, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS, LocalDate.now()));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertFalse(validationResult.isValid());

        verify(permittedAccountReferenceValidator, never()).validate(any(AisConsent.class), anyString(), anyBoolean());
        verify(accountConsentValidator, never()).validate(any(AisConsent.class), anyString());
    }

    @Test
    void validate_withInvalidTppInConsent_shouldReturnTppValidationError() {
        // Given
        AisConsent aisConsent = buildAccountConsent(INVALID_TPP_INFO);

        when(aisAccountTppInfoValidator.validateTpp(INVALID_TPP_INFO))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = getTransactionsReportValidator.validate(new TransactionsReportByPeriodObject(aisConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, ENTRY_REFERENCE_FROM, DELTA_LIST, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS, LocalDate.now()));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withDeltaListNoSupported_shouldReturnInvalid() {
        // Given
        AisConsent aisConsent = buildAccountConsent(TPP_INFO);
        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_JSON_VALUE))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported())
            .thenReturn(false);
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported())
            .thenReturn(false);
        when(aspspProfileService.isEntryReferenceFromSupported())
            .thenReturn(false);

        // When
        ValidationResult validationResult = getTransactionsReportValidator.validate(new TransactionsReportByPeriodObject(aisConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, ENTRY_REFERENCE_FROM, Boolean.TRUE, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS, null));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(DELTA_LIST_PARAMETER_NOT_SUPPORTED_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withDeltaListNoSupportedDateFromPresent_shouldReturnValid() {
        // Given
        AisConsent aisConsent = buildAccountConsent(TPP_INFO);

        when(accountReferenceAccessValidator.validate(aisConsent, aisConsent.getAccess().getTransactions(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS))
            .thenReturn(ValidationResult.valid());
        when(permittedAccountReferenceValidator.validate(aisConsent, ACCOUNT_ID, WITH_BALANCE))
            .thenReturn(ValidationResult.valid());
        when(oauthConsentValidator.validate(aisConsent))
            .thenReturn(ValidationResult.valid());
        when(accountConsentValidator.validate(aisConsent, REQUEST_URI))
            .thenReturn(ValidationResult.valid());
        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_JSON_VALUE))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported())
            .thenReturn(false);
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.isEntryReferenceFromSupported())
            .thenReturn(false);
        when(aspspProfileService.getAvailableBookingStatuses())
            .thenReturn(Collections.singletonList(BOOKING_STATUS));

        // When
        ValidationResult validationResult = getTransactionsReportValidator.validate(new TransactionsReportByPeriodObject(aisConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, ENTRY_REFERENCE_FROM, Boolean.TRUE, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS, LocalDate.now()));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withEntryReferenceFromNoSupported_shouldReturnInvalid() {
        // Given
        AisConsent aisConsent = buildAccountConsent(TPP_INFO);
        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_JSON_VALUE))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.isEntryReferenceFromSupported())
            .thenReturn(false);
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported())
            .thenReturn(false);

        // When
        ValidationResult validationResult = getTransactionsReportValidator.validate(new TransactionsReportByPeriodObject(aisConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, "777", DELTA_LIST, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS, null));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(ENTRY_REFERENCE_FROM_PARAMETER_NOT_SUPPORTED_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withEntryReferenceFromNoSupportedDateFromPresent_shouldReturnValid() {
        // Given
        AisConsent aisConsent = buildAccountConsent(TPP_INFO);

        when(accountReferenceAccessValidator.validate(aisConsent, aisConsent.getAccess().getTransactions(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS))
            .thenReturn(ValidationResult.valid());
        when(permittedAccountReferenceValidator.validate(aisConsent, ACCOUNT_ID, WITH_BALANCE))
            .thenReturn(ValidationResult.valid());
        when(oauthConsentValidator.validate(aisConsent))
            .thenReturn(ValidationResult.valid());
        when(accountConsentValidator.validate(aisConsent, REQUEST_URI))
            .thenReturn(ValidationResult.valid());
        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_JSON_VALUE))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.isEntryReferenceFromSupported())
            .thenReturn(false);
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported())
            .thenReturn(false);
        when(aspspProfileService.getAvailableBookingStatuses())
            .thenReturn(Collections.singletonList(BOOKING_STATUS));

        // When
        ValidationResult validationResult = getTransactionsReportValidator.validate(new TransactionsReportByPeriodObject(aisConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, "777", DELTA_LIST, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS, LocalDate.now()));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withDeltaListAndEntryReferenceFromNoSupported_shouldReturnInvalid() {
        // Given
        AisConsent aisConsent = buildAccountConsent(TPP_INFO);
        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_JSON_VALUE))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported())
            .thenReturn(false);
        when(aspspProfileService.isEntryReferenceFromSupported())
            .thenReturn(false);
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = getTransactionsReportValidator.validate(new TransactionsReportByPeriodObject(aisConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, "777", Boolean.TRUE, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS, null));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

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
        AisConsent aisConsent = buildAccountConsent(TPP_INFO);
        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_JSON_VALUE))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported())
            .thenReturn(true);
        when(aspspProfileService.isEntryReferenceFromSupported())
            .thenReturn(true);
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = getTransactionsReportValidator.validate(new TransactionsReportByPeriodObject(aisConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, "777", Boolean.TRUE, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS, LocalDate.now()));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(ONE_DELTA_REPORT_PARAMETER_CAN_BE_PRESENT_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withNotSupportedBookingStatus_shouldReturnBookingStatusValidationError() {
        // Given
        AisConsent aisConsent = buildAccountConsent(TPP_INFO);

        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_JSON_VALUE))
            .thenReturn(ValidationResult.valid());
        when(accountReferenceAccessValidator.validate(aisConsent, aisConsent.getAccess().getTransactions(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS))
            .thenReturn(ValidationResult.valid());
        when(permittedAccountReferenceValidator.validate(aisConsent, ACCOUNT_ID, WITH_BALANCE))
            .thenReturn(ValidationResult.valid());
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported())
            .thenReturn(false);
        when(aspspProfileService.isEntryReferenceFromSupported())
            .thenReturn(false);
        when(aspspProfileService.getAvailableBookingStatuses())
            .thenReturn(Collections.singletonList(BOOKING_STATUS));

        // When
        ValidationResult validationResult = getTransactionsReportValidator.validate(new TransactionsReportByPeriodObject(aisConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, ENTRY_REFERENCE_FROM, DELTA_LIST, MediaType.APPLICATION_JSON_VALUE, PENDING, LocalDate.now()));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(BOOKING_STATUS_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void buildWarningMessages() {
        // Given
        AisConsent aisConsent = buildAccountConsent(TPP_INFO);
        TransactionsReportByPeriodObject transactionsReportByPeriodObject =
            new TransactionsReportByPeriodObject(aisConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, ENTRY_REFERENCE_FROM, DELTA_LIST, MediaType.APPLICATION_JSON_VALUE, PENDING, LocalDate.now());

        //When
        Set<TppMessageInformation> actual = getTransactionsReportValidator.buildWarningMessages(transactionsReportByPeriodObject);

        //Then
        assertThat(actual).isEmpty();
        verifyNoInteractions(accountConsentValidator);
        verifyNoInteractions(aisAccountTppInfoValidator);
        verifyNoInteractions(accountReferenceAccessValidator);
        verifyNoInteractions(permittedAccountReferenceValidator);
        verifyNoInteractions(transactionReportAcceptHeaderValidator);
        verifyNoInteractions(aspspProfileService);
        verifyNoInteractions(oauthConsentValidator);
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }

    private AisConsent buildAccountConsent(TppInfo tppInfo) {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/ais-consent-with-iban.json", AisConsent.class);
        ConsentTppInformation consentTppInformation = new ConsentTppInformation();
        consentTppInformation.setTppInfo(tppInfo);
        aisConsent.setConsentTppInformation(consentTppInformation);
        aisConsent.setConsentData(AisConsentData.buildDefaultAisConsentData());

        return aisConsent;
    }
}

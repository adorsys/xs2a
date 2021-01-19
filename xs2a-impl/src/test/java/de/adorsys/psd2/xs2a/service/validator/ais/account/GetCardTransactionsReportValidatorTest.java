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
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.OauthConsentValidator;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountReferenceAccessValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.TransactionReportAcceptHeaderValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.CardTransactionsReportByPeriodObject;
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

import static de.adorsys.psd2.xs2a.core.ais.BookingStatus.PENDING;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCardTransactionsReportValidatorTest {

    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");
    private static final String ACCOUNT_ID = "account id";
    private static final String REQUEST_URI = "/accounts";
    private static final Boolean DELTA_LIST = Boolean.FALSE;
    private static final BookingStatus BOOKING_STATUS = BookingStatus.BOOKED;

    private static final MessageError TPP_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(UNAUTHORIZED));

    private static final MessageError DELTA_LIST_PARAMETER_NOT_SUPPORTED_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(PARAMETER_NOT_SUPPORTED_DELTA_LIST));

    private static final MessageError ONE_DELTA_REPORT_PARAMETER_CAN_BE_PRESENT_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(FORMAT_ERROR_MULTIPLE_DELTA_REPORT));

    private static final MessageError BOOKING_STATUS_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(PARAMETER_NOT_SUPPORTED_BOOKING_STATUS, PENDING.getValue()));

    private static final MessageError REQUESTED_FORMATS_INVALID_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(REQUESTED_FORMATS_INVALID));

    @InjectMocks
    private GetCardTransactionsReportValidator getCardTransactionsReportValidator;

    @Mock
    private AccountConsentValidator accountConsentValidator;
    @Mock
    private AisAccountTppInfoValidator aisAccountTppInfoValidator;
    @Mock
    private AccountReferenceAccessValidator accountReferenceAccessValidator;
    @Mock
    private TransactionReportAcceptHeaderValidator transactionReportAcceptHeaderValidator;
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private OauthConsentValidator oauthConsentValidator;

    private JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        // Inject pisTppInfoValidator via setter
        getCardTransactionsReportValidator.setAisAccountTppInfoValidator(aisAccountTppInfoValidator);
    }

    @Test
    void validate_withValidConsentObject_shouldReturnValid() {
        // Given
        AisConsent aisConsent = buildAisConsent(TPP_INFO);

        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported())
            .thenReturn(false);
        when(aspspProfileService.getAvailableBookingStatuses())
            .thenReturn(Collections.singletonList(BOOKING_STATUS));
        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_JSON_VALUE))
            .thenReturn(ValidationResult.valid());
        when(accountReferenceAccessValidator.validate(aisConsent, aisConsent.getAccess().getTransactions(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS))
            .thenReturn(ValidationResult.valid());
        when(accountConsentValidator.validate(aisConsent, REQUEST_URI))
            .thenReturn(ValidationResult.valid());
        when(oauthConsentValidator.validate(aisConsent))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = getCardTransactionsReportValidator.validate(new CardTransactionsReportByPeriodObject(aisConsent, ACCOUNT_ID, REQUEST_URI, DELTA_LIST, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS, LocalDate.now(), LocalDate.now()));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withAcceptHeader_shouldReturnInvalid() {
        // Given
        AisConsent aisConsent = buildAisConsent(TPP_INFO);

        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_ATOM_XML_VALUE))
            .thenReturn(ValidationResult.invalid(REQUESTED_FORMATS_INVALID_ERROR));

        // When
        ValidationResult validationResult = getCardTransactionsReportValidator.validate(
            new CardTransactionsReportByPeriodObject(aisConsent, ACCOUNT_ID, REQUEST_URI, Boolean.TRUE, MediaType.APPLICATION_ATOM_XML_VALUE, BOOKING_STATUS, LocalDate.now(), LocalDate.now()));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(REQUESTED_FORMATS_INVALID_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidAccountReferenceAccess_error() {
        // Given
        AisConsent aisConsent = buildAisConsent(TPP_INFO);

        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported())
            .thenReturn(false);
        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_JSON_VALUE))
            .thenReturn(ValidationResult.valid());
        when(accountReferenceAccessValidator.validate(aisConsent, aisConsent.getAccess().getTransactions(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS))
            .thenReturn(ValidationResult.invalid(ErrorType.AIS_401, CONSENT_INVALID));

        // When
        ValidationResult validationResult = getCardTransactionsReportValidator.validate(new CardTransactionsReportByPeriodObject(aisConsent, ACCOUNT_ID, REQUEST_URI, DELTA_LIST, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS, LocalDate.now(), LocalDate.now()));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertFalse(validationResult.isValid());

        verify(accountConsentValidator, never()).validate(any(AisConsent.class), anyString());
    }

    @Test
    void validate_withInvalidTppInConsent_shouldReturnTppValidationError() {
        // Given
        AisConsent aisConsent = buildAisConsent(INVALID_TPP_INFO);

        when(aisAccountTppInfoValidator.validateTpp(INVALID_TPP_INFO))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = getCardTransactionsReportValidator.validate(new CardTransactionsReportByPeriodObject(aisConsent, ACCOUNT_ID, REQUEST_URI, DELTA_LIST, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS, LocalDate.now(), LocalDate.now()));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withDeltaListNoSupported_shouldReturnInvalid() {
        // Given
        AisConsent aisConsent = buildAisConsent(TPP_INFO);
        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_JSON_VALUE))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported())
            .thenReturn(false);
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported())
            .thenReturn(false);

        // When
        ValidationResult validationResult = getCardTransactionsReportValidator.validate(new CardTransactionsReportByPeriodObject(aisConsent, ACCOUNT_ID, REQUEST_URI, Boolean.TRUE, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS, null, null));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(DELTA_LIST_PARAMETER_NOT_SUPPORTED_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withDeltaListNoSupportedDateFromPresent_shouldReturnValid() {
        // Given
        AisConsent aisConsent = buildAisConsent(TPP_INFO);
        when(accountReferenceAccessValidator.validate(aisConsent, aisConsent.getAccess().getTransactions(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS))
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
        when(aspspProfileService.getAvailableBookingStatuses())
            .thenReturn(Collections.singletonList(BOOKING_STATUS));

        // When
        ValidationResult validationResult = getCardTransactionsReportValidator.validate(new CardTransactionsReportByPeriodObject(aisConsent, ACCOUNT_ID, REQUEST_URI, Boolean.TRUE, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS, LocalDate.now(), LocalDate.now()));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withEntryReferenceFromNoSupportedDateFromPresent_shouldReturnValid() {
        // Given
        AisConsent aisConsent = buildAisConsent(TPP_INFO);
        when(accountReferenceAccessValidator.validate(aisConsent, aisConsent.getAccess().getTransactions(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS))
            .thenReturn(ValidationResult.valid());
        when(oauthConsentValidator.validate(aisConsent))
            .thenReturn(ValidationResult.valid());
        when(accountConsentValidator.validate(aisConsent, REQUEST_URI))
            .thenReturn(ValidationResult.valid());
        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_JSON_VALUE))
            .thenReturn(ValidationResult.valid());
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported())
            .thenReturn(false);
        when(aspspProfileService.getAvailableBookingStatuses())
            .thenReturn(Collections.singletonList(BOOKING_STATUS));

        // When
        ValidationResult validationResult = getCardTransactionsReportValidator.validate(new CardTransactionsReportByPeriodObject(aisConsent, ACCOUNT_ID, REQUEST_URI, DELTA_LIST, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS, LocalDate.now(), LocalDate.now()));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withOneDeltaReportParameterCanBePresent_shouldReturnInvalid() {
        // Given
        AisConsent aisConsent = buildAisConsent(TPP_INFO);
        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_JSON_VALUE))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported())
            .thenReturn(true);
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = getCardTransactionsReportValidator.validate(new CardTransactionsReportByPeriodObject(aisConsent, ACCOUNT_ID, REQUEST_URI, Boolean.TRUE, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS, LocalDate.now(), LocalDate.now()));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(ONE_DELTA_REPORT_PARAMETER_CAN_BE_PRESENT_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withNotSupportedBookingStatus_shouldReturnBookingStatusValidationError() {
        // Given
        AisConsent aisConsent = buildAisConsent(TPP_INFO);
        when(transactionReportAcceptHeaderValidator.validate(MediaType.APPLICATION_JSON_VALUE))
            .thenReturn(ValidationResult.valid());
        when(accountReferenceAccessValidator.validate(aisConsent, aisConsent.getAccess().getTransactions(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS))
            .thenReturn(ValidationResult.valid());
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.isDeltaListSupported())
            .thenReturn(false);
        when(aspspProfileService.getAvailableBookingStatuses())
            .thenReturn(Collections.singletonList(BOOKING_STATUS));

        // When
        ValidationResult validationResult = getCardTransactionsReportValidator.validate(new CardTransactionsReportByPeriodObject(aisConsent, ACCOUNT_ID, REQUEST_URI, DELTA_LIST, MediaType.APPLICATION_JSON_VALUE, PENDING, LocalDate.now(), LocalDate.now()));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(BOOKING_STATUS_VALIDATION_ERROR, validationResult.getMessageError());
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }

    private AisConsent buildAisConsent(TppInfo tppInfo) {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/ais-consent-with-masked-pan.json", AisConsent.class);
        aisConsent.getConsentTppInformation().setTppInfo(tppInfo);
        aisConsent.setConsentData(AisConsentData.buildDefaultAisConsentData());
        return aisConsent;
    }
}

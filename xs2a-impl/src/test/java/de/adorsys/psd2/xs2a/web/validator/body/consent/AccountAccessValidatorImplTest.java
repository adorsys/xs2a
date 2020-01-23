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

package de.adorsys.psd2.xs2a.web.validator.body.consent;

import com.fasterxml.jackson.core.type.TypeReference;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.Consents;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.web.converter.LocalDateConverter;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.*;
import de.adorsys.psd2.xs2a.web.validator.body.raw.FieldExtractor;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountAccessValidatorImplTest {

    private static final String VALUE_36_LENGHT = "QWERTYUIOPQWERTYUIOPQWERTYUIOPDFGHJK";
    private static final String VALID_UNTIL_FIELD_NAME = "validUntil";
    private static final String CORRECT_FORMAT_DATE = "2021-10-10";
    private static final String WRONG_FORMAT_DATE = "07/01/2019 00:00:00";

    private static final MessageError VALID_UNTIL_DATE_WRONG_VALUE_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_DATE_FIELD, "validUntil", "ISO_DATE", "YYYY-MM-DD"));

    private HttpServletRequest request;
    private AccountAccessValidatorImpl validator;
    private DateFieldValidator dateFieldValidator;
    private Consents consents;
    private MessageError messageError;
    private JsonReader jsonReader;
    private AccountReferenceValidator accountReferenceValidator;
    private FieldExtractor fieldExtractor;
    private CurrencyValidator currencyValidator;

    @Mock
    private Xs2aObjectMapper xs2aObjectMapper;

    @BeforeEach
    void setUp() {
        jsonReader = new JsonReader();
        consents = jsonReader.getObjectFromFile("json/validation/ais/consents.json", Consents.class);
        messageError = new MessageError(ErrorType.AIS_400);
        request = new MockHttpServletRequest();
        ErrorBuildingService errorService = new ErrorBuildingServiceMock(ErrorType.AIS_400);
        fieldExtractor = new FieldExtractor(errorService, xs2aObjectMapper);
        dateFieldValidator = new DateFieldValidator(errorService, new LocalDateConverter(), fieldExtractor);
        currencyValidator = new CurrencyValidator(errorService);
        OptionalFieldMaxLengthValidator stringValidator = new OptionalFieldMaxLengthValidator(new StringMaxLengthValidator(errorService));
        accountReferenceValidator =
            new AccountReferenceValidator(errorService, stringValidator, currencyValidator, new IbanValidator(errorService));

        validator = createValidator(consents);
    }

    @Test
    void validate_success() {
        validator.validate(request, messageError);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_access_null_error() {
        consents.setAccess(null);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR_NULL_VALUE, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    void validate_account_wrongIban_error() {
        // Given
        consents.getAccess().getAccounts().get(0).setIban("123");

        // When
        validator.validate(request, messageError);

        // Then
        assertEquals(MessageErrorCode.FORMAT_ERROR_INVALID_FIELD, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    void validate_account_wrongBban_error() {
        consents.getAccess().getBalances().get(0).setBban("123");

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR_INVALID_FIELD, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    void validate_account_pan_tooLong_error() {
        consents.getAccess().getBalances().get(0).setPan(VALUE_36_LENGHT);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    void validate_account_maskedPan_tooLong_error() {
        consents.getAccess().getAccounts().get(0).setMaskedPan(VALUE_36_LENGHT);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    void validate_account_Msisdn_tooLong_error() {
        consents.getAccess().getTransactions().get(0).setMsisdn(VALUE_36_LENGHT);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    void validate_account_currency_blank_error() {
        consents.getAccess().getBalances().get(0).setCurrency("");

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR_EMPTY_FIELD, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    void validate_account_currency_wrongFormat_error() {
        consents.getAccess().getBalances().get(0).setCurrency("zzz");

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_VALUE, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    void validate_allPsd2_error() {
        consents = jsonReader.getObjectFromFile("json/validation/ais/consents-allPsd2.json", Consents.class);
        validator = createValidator(consents);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR_CONSENT_INCORRECT, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    void validate_availableAccounts_error() {
        consents = jsonReader.getObjectFromFile("json/validation/ais/consents-availableAccounts.json", Consents.class);
        validator = createValidator(consents);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR_CONSENT_INCORRECT, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    void validate_validUntilDateWrongValue_wrongFormat_error() {
        // Given
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq(VALID_UNTIL_FIELD_NAME), any(TypeReference.class))).thenReturn(Optional.of(WRONG_FORMAT_DATE));

        // When
        validator.validate(request, messageError);

        // Then
        assertEquals(VALID_UNTIL_DATE_WRONG_VALUE_ERROR, messageError);
    }

    @Test
    void validate_requestedExecutionDateCorrectValue_success() {
        // Given
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq(VALID_UNTIL_FIELD_NAME), any(TypeReference.class))).thenReturn(Optional.of(CORRECT_FORMAT_DATE));

        // When
        validator.validate(request, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    private AccountAccessValidatorImpl createValidator(Consents consents) {
        return new AccountAccessValidatorImpl(new ErrorBuildingServiceMock(ErrorType.AIS_400), new Xs2aObjectMapper(), accountReferenceValidator, dateFieldValidator) {
            @SuppressWarnings("unchecked")
            @Override
            protected <T> Optional<T> mapBodyToInstance(HttpServletRequest request, MessageError messageError, Class<T> clazz) {
                assertEquals(Consents.class, clazz);
                return (Optional<T>) Optional.of(consents);
            }
        };
    }
}

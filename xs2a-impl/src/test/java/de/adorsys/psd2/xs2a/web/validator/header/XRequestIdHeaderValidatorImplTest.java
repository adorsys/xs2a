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

package de.adorsys.psd2.xs2a.web.validator.header;

import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.web.validator.header.account.TransactionListDownloadHeaderValidator;
import de.adorsys.psd2.xs2a.web.validator.header.account.TransactionListHeaderValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class XRequestIdHeaderValidatorImplTest {

    private static final String X_REQUEST_ID_HEADER = UUID.randomUUID().toString();
    private static final String[] X_REQUEST_ID_HEADER_NAME = {"x-request-id"};

    private XRequestIdHeaderValidatorImpl validator;
    private MessageError messageError;
    private Map<String, String> headers;

    @BeforeEach
    void setUp() {
        validator = new XRequestIdHeaderValidatorImpl(new ErrorBuildingServiceMock(ErrorType.AIS_400));
        messageError = new MessageError();
        headers = new HashMap<>();
    }

    @Test
    void validate_success() {
        headers.put(validator.getHeaderName(), X_REQUEST_ID_HEADER);
        validator.validate(headers, messageError);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_absentHeaderError() {
        validator.validate(headers, messageError);

        assertEquals(MessageErrorCode.FORMAT_ERROR_ABSENT_HEADER, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(X_REQUEST_ID_HEADER_NAME, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void validate_nullHeaderError() {
        headers.put(validator.getHeaderName(), null);
        validator.validate(headers, messageError);

        assertEquals(MessageErrorCode.FORMAT_ERROR_NULL_HEADER, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(X_REQUEST_ID_HEADER_NAME, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void validate_blankHeaderError() {
        headers.put(validator.getHeaderName(), "");
        validator.validate(headers, messageError);

        assertEquals(MessageErrorCode.FORMAT_ERROR_BLANK_HEADER, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(X_REQUEST_ID_HEADER_NAME, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void validate_wrongFormatError() {
        headers.put(validator.getHeaderName(), "wrong_format");
        validator.validate(headers, messageError);

        assertEquals(MessageErrorCode.FORMAT_ERROR_WRONG_HEADER, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    void checkInterfaces() {
        assertTrue(
            Stream.of(ConsentHeaderValidator.class, PaymentHeaderValidator.class, TransactionListHeaderValidator.class, FundsConfirmationHeaderValidator.class,
                      CancelPaymentHeaderValidator.class, TransactionListDownloadHeaderValidator.class, CreateConsentConfirmationOfFundsHeaderValidator.class)
                .allMatch(interfaceClass -> interfaceClass.isInstance(validator))
        );
    }
}

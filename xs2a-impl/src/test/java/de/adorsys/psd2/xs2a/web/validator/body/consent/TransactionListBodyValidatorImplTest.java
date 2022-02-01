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

package de.adorsys.psd2.xs2a.web.validator.body.consent;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.FieldLengthValidator;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

class TransactionListBodyValidatorImplTest {

    private TransactionListBodyValidatorImpl transactionListBodyValidatorImpl;
    private MessageError messageError;
    private MockHttpServletRequest request;

    @BeforeEach
    void init() {
        messageError = new MessageError();
        request = new MockHttpServletRequest();

        ErrorBuildingService errorBuildingService = new ErrorBuildingServiceMock(ErrorType.AIS_400);
        transactionListBodyValidatorImpl = new TransactionListBodyValidatorImpl(errorBuildingService,
                                                                                new Xs2aObjectMapper(),
                                                                                new FieldLengthValidator(errorBuildingService));
    }

    @Test
    void validate_successForAnyBookingStatusWhenJson() {
        request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        for (BookingStatus bookingStatus : BookingStatus.values()) {
            request.setParameter(TransactionListBodyValidatorImpl.BOOKING_STATUS_PARAM, bookingStatus.getValue());

            transactionListBodyValidatorImpl.validate(request, messageError);
            assertTrue(messageError.getTppMessages().isEmpty());
        }
    }

    @Test
    void validate_requestedFormatInvalidError() {
        request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE);
        request.setParameter(TransactionListBodyValidatorImpl.BOOKING_STATUS_PARAM, BookingStatus.INFORMATION.getValue());

        transactionListBodyValidatorImpl.validate(request, messageError);

        assertFalse(messageError.getTppMessages().isEmpty());
        assertEquals(MessageErrorCode.REQUESTED_FORMATS_INVALID, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    void validate_successWithOtherBookingStatuses() {
        request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE);

        for (BookingStatus bookingStatus : BookingStatus.values()) {
            if (BookingStatus.INFORMATION != bookingStatus) {
                request.setParameter(TransactionListBodyValidatorImpl.BOOKING_STATUS_PARAM, bookingStatus.getValue());

                transactionListBodyValidatorImpl.validate(request, messageError);
                assertTrue(messageError.getTppMessages().isEmpty());
            }
        }
    }

    @Test
    void validate_noAcceptHeader() {
        transactionListBodyValidatorImpl.validate(request, messageError);
        assertTrue(messageError.getTppMessages().isEmpty());
    }
}

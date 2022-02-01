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
import de.adorsys.psd2.xs2a.core.profile.PiisConsentSupported;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsentIdHeaderCofValidatorImplTest {
    private static final String[] CONSENT_ID_HEADER_NAME = {"consent-id"};
    private static final String CONSENT_ID = "consent-id";

    private ConsentIdHeaderCofValidatorImpl validator;
    private MessageError messageError;
    private Map<String, String> headers;

    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;

    @BeforeEach
    void setUp() {
        validator = new ConsentIdHeaderCofValidatorImpl(new ErrorBuildingServiceMock(ErrorType.PIIS_400), aspspProfileServiceWrapper);
        messageError = new MessageError();
        headers = new HashMap<>();
    }

    @Test
    void validate_success_tpp() {
        //Given
        when(aspspProfileServiceWrapper.getPiisConsentSupported())
            .thenReturn(PiisConsentSupported.TPP_CONSENT_SUPPORTED);
        headers.put(validator.getHeaderName(), CONSENT_ID);
        //When
        validator.validate(headers, messageError);
        //Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_absentHeaderError_tpp() {
        //Given
        when(aspspProfileServiceWrapper.getPiisConsentSupported())
            .thenReturn(PiisConsentSupported.TPP_CONSENT_SUPPORTED);
        //When
        validator.validate(headers, messageError);
        //Then
        assertEquals(MessageErrorCode.FORMAT_ERROR_ABSENT_HEADER, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(CONSENT_ID_HEADER_NAME, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void validate_nullHeaderError_tpp() {
        //Given
        when(aspspProfileServiceWrapper.getPiisConsentSupported())
            .thenReturn(PiisConsentSupported.TPP_CONSENT_SUPPORTED);
        headers.put(validator.getHeaderName(), null);
        //When
        validator.validate(headers, messageError);
        //Then
        assertEquals(MessageErrorCode.FORMAT_ERROR_NULL_HEADER, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(CONSENT_ID_HEADER_NAME, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void validate_blankHeaderError_tpp() {
        //Given
        when(aspspProfileServiceWrapper.getPiisConsentSupported())
            .thenReturn(PiisConsentSupported.TPP_CONSENT_SUPPORTED);
        headers.put(validator.getHeaderName(), "");
        //When
        validator.validate(headers, messageError);
        //Then
        assertEquals(MessageErrorCode.FORMAT_ERROR_BLANK_HEADER, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(CONSENT_ID_HEADER_NAME, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void validate_success_aspsp() {
        //Given
        when(aspspProfileServiceWrapper.getPiisConsentSupported())
            .thenReturn(PiisConsentSupported.ASPSP_CONSENT_SUPPORTED);
        headers.put(validator.getHeaderName(), CONSENT_ID);
        //When
        validator.validate(headers, messageError);
        //Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_absentHeaderError_aspsp() {
        //Given
        when(aspspProfileServiceWrapper.getPiisConsentSupported())
            .thenReturn(PiisConsentSupported.ASPSP_CONSENT_SUPPORTED);
        //When
        validator.validate(headers, messageError);
        //Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_nullHeaderError_aspsp() {
        //Given
        when(aspspProfileServiceWrapper.getPiisConsentSupported())
            .thenReturn(PiisConsentSupported.ASPSP_CONSENT_SUPPORTED);
        headers.put(validator.getHeaderName(), null);
        //When
        validator.validate(headers, messageError);
        //Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_blankHeaderError_aspsp() {
        //Given
        when(aspspProfileServiceWrapper.getPiisConsentSupported())
            .thenReturn(PiisConsentSupported.ASPSP_CONSENT_SUPPORTED);
        headers.put(validator.getHeaderName(), "");
        //When
        validator.validate(headers, messageError);
        //Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void checkInterfaces() {
        assertTrue(
            Stream.of(FundsConfirmationHeaderValidator.class)
                .allMatch(interfaceClass -> interfaceClass.isInstance(validator))
        );
    }
}

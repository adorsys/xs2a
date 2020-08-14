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
public class ConsentIdHeaderCofValidatorImplTest {
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

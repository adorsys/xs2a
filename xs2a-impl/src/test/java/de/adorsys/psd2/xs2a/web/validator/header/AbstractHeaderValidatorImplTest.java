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

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_BOOLEAN_VALUE;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractHeaderValidatorImplTest {

    @Mock
    private ErrorBuildingService errorBuildingService;
    private TppRedirectPreferredHeaderValidatorImpl validator;
    private MessageError messageError;
    private Map<String, String> headers;

    @BeforeEach
    void setUp() {
        validator = new TppRedirectPreferredHeaderValidatorImpl(errorBuildingService);
        messageError = new MessageError();
        headers = new HashMap<>();
    }

    @Test
    void checkBooleanFormat_headerIsNotPresented() {
        validator.checkBooleanFormat(headers, messageError);

        verify(errorBuildingService, never()).enrichMessageError(any(MessageError.class), any(TppMessageInformation.class));
    }

    @Test
    void checkBooleanFormat_success() {
        headers.put(validator.getHeaderName(), "true");
        validator.checkBooleanFormat(headers, messageError);

        verify(errorBuildingService, never()).enrichMessageError(any(MessageError.class), any(TppMessageInformation.class));
        reset(errorBuildingService);

        validator = new TppRedirectPreferredHeaderValidatorImpl(errorBuildingService);
        headers.put(validator.getHeaderName(), "FaLsE");
        validator.checkBooleanFormat(headers, messageError);

        verify(errorBuildingService, never()).enrichMessageError(any(MessageError.class), any(TppMessageInformation.class));
        reset(errorBuildingService);
    }

    @Test
    void checkBooleanFormat_error() {
        headers.put(validator.getHeaderName(), "wrong_format");
        validator.checkBooleanFormat(headers, messageError);

        verify(errorBuildingService, times(1)).enrichMessageError(messageError,
                                                                  TppMessageInformation.of(FORMAT_ERROR_BOOLEAN_VALUE, validator.getHeaderName()));
    }
}

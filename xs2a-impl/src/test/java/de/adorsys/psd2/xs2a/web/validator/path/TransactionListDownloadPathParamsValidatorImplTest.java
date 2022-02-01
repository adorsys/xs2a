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

package de.adorsys.psd2.xs2a.web.validator.path;

import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_PATH_PARAMETER_INVALID;
import static org.junit.jupiter.api.Assertions.*;

class TransactionListDownloadPathParamsValidatorImplTest {

    private static final String CORRECT_BASE64_STRING = "dGVzdA==";
    private static final String WRONG_BASE64_STRING = "wrong base64";
    private static final String DOWNLOAD_ID = "download-id";

    private TransactionListDownloadPathParamsValidatorImpl transactionListDownloadPathParamsValidator;
    private MessageError messageError;

    @BeforeEach
    void init() {
        messageError = new MessageError();
        ErrorBuildingService errorBuildingService = new ErrorBuildingServiceMock(ErrorType.AIS_400);
        transactionListDownloadPathParamsValidator = new TransactionListDownloadPathParamsValidatorImpl(errorBuildingService);
    }

    @Test
    void validate_success() {
        // Given
        Map<String, String> pathParametersMap = new HashMap<>();
        pathParametersMap.put(DOWNLOAD_ID, CORRECT_BASE64_STRING);

        // When
        transactionListDownloadPathParamsValidator.validate(pathParametersMap, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_wrongBase64_shouldFail() {
        // Given
        Map<String, String> pathParametersMap = new HashMap<>();
        pathParametersMap.put(DOWNLOAD_ID, WRONG_BASE64_STRING);

        // When
        transactionListDownloadPathParamsValidator.validate(pathParametersMap, messageError);

        // Then
        assertFalse(messageError.getTppMessages().isEmpty());
        assertEquals(FORMAT_ERROR_PATH_PARAMETER_INVALID, messageError.getTppMessage().getMessageErrorCode());
    }
}

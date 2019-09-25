/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.validator.path;

import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_PATH_PARAMETER_INVALID;
import static org.junit.Assert.*;

public class TransactionListDownloadPathParamsValidatorImplTest {

    private static final String CORRECT_BASE64_STRING = "dGVzdA==";
    private static final String WRONG_BASE64_STRING = "wrong base64";
    private static final String DOWNLOAD_ID = "download-id";

    private TransactionListDownloadPathParamsValidatorImpl transactionListDownloadPathParamsValidator;
    private MessageError messageError;

    @Before
    public void init() {
        messageError = new MessageError();
        ErrorBuildingService errorBuildingService = new ErrorBuildingServiceMock(ErrorType.AIS_400);
        transactionListDownloadPathParamsValidator = new TransactionListDownloadPathParamsValidatorImpl(errorBuildingService);
    }

    @Test
    public void validate_success() {
        // Given
        Map<String, String> pathParametersMap = new HashMap<>();
        pathParametersMap.put(DOWNLOAD_ID, CORRECT_BASE64_STRING);

        // When
        transactionListDownloadPathParamsValidator.validate(pathParametersMap, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    public void validate_wrongBase64_shouldFail() {
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

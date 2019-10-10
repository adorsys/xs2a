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

package de.adorsys.psd2.xs2a.component.logger.request;

import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RequestResponseLoggerTest {
    private static final String LOG_MESSAGE_JSON_PATH = "json/component/logger/request/log-message.json";
    private static final String MESSAGE = "some log message";

    @Mock
    private Logger logger;
    private JsonReader jsonReader = new JsonReader();

    @Test
    public void log_shouldWriteMessageToLogger() {
        // Given
        RequestResponseLogger requestResponseLogger = new MockRequestResponseLogger(logger);
        RequestResponseLogMessage logMessage = jsonReader.getObjectFromFile(LOG_MESSAGE_JSON_PATH, RequestResponseLogMessage.class);

        // When
        requestResponseLogger.logMessage(logMessage);

        // Then
        verify(logger).info(MESSAGE);
    }
}

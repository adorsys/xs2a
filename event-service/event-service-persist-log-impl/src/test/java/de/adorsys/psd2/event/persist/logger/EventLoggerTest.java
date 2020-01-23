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

package de.adorsys.psd2.event.persist.logger;

import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventLoggerTest {
    private static final String LOG_MESSAGE_JSON_PATH = "json/logger/log-message.json";
    private static final String MESSAGE = "some log message";

    @Mock
    private Logger logger;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void logMessage_shouldWriteMessageToLogger() {
        // Given
        EventLogger eventLogger = new MockEventLogger(logger);
        EventLogMessage logMessage = jsonReader.getObjectFromFile(LOG_MESSAGE_JSON_PATH, EventLogMessage.class);

        // When
        eventLogger.logMessage(logMessage);

        // Then
        verify(logger).info(MESSAGE);
    }
}

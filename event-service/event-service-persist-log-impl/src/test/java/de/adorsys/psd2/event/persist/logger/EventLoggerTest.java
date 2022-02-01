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

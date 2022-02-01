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

package de.adorsys.psd2.event.persist;

import de.adorsys.psd2.event.persist.logger.EventLogMessage;
import de.adorsys.psd2.event.persist.logger.EventLogger;
import de.adorsys.psd2.event.persist.model.EventPO;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LogEventRepositoryImplTest {
    @Mock
    private EventLogger eventLogger;

    @InjectMocks
    private LogEventRepositoryImpl logEventRepositoryImpl;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void save_shouldLogEvent() {
        // Given
        EventPO eventPO = jsonReader.getObjectFromFile("json/logger/event.json", EventPO.class);
        EventLogMessage logMessage = EventLogMessage.builder(eventPO)
                                         .withTimestamp()
                                         .withEventOrigin()
                                         .withEventType()
                                         .withInternalRequestId()
                                         .withXRequestId()
                                         .withConsentId()
                                         .withPaymentId()
                                         .withTppAuthorisationNumber()
                                         .withPsuData()
                                         .withPayload()
                                         .build();

        // When
        Long savedEventId = logEventRepositoryImpl.save(eventPO);

        // Then
        assertNotNull(savedEventId);
        verify(eventLogger).logMessage(logMessage);
    }
}

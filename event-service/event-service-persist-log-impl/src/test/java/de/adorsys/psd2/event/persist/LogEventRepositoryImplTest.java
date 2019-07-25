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

package de.adorsys.psd2.event.persist;

import de.adorsys.psd2.event.persist.logger.EventLogMessage;
import de.adorsys.psd2.event.persist.logger.EventLogger;
import de.adorsys.psd2.event.persist.model.EventPO;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LogEventRepositoryImplTest {
    @Mock
    private EventLogger eventLogger;

    @InjectMocks
    private LogEventRepositoryImpl logEventRepositoryImpl;

    private JsonReader jsonReader = new JsonReader();

    @Test
    public void save_shouldLogEvent() {
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

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link EventRepository} that logs events via {@link EventLogger} instead of saving them
 */
@Service
@RequiredArgsConstructor
public class LogEventRepositoryImpl implements EventRepository {
    private final EventLogger eventLogger;

    @Override
    public Long save(EventPO eventPO) {
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

        eventLogger.logMessage(logMessage);

        return 0L;
    }
}

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

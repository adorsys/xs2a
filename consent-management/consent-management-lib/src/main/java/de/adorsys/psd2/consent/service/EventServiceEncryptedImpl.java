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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.event.service.Xs2aEventService;
import de.adorsys.psd2.event.service.Xs2aEventServiceEncrypted;
import de.adorsys.psd2.event.service.model.EventBO;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventServiceEncryptedImpl implements Xs2aEventServiceEncrypted {
    private final SecurityDataService securityDataService;
    private final Xs2aEventService eventService;

    @Override
    @Transactional
    public boolean recordEvent(@NotNull EventBO event) {
        String decryptedConsentId = decryptId(event.getConsentId());
        String decryptedPaymentId = decryptId(event.getPaymentId());

        EventBO decryptedEvent = EventBO.builder()
                                     .timestamp(event.getTimestamp())
                                     .consentId(decryptedConsentId)
                                     .paymentId(decryptedPaymentId)
                                     .payload(event.getPayload())
                                     .eventOrigin(event.getEventOrigin())
                                     .eventType(event.getEventType())
                                     .psuIdData(event.getPsuIdData())
                                     .tppAuthorisationNumber(event.getTppAuthorisationNumber())
                                     .xRequestId(event.getXRequestId())
                                     .internalRequestId(event.getInternalRequestId())
                                     .instanceId(event.getInstanceId())
                                     .build();
        return eventService.recordEvent(decryptedEvent);
    }

    private String decryptId(String id) {
        return Optional.ofNullable(id)
                   .flatMap(securityDataService::decryptId)
                   .orElse(null);
    }
}

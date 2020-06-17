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

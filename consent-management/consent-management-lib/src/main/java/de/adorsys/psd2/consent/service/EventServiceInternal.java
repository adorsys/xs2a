/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

import de.adorsys.psd2.consent.api.service.EventService;
import de.adorsys.psd2.consent.domain.event.EventEntity;
import de.adorsys.psd2.consent.repository.EventRepository;
import de.adorsys.psd2.consent.service.mapper.EventMapper;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.event.Event;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventServiceInternal implements EventService {
    private final EventMapper eventMapper;
    private final EventRepository eventRepository;
    private final SecurityDataService securityDataService;

    @Override
    @Transactional
    public boolean recordEvent(@NotNull Event event) {
        String consentId = decryptId(event.getConsentId());
        event.setConsentId(consentId);

        String paymentId = decryptId(event.getPaymentId());
        event.setPaymentId(paymentId);

        EventEntity eventEntity = eventMapper.mapToEventEntity(event);
        EventEntity savedEventEntity = eventRepository.save(eventEntity);

        return savedEventEntity.getId() != null;
    }

    private String decryptId(String encryptedId) {
        if (encryptedId == null) {
            return null;
        }

        return securityDataService.decryptId(encryptedId)
                   .orElse(null);
    }
}

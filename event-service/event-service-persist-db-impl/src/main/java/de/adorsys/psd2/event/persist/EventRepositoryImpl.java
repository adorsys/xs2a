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

import de.adorsys.psd2.event.persist.entity.EventEntity;
import de.adorsys.psd2.event.persist.jpa.EventJPARepository;
import de.adorsys.psd2.event.persist.mapper.EventDBMapper;
import de.adorsys.psd2.event.persist.model.EventPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventRepositoryImpl implements EventRepository {
    private final EventJPARepository eventRepository;
    private final EventDBMapper eventDBMapper;

    @Override
    @Transactional
    public Long save(EventPO eventPO) {
        EventEntity entity = eventDBMapper.toEventEntity(eventPO);
        eventRepository.save(entity);
        return entity.getId();
    }
}

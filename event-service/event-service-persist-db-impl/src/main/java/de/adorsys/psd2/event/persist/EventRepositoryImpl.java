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

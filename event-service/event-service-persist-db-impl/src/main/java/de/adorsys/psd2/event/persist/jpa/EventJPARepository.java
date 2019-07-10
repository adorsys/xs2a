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

package de.adorsys.psd2.event.persist.jpa;

import de.adorsys.psd2.event.persist.entity.EventEntity;
import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.core.model.EventType;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface EventJPARepository extends CrudRepository<EventEntity, Long>, JpaSpecificationExecutor<EventEntity> {

    List<EventEntity> findByTimestampBetweenAndInstanceIdOrderByTimestampAsc(OffsetDateTime from, OffsetDateTime to, String instanceId);

    List<EventEntity> findByTimestampBetweenAndConsentIdAndInstanceIdOrderByTimestampAsc(OffsetDateTime from, OffsetDateTime to, String consentId, String instanceId);

    List<EventEntity> findByTimestampBetweenAndPaymentIdAndInstanceIdOrderByTimestampAsc(OffsetDateTime from, OffsetDateTime to, String paymentId, String instanceId);

    List<EventEntity> findByTimestampBetweenAndEventTypeAndInstanceIdOrderByTimestampAsc(OffsetDateTime from, OffsetDateTime to, EventType eventType, String instanceId);

    List<EventEntity> findByTimestampBetweenAndEventOriginAndInstanceIdOrderByTimestampAsc(OffsetDateTime from, OffsetDateTime to, EventOrigin eventOrigin, String instanceId);
}

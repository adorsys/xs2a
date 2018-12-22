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

package de.adorsys.psd2.consent.repository.specification;

import de.adorsys.psd2.consent.domain.event.EventEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

import static de.adorsys.psd2.consent.repository.specification.EntityAttributeSpecificationProvider.provideSpecificationForEntityAttribute;

@Service
public class EventEntitySpecification {
    private static final String INSTANCE_ID_ATTRIBUTE = "instanceId";
    private static final String CONSENT_ID_ATTRIBUTE = "consentId";
    private static final String PAYMENT_ID_ATTRIBUTE = "paymentId";
    private static final String TIMESTAMP_ID_ATTRIBUTE = "timestamp";

    public Specification<EventEntity> byPeriodAndInstanceId(OffsetDateTime start, OffsetDateTime end, String instanceId) {
        return Specifications.where(eventPeriodSpecification(start, end))
                   .and(provideSpecificationForEntityAttribute(INSTANCE_ID_ATTRIBUTE, instanceId));
    }

    public Specification<EventEntity> byPeriodAndConsentIdAndInstanceId(OffsetDateTime start, OffsetDateTime end, String consentId, String instanceId) {
        return Specifications.where(eventPeriodSpecification(start, end))
                   .and(provideSpecificationForEntityAttribute(INSTANCE_ID_ATTRIBUTE, instanceId))
                   .and(provideSpecificationForEntityAttribute(CONSENT_ID_ATTRIBUTE, consentId));
    }

    public Specification<EventEntity> byPeriodAndPaymentIdAndInstanceId(OffsetDateTime start, OffsetDateTime end, String paymentId, String instanceId) {
        return Specifications.where(eventPeriodSpecification(start, end))
                   .and(provideSpecificationForEntityAttribute(INSTANCE_ID_ATTRIBUTE, instanceId))
                   .and(provideSpecificationForEntityAttribute(PAYMENT_ID_ATTRIBUTE, paymentId));
    }

    private Specification<EventEntity> eventPeriodSpecification(OffsetDateTime start, OffsetDateTime end) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.orderBy(criteriaBuilder.asc(root.get(TIMESTAMP_ID_ATTRIBUTE)));
            return criteriaBuilder.between(root.get(TIMESTAMP_ID_ATTRIBUTE), start, end);
        };
    }
}

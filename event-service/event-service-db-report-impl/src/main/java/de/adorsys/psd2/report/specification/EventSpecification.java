/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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

package de.adorsys.psd2.report.specification;

import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.report.entity.EventReportEntity;
import de.adorsys.psd2.report.entity.EventConsentEntity;
import de.adorsys.psd2.report.entity.EventPaymentEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class EventSpecification {
    private static final String TIMESTAMP_ATTRIBUTE = "timestamp";
    private static final String INSTANCE_ID_ATTRIBUTE = "instanceId";
    private static final String CONSENT_ID_ATTRIBUTE = "externalId";
    private static final String PAYMENT_ID_ATTRIBUTE = "paymentId";
    private static final String EVENT_TYPE_ATTRIBUTE = "eventType";
    private static final String EVENT_ORIGIN_ATTRIBUTE = "eventOrigin";
    private static final String CONSENT_ATTRIBUTE = "consent";
    private static final String PAYMENT_ATTRIBUTE = "payment";

    public Specification<EventReportEntity> byPeriodAndInstanceId(OffsetDateTime start, OffsetDateTime end, String instanceId) {
        return Optional.of(byPeriod(start, end))
                   .map(s -> s.and(byInstanceId(instanceId)))
                   .orElse(null);
    }

    private Specification<EventReportEntity> byPeriod(@Nullable OffsetDateTime start, @Nullable OffsetDateTime end) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.orderBy(criteriaBuilder.asc(root.get(TIMESTAMP_ATTRIBUTE)));

            List<Predicate> predicates = new ArrayList<>();
            if (start != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(TIMESTAMP_ATTRIBUTE), start));
            }

            if (end != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(TIMESTAMP_ATTRIBUTE), end));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    protected Specification<EventReportEntity> byInstanceId(@Nullable String instanceId) {
        return EventEntityAttributeSpecificationProvider.provideSpecificationForEntityAttribute(INSTANCE_ID_ATTRIBUTE, instanceId);
    }

    public Specification<EventReportEntity> byPeriodAndInstanceIdAndConsentId(OffsetDateTime start, OffsetDateTime end, String instanceId, String consentId) {
        return Optional.of(byPeriod(start, end))
                   .map(s -> s.and(byInstanceId(instanceId)))
                   .map(s -> s.and(byConsentId(consentId)))
                   .orElse(null);
    }

    private Specification<EventReportEntity> byConsentId(@Nullable String consentId) {
        if (consentId == null) {
            return null;
        }

        return (root, query, cb) -> {
            Join<EventReportEntity, EventConsentEntity> consentEntityJoin = root.join(CONSENT_ATTRIBUTE);
            return Optional.of(Specification.where(EventEntityAttributeSpecificationProvider.provideSpecificationForJoinedEntityAttribute(consentEntityJoin, CONSENT_ID_ATTRIBUTE, consentId)))
                       .map(s -> s.toPredicate(root, query, cb))
                       .orElse(null);
        };
    }

    public Specification<EventReportEntity> byPeriodAndInstanceIdAndPaymentId(OffsetDateTime start, OffsetDateTime end, String instanceId, String paymentId) {
        return Optional.of(byPeriod(start, end))
                   .map(s -> s.and(byInstanceId(instanceId)))
                   .map(s -> s.and(byPaymentId(paymentId)))
                   .orElse(null);
    }

    private Specification<EventReportEntity> byPaymentId(@Nullable String paymentId) {
        if (paymentId == null) {
            return null;
        }

        return (root, query, cb) -> {
            Join<EventReportEntity, EventPaymentEntity> paymentEntityJoin = root.join(PAYMENT_ATTRIBUTE);
            return Optional.of(Specification.where(EventEntityAttributeSpecificationProvider.provideSpecificationForJoinedEntityAttribute(paymentEntityJoin, PAYMENT_ID_ATTRIBUTE, paymentId)))
                       .map(s -> s.toPredicate(root, query, cb))
                       .orElse(null);
        };
    }

    public Specification<EventReportEntity> byPeriodAndInstanceIdAndEventType(OffsetDateTime start, OffsetDateTime end, String instanceId, EventType eventType) {
        return Optional.of(byPeriod(start, end))
                   .map(s -> s.and(byInstanceId(instanceId)))
                   .map(s -> s.and(byEventType(eventType)))
                   .orElse(null);
    }

    private Specification<EventReportEntity> byEventType(@NotNull EventType eventType) {
        return EventEntityAttributeSpecificationProvider.provideSpecificationForEntityObjectAttribute(EVENT_TYPE_ATTRIBUTE, eventType);
    }

    public Specification<EventReportEntity> byPeriodAndInstanceIdAndEventOrigin(OffsetDateTime start, OffsetDateTime end, String instanceId, EventOrigin eventOrigin) {
        return Optional.of(byPeriod(start, end))
                   .map(s -> s.and(byInstanceId(instanceId)))
                   .map(s -> s.and(byEventOrigin(eventOrigin)))
                   .orElse(null);
    }

    private Specification<EventReportEntity> byEventOrigin(@NotNull EventOrigin eventOrigin) {
        return EventEntityAttributeSpecificationProvider.provideSpecificationForEntityObjectAttribute(EVENT_ORIGIN_ATTRIBUTE, eventOrigin);
    }
}

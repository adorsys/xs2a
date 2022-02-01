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

package de.adorsys.psd2.report.specification;

import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.report.entity.EventConsentEntity;
import de.adorsys.psd2.report.entity.EventPaymentEntity;
import de.adorsys.psd2.report.entity.EventReportEntity;
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

/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.consent.repository.specification.EntityAttribute.*;
import static de.adorsys.psd2.consent.repository.specification.EntityAttributeSpecificationProvider.provideSpecificationForEntityAttribute;
import static de.adorsys.psd2.consent.repository.specification.EntityAttributeSpecificationProvider.provideSpecificationForJoinedEntityAttribute;

@Service
public class CommonSpecification<T> {
    /**
     * Returns specification for some entity for filtering data by PSU ID Data, creation date and instance ID.
     *
     * @param psuIdData      mandatory PSU ID data
     * @param createDateFrom optional creation date that limits resulting data to entities created after this date(inclusive)
     * @param createDateTo   optional creation date that limits resulting data to entities created before this date(inclusive)
     * @param instanceId     optional instance ID
     * @return resulting specification
     */
    public Specification<T> byPsuIdDataAndCreationPeriodAndInstanceId(@NotNull PsuIdData psuIdData,
                                                                      @Nullable LocalDate createDateFrom,
                                                                      @Nullable LocalDate createDateTo,
                                                                      @Nullable String instanceId) {
        return Optional.of(Specification.where(byPsuIdDataInList(psuIdData)))
                   .map(s -> s.and(byCreationTimestamp(createDateFrom, createDateTo)))
                   .map(s -> s.and(byInstanceId(instanceId)))
                   .orElse(null);
    }

    /**
     * Returns specification for some entity for filtering data by PSU ID data from list.
     * <p>
     * If all fields in the given PsuIdData are null, this specification will not affect resulting data.
     *
     * @param psuIdData optional PSU ID data
     * @return resulting specification, or <code>null</code> if PSU ID data was omitted
     */
    protected Specification<T> byPsuIdDataInList(@Nullable PsuIdData psuIdData) {
        return byPsuIdData(psuIdData);
    }

    private Specification<T> byPsuIdData(@Nullable PsuIdData psuIdData) {
        if (psuIdData == null) {
            return null;
        }

        return (root, query, cb) -> {
            Join<T, PsuData> psuDataJoin = root.join(PSU_DATA_LIST_ATTRIBUTE);
            return Optional.of(Specification.where(provideSpecificationForJoinedEntityAttribute(psuDataJoin, PSU_ID_ATTRIBUTE, psuIdData.getPsuId())))
                       .map(s -> s.and(provideSpecificationForJoinedEntityAttribute(psuDataJoin, PSU_ID_TYPE_ATTRIBUTE, psuIdData.getPsuIdType())))
                       .map(s -> s.and(provideSpecificationForJoinedEntityAttribute(psuDataJoin, PSU_CORPORATE_ID_ATTRIBUTE, psuIdData.getPsuCorporateId())))
                       .map(s -> s.and(provideSpecificationForJoinedEntityAttribute(psuDataJoin, PSU_CORPORATE_ID_TYPE_ATTRIBUTE, psuIdData.getPsuCorporateIdType())))
                       .map(s -> s.toPredicate(root, query, cb))
                       .orElse(null);
        };
    }

    /**
     * Returns specification for some entity for filtering data by instance id.
     *
     * @param instanceId optional ID of particular service instance
     * @return resulting specification, or <code>null</code> if instance id was omitted
     */
    protected Specification<T> byInstanceId(@Nullable String instanceId) {
        return provideSpecificationForEntityAttribute(INSTANCE_ID_ATTRIBUTE, instanceId);
    }

    /**
     * Returns specification for some entity for filtering data by creation date.
     *
     * <p>
     * If both optional parameters are not provided, this specification will not affect resulting data.
     *
     * @param start optional creation date that limits resulting data to objects created after this date(inclusive)
     * @param end   optional creation date that limits resulting data to objects created before this date(inclusive)
     * @return resulting specification
     */
    protected Specification<T> byCreationTimestamp(@Nullable LocalDate start, @Nullable LocalDate end) {
        ZoneOffset currentOffset = OffsetDateTime.now().getOffset();
        OffsetDateTime startOffsetDateTime = Optional.ofNullable(start)
                                                 .map(odt -> OffsetDateTime.of(odt, LocalTime.MIN, currentOffset))
                                                 .orElse(null);
        OffsetDateTime endOffsetDateTime = Optional.ofNullable(end)
                                               .map(odt -> OffsetDateTime.of(odt, LocalTime.MAX, currentOffset))
                                               .orElse(null);

        return byCreationTimestamp(startOffsetDateTime, endOffsetDateTime);
    }

    /**
     * Returns specification for some entity for filtering data by creation date-time.
     *
     * <p>
     * If both optional parameters are not provided, this specification will not affect resulting data.
     *
     * @param start optional creation date-time that limits resulting data to objects created after this date-time(inclusive)
     * @param end   optional creation date-time that limits resulting data to objects created before this date-time(inclusive)
     * @return resulting specification
     */
    private Specification<T> byCreationTimestamp(@Nullable OffsetDateTime start, @Nullable OffsetDateTime end) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.orderBy(criteriaBuilder.asc(root.get(CREATION_TIMESTAMP_ATTRIBUTE)));

            List<Predicate> predicates = new ArrayList<>();
            if (start != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(CREATION_TIMESTAMP_ATTRIBUTE), start));
            }

            if (end != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(CREATION_TIMESTAMP_ATTRIBUTE), end));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

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

import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

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

public abstract class GenericSpecification {

    /**
     * Returns specification for some entity for filtering data by PSU ID data and instance id.
     *
     * @param psuId      PSU ID data
     * @param instanceId ID of particular service instance
     * @param <T>        type of the entity, for which this specification will be created
     * @return resulting specification
     */
    public <T> Specification<T> byPsuIdIdAndInstanceId(String psuId, String instanceId) {
        Specification<T> aisConsentSpecification = (root, query, cb) -> {
            Join<T, PsuData> aisConsentPsuDataJoin = root.join(PSU_DATA_ATTRIBUTE);
            return cb.equal(aisConsentPsuDataJoin.get(PSU_ID_ATTRIBUTE), psuId);
        };
        return Specifications.where(aisConsentSpecification)
                   .and(provideSpecificationForEntityAttribute(INSTANCE_ID_ATTRIBUTE, instanceId));
    }

    /**
     * Returns specification for some entity for filtering data by PSU ID data.<p>
     * <p>
     * If all fields in the given PsuIdData are null, this specification will not affect resulting data.
     *
     * @param psuIdData optional PSU ID data
     * @param <T>       type of the entity, for which this specification will be created
     * @return resulting specification, or <code>null</code> if PSU ID data was omitted
     */
    protected <T> Specification<T> byPsuIdData(@Nullable PsuIdData psuIdData) {
        if (psuIdData == null) {
            return null;
        }

        return (root, query, cb) -> {
            Join<T, PsuData> psuDataJoin = root.join(PSU_DATA_ATTRIBUTE);
            return Specifications.where(provideSpecificationForJoinedEntityAttribute(psuDataJoin, PSU_ID_ATTRIBUTE, psuIdData.getPsuId()))
                       .and(provideSpecificationForJoinedEntityAttribute(psuDataJoin, PSU_ID_TYPE_ATTRIBUTE, psuIdData.getPsuIdType()))
                       .and(provideSpecificationForJoinedEntityAttribute(psuDataJoin, PSU_CORPORATE_ID_ATTRIBUTE, psuIdData.getPsuCorporateId()))
                       .and(provideSpecificationForJoinedEntityAttribute(psuDataJoin, PSU_CORPORATE_ID_TYPE_ATTRIBUTE, psuIdData.getPsuCorporateIdType()))
                       .toPredicate(root, query, cb);
        };
    }

    /**
     * Returns specification for some entity for filtering data by TPP authorisation number.
     *
     * <p>
     * If optional parameter is not provided, this specification will not affect resulting data.
     *
     * @param tppAuthorisationNumber optional TPP authorisation number
     * @param <T>                    type of the entity, for which this specification will be created
     * @return resulting specification
     */
    protected <T> Specification<T> byTppAuthorisationNumber(@Nullable String tppAuthorisationNumber) {
        return (root, query, cb) -> {
            Join<T, TppInfoEntity> tppInfoJoin = root.join(TPP_INFO_ATTRIBUTE);
            return provideSpecificationForJoinedEntityAttribute(tppInfoJoin, TPP_INFO_AUTHORISATION_NUMBER_ATTRIBUTE, tppAuthorisationNumber)
                       .toPredicate(root, query, cb);
        };
    }

    /**
     * Returns specification for some entity for filtering data by aspsp account id in aspsp account access list
     *
     * <p>
     * If optional parameter is not provided, this specification will not affect resulting data.
     *
     * @param aspspAccountId Bank specific account identifier
     * @param <T>            type of the entity, for which this specification will be created
     * @return resulting specification
     */
    protected <T> Specification<T> byAspspAccountIdInAspspAccountAccess(@Nullable String aspspAccountId) {
        return (root, query, cb) -> {
            Join<T, List<AspspAccountAccess>> aspspAccountAccessJoin = root.join(ASPSP_ACCOUNT_ACCESSES_ATTRIBUTE);
            return provideSpecificationForJoinedEntityAttribute(aspspAccountAccessJoin, ASPSP_ACCOUNT_ID_ATTRIBUTE, aspspAccountId)
                       .toPredicate(root, query, cb);
        };
    }

    /**
     * Returns specification for some entity for filtering data by aspsp account id.
     *
     * <p>
     * If optional parameter is not provided, this specification will not affect resulting data.
     *
     * @param aspspAccountId Bank specific account identifier
     * @param <T>            type of the entity, for which this specification will be created
     * @return resulting specification
     */
    protected <T> Specification<T> byAspspAccountId(@Nullable String aspspAccountId) {
        return provideSpecificationForEntityAttribute(ASPSP_ACCOUNT_ID_ATTRIBUTE, aspspAccountId);
    }

    /**
     * Returns specification for some entity for filtering data by instance id.
     *
     * @param instanceId optional ID of particular service instance
     * @param <T>        type of the entity, for which this specification will be created
     * @return resulting specification, or <code>null</code> if instance id was omitted
     */
    protected <T> Specification<T> byInstanceId(@Nullable String instanceId) {
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
     * @param <T>   type of the entity, for which this specification will be created
     * @return resulting specification
     */
    protected <T> Specification<T> byCreationTimestamp(@Nullable LocalDate start, @Nullable LocalDate end) {
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
     * @param <T>   type of the entity, for which this specification will be created
     * @return resulting specification
     */
    protected <T> Specification<T> byCreationTimestamp(@Nullable OffsetDateTime start, @Nullable OffsetDateTime end) {
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

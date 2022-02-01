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

package de.adorsys.psd2.consent.repository.specification;

import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.psd2.consent.repository.specification.EntityAttribute.*;
import static de.adorsys.psd2.consent.repository.specification.EntityAttributeSpecificationProvider.*;

@RequiredArgsConstructor
public abstract class ConsentFilterableSpecification {
    private final CommonSpecification<ConsentEntity> commonSpecification;
    private final ConsentSpecification consentSpecification;

    public abstract List<ConsentType> getTypes();

    /**
     * Returns specification for ConsentEntity entity for filtering data by consent ID and instance ID.
     *
     * @param consentId  consent external ID
     * @param instanceId instance ID
     * @return specification for ConsentEntity entity
     */
    public Specification<ConsentEntity> byConsentIdAndInstanceId(String consentId, String instanceId) {
        return consentSpecification.byConsentIdAndInstanceId(consentId, instanceId)
                   .and(byConsentType());
    }

    /**
     * Returns specification for ConsentEntity entity for filtering data by TPP authorisation number, creation date, PSU ID data and instance ID.
     *
     * @param tppAuthorisationNumber mandatory TPP authorisation number
     * @param createDateFrom         optional creation date that limits results to AIS consents created after this date(inclusive)
     * @param createDateTo           optional creation date that limits results to AIS consents created before this date(inclusive)
     * @param psuIdData              optional PSU ID data
     * @param instanceId             optional instance ID
     * @return specification for ConsentEntity entity
     */
    public Specification<ConsentEntity> byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(@NotNull String tppAuthorisationNumber,
                                                                                          @Nullable LocalDate createDateFrom,
                                                                                          @Nullable LocalDate createDateTo,
                                                                                          @Nullable PsuIdData psuIdData,
                                                                                          @Nullable String instanceId,
                                                                                          @Nullable String additionalTppInfo) {
        return (root, query, cb) -> {
            root.joinList(ASPSP_ACCOUNT_ACCESSES_ATTRIBUTE);
            return Optional.ofNullable(consentSpecification.byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(tppAuthorisationNumber, createDateFrom, createDateTo, psuIdData, instanceId))
                       .map(s -> s.and(byConsentType()))
                       .map(s -> s.and(byAdditionalTppInfo(additionalTppInfo)))
                       .map(s -> s.toPredicate(root, query, cb))
                       .orElse(null);
        };
    }

    public Specification<ConsentEntity> byPsuDataInListAndInstanceIdAndAdditionalTppInfo(PsuIdData psuIdData, String instanceId,
                                                                                         String additionalTppInfo, List<ConsentStatus> statuses,
                                                                                         List<String> accountNumbers) {
        return Optional.ofNullable(consentSpecification.byPsuDataInListAndInstanceId(psuIdData, instanceId))
                   .map(s -> s.and(byAdditionalTppInfo(additionalTppInfo)))
                   .map(s -> s.and(byInConsentStatuses(statuses)))
                   .map(s -> s.and(byInAccountNumbers(accountNumbers)))
                   .map(s -> s.and(byConsentType()))
                   .orElse(null);
    }

    private Specification<ConsentEntity> byInConsentStatuses(List<ConsentStatus> statuses) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (CollectionUtils.isEmpty(statuses)) {
                return null;
            }
            return criteriaBuilder.and(root.get(CONSENT_STATUS).in(statuses));
        };
    }

    private Specification<ConsentEntity> byInAccountNumbers(List<String> accountNumbers) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(accountNumbers)) {
                return null;
            }
            Join<ConsentEntity, List<AspspAccountAccess>> aspspAccountAccessesJoin = root.join(ASPSP_ACCOUNT_ACCESSES_ATTRIBUTE);
            return Specification
                       .where(provideSpecificationForJoinedEntityAttributeIn(aspspAccountAccessesJoin, ACCOUNT_ACCESS_ATTRIBUTE_ACCOUNT_IDENTIFIER, accountNumbers))
                       .toPredicate(root, query, cb);
        };
    }

    public Specification<ConsentEntity> byAdditionalTppInfo(@Nullable String additionalTppInfo) {
        return (root, query, cb) -> {
            Join<ConsentEntity, ConsentTppInformation> consentTppInformationJoin = root.join(CONSENT_TPP_INFORMATION_ATTRIBUTE);
            return provideSpecificationForJoinedEntityAttribute(consentTppInformationJoin, ADDITIONAL_TPP_INFORMATION_ATTRIBUTE, additionalTppInfo)
                       .toPredicate(root, query, cb);
        };
    }

    public Specification<ConsentEntity> byPsuDataInListAndInstanceId(PsuIdData psuIdData, String instanceId) {
        return consentSpecification.byPsuDataInListAndInstanceId(psuIdData, instanceId)
                   .and(byConsentType());
    }

    /**
     * Returns specification for ConsentEntity entity for filtering data by PSU ID Data, creation date and instance ID.
     *
     * @param psuIdData      mandatory PSU ID data
     * @param createDateFrom optional creation date that limits resulting data to AIS consents created after this date(inclusive)
     * @param createDateTo   optional creation date that limits resulting data to AIS consents created before this date(inclusive)
     * @param instanceId     optional instance ID
     * @return specification for ConsentEntity entity
     */
    public Specification<ConsentEntity> byPsuIdDataAndCreationPeriodAndInstanceIdAndAdditionalTppInfo(@NotNull PsuIdData psuIdData,
                                                                                                      @Nullable LocalDate createDateFrom,
                                                                                                      @Nullable LocalDate createDateTo,
                                                                                                      @Nullable String instanceId,
                                                                                                      @Nullable String additionalTppInfo) {
        return (root, query, cb) -> {
            root.joinList(ASPSP_ACCOUNT_ACCESSES_ATTRIBUTE);
            return Optional.ofNullable(commonSpecification
                                    .byPsuIdDataAndCreationPeriodAndInstanceId(psuIdData, createDateFrom, createDateTo, instanceId))
                .map(s -> s.and(byConsentType()))
                .map(s -> s.and(byAdditionalTppInfo(additionalTppInfo)))
                .map(s -> s.toPredicate(root, query, cb))
                .orElse(null);
        };
    }

    /**
     * Returns specification for ConsentEntity entity for filtering data by ASPSP account ID, creation date and instance ID.
     *
     * @param aspspAccountId bank-specific account identifier
     * @param createDateFrom optional creation date that limits resulting data to AIS consents created after this date(inclusive)
     * @param createDateTo   optional creation date that limits resulting data to AIS consents created before this date(inclusive)
     * @param instanceId     optional instance ID
     * @return specification for ConsentEntity entity
     */
    public Specification<ConsentEntity> byAspspAccountIdAndCreationPeriodAndInstanceIdAndAdditionalTppInfo(@NotNull String aspspAccountId,
                                                                                                           @Nullable LocalDate createDateFrom,
                                                                                                           @Nullable LocalDate createDateTo,
                                                                                                           @Nullable String instanceId,
                                                                                                           @Nullable String additionalTppInfo) {
        return Optional.ofNullable(consentSpecification
                                       .byAspspAccountIdAndCreationPeriodAndInstanceId(aspspAccountId, createDateFrom, createDateTo, instanceId))
                   .map(s -> s.and(byConsentType()))
                   .map(s -> s.and(byAdditionalTppInfo(additionalTppInfo)))
                   .orElse(null);
    }

    /**
     * Returns specification for ConsentEntity entity for filtering data by consent type list.
     * Uses predefined method `getTypes` for getting consent types.
     *
     * @return specification for ConsentEntity sent entity
     */
    public Specification<ConsentEntity> byConsentType() {
        return provideSpecificationForEntityAttributeInList(CONSENT_TYPE_ATTRIBUTE,
                                                            getTypes().stream()
                                                                .map(ConsentType::name)
                                                                .collect(Collectors.toList()));
    }
}

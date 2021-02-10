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

import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.psd2.consent.repository.specification.EntityAttribute.*;
import static de.adorsys.psd2.consent.repository.specification.EntityAttributeSpecificationProvider.provideSpecificationForEntityAttributeInList;
import static de.adorsys.psd2.consent.repository.specification.EntityAttributeSpecificationProvider.provideSpecificationForJoinedEntityAttribute;

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
        return Optional.ofNullable(
            consentSpecification
                .byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(tppAuthorisationNumber, createDateFrom, createDateTo, psuIdData, instanceId))
                   .map(s -> s.and(byConsentType()))
                   .map(s -> s.and(byAdditionalTppInfo(additionalTppInfo)))
                   .orElse(null);
    }

    public Specification<ConsentEntity> byPsuDataInListAndInstanceIdAndAdditionalTppInfo(PsuIdData psuIdData, String instanceId, String additionalTppInfo) {
        return Optional.ofNullable(consentSpecification.byPsuDataInListAndInstanceId(psuIdData, instanceId))
                   .map(s -> s.and(byAdditionalTppInfo(additionalTppInfo)))
                   .map(s -> s.and(byConsentType()))
                   .orElse(null);
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
        return Optional.ofNullable(commonSpecification
                                       .byPsuIdDataAndCreationPeriodAndInstanceId(psuIdData, createDateFrom, createDateTo, instanceId))
                   .map(s -> s.and(byConsentType()))
                   .map(s -> s.and(byAdditionalTppInfo(additionalTppInfo)))
                   .orElse(null);
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

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

import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Join;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.consent.repository.specification.EntityAttribute.*;
import static de.adorsys.psd2.consent.repository.specification.EntityAttributeSpecificationProvider.provideSpecificationForEntityAttribute;
import static de.adorsys.psd2.consent.repository.specification.EntityAttributeSpecificationProvider.provideSpecificationForJoinedEntityAttribute;

@Service
@RequiredArgsConstructor
public class ConsentSpecification {
    private final CommonSpecification<ConsentEntity> commonSpecification;

    /**
     * Returns specification for ConsentEntity entity for filtering data by ASPSP account ID, creation date and instance ID.
     *
     * @param aspspAccountId bank-specific account identifier
     * @param createDateFrom optional creation date that limits resulting data to AIS consents created after this date(inclusive)
     * @param createDateTo   optional creation date that limits resulting data to AIS consents created before this date(inclusive)
     * @param instanceId     optional instance ID
     * @return specification for AisCoConsentEntitynsent entity
     */
    public Specification<ConsentEntity> byAspspAccountIdAndCreationPeriodAndInstanceId(@NotNull String aspspAccountId,
                                                                                       @Nullable LocalDate createDateFrom,
                                                                                       @Nullable LocalDate createDateTo,
                                                                                       @Nullable String instanceId) {
        return Optional.of(Specification.where(byAspspAccountIdInAspspAccountAccess(aspspAccountId)))
                   .map(s -> s.and(commonSpecification.byCreationTimestamp(createDateFrom, createDateTo)))
                   .map(s -> s.and(commonSpecification.byInstanceId(instanceId)))
                   .orElse(null);
    }

    /**
     * Returns specification for ConsentEntity entity for filtering data by ASPSP account ID and PSU ID Data and instance ID.
     *
     * @param psuIdData      mandatory PSU ID data
     * @param aspspAccountId bank-specific account identifier
     * @param instanceId     optional instance ID
     * @return specification for ConsentEntity entity
     */
    public Specification<ConsentEntity> byPsuIdDataAndAspspAccountIdAndInstanceId(@NotNull PsuIdData psuIdData,
                                                                                  @Nullable String aspspAccountId,
                                                                                  @Nullable String instanceId) {
        return Optional.of(Specification.where(byAspspAccountIdInAspspAccountAccess(aspspAccountId)))
                   .map(s -> s.and(commonSpecification.byPsuIdDataInList(psuIdData)))
                   .map(s -> s.and(commonSpecification.byInstanceId(instanceId)))
                   .orElse(null);
    }

    /**
     * Returns specification for ConsentEntity entity for filtering data by consent ID and instance ID.
     *
     * @param consentId  consent external ID
     * @param instanceId instance ID
     * @return specification for ConsentEntity entity
     */
    public Specification<ConsentEntity> byConsentIdAndInstanceId(String consentId, String instanceId) {
        return Optional.of(Specification.where(commonSpecification.byInstanceId(instanceId)))
                   .map(s -> s.and(provideSpecificationForEntityAttribute(CONSENT_EXTERNAL_ID_ATTRIBUTE, consentId)))
                   .orElse(null);
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
                                                                                          @Nullable String instanceId) {
        return Optional.of(Specification.where(byTpp(tppAuthorisationNumber)))
                   .map(s -> s.and(commonSpecification.byCreationTimestamp(createDateFrom, createDateTo)))
                   .map(s -> s.and(commonSpecification.byPsuIdDataInList(psuIdData)))
                   .map(s -> s.and(commonSpecification.byInstanceId(instanceId)))
                   .orElse(null);
    }

    /**
     * Returns specification for some entity for filtering data by PSU data from list and instance id.
     *
     * @param psuIdData  PSU data
     * @param instanceId ID of particular service instance
     * @return resulting specification
     */
    public Specification<ConsentEntity> byPsuDataInListAndInstanceId(PsuIdData psuIdData, String instanceId) {
        return commonSpecification.byPsuIdDataInList(psuIdData).and(commonSpecification.byInstanceId(instanceId));
    }

    /**
     * Returns specification for ConsentEntity entity for filtering data by TPP authorisation number using
     * the tppInformation attribute.
     *
     * @param tppAuthorisationNumber mandatory TPP authorisation number
     * @return specification for ConsentEntity entity
     */
    public Specification<ConsentEntity> byTpp(@Nullable String tppAuthorisationNumber) {
        return (root, query, cb) -> {
            Join<ConsentEntity, ConsentTppInformation> consentTppInformationJoin = root.join(CONSENT_TPP_INFORMATION_ATTRIBUTE);
            Join<ConsentEntity, TppInfoEntity> tppInfoJoin = consentTppInformationJoin.join(TPP_INFO_ATTRIBUTE);
            return provideSpecificationForJoinedEntityAttribute(tppInfoJoin, TPP_INFO_AUTHORISATION_NUMBER_ATTRIBUTE, tppAuthorisationNumber)
                       .toPredicate(root, query, cb);
        };
    }

    private Specification<ConsentEntity> byAspspAccountIdInAspspAccountAccess(@Nullable String aspspAccountId) {
        return (root, query, cb) -> {
            Join<ConsentEntity, List<AspspAccountAccess>> aspspAccountAccessJoin = root.join(ASPSP_ACCOUNT_ACCESSES_ATTRIBUTE);
            return provideSpecificationForJoinedEntityAttribute(aspspAccountAccessJoin, ASPSP_ACCOUNT_ID_ATTRIBUTE, aspspAccountId)
                       .toPredicate(root, query, cb);
        };
    }
}

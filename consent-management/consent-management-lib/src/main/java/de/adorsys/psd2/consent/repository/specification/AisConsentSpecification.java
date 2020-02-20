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

import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Join;
import java.time.LocalDate;
import java.util.List;

import static de.adorsys.psd2.consent.repository.specification.EntityAttribute.*;
import static de.adorsys.psd2.consent.repository.specification.EntityAttributeSpecificationProvider.provideSpecificationForEntityAttribute;
import static de.adorsys.psd2.consent.repository.specification.EntityAttributeSpecificationProvider.provideSpecificationForJoinedEntityAttribute;

@Service
public class AisConsentSpecification extends GenericSpecification {
    /**
     * Returns specification for ConsentEntity entity for filtering data by consent ID and instance ID.
     *
     * @param consentId  consent external ID
     * @param instanceId instance ID
     * @return specification for ConsentEntity entity
     */
    public Specification<ConsentEntity> byConsentIdAndInstanceId(String consentId, String instanceId) {
        return Specification.<ConsentEntity>where(byInstanceId(instanceId))
                   .and(provideSpecificationForEntityAttribute(CONSENT_EXTERNAL_ID_ATTRIBUTE, consentId))
                   .and(byAisConsentType());
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
        return Specification.<ConsentEntity>where(byTpp(tppAuthorisationNumber))
                   .and(byCreationTimestamp(createDateFrom, createDateTo))
                   .and(byPsuIdDataInList(psuIdData))
                   .and(byInstanceId(instanceId))
                   .and(byAisConsentType());
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
    public Specification<ConsentEntity> byPsuIdDataAndCreationPeriodAndInstanceId(@NotNull PsuIdData psuIdData,
                                                                                  @Nullable LocalDate createDateFrom,
                                                                                  @Nullable LocalDate createDateTo,
                                                                                  @Nullable String instanceId) {
        return Specification.<ConsentEntity>where(byPsuIdDataInList(psuIdData))
                   .and(byCreationTimestamp(createDateFrom, createDateTo))
                   .and(byInstanceId(instanceId))
                   .and(byAisConsentType());
    }

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
        return Specification.where(byAisConsentType())
                   .and(byAspspAccountIdInAspspAccountAccess(aspspAccountId))
                   .and(byCreationTimestamp(createDateFrom, createDateTo))
                   .and(byInstanceId(instanceId));
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

        return Specification.where(byAisConsentType())
                   .and(byPsuIdDataInList(psuIdData))
                   .and(byAspspAccountIdInAspspAccountAccess(aspspAccountId))
                   .and(byInstanceId(instanceId));
    }

    private <T> Specification<T> byTpp(@Nullable String tppAuthorisationNumber) {
        return (root, query, cb) -> {
            Join<T, ConsentTppInformation> consentTppInformationJoin = root.join(CONSENT_TPP_INFORMATION_ATTRIBUTE);
            Join<T, TppInfoEntity> tppInfoJoin = consentTppInformationJoin.join(TPP_INFO_ATTRIBUTE);
            return provideSpecificationForJoinedEntityAttribute(tppInfoJoin, TPP_INFO_AUTHORISATION_NUMBER_ATTRIBUTE, tppAuthorisationNumber)
                       .toPredicate(root, query, cb);
        };
    }

    private Specification<ConsentEntity> byAisConsentType() {
        return provideSpecificationForEntityAttribute(CONSENT_TYPE_ATTRIBUTE, ConsentType.AIS.getName());
    }

    private Specification<ConsentEntity> byAspspAccountIdInAspspAccountAccess(@Nullable String aspspAccountId) {
        return (root, query, cb) -> {
            Join<ConsentEntity, List<AspspAccountAccess>> aspspAccountAccessJoin = root.join(ASPSP_ACCOUNT_ACCESSES_ATTRIBUTE);
            query.distinct(true);
            return provideSpecificationForJoinedEntityAttribute(aspspAccountAccessJoin, ASPSP_ACCOUNT_ID_ATTRIBUTE, aspspAccountId)
                       .toPredicate(root, query, cb);
        };
    }
}

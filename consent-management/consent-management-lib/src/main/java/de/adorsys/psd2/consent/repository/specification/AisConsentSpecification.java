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

import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
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
     * Returns specification for AisConsent entity for filtering data by consent ID and instance ID.
     *
     * @param consentId  consent external ID
     * @param instanceId instance ID
     * @return specification for AisConsent entity
     */
    public Specification<AisConsent> byConsentIdAndInstanceId(String consentId, String instanceId) {
        return Specifications.<AisConsent>where(byInstanceId(instanceId))
                   .and(provideSpecificationForEntityAttribute(CONSENT_EXTERNAL_ID_ATTRIBUTE, consentId));
    }

    /**
     * Returns specification for AisConsent entity for filtering data by TPP authorisation number, creation date, PSU ID data and instance ID.
     *
     * @param tppAuthorisationNumber mandatory TPP authorisation number
     * @param createDateFrom         optional creation date that limits results to AIS consents created after this date(inclusive)
     * @param createDateTo           optional creation date that limits results to AIS consents created before this date(inclusive)
     * @param psuIdData              optional PSU ID data
     * @param instanceId             optional instance ID
     * @return specification for AisConsent entity
     */
    public Specification<AisConsent> byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(@NotNull String tppAuthorisationNumber,
                                                                                       @Nullable LocalDate createDateFrom,
                                                                                       @Nullable LocalDate createDateTo,
                                                                                       @Nullable PsuIdData psuIdData,
                                                                                       @Nullable String instanceId) {
        return Specifications.<AisConsent>where(byTppAuthorisationNumber(tppAuthorisationNumber))
                   .and(byCreationTimestamp(createDateFrom, createDateTo))
                   .and(byPsuIdDataInList(psuIdData))
                   .and(byInstanceId(instanceId));
    }

    /**
     * Returns specification for AisConsent entity for filtering data by PSU ID Data, creation date and instance ID.
     *
     * @param psuIdData      mandatory PSU ID data
     * @param createDateFrom optional creation date that limits resulting data to AIS consents created after this date(inclusive)
     * @param createDateTo   optional creation date that limits resulting data to AIS consents created before this date(inclusive)
     * @param instanceId     optional instance ID
     * @return specification for AisConsent entity
     */
    public Specification<AisConsent> byPsuIdDataAndCreationPeriodAndInstanceId(@NotNull PsuIdData psuIdData,
                                                                               @Nullable LocalDate createDateFrom,
                                                                               @Nullable LocalDate createDateTo,
                                                                               @Nullable String instanceId) {
        return Specifications.<AisConsent>where(byPsuIdDataInList(psuIdData))
                   .and(byCreationTimestamp(createDateFrom, createDateTo))
                   .and(byInstanceId(instanceId));
    }

    /**
     * Returns specification for AisConsent entity for filtering data by ASPSP account ID, creation date and instance ID.
     *
     * @param aspspAccountId Bank specific account identifier
     * @param createDateFrom optional creation date that limits resulting data to AIS consents created after this date(inclusive)
     * @param createDateTo   optional creation date that limits resulting data to AIS consents created before this date(inclusive)
     * @param instanceId     optional instance ID
     * @return specification for AisConsent entity
     */
    public Specification<AisConsent> byAspspAccountIdAndCreationPeriodAndInstanceId(@NotNull String aspspAccountId,
                                                                                    @Nullable LocalDate createDateFrom,
                                                                                    @Nullable LocalDate createDateTo,
                                                                                    @Nullable String instanceId) {
        return Specifications.<AisConsent>where(byAspspAccountIdInAspspAccountAccess(aspspAccountId))
                   .and(byCreationTimestamp(createDateFrom, createDateTo))
                   .and(byInstanceId(instanceId));
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
    private <T> Specification<T> byAspspAccountIdInAspspAccountAccess(@Nullable String aspspAccountId) {
        return (root, query, cb) -> {
            Join<T, List<AspspAccountAccess>> aspspAccountAccessJoin = root.join(ASPSP_ACCOUNT_ACCESSES_ATTRIBUTE);
            query.distinct(true);
            return provideSpecificationForJoinedEntityAttribute(aspspAccountAccessJoin, ASPSP_ACCOUNT_ID_ATTRIBUTE, aspspAccountId)
                       .toPredicate(root, query, cb);
        };
    }
}

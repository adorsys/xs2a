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

package de.adorsys.psd2.consent.repository;

import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AisConsentRepository extends Xs2aCrudRepository<AisConsent, Long>, JpaSpecificationExecutor<AisConsent> {
    List<AisConsent> findByConsentStatusIn(Set<ConsentStatus> statuses);

    Optional<AisConsent> findByExternalId(String externalId);

    @Query(
        "select distinct c from ais_consent c " +
            "join c.psuDataList psuList " +
            "where psuList.psuId in :psuIds " +
            "and c.tppInfo.authorisationNumber = :authorisationNumber " +
            "and c.tppInfo.authorityId = :authorityId " +
            "and c.instanceId = :instanceId " +
            "and c.consentStatus in :consentStatuses " +
            "and c.externalId <> :newConsentId"
    )
    List<AisConsent> findOldConsentsByNewConsentParams(@Param("psuIds") Set<String> psuIds,
                                                       @Param("authorisationNumber") String tppAuthorisationNumber,
                                                       @Param("authorityId") String tppAuthorityId,
                                                       @Param("instanceId") String instanceId,
                                                       @Param("newConsentId") String newConsentId,
                                                       @Param("consentStatuses") Set<ConsentStatus> consentStatuses);
}

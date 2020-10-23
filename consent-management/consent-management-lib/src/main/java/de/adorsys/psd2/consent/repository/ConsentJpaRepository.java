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

package de.adorsys.psd2.consent.repository;

import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ConsentJpaRepository extends CrudRepository<ConsentEntity, Long>, JpaSpecificationExecutor<ConsentEntity> {
    List<ConsentEntity> findByConsentStatusIn(Set<ConsentStatus> statuses);

    Optional<ConsentEntity> findByExternalId(String externalId);

    List<ConsentEntity> findAllByExternalIdIn(List<String> externalIds);

    @Query(
        "select c from consent c " +
            "join c.psuDataList psuList " +
            "where psuList.psuId in :psuIds " +
            "and c.tppInformation.tppInfo.authorisationNumber = :authorisationNumber " +
            "and c.instanceId = :instanceId " +
            "and c.consentStatus in :consentStatuses " +
            "and c.externalId <> :newConsentId"
    )
    List<ConsentEntity> findOldConsentsByNewConsentParams(@Param("psuIds") Set<String> psuIds,
                                                          @Param("authorisationNumber") String tppAuthorisationNumber,
                                                          @Param("instanceId") String instanceId,
                                                          @Param("newConsentId") String newConsentId,
                                                          @Param("consentStatuses") Set<ConsentStatus> consentStatuses);

    @Query(
        "select c from consent c " +
            "join c.usages u " +
            "where c.recurringIndicator = false " +
            "and c.consentStatus in :consentStatuses " +
            "and u.usageDate < :currentDate"
    )
    List<ConsentEntity> findUsedNonRecurringConsents(@Param("consentStatuses") Set<ConsentStatus> consentStatuses,
                                                     @Param("currentDate") LocalDate currentDate);
}

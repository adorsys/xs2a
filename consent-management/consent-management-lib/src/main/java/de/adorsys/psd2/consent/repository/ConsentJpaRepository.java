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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ConsentJpaRepository extends CrudRepository<ConsentEntity, Long>, JpaSpecificationExecutor<ConsentEntity> {

    List<ConsentEntity> findByConsentStatusIn(Set<ConsentStatus> statuses, Pageable pageable);

    Long countByConsentStatusIn(Set<ConsentStatus> statuses);

    Optional<ConsentEntity> findByExternalId(String externalId);

    @Query(
        "UPDATE consent " +
            "SET consentStatus = 'EXPIRED', expireDate = CURRENT_TIMESTAMP " +
            "WHERE consentStatus IN :consentStatuses AND validUntil < CURRENT_DATE"
    )
    @Modifying
    void expireByConsentStatusIn(@Param("consentStatuses") Set<ConsentStatus> consentStatuses);

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
        "UPDATE consent " +
            "SET consentStatus = 'EXPIRED', expireDate = CURRENT_TIMESTAMP " +
            "WHERE recurringIndicator = false " +
            "AND consentStatus IN :consentStatuses " +
            "AND id IN (SELECT cu.consent.id FROM consent_usage cu WHERE cu.usageDate < CURRENT_DATE)"
    )
    @Modifying
    void expireUsedNonRecurringConsents(@Param("consentStatuses") Set<ConsentStatus> consentStatuses);

    @Query(
        value = "select * from {h-schema}consent c " +
                    "join " +
                    "(select consent_id cid, aspsp_account_id from {h-schema}aspsp_account_access group by consent_id, aspsp_account_id) a " +
                    "on a.cid = c.consent_id " +
                    "where c.consent_type in :consentType " +
                    "and a.aspsp_account_id = :aspspAccountId " +
                    "and c.creation_timestamp between :createDateFrom and :createDateTo " +
                    "and c.instance_id = :instanceId "
        ,
        nativeQuery = true
    )
    Page<ConsentEntity> findAllWithPagination(
        @Param("consentType") Set<String> consentType,
        @Param("aspspAccountId") String aspspAccountId,
        @Param("createDateFrom") OffsetDateTime createDateFrom,
        @Param("createDateTo") OffsetDateTime createDateTo,
        @Param("instanceId") String instanceId,
        Pageable pageable
    );

    @Query(
        value = "select * from {h-schema}consent c " +
                    "join " +
                    "(select consent_id cid, aspsp_account_id from {h-schema}aspsp_account_access group by consent_id, aspsp_account_id) a " +
                    "on a.cid = c.consent_id " +
                    "join " +
                    "(select consent_tpp_information_id tid, additional_info from {h-schema}consent_tpp_information) t " +
                    "on t.tid = c.consent_tpp_information_id " +
                    "where c.consent_type in :consentType " +
                    "and a.aspsp_account_id = :aspspAccountId " +
                    "and c.creation_timestamp between :createDateFrom and :createDateTo " +
                    "and c.instance_id = :instanceId " +
                    "and t.additional_info = :additionalTppInfo"
        ,
        nativeQuery = true
    )
    Page<ConsentEntity> findAllWithPaginationAndTppInfo(
        @Param("consentType") Set<String> consentType,
        @Param("aspspAccountId") String aspspAccountId,
        @Param("createDateFrom") OffsetDateTime createDateFrom,
        @Param("createDateTo") OffsetDateTime createDateTo,
        @Param("instanceId") String instanceId,
        Pageable pageable,
        @Param("additionalTppInfo") String additionalTppInfo
    );
}

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

    List<ConsentEntity> findByConsentStatusIn(Set<ConsentStatus> statuses);

    Long countByConsentStatusIn(Set<ConsentStatus> statuses);

    Optional<ConsentEntity> findByExternalId(String externalId);

    List<ConsentEntity> findAllByExternalIdIn(List<String> externalIds);

    @Query(
        "UPDATE consent " +
            "SET consentStatus = 'EXPIRED', expireDate = CURRENT_TIMESTAMP " +
            "WHERE consentStatus IN :consentStatuses AND validUntil < CURRENT_DATE"
    )
    @Modifying
    void expireByConsentStatusIn(@Param("consentStatuses") Set<ConsentStatus> consentStatuses);

    @Query(
        "UPDATE consent " +
            "SET consentStatus = 'REJECTED', lastActionDate = CURRENT_TIMESTAMP " +
            "WHERE externalId IN :ids"
    )
    @Modifying
    void expireConsentsByIds(@Param("ids") List<String> ids);

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

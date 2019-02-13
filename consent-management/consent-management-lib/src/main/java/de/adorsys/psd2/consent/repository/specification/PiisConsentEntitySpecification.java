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

import de.adorsys.psd2.consent.domain.AccountReferenceEntity;
import de.adorsys.psd2.consent.domain.piis.PiisConsentEntity;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Join;
import java.time.LocalDate;

import static de.adorsys.psd2.consent.repository.specification.EntityAttribute.*;
import static de.adorsys.psd2.consent.repository.specification.EntityAttributeSpecificationProvider.provideSpecificationForEntityAttribute;
import static de.adorsys.psd2.consent.repository.specification.EntityAttributeSpecificationProvider.provideSpecificationForJoinedEntityAttribute;

@Service
public class PiisConsentEntitySpecification extends GenericSpecification {

    public Specification<PiisConsentEntity> byConsentIdAndInstanceId(String consentId, String instanceId) {
        return Specifications.<PiisConsentEntity>where(provideSpecificationForEntityAttribute(INSTANCE_ID_ATTRIBUTE, instanceId))
                   .and(provideSpecificationForEntityAttribute(CONSENT_EXTERNAL_ID_ATTRIBUTE, consentId));
    }

    /**
     * Returns specification for PiisConsentEntity for filtering PIIS consents by TPP authorisation number, creation date, PSU ID data and instance ID.
     *
     * @param tppAuthorisationNumber mandatory TPP authorisation number
     * @param createDateFrom         optional creation date that limits results to consents created after this date(inclusive)
     * @param createDateTo           optional creation date that limits results to consents created before this date(inclusive)
     * @param psuIdData              optional PSU ID data
     * @param instanceId             optional instance ID
     * @return resulting specification for PiisConsentEntity
     */
    public Specification<PiisConsentEntity> byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(@NotNull String tppAuthorisationNumber,
                                                                                              @Nullable LocalDate createDateFrom,
                                                                                              @Nullable LocalDate createDateTo,
                                                                                              @Nullable PsuIdData psuIdData,
                                                                                              @Nullable String instanceId) {
        return Specifications.<PiisConsentEntity>where(byTppAuthorisationNumber(tppAuthorisationNumber))
                   .and(byCreationTimestamp(createDateFrom, createDateTo))
                   .and(byPsuIdData(psuIdData))
                   .and(byInstanceId(instanceId));
    }

    /**
     * Returns specification for PiisConsentEntity for filtering consents by PSU ID Data, creation date and instance ID.
     *
     * @param psuIdData      mandatory PSU ID data
     * @param createDateFrom optional creation date that limits resulting data to consents created after this date(inclusive)
     * @param createDateTo   optional creation date that limits resulting data to consents created before this date(inclusive)
     * @param instanceId     optional instance ID
     * @return resulting specification for PiisConsentEntity
     */
    public Specification<PiisConsentEntity> byPsuIdDataAndCreationPeriodAndInstanceId(@NotNull PsuIdData psuIdData,
                                                                                      @Nullable LocalDate createDateFrom,
                                                                                      @Nullable LocalDate createDateTo,
                                                                                      @Nullable String instanceId) {
        return Specifications.<PiisConsentEntity>where(byPsuIdData(psuIdData))
                   .and(byCreationTimestamp(createDateFrom, createDateTo))
                   .and(byInstanceId(instanceId));
    }

    /**
     * Returns specification for PiisConsentEntity for filtering consents by aspsp account id, creation date and instance ID.
     *
     * @param aspspAccountId mandatory bank specific account identifier
     * @param createDateFrom optional creation date that limits resulting data to consents created after this date(inclusive)
     * @param createDateTo   optional creation date that limits resulting data to consents created before this date(inclusive)
     * @param instanceId     optional instance ID
     * @return resulting specification for PiisConsentEntity
     */
    public Specification<PiisConsentEntity> byAspspAccountIdAndCreationPeriodAndInstanceId(@NotNull String aspspAccountId,
                                                                                           @Nullable LocalDate createDateFrom,
                                                                                           @Nullable LocalDate createDateTo,
                                                                                           @Nullable String instanceId) {
        return Specifications.where(byAspspAccountIdInAccounts(aspspAccountId))
                   .and(byCreationTimestamp(createDateFrom, createDateTo))
                   .and(byInstanceId(instanceId));
    }

    /**
     * Returns specification for PiisConsentEntity for filtering data by aspsp account id.
     *
     * <p>
     * If optional parameter is not provided, this specification will not affect resulting data.
     *
     * @param aspspAccountId Bank specific account identifier
     * @return resulting specification
     */
    private Specification<PiisConsentEntity> byAspspAccountIdInAccounts(@Nullable String aspspAccountId) {
        return (root, query, cb) -> {
            Join<PiisConsentEntity, AccountReferenceEntity> accountsJoin = root.join(ACCOUNTS_ATTRIBUTE);
            return provideSpecificationForJoinedEntityAttribute(accountsJoin, ASPSP_ACCOUNT_ID_ATTRIBUTE, aspspAccountId)
                       .toPredicate(root, query, cb);
        };
    }
}

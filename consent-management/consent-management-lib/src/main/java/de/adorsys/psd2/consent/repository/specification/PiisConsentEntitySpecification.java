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

import de.adorsys.psd2.consent.domain.AccountReferenceEntity;
import de.adorsys.psd2.consent.domain.piis.PiisConsentEntity;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceSelector;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Join;
import java.time.LocalDate;
import java.util.Currency;

import static de.adorsys.psd2.consent.repository.specification.EntityAttribute.*;
import static de.adorsys.psd2.consent.repository.specification.EntityAttributeSpecificationProvider.provideSpecificationForEntityAttribute;
import static de.adorsys.psd2.consent.repository.specification.EntityAttributeSpecificationProvider.provideSpecificationForJoinedEntityAttribute;

@Service
public class PiisConsentEntitySpecification extends GenericSpecification {

    public Specification<PiisConsentEntity> byConsentIdAndInstanceId(String consentId, String instanceId) {
        return Specification.<PiisConsentEntity>where(provideSpecificationForEntityAttribute(INSTANCE_ID_ATTRIBUTE, instanceId))
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

        return Specification.<PiisConsentEntity>where(byTppAuthorisationNumberWithoutJoin(tppAuthorisationNumber))
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
        return Specification.<PiisConsentEntity>where(byPsuIdData(psuIdData))
                   .and(byCreationTimestamp(createDateFrom, createDateTo))
                   .and(byInstanceId(instanceId));
    }

    /**
     * Returns specification for PiisConsentEntity for filtering consents by ASPSP account ID in Account, creation date and instance ID.
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
        return Specification.where(byAspspAccountIdInAccount(aspspAccountId))
                   .and(byCreationTimestamp(createDateFrom, createDateTo))
                   .and(byInstanceId(instanceId));
    }

    /**
     * Returns specification for PiisConsentEntity for filtering consents by PsuIdData, TppInfo and AccountReference.
     *
     * @param psuIdData              mandatory PSU ID data
     * @param tppAuthorisationNumber mandatory TPP authorisation number
     * @param accountReference       mandatory PIIS Account Reference
     * @return resulting specification for PiisConsentEntity
     */
    public Specification<PiisConsentEntity> byPsuIdDataAndAuthorisationNumberAndAccountReference(@NotNull PsuIdData psuIdData,
                                                                                                 @NotNull String tppAuthorisationNumber,
                                                                                                 @NotNull AccountReference accountReference) {
        return Specification.<PiisConsentEntity>where(byPsuIdData(psuIdData))
                   .and(byTppAuthorisationNumberWithoutJoin(tppAuthorisationNumber))
                   .and(byAccountReference(accountReference));
    }

    /**
     * Returns specification for PiisConsentEntity for filtering consents by Currency and Account Reference Selector.
     *
     * @param currency optional Currency
     * @param selector mandatory Account Reference Selector
     * @return resulting specification for PiisConsentEntity
     */
    public Specification<PiisConsentEntity> byCurrencyAndAccountReferenceSelector(@Nullable Currency currency, @NotNull AccountReferenceSelector selector) {
        return (root, query, cb) -> {
            Join<PiisConsentEntity, AccountReferenceEntity> accountJoin = root.join(ACCOUNT_ATTRIBUTE);

            return Specification
                       .where(provideSpecificationForJoinedEntityAttribute(accountJoin, selector.getAccountReferenceType().getValue(), selector.getAccountValue()))
                       .and(provideSpecificationForJoinedEntityAttribute(accountJoin, CURRENCY_ATTRIBUTE, currency))
                       .toPredicate(root, query, cb);
        };
    }

    /**
     * Returns specification for PiisConsentEntity for filtering consents by Account Reference Selector.
     *
     * @param selector mandatory Account Reference Selector
     * @return resulting specification for PiisConsentEntity
     */
    public Specification<PiisConsentEntity> byAccountReferenceSelector(@NotNull AccountReferenceSelector selector) {
        return (root, query, cb) -> {
            Join<PiisConsentEntity, AccountReferenceEntity> accountJoin = root.join(ACCOUNT_ATTRIBUTE);

            return Specification
                       .where(provideSpecificationForJoinedEntityAttribute(accountJoin, selector.getAccountReferenceType().getValue(), selector.getAccountValue()))
                       .toPredicate(root, query, cb);
        };
    }

    /**
     * Returns specification for PiisConsent entity for filtering data by ASPSP account ID and by PSU ID Data.
     *
     * @param aspspAccountId Bank specific account identifier
     * @param psuIdData      mandatory PSU ID data
     * @param instanceId     optional instance ID
     * @return specification for AisConsent entity
     */
    public Specification<PiisConsentEntity> byAspspAccountIdAndPsuIdDataAndInstanceId(@Nullable String aspspAccountId,
                                                                                      @NotNull PsuIdData psuIdData,
                                                                                      @Nullable String instanceId) {
        return Specification.where(byAspspAccountIdInAccount(aspspAccountId))
                   .and(byPsuIdData(psuIdData))
                   .and(byInstanceId(instanceId));
    }

    /**
     * Returns specification for PiisConsentEntity for filtering data by ASPSP account ID.
     *
     * <p>
     * If optional parameter is not provided, this specification will not affect resulting data.
     *
     * @param aspspAccountId Bank specific account identifier
     * @return resulting specification
     */
    private Specification<PiisConsentEntity> byAspspAccountIdInAccount(@Nullable String aspspAccountId) {
        return (root, query, cb) -> {
            Join<PiisConsentEntity, AccountReferenceEntity> accountJoin = root.join(ACCOUNT_ATTRIBUTE);
            return provideSpecificationForJoinedEntityAttribute(accountJoin, ASPSP_ACCOUNT_ID_ATTRIBUTE, aspspAccountId)
                       .toPredicate(root, query, cb);
        };
    }

    /**
     * Returns specification for PiisConsentEntity for filtering data by AccountReference.
     *
     * <p>
     * If optional parameter is not provided, this specification will not affect resulting data.
     *
     * @param accountReference PIIS Account Reference
     * @return resulting specification
     */
    private Specification<PiisConsentEntity> byAccountReference(@NotNull AccountReference accountReference) {
        return (root, query, cb) -> {
            AccountReferenceSelector selector = accountReference.getUsedAccountReferenceSelector();
            Join<PiisConsentEntity, AccountReferenceEntity> accountJoin = root.join(ACCOUNT_ATTRIBUTE);
            Specification<PiisConsentEntity> specifications = Specification
                                                                  .where(provideSpecificationForJoinedEntityAttribute(accountJoin, selector.getAccountReferenceType().getValue(), selector.getAccountValue()));

            if (accountReference.getCurrency() != null) {
                specifications.and(provideSpecificationForJoinedEntityAttribute(accountJoin, CURRENCY_ATTRIBUTE, accountReference.getCurrency()));
            }

            return specifications.toPredicate(root, query, cb);
        };
    }
}

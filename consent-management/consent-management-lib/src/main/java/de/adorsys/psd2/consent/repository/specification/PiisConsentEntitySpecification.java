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

import de.adorsys.psd2.consent.domain.AccountReferenceEntity;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceSelector;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Join;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.consent.repository.specification.EntityAttribute.*;
import static de.adorsys.psd2.consent.repository.specification.EntityAttributeSpecificationProvider.provideSpecificationForJoinedEntityAttribute;

@Service
public class PiisConsentEntitySpecification extends ConsentFilterableSpecification {
    private final CommonSpecification<ConsentEntity> commonSpecification;
    private final ConsentSpecification consentSpecification;

    public PiisConsentEntitySpecification(CommonSpecification<ConsentEntity> commonSpecification, ConsentSpecification consentSpecification) {
        super(commonSpecification, consentSpecification);
        this.commonSpecification = commonSpecification;
        this.consentSpecification = consentSpecification;
    }

    @Override
    public List<ConsentType> getTypes() {
        return Collections.singletonList(ConsentType.PIIS_ASPSP);
    }

    /**
     * Returns specification for some entity for filtering data by PSU data and instance id.
     *
     * @param psuIdData  PSU data
     * @param instanceId ID of particular service instance
     * @return resulting specification
     */
    public Specification<ConsentEntity> byPsuDataAndInstanceId(PsuIdData psuIdData, String instanceId) {
        return consentSpecification.byPsuDataInListAndInstanceId(psuIdData, instanceId)
                   .and(byConsentType());
    }

    /**
     * Returns specification for ConsentEntity for filtering consents by PsuIdData, TppInfo and AccountReference.
     *
     * @param psuIdData              mandatory PSU ID data
     * @param tppAuthorisationNumber mandatory TPP authorisation number
     * @param accountReference       mandatory PIIS Account Reference
     * @return resulting specification for ConsentEntity
     */
    public Specification<ConsentEntity> byPsuIdDataAndAuthorisationNumberAndAccountReferenceAndInstanceId(@NotNull PsuIdData psuIdData,
                                                                                                          @NotNull String tppAuthorisationNumber,
                                                                                                          @NotNull AccountReference accountReference,
                                                                                                          @NotNull String instanceId) {
        return Optional.of(Specification.where(commonSpecification.byPsuIdDataInList(psuIdData)))
                   .map(s -> s.and(consentSpecification.byTpp(tppAuthorisationNumber)))
                   .map(s -> s.and(byAccountReference(accountReference)))
                   .map(s -> s.and(byConsentType()))
                   .map(s -> s.and(commonSpecification.byInstanceId(instanceId)))
                   .orElse(null);
    }

    /**
     * Returns specification for ConsentEntity for filtering consents by Currency and Account Reference Selector.
     *
     * @param currency optional Currency
     * @param selector mandatory Account Reference Selector
     * @return resulting specification for ConsentEntity
     */
    public Specification<ConsentEntity> byCurrencyAndAccountReferenceSelector(@Nullable Currency currency, @NotNull AccountReferenceSelector selector) {
        return (root, query, cb) -> {
            Join<ConsentEntity, List<AspspAccountAccess>> aspspAccountAccessesJoin = root.join(ASPSP_ACCOUNT_ACCESSES_ATTRIBUTE);

            return Optional.of(Specification.where(provideSpecificationForJoinedEntityAttribute(aspspAccountAccessesJoin, ACCOUNT_ACCESS_ATTRIBUTE_ACCOUNT_IDENTIFIER, selector.getAccountValue())))
                       .map(s -> s.and(provideSpecificationForJoinedEntityAttribute(aspspAccountAccessesJoin, CURRENCY_ATTRIBUTE, currency)))
                       .map(s -> s.and(byConsentType()))
                       .map(s -> s.toPredicate(root, query, cb))
                       .orElse(null);
        };
    }

    /**
     * Returns specification for ConsentEntity for filtering consents by Account Reference Selector.
     *
     * @param selector mandatory Account Reference Selector
     * @return resulting specification for ConsentEntity
     */
    public Specification<ConsentEntity> byAccountReferenceSelector(@NotNull AccountReferenceSelector selector) {
        return (root, query, cb) -> {
            Join<ConsentEntity, AccountReferenceEntity> aspspAccountAccessesJoin = root.join(ASPSP_ACCOUNT_ACCESSES_ATTRIBUTE);

            return Optional.of(Specification.where(provideSpecificationForJoinedEntityAttribute(aspspAccountAccessesJoin, ACCOUNT_ACCESS_ATTRIBUTE_ACCOUNT_IDENTIFIER, selector.getAccountValue())))
                       .map(s -> s.and(byConsentType()))
                       .map(s -> s.toPredicate(root, query, cb))
                       .orElse(null);
        };
    }

    /**
     * Returns specification for ConsentEntity for filtering data by AccountReference.
     *
     * <p>
     * If optional parameter is not provided, this specification will not affect resulting data.
     *
     * @param accountReference PIIS Account Reference
     * @return resulting specification
     */
    private Specification<ConsentEntity> byAccountReference(@NotNull AccountReference accountReference) {
        return (root, query, cb) -> {
            AccountReferenceSelector selector = accountReference.getUsedAccountReferenceSelector();

            Join<ConsentEntity, List<AspspAccountAccess>> aspspAccountAccessJoin = root.join(ASPSP_ACCOUNT_ACCESSES_ATTRIBUTE);
            Specification<ConsentEntity> specifications = Specification
                                                              .where(provideSpecificationForJoinedEntityAttribute(aspspAccountAccessJoin, ACCOUNT_ACCESS_ATTRIBUTE_ACCOUNT_IDENTIFIER, selector.getAccountValue()));

            if (accountReference.getCurrency() != null) {
                specifications.and(provideSpecificationForJoinedEntityAttribute(aspspAccountAccessJoin, CURRENCY_ATTRIBUTE, accountReference.getCurrency()));
            }

            return specifications.toPredicate(root, query, cb);
        };
    }
}

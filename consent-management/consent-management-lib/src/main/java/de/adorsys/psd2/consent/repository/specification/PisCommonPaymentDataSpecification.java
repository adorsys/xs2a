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
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Join;
import java.time.LocalDate;
import java.util.Optional;

import static de.adorsys.psd2.consent.repository.specification.EntityAttribute.*;
import static de.adorsys.psd2.consent.repository.specification.EntityAttributeSpecificationProvider.provideSpecificationForEntityAttribute;
import static de.adorsys.psd2.consent.repository.specification.EntityAttributeSpecificationProvider.provideSpecificationForJoinedEntityAttribute;

@Service
@RequiredArgsConstructor
public class PisCommonPaymentDataSpecification {
    private final CommonSpecification<PisCommonPaymentData> commonSpecification;

    public Specification<PisCommonPaymentData> byPaymentId(String paymentId) {
        return Specification.where(provideSpecificationForEntityAttribute(PAYMENT_ID_ATTRIBUTE, paymentId));
    }

    public Specification<PisCommonPaymentData> byPaymentIdAndInstanceId(String paymentId, String instanceId) {
        return Optional.of(Specification.<PisCommonPaymentData>where(provideSpecificationForEntityAttribute(PAYMENT_ID_ATTRIBUTE, paymentId)))
                   .map(s -> s.and(provideSpecificationForEntityAttribute(INSTANCE_ID_ATTRIBUTE, instanceId)))
                   .orElse(null)
            ;
    }

    /**
     * Returns specification for PisCommonPaymentData entity for filtering payments by TPP authorisation number, creation date, PSU ID data and instance ID.
     *
     * @param tppAuthorisationNumber mandatory TPP authorisation number
     * @param createDateFrom         optional creation date that limits results to payments created after this date(inclusive)
     * @param createDateTo           optional creation date that limits results to payments created before this date(inclusive)
     * @param psuIdData              optional PSU ID data
     * @param instanceId             optional instance ID
     * @return resulting specification for PisCommonPaymentData entity
     */
    public Specification<PisCommonPaymentData> byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(@NotNull String tppAuthorisationNumber,
                                                                                                 @Nullable LocalDate createDateFrom,
                                                                                                 @Nullable LocalDate createDateTo,
                                                                                                 @Nullable PsuIdData psuIdData,
                                                                                                 @Nullable String instanceId) {
        return Optional.of(Specification.where(byTppAuthorisationNumber(tppAuthorisationNumber)))
                   .map(s -> s.and(commonSpecification.byCreationTimestamp(createDateFrom, createDateTo)))
                   .map(s -> s.and(commonSpecification.byPsuIdDataInList(psuIdData)))
                   .map(s -> s.and(commonSpecification.byInstanceId(instanceId)))
                   .orElse(null);
    }

    /**
     * Returns specification for PisCommonPaymentData entity for filtering payments by PSU ID Data, creation date and instance ID.
     *
     * @param psuIdData      mandatory PSU ID data
     * @param createDateFrom optional creation date that limits resulting data to payments created after this date(inclusive)
     * @param createDateTo   optional creation date that limits resulting data to payments created before this date(inclusive)
     * @param instanceId     optional instance ID
     * @return resulting specification for PisCommonPaymentData entity
     */
    public Specification<PisCommonPaymentData> byPsuIdDataAndCreationPeriodAndInstanceId(@NotNull PsuIdData psuIdData,
                                                                                         @Nullable LocalDate createDateFrom,
                                                                                         @Nullable LocalDate createDateTo,
                                                                                         @Nullable String instanceId) {
        return commonSpecification.byPsuIdDataAndCreationPeriodAndInstanceId(psuIdData, createDateFrom, createDateTo, instanceId);
    }

    /**
     * Returns specification for PisCommonPaymentData entity for filtering payments by aspsp account id, creation date and instance ID.
     *
     * @param aspspAccountId Bank specific account identifier
     * @param createDateFrom optional creation date that limits resulting data to payments created after this date(inclusive)
     * @param createDateTo   optional creation date that limits resulting data to payments created before this date(inclusive)
     * @param instanceId     optional instance ID
     * @return resulting specification for PisCommonPaymentData entity
     */
    public Specification<PisCommonPaymentData> byAspspAccountIdAndCreationPeriodAndInstanceId(@NotNull String aspspAccountId,
                                                                                              @Nullable LocalDate createDateFrom,
                                                                                              @Nullable LocalDate createDateTo,
                                                                                              @Nullable String instanceId) {
        return Optional.of(Specification.where(byAspspAccountId(aspspAccountId)))
                   .map(s -> s.and(commonSpecification.byCreationTimestamp(createDateFrom, createDateTo)))
                   .map(s -> s.and(commonSpecification.byInstanceId(instanceId)))
                   .orElse(null);
    }

    private Specification<PisCommonPaymentData> byAspspAccountId(@Nullable String aspspAccountId) {
        return provideSpecificationForEntityAttribute(ASPSP_ACCOUNT_ID_ATTRIBUTE, aspspAccountId);
    }

    /**
     * Returns specification for PisCommonPaymentData entity for filtering data by TPP authorisation number.
     *
     * <p>
     * If optional parameter is not provided, this specification will not affect resulting data.
     *
     * @param tppAuthorisationNumber optional TPP authorisation number
     * @return resulting specification
     */
    private Specification<PisCommonPaymentData> byTppAuthorisationNumber(@Nullable String tppAuthorisationNumber) {
        return (root, query, cb) -> {
            Join<PisCommonPaymentData, TppInfoEntity> tppInfoJoin = root.join(TPP_INFO_ATTRIBUTE);
            return provideSpecificationForJoinedEntityAttribute(tppInfoJoin, TPP_INFO_AUTHORISATION_NUMBER_ATTRIBUTE, tppAuthorisationNumber)
                       .toPredicate(root, query, cb);
        };
    }
}

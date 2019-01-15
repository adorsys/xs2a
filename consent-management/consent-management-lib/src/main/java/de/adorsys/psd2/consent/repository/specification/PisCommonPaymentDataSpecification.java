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

import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static de.adorsys.psd2.consent.repository.specification.EntityAttributeSpecificationProvider.provideSpecificationForEntityAttribute;

@Service
public class PisCommonPaymentDataSpecification extends GenericSpecification {
    public Specification<PisCommonPaymentData> byPaymentId(String paymentId) {
        return Specifications.where(provideSpecificationForEntityAttribute(EntityAttribute.PAYMENT_ID_ATTRIBUTE, paymentId));
    }

    public Specification<PisCommonPaymentData> byPaymentIdAndInstanceId(String paymentId, String instanceId) {
        return Specifications.<PisCommonPaymentData>where(provideSpecificationForEntityAttribute(EntityAttribute.PAYMENT_ID_ATTRIBUTE, paymentId))
                   .and(provideSpecificationForEntityAttribute(EntityAttribute.INSTANCE_ID_ATTRIBUTE, instanceId));
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
        return Specifications.<PisCommonPaymentData>where(byTppAuthorisationNumber(tppAuthorisationNumber))
                   .and(byCreationTimestamp(createDateFrom, createDateTo))
                   .and(byPsuIdData(psuIdData))
                   .and(byInstanceId(instanceId));
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
        return Specifications.<PisCommonPaymentData>where(byPsuIdData(psuIdData))
                   .and(byCreationTimestamp(createDateFrom, createDateTo))
                   .and(byInstanceId(instanceId));
    }

    /**
     * Returns specification for PisCommonPaymentData entity for filtering payments by aspsp account id, TPP authorisation number, creation date and instance ID.
     *
     * @param aspspAccountId         Bank specific account identifier
     * @param tppAuthorisationNumber mandatory TPP authorisation number
     * @param createDateFrom         optional creation date that limits resulting data to payments created after this date(inclusive)
     * @param createDateTo           optional creation date that limits resulting data to payments created before this date(inclusive)
     * @param instanceId             optional instance ID
     * @return resulting specification for PisCommonPaymentData entity
     */
    public Specification<PisCommonPaymentData> byAspspAccountIdAndTppIdAndCreationPeriodAndInstanceId(@NotNull String aspspAccountId,
                                                                                                      @NotNull String tppAuthorisationNumber,
                                                                                                      @Nullable LocalDate createDateFrom,
                                                                                                      @Nullable LocalDate createDateTo,
                                                                                                      @Nullable String instanceId) {
        return Specifications.<PisCommonPaymentData>where(byAspspAccountId(aspspAccountId))
                                                 .and(byTppAuthorisationNumber(tppAuthorisationNumber))
                                                 .and(byCreationTimestamp(createDateFrom, createDateTo))
                                                 .and(byInstanceId(instanceId));
    }
}

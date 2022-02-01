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

import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PisCommonPaymentDataRepository extends CrudRepository<PisCommonPaymentData, Long>, JpaSpecificationExecutor<PisCommonPaymentData> {
    Optional<PisCommonPaymentData> findByPaymentIdAndTransactionStatusIn(String paymentId, List<TransactionStatus> status);

    Optional<PisCommonPaymentData> findByPaymentId(String paymentId);

    /**
     * Gets payment list by payment ids. Uses in signing basket plugin (don't remove).
     *
     * @param externalIds external payment ids
     * @return list of payments
     */
    List<PisCommonPaymentData> findAllByPaymentIdIn(List<String> externalIds);

    Long countByTransactionStatusIn(Set<TransactionStatus> statuses);

    List<PisCommonPaymentData> findByTransactionStatusIn(Set<TransactionStatus> statuses, Pageable pageable);
}

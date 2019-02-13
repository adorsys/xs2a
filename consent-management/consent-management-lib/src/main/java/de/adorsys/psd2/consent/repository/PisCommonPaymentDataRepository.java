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

import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PisCommonPaymentDataRepository extends Xs2aCrudRepository<PisCommonPaymentData, Long>, JpaSpecificationExecutor<PisCommonPaymentData> {
    Optional<PisCommonPaymentData> findByPaymentIdAndTransactionStatusIn(String paymentId, List<TransactionStatus> status);// todo method should be changed to  findByPaymentIdAndTransactionStatus https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/534

    Optional<PisCommonPaymentData> findByPaymentId(String paymentId);

    List<PisCommonPaymentData> findByTransactionStatusIn(Set<TransactionStatus> statuses);
}

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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.specification.PisCommonPaymentDataSpecification;
import de.adorsys.psd2.xs2a.core.pis.InternalPaymentStatus;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonPaymentDataService {
    private final PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    private final PisCommonPaymentDataSpecification pisCommonPaymentDataSpecification;

    public Optional<PisCommonPaymentData> getPisCommonPaymentData(String paymentId, @Nullable String instanceId) {
        Specification<PisCommonPaymentData> specification = Optional.ofNullable(instanceId)
                                                                .map(i -> pisCommonPaymentDataSpecification.byPaymentIdAndInstanceId(paymentId, i))
                                                                .orElseGet(() -> pisCommonPaymentDataSpecification.byPaymentId(paymentId));

        return pisCommonPaymentDataRepository.findOne(specification);
    }

    @Transactional
    public boolean updateStatusInPaymentData(PisCommonPaymentData paymentData, TransactionStatus status) {
        paymentData.setTransactionStatus(status);
        if (status == TransactionStatus.PATC) {
            paymentData.setMultilevelScaRequired(true);
        }
        PisCommonPaymentData saved = pisCommonPaymentDataRepository.save(paymentData);
        return saved.getPaymentId() != null;
    }

    @Transactional
    public boolean updateInternalStatusInPaymentData(PisCommonPaymentData paymentData, InternalPaymentStatus status) {
        paymentData.setInternalPaymentStatus(status);
        PisCommonPaymentData saved = pisCommonPaymentDataRepository.save(paymentData);
        return saved.getPaymentId() != null;
    }

    @Transactional
    public boolean updateCancelTppRedirectURIs(PisCommonPaymentData paymentData, @NotNull TppRedirectUri tppRedirectUri) {
        paymentData.getAuthorisationTemplate().setCancelRedirectUri(tppRedirectUri.getUri());
        paymentData.getAuthorisationTemplate().setCancelNokRedirectUri(tppRedirectUri.getNokUri());
        PisCommonPaymentData saved = pisCommonPaymentDataRepository.save(paymentData);
        return saved.getPaymentId() != null;
    }

    @Transactional
    public boolean updatePaymentCancellationInternalRequestId(PisCommonPaymentData paymentData, @NotNull String internalRequestId) {
        paymentData.setCancellationInternalRequestId(internalRequestId);
        PisCommonPaymentData saved = pisCommonPaymentDataRepository.save(paymentData);
        return saved.getPaymentId() != null;
    }

    public boolean updatePaymentData(PisCommonPaymentData paymentData, byte[] payment) {
        paymentData.setPayment(payment);
        PisCommonPaymentData saved = pisCommonPaymentDataRepository.save(paymentData);
        return saved.getPaymentId() != null;
    }
}

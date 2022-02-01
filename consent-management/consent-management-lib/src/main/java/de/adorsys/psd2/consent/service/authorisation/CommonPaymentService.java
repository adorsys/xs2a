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

package de.adorsys.psd2.consent.service.authorisation;

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.consent.service.CorePaymentsConvertService;
import de.adorsys.psd2.consent.service.PisCommonPaymentConfirmationExpirationService;
import de.adorsys.psd2.consent.service.mapper.PisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CommonPaymentService {
    private final PisCommonPaymentMapper pisCommonPaymentMapper;
    private final PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    private final CorePaymentsConvertService corePaymentsConvertService;

    private final PisPaymentDataRepository pisPaymentDataRepository;
    private final PisCommonPaymentConfirmationExpirationService pisCommonPaymentConfirmationExpirationService;

    public PisCommonPaymentData save(PisCommonPaymentData pisCommonPaymentData) {
        return pisCommonPaymentDataRepository.save(pisCommonPaymentData);
    }

    public PisPayment mapToPisPayment(PisPaymentData pisPaymentData) {
        return pisCommonPaymentMapper.mapToPisPayment(pisPaymentData);
    }

    public byte[] buildPaymentData(List<PisPayment> pisPayments, PaymentType paymentType) {
        return corePaymentsConvertService.buildPaymentData(pisPayments, paymentType);
    }

    public Optional<List<PisPaymentData>> findByPaymentIdAndPaymentDataTransactionStatusIn(String parentId, List<TransactionStatus> transactionStatuses) {
        return pisPaymentDataRepository.findByPaymentIdAndPaymentDataTransactionStatusIn(parentId, transactionStatuses);
    }

    public PisCommonPaymentData checkAndUpdateOnConfirmationExpiration(PisCommonPaymentData pisCommonPaymentData) {
        return pisCommonPaymentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(pisCommonPaymentData);
    }

    public Optional<PisCommonPaymentData> findByPaymentIdAndTransactionStatusIn(String parentId, List<TransactionStatus> transactionStatuses) {
        return pisCommonPaymentDataRepository.findByPaymentIdAndTransactionStatusIn(parentId, transactionStatuses);
    }

    public Optional<PisCommonPaymentData> findOneByPaymentId(String parentId) {
        return pisCommonPaymentDataRepository.findByPaymentId(parentId);
    }

    public Optional<List<PisPaymentData>> findByPaymentId(String parentId) {
        return pisPaymentDataRepository.findByPaymentId(parentId);
    }
}

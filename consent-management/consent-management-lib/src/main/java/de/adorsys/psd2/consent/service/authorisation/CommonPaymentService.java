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

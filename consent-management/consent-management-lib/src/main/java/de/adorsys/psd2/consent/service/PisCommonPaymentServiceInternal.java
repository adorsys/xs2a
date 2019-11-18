/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentService;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.consent.repository.TppInfoRepository;
import de.adorsys.psd2.consent.service.mapper.PisCommonPaymentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PisCommonPaymentServiceInternal implements PisCommonPaymentService {
    private final PisCommonPaymentMapper pisCommonPaymentMapper;
    private final PsuDataMapper psuDataMapper;
    private final PisPaymentDataRepository pisPaymentDataRepository;
    private final PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    private final TppInfoRepository tppInfoRepository;
    private final PisCommonPaymentConfirmationExpirationService pisCommonPaymentConfirmationExpirationService;

    /**
     * Creates new pis common payment with full information about payment
     *
     * @param request Consists information about payments.
     * @return Response containing identifier of common payment
     */
    @Override
    @Transactional
    public Optional<CreatePisCommonPaymentResponse> createCommonPayment(PisPaymentInfo request) {
        PisCommonPaymentData commonPaymentData = pisCommonPaymentMapper.mapToPisCommonPaymentData(request);
        tppInfoRepository.findByAuthorisationNumber(request.getTppInfo().getAuthorisationNumber()).ifPresent(commonPaymentData::setTppInfo);

        PisCommonPaymentData saved = pisCommonPaymentDataRepository.save(commonPaymentData);

        if (saved.getId() == null) {
            log.info("Payment ID: [{}]. Pis common payment cannot be created, because when saving to DB got null PisCommonPaymentData ID",
                     request.getPaymentId());
            return Optional.empty();
        }

        return Optional.of(new CreatePisCommonPaymentResponse(saved.getPaymentId()));
    }

    /**
     * Retrieves common payment status from pis common payment by payment identifier
     *
     * @param paymentId String representation of pis payment identifier
     * @return Information about the status of a common payment
     */
    @Override
    @Transactional
    public Optional<TransactionStatus> getPisCommonPaymentStatusById(String paymentId) {
        return pisCommonPaymentDataRepository.findByPaymentId(paymentId)
                   .map(pisCommonPaymentConfirmationExpirationService::checkAndUpdatePaymentDataOnConfirmationExpiration)
                   .map(PisCommonPaymentData::getTransactionStatus);
    }

    /**
     * Reads full information of pis common payment by payment identifier
     *
     * @param paymentId String representation of pis payment identifier
     * @return Response containing full information about pis common payment
     */
    @Override
    @Transactional
    public Optional<PisCommonPaymentResponse> getCommonPaymentById(String paymentId) {
        return pisCommonPaymentDataRepository.findByPaymentId(paymentId)
                   .map(pisCommonPaymentConfirmationExpirationService::checkAndUpdatePaymentDataOnConfirmationExpiration)
                   .flatMap(pisCommonPaymentMapper::mapToPisCommonPaymentResponse);
    }

    /**
     * Updates pis common payment status by payment identifier
     *
     * @param paymentId String representation of pis payment identifier
     * @param status    new common payment status
     * @return Response containing result of status changing
     */
    @Override
    @Transactional
    public Optional<Boolean> updateCommonPaymentStatusById(String paymentId, TransactionStatus status) {
        return pisCommonPaymentDataRepository.findByPaymentId(paymentId)
                   .map(pisCommonPaymentConfirmationExpirationService::checkAndUpdatePaymentDataOnConfirmationExpiration)
                   .filter(pm -> !pm.getTransactionStatus().isFinalisedStatus())
                   .map(pmt -> setStatusAndSaveCommonPaymentData(pmt, status))
                   .map(con -> con.getTransactionStatus() == status);
    }

    /**
     * Update PIS common payment payment data and stores it into database
     *
     * @param request   PIS common payment request for update payment data
     * @param paymentId common payment ID
     */
    // TODO return correct error code in case payment was not found https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/408
    @Override
    @Transactional
    public void updateCommonPayment(PisCommonPaymentRequest request, String paymentId) {
        Optional<PisCommonPaymentData> pisCommonPaymentById = pisCommonPaymentDataRepository.findByPaymentId(paymentId);
        pisCommonPaymentById
            .ifPresent(commonPayment -> savePaymentData(commonPayment, request));
    }

    /**
     * Updates multilevelScaRequired and stores changes into database
     *
     * @param paymentId             Payment ID
     * @param multilevelScaRequired new value for boolean multilevel sca required
     */
    @Override
    @Transactional
    public boolean updateMultilevelSca(String paymentId, boolean multilevelScaRequired) {
        Optional<PisCommonPaymentData> pisCommonPaymentDataOptional = pisCommonPaymentDataRepository.findByPaymentId(paymentId);
        if (!pisCommonPaymentDataOptional.isPresent()) {
            log.info("Payment ID: [{}]. Update multilevel SCA required status failed, because payment is not found",
                     paymentId);
            return false;
        }
        PisCommonPaymentData payment = pisCommonPaymentDataOptional.get();
        payment.setMultilevelScaRequired(multilevelScaRequired);
        pisCommonPaymentDataRepository.save(payment);

        return true;
    }

    /**
     * Reads PSU data list by payment Id
     *
     * @param paymentId id of the payment
     * @return response contains data of Psu list
     */
    @Override
    public Optional<List<PsuIdData>> getPsuDataListByPaymentId(String paymentId) {

        return readPisCommonPaymentDataByPaymentId(paymentId)
                   .map(pc -> psuDataMapper.mapToPsuIdDataList(pc.getPsuDataList()));
    }

    private PisCommonPaymentData setStatusAndSaveCommonPaymentData(PisCommonPaymentData commonPaymentData, TransactionStatus status) {
        commonPaymentData.setTransactionStatus(status);
        return pisCommonPaymentDataRepository.save(commonPaymentData);
    }

    private Optional<PisCommonPaymentData> readPisCommonPaymentDataByPaymentId(String paymentId) {
        // todo implementation should be changed https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/534
        Optional<PisCommonPaymentData> commonPaymentData = pisPaymentDataRepository.findByPaymentId(paymentId)
                                                               .filter(CollectionUtils::isNotEmpty)
                                                               .map(list -> list.get(0).getPaymentData());
        if (!commonPaymentData.isPresent()) {
            commonPaymentData = pisCommonPaymentDataRepository.findByPaymentId(paymentId);
        }

        return commonPaymentData;
    }

    private void savePaymentData(PisCommonPaymentData pisCommonPayment, PisCommonPaymentRequest request) {
        boolean isCommonPayment = CollectionUtils.isEmpty(request.getPayments()) && request.getPaymentInfo() != null;
        // todo implementation should be changed  https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/534

        if (isCommonPayment) {
            pisCommonPaymentDataRepository.save(pisCommonPaymentMapper.mapToPisCommonPaymentData(request.getPaymentInfo()));
        } else {
            pisPaymentDataRepository.saveAll(pisCommonPaymentMapper.mapToPisPaymentDataList(request.getPayments(), pisCommonPayment));
        }
    }
}

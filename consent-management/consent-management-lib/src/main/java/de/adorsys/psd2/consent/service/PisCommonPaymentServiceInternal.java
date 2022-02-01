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

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentService;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.consent.repository.TppInfoRepository;
import de.adorsys.psd2.consent.service.mapper.PisCommonPaymentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.psd2.consent.api.CmsError.LOGICAL_ERROR;
import static de.adorsys.psd2.consent.api.CmsError.TECHNICAL_ERROR;

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
    private final CorePaymentsConvertService corePaymentsConvertService;
    private final AuthorisationRepository authorisationRepository;

    /**
     * Creates new pis common payment with full information about payment
     *
     * @param request Consists information about payments.
     * @return Response containing identifier of common payment
     */
    @Override
    @Transactional
    public CmsResponse<CreatePisCommonPaymentResponse> createCommonPayment(PisPaymentInfo request) {
        PisCommonPaymentData commonPaymentData = pisCommonPaymentMapper.mapToPisCommonPaymentData(request);
        tppInfoRepository.findByAuthorisationNumber(request.getTppInfo().getAuthorisationNumber())
            .ifPresent(commonPaymentData::setTppInfo);

        PisCommonPaymentData saved = pisCommonPaymentDataRepository.save(commonPaymentData);

        if (saved.getId() == null) {
            log.info("Payment ID: [{}]. Pis common payment cannot be created, because when saving to DB got null PisCommonPaymentData ID",
                     request.getPaymentId());
            return CmsResponse.<CreatePisCommonPaymentResponse>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return CmsResponse.<CreatePisCommonPaymentResponse>builder()
                   .payload(new CreatePisCommonPaymentResponse(saved.getPaymentId(), saved.getTppNotificationContentPreferred()))
                   .build();
    }

    /**
     * Retrieves common payment status from pis common payment by payment identifier
     *
     * @param paymentId String representation of pis payment identifier
     * @return Information about the status of a common payment
     */
    @Override
    @Transactional
    public CmsResponse<TransactionStatus> getPisCommonPaymentStatusById(String paymentId) {
        Optional<TransactionStatus> statusOptional = pisCommonPaymentDataRepository.findByPaymentId(paymentId)
                                                         .map(pisCommonPaymentConfirmationExpirationService::checkAndUpdateOnConfirmationExpiration)
                                                         .map(PisCommonPaymentData::getTransactionStatus);

        if (statusOptional.isPresent()) {
            return CmsResponse.<TransactionStatus>builder()
                       .payload(statusOptional.get())
                       .build();
        }

        log.info("Payment ID: [{}]. Get common payment status by ID failed, because payment was not found by the ID",
                 paymentId);
        return CmsResponse.<TransactionStatus>builder()
                   .error(LOGICAL_ERROR)
                   .build();
    }

    /**
     * Reads full information of pis common payment by payment identifier
     *
     * @param paymentId String representation of pis payment identifier
     * @return Response containing full information about pis common payment
     */
    @Override
    @Transactional
    public CmsResponse<PisCommonPaymentResponse> getCommonPaymentById(String paymentId) {
        Optional<PisCommonPaymentData> paymentOptional = pisCommonPaymentDataRepository.findByPaymentId(paymentId);
        if (paymentOptional.isPresent()) {
            List<AuthorisationEntity> authorisations =
                authorisationRepository.findAllByParentExternalIdAndTypeIn(paymentId, EnumSet.of(AuthorisationType.PIS_CREATION, AuthorisationType.PIS_CANCELLATION));
            Optional<PisCommonPaymentResponse> responseOptional = paymentOptional
                                                                      .map(pisCommonPaymentConfirmationExpirationService::checkAndUpdateOnConfirmationExpiration)
                                                                      .flatMap(p -> pisCommonPaymentMapper.mapToPisCommonPaymentResponse(p, authorisations));


            if (responseOptional.isPresent()) {
                PisCommonPaymentResponse pisCommonPaymentResponse = responseOptional.get();
                transferCorePaymentToCommonPayment(pisCommonPaymentResponse, paymentOptional.get());
                return CmsResponse.<PisCommonPaymentResponse>builder()
                           .payload(pisCommonPaymentResponse)
                           .build();
            }
        }

        log.info("Payment ID: [{}]. Get common payment by ID failed, because payment was not found by the ID",
                 paymentId);
        return CmsResponse.<PisCommonPaymentResponse>builder()
                   .error(LOGICAL_ERROR)
                   .build();
    }

    void transferCorePaymentToCommonPayment(PisCommonPaymentResponse pisCommonPaymentResponse, PisCommonPaymentData pisCommonPaymentData) {
        if (pisCommonPaymentData.getPayment() != null) {
            return;
        }

        List<PisPayment> pisPayments = pisCommonPaymentData.getPayments().stream()
                                           .map(pisCommonPaymentMapper::mapToPisPayment)
                                           .collect(Collectors.toList());
        byte[] paymentData = corePaymentsConvertService.buildPaymentData(pisPayments, pisCommonPaymentData.getPaymentType());
        if (paymentData != null) {
            pisCommonPaymentData.setPayment(paymentData);
            pisCommonPaymentResponse.setPaymentData(paymentData);
        }
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
    public CmsResponse<Boolean> updateCommonPaymentStatusById(String paymentId, TransactionStatus status) {
        Optional<Boolean> isUpdatedOptional = pisCommonPaymentDataRepository.findByPaymentId(paymentId)
                                                  .map(pisCommonPaymentConfirmationExpirationService::checkAndUpdateOnConfirmationExpiration)
                                                  .filter(pm -> !pm.getTransactionStatus().isFinalisedStatus())
                                                  .map(pmt -> setStatusAndSaveCommonPaymentData(pmt, status))
                                                  .map(con -> con.getTransactionStatus() == status);

        if (isUpdatedOptional.isPresent()) {
            return CmsResponse.<Boolean>builder()
                       .payload(isUpdatedOptional.get())
                       .build();
        }

        log.info("Payment ID: [{}]. Update common payment by ID failed, because payment was not found by the ID",
                 paymentId);
        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }

    /**
     * Updates multilevelScaRequired and stores changes into database
     *
     * @param paymentId             Payment ID
     * @param multilevelScaRequired new value for boolean multilevel sca required
     */
    @Override
    @Transactional
    public CmsResponse<Boolean> updateMultilevelSca(String paymentId, boolean multilevelScaRequired) {
        Optional<PisCommonPaymentData> pisCommonPaymentDataOptional = pisCommonPaymentDataRepository.findByPaymentId(paymentId);
        if (pisCommonPaymentDataOptional.isEmpty()) {
            log.info("Payment ID: [{}]. Update multilevel SCA required status failed, because payment is not found",
                     paymentId);
            return CmsResponse.<Boolean>builder()
                       .payload(false)
                       .build();
        }
        PisCommonPaymentData payment = pisCommonPaymentDataOptional.get();
        payment.setMultilevelScaRequired(multilevelScaRequired);

        return CmsResponse.<Boolean>builder()
                   .payload(true)
                   .build();
    }

    /**
     * Reads PSU data list by payment Id
     *
     * @param paymentId id of the payment
     * @return response contains data of Psu list
     */
    @Override
    public CmsResponse<List<PsuIdData>> getPsuDataListByPaymentId(String paymentId) {
        Optional<List<PsuIdData>> psuDataListOptional = readPisCommonPaymentDataByPaymentId(paymentId)
                                                            .map(pc -> psuDataMapper.mapToPsuIdDataList(pc.getPsuDataList()));

        if (psuDataListOptional.isPresent()) {
            return CmsResponse.<List<PsuIdData>>builder()
                       .payload(psuDataListOptional.get())
                       .build();
        }

        log.info("PaymentId ID: [{}]. Get PSU data list by payment ID failed, because payment is not found", paymentId);
        return CmsResponse.<List<PsuIdData>>builder()
                   .error(LOGICAL_ERROR)
                   .build();
    }

    private PisCommonPaymentData setStatusAndSaveCommonPaymentData(PisCommonPaymentData commonPaymentData, TransactionStatus status) {
        commonPaymentData.setTransactionStatus(status);
        return pisCommonPaymentDataRepository.save(commonPaymentData);
    }

    private Optional<PisCommonPaymentData> readPisCommonPaymentDataByPaymentId(String paymentId) {
        Optional<PisCommonPaymentData> commonPaymentData = pisPaymentDataRepository.findByPaymentId(paymentId)
                                                               .filter(CollectionUtils::isNotEmpty)
                                                               .map(list -> list.get(0).getPaymentData());
        if (commonPaymentData.isEmpty()) {
            commonPaymentData = pisCommonPaymentDataRepository.findByPaymentId(paymentId);
        }

        return commonPaymentData;
    }
}

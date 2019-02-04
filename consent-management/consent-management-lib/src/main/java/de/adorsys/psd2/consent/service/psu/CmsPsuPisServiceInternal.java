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

package de.adorsys.psd2.consent.service.psu;

import de.adorsys.psd2.consent.api.pis.CmsPayment;
import de.adorsys.psd2.consent.api.pis.CmsPaymentResponse;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentService;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.payment.PisAuthorization;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.psu.api.CmsPsuPisService;
import de.adorsys.psd2.consent.repository.PisAuthorisationRepository;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.consent.repository.PsuDataRepository;
import de.adorsys.psd2.consent.repository.specification.PisAuthorisationSpecification;
import de.adorsys.psd2.consent.repository.specification.PisPaymentDataSpecification;
import de.adorsys.psd2.consent.service.CommonPaymentDataService;
import de.adorsys.psd2.consent.service.mapper.CmsPsuPisMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CmsPsuPisServiceInternal implements CmsPsuPisService {
    private final PisPaymentDataRepository pisPaymentDataRepository;
    private final PisAuthorisationRepository pisAuthorisationRepository;
    private final CmsPsuPisMapper cmsPsuPisMapper;
    private final PisCommonPaymentService pisCommonPaymentService;
    private final CommonPaymentDataService commonPaymentDataService;
    private final PsuDataMapper psuDataMapper;
    private final PsuDataRepository psuDataRepository;
    private final PisAuthorisationSpecification pisAuthorisationSpecification;
    private final PisPaymentDataSpecification pisPaymentDataSpecification;

    @Override
    @Transactional
    public boolean updatePsuInPayment(@NotNull PsuIdData psuIdData, @NotNull String authorisationId, @NotNull String instanceId) {
        PisAuthorization authorisation =
            pisAuthorisationRepository.findOne(
                pisAuthorisationSpecification.byExternalIdAndInstanceId(authorisationId, instanceId)
            );
        return Optional.ofNullable(authorisation)
                   .map(PisAuthorization::getPsuData)
                   .map(psuData -> updatePsuData(psuData, psuIdData))
                   .orElse(false);
    }

    @Override
    public @NotNull Optional<CmsPayment> getPayment(@NotNull PsuIdData psuIdData, @NotNull String paymentId, @NotNull String instanceId) {
        if (isPsuDataEquals(paymentId, psuIdData)) {
            List<PisPaymentData> list = pisPaymentDataRepository.findAll(pisPaymentDataSpecification.byPaymentIdAndInstanceId(paymentId, instanceId));

            // todo implementation should be changed https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/534
            if (!list.isEmpty()) {
                return Optional.of(cmsPsuPisMapper.mapToCmsPayment(list));
            } else {
                return commonPaymentDataService.getPisCommonPaymentData(paymentId, instanceId)
                           .map(cmsPsuPisMapper::mapToCmsPayment);
            }
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    public @NotNull Optional<CmsPaymentResponse> checkRedirectAndGetPayment(@NotNull String redirectId, @NotNull String instanceId) {
        Optional<PisAuthorization> optionalAuthorisation = Optional.ofNullable(pisAuthorisationRepository.findOne(pisAuthorisationSpecification.byExternalIdAndInstanceId(redirectId, instanceId)))
                                                               .filter(a -> !a.getScaStatus().isFinalisedStatus());

        if (optionalAuthorisation.isPresent()) {
            PisAuthorization authorisation = optionalAuthorisation.get();

            if (authorisation.isNotExpired()) {
                return Optional.of(buildCmsPaymentResponse(authorisation));
            }

            changeAuthorisationStatusToFailed(authorisation);
            String tppNokRedirectUri = authorisation.getPaymentData().getTppInfo().getNokRedirectUri();

            return Optional.of(new CmsPaymentResponse(tppNokRedirectUri));
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    public @NotNull Optional<CmsPaymentResponse> checkRedirectAndGetPaymentForCancellation(@NotNull String redirectId, @NotNull String instanceId) {
        Optional<PisAuthorization> optionalAuthorisation = Optional.ofNullable(pisAuthorisationRepository.findOne(pisAuthorisationSpecification.byExternalIdAndInstanceId(redirectId, instanceId)))
                                                               .filter(a -> !a.getScaStatus().isFinalisedStatus());

        if (!optionalAuthorisation.isPresent()) {
            return Optional.empty();
        }

        PisAuthorization authorisation = optionalAuthorisation.get();

        if (authorisation.isExpired()) {
            changeAuthorisationStatusToFailed(authorisation);
            return Optional.ofNullable(authorisation.getPaymentData())
                       .map(PisCommonPaymentData::getTppInfo)
                       .map(TppInfoEntity::getNokRedirectUri)
                       .map(CmsPaymentResponse::new);
        }

        return Optional.of(buildCmsPaymentResponseForCancellation(authorisation));
    }

    @Override
    @Transactional
    public boolean updateAuthorisationStatus(@NotNull PsuIdData psuIdData, @NotNull String paymentId,
                                             @NotNull String authorisationId, @NotNull ScaStatus status, @NotNull String instanceId) {
        Optional<PisAuthorization> pisAuthorisation = Optional.ofNullable(pisAuthorisationRepository.findOne(pisAuthorisationSpecification.byExternalIdAndInstanceId(authorisationId, instanceId)));

        boolean isValid = pisAuthorisation
                              .map(auth -> auth.getPaymentData().getPaymentId())
                              .map(id -> validateGivenData(id, paymentId, psuIdData))
                              .orElse(false);

        return isValid && updateAuthorisationStatusAndSaveAuthorisation(pisAuthorisation.get(), status);
    }

    @Override
    @Transactional
    public boolean updatePaymentStatus(@NotNull String paymentId, @NotNull TransactionStatus status, @NotNull String instanceId) {
        List<PisPaymentData> list = pisPaymentDataRepository.findAll(pisPaymentDataSpecification.byPaymentIdAndInstanceId(paymentId, instanceId));

        // todo implementation should be changed https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/534
        if (!list.isEmpty()) {
            return updateStatusInPaymentDataList(list, status);
        } else {
            Optional<PisCommonPaymentData> paymentDataOptional = commonPaymentDataService.getPisCommonPaymentData(paymentId, instanceId);

            return paymentDataOptional.isPresent()
                       && commonPaymentDataService.updateStatusInPaymentData(paymentDataOptional.get(), status);
        }
    }

    private boolean updatePsuData(PsuData psuData, PsuIdData psuIdData) {
        PsuData newPsuData = psuDataMapper.mapToPsuData(psuIdData);
        if (newPsuData == null || StringUtils.isBlank(newPsuData.getPsuId())) {
            return false;
        }

        newPsuData.setId(psuData.getId());
        psuDataRepository.save(newPsuData);

        return true;
    }

    private boolean validateGivenData(String realPaymentId, String givenPaymentId, PsuIdData psuIdData) {
        return Optional.of(givenPaymentId)
                   .filter(p -> isPsuDataEquals(p, psuIdData))
                   .map(id -> StringUtils.equals(realPaymentId, id))
                   .orElse(false);
    }

    private boolean updateAuthorisationStatusAndSaveAuthorisation(PisAuthorization pisAuthorisation, ScaStatus status) {
        if (pisAuthorisation.getScaStatus().isFinalisedStatus()) {
            return false;
        }
        pisAuthorisation.setScaStatus(status);
        return Optional.ofNullable(pisAuthorisationRepository.save(pisAuthorisation))
                   .isPresent();
    }

    private boolean isPsuDataEquals(String paymentId, PsuIdData psuIdData) {
        return pisCommonPaymentService.getPsuDataListByPaymentId(paymentId)
                   .map(lst -> lst.stream()
                                   .anyMatch(psu -> psu.contentEquals(psuIdData)))
                   .orElse(false);
    }

    private boolean updateStatusInPaymentDataList(List<PisPaymentData> dataList, TransactionStatus givenStatus) {
        for (PisPaymentData pisPaymentData : dataList) {
            if (pisPaymentData.getTransactionStatus().isFinalisedStatus()) {
                return false;
            }
            pisPaymentData.setTransactionStatus(givenStatus);
            pisPaymentDataRepository.save(pisPaymentData);
        }
        return true;
    }

    private CmsPaymentResponse buildCmsPaymentResponse(PisAuthorization authorisation) {
        PisCommonPaymentData commonPayment = authorisation.getPaymentData();
        CmsPayment payment = cmsPsuPisMapper.mapToCmsPayment(commonPayment.getPayments());
        TppInfoEntity tppInfo = commonPayment.getTppInfo();

        String tppOkRedirectUri = tppInfo.getRedirectUri();
        String tppNokRedirectUri = tppInfo.getNokRedirectUri();

        return new CmsPaymentResponse(
            payment,
            authorisation.getExternalId(),
            tppOkRedirectUri,
            tppNokRedirectUri);
    }

    private CmsPaymentResponse buildCmsPaymentResponseForCancellation(PisAuthorization authorisation) {
        PisCommonPaymentData commonPayment = authorisation.getPaymentData();
        CmsPayment payment = cmsPsuPisMapper.mapToCmsPayment(commonPayment.getPayments());

        return new CmsPaymentResponse(
            payment,
            authorisation.getExternalId(),
            null,   // TODO temporary solution to keep the response the same as for payment confirmation (till the specification clarification) https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/588
            null);
    }

    private void changeAuthorisationStatusToFailed(PisAuthorization authorisation) {
        authorisation.setScaStatus(ScaStatus.FAILED);
        pisAuthorisationRepository.save(authorisation);
    }
}

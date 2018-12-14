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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.pis.CmsPayment;
import de.adorsys.psd2.consent.api.pis.CmsPaymentResponse;
import de.adorsys.psd2.consent.api.service.PisConsentService;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisConsent;
import de.adorsys.psd2.consent.domain.payment.PisConsentAuthorization;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.psu.api.CmsPsuPisService;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.PisConsentAuthorizationRepository;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.consent.repository.PsuDataRepository;
import de.adorsys.psd2.consent.service.mapper.CmsPsuPisMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
    private final PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    private final PisConsentAuthorizationRepository pisConsentAuthorizationRepository;
    private final CmsPsuPisMapper cmsPsuPisMapper;
    private final PisConsentService pisConsentService;
    private final CommonPaymentDataService commonPaymentDataService;
    private final PsuDataRepository psuDataRepository;
    private final PsuDataMapper psuDataMapper;

    @Override
    @Transactional
    public boolean updatePsuInPayment(@NotNull PsuIdData psuIdData, @NotNull String paymentId) {
        Optional<PisConsent> pisConsent = getPisConsentByPaymentId(paymentId);

        return pisConsent.isPresent() && updatePsuData(pisConsent.get(), psuIdData);
    }

    @Override
    public @NotNull Optional<CmsPayment> getPayment(@NotNull PsuIdData psuIdData, @NotNull String paymentId) {
        if (isPsuDataEquals(paymentId, psuIdData)) {
            Optional<List<PisPaymentData>> list = pisPaymentDataRepository.findByPaymentId(paymentId);

            // todo implementation should be changed https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/534
            if (list.isPresent()) {
                return list
                           .filter(CollectionUtils::isNotEmpty)
                           .map(cmsPsuPisMapper::mapToCmsPayment);
            } else {
                return commonPaymentDataService.getPisCommonPaymentData(paymentId)
                           .map(cmsPsuPisMapper::mapToCmsPayment);
            }
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    public @NotNull Optional<CmsPaymentResponse> checkRedirectAndGetPayment(@NotNull PsuIdData psuIdData, @NotNull String redirectId) {
        Optional<PisConsentAuthorization> optionalAuthorization = pisConsentAuthorizationRepository.findByExternalId(redirectId)
                                                                      .filter(a -> isAuthorisationValidForPsuAndStatus(psuIdData, a));
        if (optionalAuthorization.isPresent()) {
            PisConsentAuthorization authorization = optionalAuthorization.get();

            if (authorization.isNotExpired()) {
                return Optional.of(buildCmsPaymentResponse(authorization));
            }

            changeAuthorisationStatusToFailed(authorization);
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    public boolean updateAuthorisationStatus(@NotNull PsuIdData psuIdData, @NotNull String paymentId,
                                             @NotNull String authorisationId, @NotNull ScaStatus status) {

        Optional<PisConsentAuthorization> pisConsentAuthorisation = pisConsentAuthorizationRepository.findByExternalId(authorisationId);

        boolean isValid = pisConsentAuthorisation
                              .map(auth -> auth.getConsent().getPayments().get(0).getPaymentId())
                              .map(id -> validateGivenData(id, paymentId, psuIdData))
                              .orElse(false);

        return isValid && updateAuthorisationStatusAndSaveAuthorisation(pisConsentAuthorisation.get(), status);
    }

    @Override
    @Transactional
    public boolean updatePaymentStatus(@NotNull String paymentId, @NotNull TransactionStatus status) {
        Optional<List<PisPaymentData>> list = pisPaymentDataRepository.findByPaymentId(paymentId);

        // todo implementation should be changed https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/534
        if (list.isPresent()) {
            return updateStatusInPaymentDataList(list.get(), status);
        } else {
            Optional<PisCommonPaymentData> paymentDataOptional = commonPaymentDataService.getPisCommonPaymentData(paymentId);

            return paymentDataOptional.isPresent()
                       && commonPaymentDataService.updateStatusInPaymentData(paymentDataOptional.get(), status);
        }
    }

    private boolean updatePsuData(PisConsent pisConsent, PsuIdData psuIdData) {
        PsuData newPsuData = psuDataMapper.mapToPsuData(psuIdData);
        newPsuData.setId(pisConsent.getPsuData().getId());

        return Optional.ofNullable(psuDataRepository.save(newPsuData))
                   .isPresent();
    }

    private boolean validateGivenData(String paymentId, String givenPaymentId, PsuIdData psuIdData) {
        return Optional.of(givenPaymentId)
                   .filter(p -> isPsuDataEquals(givenPaymentId, psuIdData))
                   .map(id -> StringUtils.equals(paymentId, id))
                   .orElse(false);
    }

    private boolean updateAuthorisationStatusAndSaveAuthorisation(PisConsentAuthorization pisConsentAuthorisation, ScaStatus status) {
        if (pisConsentAuthorisation.getScaStatus().isFinalisedStatus()) {
            return false;
        }
        pisConsentAuthorisation.setScaStatus(status);
        return Optional.ofNullable(pisConsentAuthorizationRepository.save(pisConsentAuthorisation))
                   .isPresent();
    }

    private boolean isPsuDataEquals(String paymentId, PsuIdData psuIdData) {
        return pisConsentService.getPsuDataByPaymentId(paymentId)
                   .map(p -> p.contentEquals(psuIdData))
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

    private boolean isAuthorisationValidForPsuAndStatus(PsuIdData givenPsuIdData, PisConsentAuthorization authorization) {
        PsuIdData actualPsuIdData = psuDataMapper.mapToPsuIdData(authorization.getPsuData());
        return actualPsuIdData.contentEquals(givenPsuIdData) && !authorization.getScaStatus().isFinalisedStatus();
    }

    private CmsPaymentResponse buildCmsPaymentResponse(PisConsentAuthorization authorisation) {
        PisConsent consent = authorisation.getConsent();
        CmsPayment payment = cmsPsuPisMapper.mapToCmsPayment(consent.getPayments());
        TppInfoEntity tppInfo = consent.getTppInfo();

        String tppOkRedirectUri = tppInfo.getRedirectUri();
        String tppNokRedirectUri = tppInfo.getNokRedirectUri();

        return new CmsPaymentResponse(
            payment,
            authorisation.getExternalId(),
            tppOkRedirectUri,
            tppNokRedirectUri);
    }

    private void changeAuthorisationStatusToFailed(PisConsentAuthorization authorisation) {
        authorisation.setScaStatus(ScaStatus.FAILED);
        pisConsentAuthorizationRepository.save(authorisation);
    }

    private Optional<PisConsent> getPisConsentByPaymentId(String paymentId) {
        // todo implementation should be changed https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/534
        Optional<PisConsent> consentOpt = pisPaymentDataRepository.findByPaymentId(paymentId)
                                              .filter(CollectionUtils::isNotEmpty)
                                              .map(list -> list.get(0).getConsent());

        if (!consentOpt.isPresent()) {
            consentOpt = pisCommonPaymentDataRepository.findByPaymentId(paymentId)
                             .map(PisCommonPaymentData::getConsent);
        }

        return consentOpt;
    }
}

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
import de.adorsys.psd2.consent.api.service.PisConsentService;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.payment.PisConsent;
import de.adorsys.psd2.consent.domain.payment.PisConsentAuthorization;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.psu.api.CmsPsuPisService;
import de.adorsys.psd2.consent.repository.PisConsentAuthorizationRepository;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.consent.repository.PsuDataRepository;
import de.adorsys.psd2.consent.service.mapper.CmsPsuPisMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CmsPsuPisServiceInternal implements CmsPsuPisService {

    private final PisPaymentDataRepository pisPaymentDataRepository;
    private final PisConsentAuthorizationRepository pisConsentAuthorizationRepository;
    private final CmsPsuPisMapper cmsPsuPisMapper;
    private final PisConsentService pisConsentService;
    private final PsuDataRepository psuDataRepository;
    private final PsuDataMapper psuDataMapper;

    @Override
    @Transactional
    public boolean updatePsuInPayment(@NotNull PsuIdData psuIdData, @NotNull String encryptedPaymentId) {
        Optional<PisConsent> pisConsent = getPaymentDataList(encryptedPaymentId)
                                              .map(lst -> lst.get(0))
                                              .map(PisPaymentData::getConsent);
        return pisConsent.isPresent() && updatePsuData(pisConsent.get(), psuIdData);
    }

    @Override
    public @NotNull Optional<CmsPayment> getPayment(@NotNull PsuIdData psuIdData, @NotNull String encryptedPaymentId) {
        if (isPsuDataEquals(encryptedPaymentId, psuIdData)) {

            return getPaymentDataList(encryptedPaymentId)
                       .filter(CollectionUtils::isNotEmpty)
                       .map(cmsPsuPisMapper::mapToCmsPayment);
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    public boolean updateAuthorisationStatus(@NotNull PsuIdData psuIdData, @NotNull String paymentId,
                                             @NotNull String authorisationId, @NotNull ScaStatus status) {

        Optional<PisConsentAuthorization> pisConsentAuthorization = pisConsentAuthorizationRepository.findByExternalId(authorisationId);

        boolean isValid = pisConsentAuthorization
                              .map(auth -> auth.getConsent().getPayments().get(0).getPaymentId())
                              .map(id -> validateGivenData(id, paymentId, psuIdData))
                              .orElse(false);
        return isValid && updateAuthorisationStatusAndSaveAuthorization(pisConsentAuthorization.get(), status);
    }

    @Override
    @Transactional
    public boolean updatePaymentStatus(@NotNull String encryptedPaymentId, @NotNull TransactionStatus status) {
        List<PisPaymentData> list = getPaymentDataList(encryptedPaymentId)
                                        .orElse(Collections.emptyList());

        return !CollectionUtils.isEmpty(list)
                   && updateStatusInPaymentDataList(list, status);
    }

    private boolean updatePsuData(PisConsent pisConsent, PsuIdData psuIdData) {
        PsuData newPsuData = psuDataMapper.mapToPsuData(psuIdData);
        newPsuData.setId(pisConsent.getPsuData().getId());

        return Optional.ofNullable(psuDataRepository.save(newPsuData))
                   .isPresent();
    }

    private boolean validateGivenData(String paymentId, String givenPaymentId, PsuIdData psuIdData) {
        return pisConsentService.getDecryptedId(givenPaymentId)
                   .filter(p -> isPsuDataEquals(givenPaymentId, psuIdData))
                   .map(id -> StringUtils.equals(paymentId, id))
                   .orElse(false);
    }

    private boolean updateAuthorisationStatusAndSaveAuthorization(PisConsentAuthorization pisConsentAuthorization, ScaStatus status) {
        pisConsentAuthorization.setScaStatus(status);
        return Optional.ofNullable(pisConsentAuthorizationRepository.save(pisConsentAuthorization))
                   .isPresent();
    }

    private boolean isPsuDataEquals(String encryptedPaymentId, PsuIdData psuIdData) {
        return pisConsentService.getPsuDataByPaymentId(encryptedPaymentId)
                   .map(p -> p.contentEquals(psuIdData))
                   .orElse(false);
    }

    private boolean updateStatusInPaymentDataList(List<PisPaymentData> dataList, TransactionStatus status) {
        for (PisPaymentData pisPaymentData : dataList) {
            pisPaymentData.setTransactionStatus(status);
            if (pisPaymentDataRepository.save(pisPaymentData) == null) {
                return false;
            }
        }
        return true;
    }

    private Optional<List<PisPaymentData>> getPaymentDataList(String encryptedPaymentId) {
        return pisConsentService.getDecryptedId(encryptedPaymentId)
                   .flatMap(pisPaymentDataRepository::findByPaymentId);
    }
}

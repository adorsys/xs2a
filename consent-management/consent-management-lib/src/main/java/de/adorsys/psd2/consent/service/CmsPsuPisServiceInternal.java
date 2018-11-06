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
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CmsPsuPisServiceInternal implements CmsPsuPisService {

    private final PisPaymentDataRepository pisPaymentDataRepository;
    private final PisConsentAuthorizationRepository pisConsentAuthorizationRepository;
    private final CmsPsuPisMapper cmsPsuPisMapper;
    private final PisConsentService pisConsentService;
    private final PsuDataRepository psuDataRepository;
    private final PsuDataMapper psuDataMapper;

    @Override
    public boolean updatePsuInPayment(@NotNull PsuIdData psuIdData, @NotNull String paymentId) {
        return getDecryptedId(paymentId)
                   .map(p -> pisPaymentDataRepository.findByPaymentId(p)
                                 .map(this::getFirstPaymentFromList)
                                 .map(PisPaymentData::getConsent)
                                 .map(c -> updatePsuData(c, psuIdData))
                                 .orElse(false))
                   .orElse(false);
    }

    @Override
    public @NotNull Optional<CmsPayment> getPayment(@NotNull PsuIdData psuIdData, @NotNull String paymentId) {
        return getDecryptedId(paymentId)
                   .flatMap(p -> pisPaymentDataRepository.findByPaymentId(p)
                                     .filter(l -> isGivenPsuDataValid(paymentId, psuIdData))
                                     .flatMap(cmsPsuPisMapper::mapToCmsPayment));
    }

    @Override
    public boolean updateAuthorisationStatus(@NotNull PsuIdData psuIdData, @NotNull String paymentId,
                                             @NotNull String authorisationId, @NotNull ScaStatus status) {
        return pisConsentAuthorizationRepository.findByExternalId(authorisationId)
                   .filter(a -> validateGivenData(a, paymentId, psuIdData))
                   .map(a -> updateAuthorisationStatusAndSaveAuthorization(a, status))
                   .orElse(false);
    }

    @Override
    public boolean updatePaymentStatus(@NotNull String paymentId, @NotNull TransactionStatus status) {
        return getDecryptedId(paymentId)
                   .map(id -> pisPaymentDataRepository.findByPaymentId(id)
                                  .map(l -> l.stream()
                                                .map(p -> updateStatusInPayment(p, status))
                                                .collect(Collectors.toList()))
                                  .isPresent())
                   .orElse(false);
    }

    private boolean updatePsuData(PisConsent pisConsent, PsuIdData psuIdData) {
        PsuData newPsuData = psuDataMapper.mapToPsuData(psuIdData);
        newPsuData.setId(pisConsent.getPsuData().getId());

        return Optional.ofNullable(psuDataRepository.save(newPsuData))
                   .isPresent();
    }

    private boolean validateGivenData(PisConsentAuthorization pisConsentAuthorization, String paymentId, PsuIdData psuIdData) {
        List<PisPaymentData> pisPaymentDataList = pisConsentAuthorization.getConsent().getPayments();

        return getDecryptedId(paymentId)
                   .filter(p -> isGivenPsuDataValid(paymentId, psuIdData))
                   .map(id -> StringUtils.equals(getFirstPaymentFromList(pisPaymentDataList).getPaymentId(), id))
                   .orElse(false);
    }

    private boolean updateAuthorisationStatusAndSaveAuthorization(PisConsentAuthorization pisConsentAuthorization, ScaStatus status) {
        pisConsentAuthorization.setScaStatus(status);
        return Optional.ofNullable(pisConsentAuthorizationRepository.save(pisConsentAuthorization))
                   .isPresent();
    }

    private boolean isGivenPsuDataValid(String paymentId, PsuIdData psuIdData) {
        return pisConsentService.getPsuDataByPaymentId(paymentId)
                   .map(p -> comparePsuIdData(p, psuIdData))
                   .orElse(false);
    }

    private PisPaymentData updateStatusInPayment(PisPaymentData pisPaymentData, TransactionStatus status) {
        pisPaymentData.setTransactionStatus(status);
        return pisPaymentData;
    }

    //TODO It should be changed after BulkPayment will be added to the Database https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/446
    private PisPaymentData getFirstPaymentFromList(List<PisPaymentData> pisPaymentDataList) {
        return pisPaymentDataList.get(0);
    }

    private Optional<String> getDecryptedId(String paymentId) {
        return pisConsentService.getDecryptedId(paymentId);
    }

    private boolean comparePsuIdData(PsuIdData paymentPsuIdData, PsuIdData givenPsuIdData) {
        return StringUtils.equals(paymentPsuIdData.getPsuId(), givenPsuIdData.getPsuId())
                   && StringUtils.equals(paymentPsuIdData.getPsuCorporateId(), givenPsuIdData.getPsuCorporateId())
                   && StringUtils.equals(paymentPsuIdData.getPsuCorporateIdType(), givenPsuIdData.getPsuCorporateIdType())
                   && StringUtils.equals(paymentPsuIdData.getPsuIdType(), givenPsuIdData.getPsuIdType());
    }
}

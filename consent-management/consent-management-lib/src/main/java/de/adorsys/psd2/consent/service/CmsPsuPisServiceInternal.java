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
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.payment.PisConsentAuthorization;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.psu.api.CmsPsuPisService;
import de.adorsys.psd2.consent.repository.PisConsentAuthorizationRepository;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.consent.repository.PsuDataRepository;
import de.adorsys.psd2.consent.service.mapper.CmsPsuPisMapper;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CmsPsuPisServiceInternal implements CmsPsuPisService {

    private final PisPaymentDataRepository pisPaymentDataRepository;
    private final PisConsentAuthorizationRepository pisConsentAuthorizationRepository;
    private final CmsPsuPisMapper cmsPsuPisMapper;
    private final PisConsentServiceInternal pisConsentServiceInternal;
    private final PsuDataRepository psuDataRepository;

    @Override
    public boolean updatePsuInPayment(@NotNull PsuIdData psuIdData, @NotNull String paymentId) {
        return pisPaymentDataRepository.findByPaymentId(paymentId)
                   .map(p -> CollectionUtils.isNotEmpty(p) && updatePsuData(p, psuIdData))
                   .orElseGet(() -> Boolean.FALSE);
    }

    @Override
    public @NotNull Optional<CmsPayment> getPayment(@NotNull PsuIdData psuIdData, @NotNull String paymentId) {
        return pisPaymentDataRepository.findByPaymentId(paymentId)
                   .flatMap(p -> validatePsuData(p, psuIdData)
                                     ? cmsPsuPisMapper.mapToCmsPayment(p)
                                     : Optional.empty());
    }

    @Override
    public boolean updateAuthorisationStatus(@NotNull PsuIdData psuIdData, @NotNull String paymentId,
                                             @NotNull String authorisationId, @NotNull ScaStatus status) {
        return pisConsentAuthorizationRepository.findByExternalId(authorisationId)
                   .map(a -> validateGivenData(a, paymentId, psuIdData) && updateAuthorisationStatusAndSaveAuthorization(a, status))
                   .orElseGet(() -> Boolean.FALSE);
    }

    @Override
    public boolean updatePaymentStatus(@NotNull String paymentId, @NotNull TransactionStatus status) {
        return pisPaymentDataRepository.findByPaymentId(paymentId)
                   .map(list -> list.stream()
                                    .map(p -> updateStatusInPayment(p, status))
                                    .collect(Collectors.toList()))
                   .isPresent();
    }

    private boolean updatePsuData(List<PisPaymentData> pisPaymentDataList, PsuIdData psuIdData) {
        PsuData psuData = getPaymentFromList(pisPaymentDataList).getConsent().getPsuData();
        psuData.setPsuId(psuIdData.getPsuId());
        psuData.setPsuIdType(psuIdData.getPsuIdType());
        psuData.setPsuCorporateId(psuIdData.getPsuCorporateId());
        psuData.setPsuCorporateIdType(psuIdData.getPsuCorporateIdType());

        return Optional.ofNullable(psuDataRepository.save(psuData))
                   .isPresent();
    }

    private boolean validatePsuData(List<PisPaymentData> pisPaymentDataList, PsuIdData psuIdData) {
        return isGivenPsuDataValid(getPaymentFromList(pisPaymentDataList), psuIdData);
    }

    private boolean validateGivenData(PisConsentAuthorization pisConsentAuthorization, String paymentId, PsuIdData psuIdData) {
        List<PisPaymentData> pisPaymentDataList = pisConsentAuthorization.getConsent().getPayments();
        return StringUtils.equals(getPaymentFromList(pisPaymentDataList).getPaymentId(), paymentId)
                   && validatePsuData(pisPaymentDataList, psuIdData);
    }

    private boolean updateAuthorisationStatusAndSaveAuthorization(PisConsentAuthorization pisConsentAuthorization, ScaStatus status) {
        pisConsentAuthorization.setScaStatus(status);
        return Optional.ofNullable(pisConsentAuthorizationRepository.save(pisConsentAuthorization))
                   .isPresent();
    }

    private boolean isGivenPsuDataValid(PisPaymentData pisPaymentData, PsuIdData psuIdData) {
        return pisConsentServiceInternal.getPsuDataByPaymentId(pisPaymentData.getPaymentId())
                   .map(p -> comparePsuIdData(p, psuIdData))
                   .orElseGet(() -> Boolean.FALSE);
    }

    private boolean comparePsuIdData(PsuIdData paymentPsuIdData, PsuIdData givenPsuIdData) {
        return StringUtils.equals(paymentPsuIdData.getPsuId(), givenPsuIdData.getPsuId())
                   && StringUtils.equals(paymentPsuIdData.getPsuCorporateId(), givenPsuIdData.getPsuCorporateId())
                   && StringUtils.equals(paymentPsuIdData.getPsuCorporateIdType(), givenPsuIdData.getPsuCorporateIdType())
                   && StringUtils.equals(paymentPsuIdData.getPsuIdType(), givenPsuIdData.getPsuIdType());
    }

    private PisPaymentData updateStatusInPayment(PisPaymentData pisPaymentData, TransactionStatus status) {
        pisPaymentData.setTransactionStatus(status);
        return pisPaymentData;
    }

    //TODO It should be changed after BulkPayment will be added to the Database https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/446
    private PisPaymentData getPaymentFromList(List<PisPaymentData> pisPaymentDataList) {
        return pisPaymentDataList.get(0);
    }
}

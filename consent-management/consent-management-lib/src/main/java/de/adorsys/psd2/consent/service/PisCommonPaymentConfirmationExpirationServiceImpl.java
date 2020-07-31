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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PisCommonPaymentConfirmationExpirationServiceImpl implements PisCommonPaymentConfirmationExpirationService {
    private final PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    private final AuthorisationRepository authorisationRepository;
    private final AspspProfileService aspspProfileService;

    @Transactional
    @Override
    public List<PisCommonPaymentData> updatePaymentDataListOnConfirmationExpiration(List<PisCommonPaymentData> pisCommonPaymentDataList) {
        return IterableUtils.toList(pisCommonPaymentDataRepository.saveAll(obsoletePaymentDataList(pisCommonPaymentDataList)));
    }

    private void failAuthorisation(AuthorisationEntity authorisation) {
        authorisation.setScaStatus(ScaStatus.FAILED);
        authorisation.setRedirectUrlExpirationTimestamp(OffsetDateTime.now());
    }

    private List<PisCommonPaymentData> obsoletePaymentDataList(List<PisCommonPaymentData> pisCommonPaymentDataList) {
        return pisCommonPaymentDataList.stream()
                   .map(this::obsoletePaymentData)
                   .collect(Collectors.toList());
    }

    private PisCommonPaymentData obsoletePaymentData(PisCommonPaymentData pisCommonPaymentData) {
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.RJCT);
        String paymentId = pisCommonPaymentData.getExternalId();
        List<AuthorisationEntity> authorisations =
            authorisationRepository.findAllByParentExternalIdAndTypeIn(paymentId, EnumSet.of(AuthorisationType.PIS_CREATION, AuthorisationType.PIS_CANCELLATION));
        authorisations.forEach(auth -> auth.setScaStatus(ScaStatus.FAILED));
        authorisationRepository.saveAll(authorisations);
        return pisCommonPaymentData;
    }

    @Transactional
    @Override
    public PisCommonPaymentData checkAndUpdateOnConfirmationExpiration(PisCommonPaymentData pisCommonPaymentData) {
        if (isConfirmationExpired(pisCommonPaymentData)) {
            return updateOnConfirmationExpiration(pisCommonPaymentData);
        }

        return pisCommonPaymentData;
    }

    @Override
    public boolean isConfirmationExpired(PisCommonPaymentData pisCommonPaymentData) {
        if (pisCommonPaymentData == null) {
            return false;
        }
        long expirationPeriodMs = aspspProfileService.getAspspSettings(pisCommonPaymentData.getInstanceId()).getPis().getNotConfirmedPaymentExpirationTimeMs();
        return pisCommonPaymentData.isConfirmationExpired(expirationPeriodMs);
    }

    @Transactional
    @Override
    public PisCommonPaymentData updateOnConfirmationExpiration(PisCommonPaymentData pisCommonPaymentData) {
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.RJCT);
        String paymentId = pisCommonPaymentData.getExternalId();
        List<AuthorisationEntity> authorisations =
            authorisationRepository.findAllByParentExternalIdAndTypeIn(paymentId, EnumSet.of(AuthorisationType.PIS_CREATION, AuthorisationType.PIS_CANCELLATION));
        authorisations.forEach(this::failAuthorisation);
        authorisationRepository.saveAll(authorisations);
        return pisCommonPaymentDataRepository.save(pisCommonPaymentData);
    }
}

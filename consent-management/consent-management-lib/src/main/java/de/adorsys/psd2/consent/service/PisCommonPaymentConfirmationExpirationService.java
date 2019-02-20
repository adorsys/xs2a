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

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.domain.payment.PisAuthorization;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PisCommonPaymentConfirmationExpirationService {
    private final PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    private final AspspProfileService aspspProfileService;

    @Transactional
    public PisCommonPaymentData checkAndUpdatePaymentDataOnConfirmationExpiration(PisCommonPaymentData pisCommonPaymentData) {
        if (isPaymentDataOnConfirmationExpired(pisCommonPaymentData)) {
            return updatePaymentDataOnConfirmationExpiration(pisCommonPaymentData);
        }

        return pisCommonPaymentData;
    }

    public boolean isPaymentDataOnConfirmationExpired(PisCommonPaymentData pisCommonPaymentData) {
        long expirationPeriodMs = aspspProfileService.getAspspSettings().getNotConfirmedPaymentExpirationPeriodMs();
        return pisCommonPaymentData != null && pisCommonPaymentData.isConfirmationExpired(expirationPeriodMs);
    }

    @Transactional
    public PisCommonPaymentData updatePaymentDataOnConfirmationExpiration(PisCommonPaymentData pisCommonPaymentData) {
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.RJCT);
        pisCommonPaymentData.getAuthorizations().forEach(this::failAuthorisation);
        return pisCommonPaymentDataRepository.save(pisCommonPaymentData);
    }

    @Transactional
    public List<PisCommonPaymentData> updatePaymentDataListOnConfirmationExpiration(List<PisCommonPaymentData> pisCommonPaymentDataList) {
        return IterableUtils.toList(pisCommonPaymentDataRepository.save(obsoletePaymentDataList(pisCommonPaymentDataList)));
    }

    private void failAuthorisation(PisAuthorization authorisation) {
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
        pisCommonPaymentData.getAuthorizations().forEach(auth -> auth.setScaStatus(ScaStatus.FAILED));
        return pisCommonPaymentData;
    }
}

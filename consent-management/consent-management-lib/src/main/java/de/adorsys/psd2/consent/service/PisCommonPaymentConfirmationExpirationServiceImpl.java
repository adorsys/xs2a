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

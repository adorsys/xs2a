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

package de.adorsys.psd2.xs2a.service.validator.piis;

import de.adorsys.psd2.core.data.Consent;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.fund.PiisConsentValidationResult;
import de.adorsys.psd2.xs2a.service.TppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.PIIS_400;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.NO_PIIS_ACTIVATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class PiisConsentValidation {
    private final TppService tppService;

    public PiisConsentValidationResult validatePiisConsentData(List<PiisConsent> piisConsents) {
        if (CollectionUtils.isEmpty(piisConsents)) {
            return new PiisConsentValidationResult(ErrorHolder.builder(PIIS_400)
                                                       .tppMessages(TppMessageInformation.of(NO_PIIS_ACTIVATION))
                                                       .build());
        }

        Optional<PiisConsent> filteredPiisConsent = piisConsents.stream()
                                                        .filter(e -> EnumSet.of(ConsentStatus.VALID, ConsentStatus.RECEIVED).contains(e.getConsentStatus()))
                                                        .filter(this::isNotExpired)
                                                        .filter(this::filterByTpp)
                                                        .sorted(Comparator.comparing(PiisConsent::getCreationTimestamp, Comparator.nullsLast(Comparator.reverseOrder())))
                                                        .findAny();

        return filteredPiisConsent.map(PiisConsentValidationResult::new)
                   .orElseGet(() -> new PiisConsentValidationResult(ErrorHolder.builder(PIIS_400)
                                                                        .tppMessages(TppMessageInformation.of(CONSENT_UNKNOWN_400))
                                                                        .build()));

    }

    private boolean isNotExpired(PiisConsent consent) {
        return Optional.ofNullable(consent.getValidUntil())
                   .map(d -> d.compareTo(LocalDate.now()) >= 0)
                   .orElse(true);
    }

    private boolean filterByTpp(PiisConsent piisConsent) {
        return Optional.of(piisConsent)
                   .map(Consent::getTppInfo)
                   .map(tpp -> tpp.getAuthorisationNumber().equals(tppService.getTppInfo().getAuthorisationNumber()))
                   .orElse(true);
    }
}

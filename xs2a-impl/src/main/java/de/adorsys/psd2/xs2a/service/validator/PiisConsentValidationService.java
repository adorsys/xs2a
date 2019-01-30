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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.piis.PiisConsent;
import de.adorsys.psd2.xs2a.core.piis.PiisConsentTppAccessType;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.fund.PiisConsentValidationResult;
import de.adorsys.psd2.xs2a.service.TppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.RECEIVED;
import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.VALID;
import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PiisConsentValidationService {
    private final TppService tppService;

    public PiisConsentValidationResult validatePiisConsentData(List<PiisConsent> piisConsents) {
        if (CollectionUtils.isEmpty(piisConsents)) {
            return new PiisConsentValidationResult(ErrorHolder.builder(NO_PIIS_ACTIVATION).errorType(PIIS_400).build());
        }
        List<PiisConsent> filteredResponse = piisConsents.stream()
                                                 .filter(e -> EnumSet.of(VALID, RECEIVED).contains(e.getConsentStatus()))
                                                 .filter(e -> Optional.ofNullable(e.getExpireDate())
                                                                  .map(d -> d.compareTo(LocalDate.now()) >= 0)
                                                                  .orElse(true)
                                                 )
                                                 .filter(this::isTppValid)
                                                 .collect(Collectors.toList());

        if (filteredResponse.isEmpty()) {
            return new PiisConsentValidationResult(ErrorHolder.builder(CONSENT_INVALID).errorType(PIIS_401).build());
        }

        Optional<PiisConsent> validResponse = filteredResponse.stream()
                                                  .filter(e -> e.getAllowedFrequencyPerDay() > 0)
                                                  .findAny();

        return validResponse.map(PiisConsentValidationResult::new)
                   .orElseGet(() -> new PiisConsentValidationResult(ErrorHolder.builder(ACCESS_EXCEEDED).errorType(PIIS_429)
                                                                        .build()));
    }

    private boolean isTppValid(PiisConsent piisConsent) {
        switch (piisConsent.getTppAccessType()) {
            case ALL_TPP:
                return true;
            case SINGLE_TPP:
                TppInfo tppInfo = piisConsent.getTppInfo();
                if (tppInfo == null) {
                    return false;
                }

                String tppIdFromConsent = tppInfo.getAuthorisationNumber();
                String actualTppId = tppService.getTppId();
                return tppIdFromConsent.equals(actualTppId);
            default:
                PiisConsentTppAccessType accessType = piisConsent.getTppAccessType();
                log.error("Unknown TPP access type: {}", accessType);
                throw new IllegalArgumentException("Unknown TPP access type: " + accessType);
        }
    }
}

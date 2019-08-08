


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

package de.adorsys.psd2.xs2a.service.validator.piis;

import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.piis.PiisConsent;
import de.adorsys.psd2.xs2a.core.piis.PiisConsentTppAccessType;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.fund.PiisConsentValidationResult;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.tpp.PiisTppInfoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIIS_400;

@Slf4j
@Service
@RequiredArgsConstructor
public class PiisConsentValidation {
    private final RequestProviderService requestProviderService;
    private final PiisTppInfoValidator piisTppInfoValidator;

    public PiisConsentValidationResult validatePiisConsentData(List<PiisConsent> piisConsents) {
        if (CollectionUtils.isEmpty(piisConsents)) {
            return new PiisConsentValidationResult(ErrorHolder.builder(PIIS_400)
                                                       .tppMessages(TppMessageInformation.of(MessageErrorCode.NO_PIIS_ACTIVATION, ""))
                                                       .build());
        }
        Optional<PiisConsent> filteredPiisConsent = piisConsents.stream()
                                                        .filter(e -> EnumSet.of(ConsentStatus.VALID, ConsentStatus.RECEIVED).contains(e.getConsentStatus()))
                                                        .filter(e -> Optional.ofNullable(e.getExpireDate())
                                                                         .map(d -> d.compareTo(LocalDate.now()) >= 0)
                                                                         .orElse(true))
                                                        .sorted(Comparator.comparing(PiisConsent::getCreationTimestamp, Comparator.nullsLast(Comparator.reverseOrder())))
                                                        .findAny();

        ValidationResult validationResult = filteredPiisConsent
                                                .map(this::isTppValid)
                                                .orElseGet(() -> ValidationResult.invalid(PIIS_400, TppMessageInformation.of(CONSENT_UNKNOWN_400)));

        if (validationResult.isNotValid()) {
            return new PiisConsentValidationResult(buildErrorHolderFromMessageError(validationResult.getMessageError()));
        }

        return new PiisConsentValidationResult(filteredPiisConsent.get());
    }

    private ValidationResult isTppValid(PiisConsent piisConsent) {
        if (piisConsent.getTppAccessType() == PiisConsentTppAccessType.ALL_TPP) {
            return ValidationResult.valid();
        } else if (piisConsent.getTppAccessType() == PiisConsentTppAccessType.SINGLE_TPP) {
            return piisTppInfoValidator.validateTpp(piisConsent.getTppAuthorisationNumber(), piisConsent.getTppInfo());
        } else {
            PiisConsentTppAccessType accessType = piisConsent.getTppAccessType();

            log.error("InR-ID: [{}], X-Request-ID: [{}]. Unknown TPP access type: {}",
                      requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), accessType);
            return ValidationResult.invalid(PIIS_400, TppMessageInformation.of(CONSENT_UNKNOWN_400, String.format("Unknown TPP access type: %s", accessType)));
        }
    }

    private ErrorHolder buildErrorHolderFromMessageError(MessageError messageError) {
        return ErrorHolder.builder(messageError.getErrorType())
                   .tppMessages(messageError.getTppMessage())
                   .build();
    }
}

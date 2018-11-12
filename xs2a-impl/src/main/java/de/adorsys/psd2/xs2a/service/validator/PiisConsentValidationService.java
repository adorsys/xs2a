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

import de.adorsys.psd2.consent.api.piis.CmsPiisValidationInfo;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.TppService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.psd2.consent.api.piis.PiisConsentTppAccessType.ALL_TPP;
import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.RECEIVED;
import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.VALID;
import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.*;

@Service
@RequiredArgsConstructor
public class PiisConsentValidationService {
    private final TppService tppService;

    public ResponseObject<String> validatePiisConsentData(List<CmsPiisValidationInfo> cmsPiisValidationInfoList) {
        if (CollectionUtils.isEmpty(cmsPiisValidationInfoList)) {
            return ResponseObject.<String>builder()
                       .fail(new MessageError(NO_PIIS_ACTIVATION))
                       .build();
        }

        String tppId = tppService.getTppId();
        List<CmsPiisValidationInfo> filteredResponse = cmsPiisValidationInfoList.stream()
                                                           .filter(e -> EnumSet.of(VALID, RECEIVED).contains(e.getConsentStatus()))
                                                           .filter(e -> Optional.ofNullable(e.getExpireDate())
                                                                            .map(d -> d.compareTo(LocalDate.now()) >= 0)
                                                                            .orElse(true)
                                                           )
                                                           .filter(e -> e.getPiisConsentTppAccessType() == ALL_TPP || tppId.equals(e.getTppInfoId()))
                                                           .collect(Collectors.toList());

        if (filteredResponse.isEmpty()) {
            return ResponseObject.<String>builder()
                       .fail(new MessageError(CONSENT_INVALID))
                       .build();
        }

        List<CmsPiisValidationInfo> validResponse = filteredResponse.stream()
                                                        .filter(e -> e.getFrequencyPerDay() > 0)
                                                        .collect(Collectors.toList());

        if (validResponse.isEmpty()) {
            return ResponseObject.<String>builder()
                       .fail(new MessageError(ACCESS_EXCEEDED))
                       .build();
        }

        String consentId = validResponse.get(0).getConsentId();

        return ResponseObject.<String>builder()
                   .body(consentId)
                   .build();
    }
}

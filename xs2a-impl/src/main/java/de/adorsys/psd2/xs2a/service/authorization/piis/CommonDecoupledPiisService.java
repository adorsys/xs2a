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

package de.adorsys.psd2.xs2a.service.authorization.piis;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.spi.domain.piis.SpiPiisConsent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommonDecoupledPiisService {

    public UpdateConsentPsuDataResponse proceedDecoupledApproach(String consentId, String authorisationId, SpiPiisConsent spiPiisConsent, PsuIdData psuData) {
        return proceedDecoupledApproach(consentId, authorisationId, spiPiisConsent, null, psuData);
    }

    public UpdateConsentPsuDataResponse proceedDecoupledApproach(String consentId, String authorisationId,
                                                                 SpiPiisConsent spiPiisConsent,
                                                                 String authenticationMethodId,
                                                                 PsuIdData psuData) {
        //TODO realisation in https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/-/issues/953
        return null;
    }
}

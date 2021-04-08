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

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.domain.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.consent.Xs2aConsentService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * PiisAuthorizationService implementation to be used in case of redirect approach
 */
@Service
@Slf4j
public class RedirectPiisAuthorizationService extends RedirectConsentAuthorizationService implements PiisAuthorizationService {
    private final Xs2aPiisConsentService xs2aPiisConsentService;

    public RedirectPiisAuthorizationService(Xs2aAuthorisationService authorisationService, Xs2aConsentService consentService, Xs2aPiisConsentService xs2aPiisConsentService) {
        super(authorisationService, consentService);
        this.xs2aPiisConsentService = xs2aPiisConsentService;
    }

    @Override
    protected boolean isConsentAbsent(String consentId) {
        Optional<PiisConsent> piisConsentOptional = xs2aPiisConsentService.getPiisConsentById(consentId);
        if (piisConsentOptional.isEmpty()) {
            log.info("Consent-ID [{}]. Create consent authorisation has failed. Consent not found by id.", consentId);
            return true;
        }
        return false;
    }

    @Override
    public AuthorisationProcessorResponse updateConsentPsuData(UpdateAuthorisationRequest request, AuthorisationProcessorResponse response) {
        return null;
    }
}

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

import de.adorsys.psd2.consent.api.ConsentType;
import de.adorsys.psd2.consent.api.service.AisConsentService;
import de.adorsys.psd2.consent.api.service.ConsentService;
import de.adorsys.psd2.consent.api.service.PiisConsentService;
import de.adorsys.psd2.consent.api.service.PisConsentService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class ConsentServiceFactory {
    private final Map<ConsentType, ConsentService> services = new HashMap<>();

    public ConsentServiceFactory(AisConsentService aisConsentService, PisConsentService pisConsentService, PiisConsentService piisConsentService) {
        services.put(ConsentType.AIS, (ConsentService) aisConsentService);
        services.put(ConsentType.PIS, (ConsentService) pisConsentService);
        services.put(ConsentType.PIIS, (ConsentService) piisConsentService);
    }

    public ConsentService getConsentServiceByConsentType(ConsentType consentType) {
        return Optional.ofNullable(services.get(consentType))
                   .orElseThrow(() -> new UnsupportedOperationException("Unknown consent type"));
    }
}

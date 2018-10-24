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

package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.aspspmockserver.config.rest.consent.AisConsentRemoteUrls;
import de.adorsys.psd2.aspsp.mock.api.consent.AspspConsentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsentService {

    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final AisConsentRemoteUrls aisConsentRemoteUrls;

    //TODO Create GlobalExceptionHandler for error 400 from consentManagement https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/158
    /**
     * Updates status of AIS consent
     *
     * @param consentId Consent primary identifier
     * @param consentStatus New status of the AIS consent
     */
    public void updateAisConsentStatus(@NotNull String consentId, AspspConsentStatus consentStatus) {
        consentRestTemplate.put(aisConsentRemoteUrls.updateAisConsentStatus(), null, consentId, consentStatus.name());
    }
}

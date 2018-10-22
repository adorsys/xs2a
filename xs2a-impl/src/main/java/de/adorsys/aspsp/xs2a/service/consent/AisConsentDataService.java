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

package de.adorsys.aspsp.xs2a.service.consent;

import de.adorsys.aspsp.xs2a.config.rest.consent.AisConsentRemoteUrls;
import de.adorsys.aspsp.xs2a.domain.Xs2aConsentData;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AisConsentDataService {
    @Qualifier("consentRestTemplate")
    protected final RestTemplate consentRestTemplate;
    private final AisConsentRemoteUrls aisConsentRemoteUrls;
    private final Base64AspspDataService base64AspspDataService;

    public AspspConsentData getAspspConsentDataByConsentId(String consentId) {
        Xs2aConsentData xs2aConsentData = consentRestTemplate.getForEntity(aisConsentRemoteUrls.getAspspConsentData(), Xs2aConsentData.class, consentId).getBody();
        byte[] bytePayload = base64AspspDataService.decode(xs2aConsentData.getAspspConsentDataBase64());
        return new AspspConsentData(bytePayload, xs2aConsentData.getConsentId());
    }

    public void updateAspspConsentData(AspspConsentData consentData) {
        String base64Payload = base64AspspDataService.encode(consentData.getAspspConsentData());

        consentRestTemplate.put(aisConsentRemoteUrls.updateAspspConsentData(),
            new Xs2aConsentData(consentData.getConsentId(), base64Payload), consentData.getConsentId());
    }
}

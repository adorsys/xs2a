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
import de.adorsys.psd2.consent.api.CmsAspspConsentDataBase64;
import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
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
        CmsAspspConsentDataBase64 consentData = consentRestTemplate.getForEntity(aisConsentRemoteUrls.getAspspConsentData(), CmsAspspConsentDataBase64.class, consentId).getBody();
        byte[] bytePayload = base64AspspDataService.decode(consentData.getAspspConsentDataBase64());
        return new AspspConsentData(bytePayload, consentData.getConsentId());
    }

    public void updateAspspConsentData(AspspConsentData consentData) {
        String base64Payload = base64AspspDataService.encode(consentData.getAspspConsentData());

        consentRestTemplate.put(aisConsentRemoteUrls.updateAspspConsentData(),
                                new CmsAspspConsentDataBase64(consentData.getConsentId(), base64Payload), consentData.getConsentId());
    }

    public void updateAccountAccess(String consentId, AisAccountAccessInfo aisAccountAccessInfo) {
        consentRestTemplate.put(aisConsentRemoteUrls.updateAisAccountAccess(),
                                aisAccountAccessInfo, consentId);
    }
}

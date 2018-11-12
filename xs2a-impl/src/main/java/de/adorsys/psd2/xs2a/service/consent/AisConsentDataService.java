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

package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.CmsAspspConsentDataBase64;
import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.consent.api.service.AisConsentService;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AisConsentDataService {
    private final AisConsentService aisConsentService;
    private final Base64AspspDataService base64AspspDataService;

    public AspspConsentData getAspspConsentDataByConsentId(String consentId) {
        return aisConsentService.getAspspConsentData(consentId)
                   .map(this::mapToAspspConsentData)
                   .orElse(null);
    }

    public void updateAspspConsentData(AspspConsentData consentData) {
        String base64Payload = base64AspspDataService.encode(consentData.getAspspConsentData());
        aisConsentService.saveAspspConsentDataInAisConsent(consentData.getConsentId(), new CmsAspspConsentDataBase64(consentData.getConsentId(), base64Payload));
    }

    public void updateAccountAccess(String consentId, AisAccountAccessInfo aisAccountAccessInfo) {
        aisConsentService.updateAccountAccess(consentId, aisAccountAccessInfo);
    }

    private AspspConsentData mapToAspspConsentData(CmsAspspConsentDataBase64 consentData) {
        byte[] bytePayload = base64AspspDataService.decode(consentData.getAspspConsentDataBase64());
        return new AspspConsentData(bytePayload, consentData.getConsentId());
    }
}

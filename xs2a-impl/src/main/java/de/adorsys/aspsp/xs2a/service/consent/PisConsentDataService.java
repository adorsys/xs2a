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

import de.adorsys.psd2.consent.api.CmsAspspConsentDataBase64;
import de.adorsys.psd2.consent.api.service.PisConsentService;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PisConsentDataService {
    private final PisConsentService pisConsentService;
    private final Base64AspspDataService base64AspspDataService;

    // TODO check on null
    public AspspConsentData getAspspConsentDataByPaymentId(String paymentId) {
        CmsAspspConsentDataBase64 consentData = pisConsentService.getAspspConsentDataByPaymentId(paymentId).orElse(null);

        byte[] bytePayload = base64AspspDataService.decode(consentData.getAspspConsentDataBase64());
        return new AspspConsentData(bytePayload, consentData.getConsentId());
    }

    // TODO check on null
    public AspspConsentData getAspspConsentDataByConsentId(String consentId) {
        CmsAspspConsentDataBase64 consentData = pisConsentService.getAspspConsentDataByConsentId(consentId).orElse(null);

        byte[] bytePayload = base64AspspDataService.decode(consentData.getAspspConsentDataBase64());
        return new AspspConsentData(bytePayload, consentData.getConsentId());
    }

    public void updateAspspConsentData(AspspConsentData consentData) {
        String base64Payload = base64AspspDataService.encode(consentData.getAspspConsentData());
        pisConsentService.updateAspspConsentDataInPisConsent(consentData.getConsentId(), new CmsAspspConsentDataBase64(consentData.getConsentId(), base64Payload));
    }

    public String getInternalPaymentIdByEncryptedString(String encryptedId) {
        return consentRestTemplate.getForEntity(pisConsentRemoteUrls.getPaymentIdByEncryptedString(), String.class, encryptedId).getBody();
    }
}

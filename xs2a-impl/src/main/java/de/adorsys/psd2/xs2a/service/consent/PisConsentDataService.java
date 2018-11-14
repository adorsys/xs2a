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
import de.adorsys.psd2.consent.api.ConsentType;
import de.adorsys.psd2.consent.api.service.CommonConsentService;
import de.adorsys.psd2.consent.api.service.PisConsentService;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PisConsentDataService {
    private final PisConsentService pisConsentService;
    private final Base64AspspDataService base64AspspDataService;
    private final CommonConsentService commonConsentService;

    public AspspConsentData getAspspConsentDataByPaymentId(String paymentId) {
        return commonConsentService.getAspspConsentDataByPaymentId(paymentId)
                   .map(this::mapToAspspConsentData)
                   .orElseGet(() -> new AspspConsentData(null, paymentId));
    }

    public AspspConsentData getAspspConsentDataByConsentId(String consentId) {
        return commonConsentService.getAspspConsentDataByConsentId(consentId, ConsentType.PIS)
                   .map(this::mapToAspspConsentData)
                   .orElseGet(() -> new AspspConsentData(null, consentId));
    }

    public void updateAspspConsentData(AspspConsentData consentData) {
        String base64Payload = base64AspspDataService.encode(consentData.getAspspConsentData());
        commonConsentService.saveAspspConsentData(consentData.getConsentId(), new CmsAspspConsentDataBase64(consentData.getConsentId(), base64Payload), ConsentType.PIS);
    }

    public String getInternalPaymentIdByEncryptedString(String encryptedId) {
        return pisConsentService.getDecryptedId(encryptedId).orElse(null);
    }

    private AspspConsentData mapToAspspConsentData(CmsAspspConsentDataBase64 consentData) {
        byte[] bytePayload = base64AspspDataService.decode(consentData.getAspspConsentDataBase64());
        return new AspspConsentData(bytePayload, consentData.getConsentId());
    }
}

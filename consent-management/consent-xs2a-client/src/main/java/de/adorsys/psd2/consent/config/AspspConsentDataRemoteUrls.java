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

package de.adorsys.psd2.consent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AspspConsentDataRemoteUrls {
    @Value("${consent-service.baseurl:http://localhost:38080/api/v1}")
    private String consentServiceBaseUrl;

    /**
     * Returns URL-string to CMS endpoint that gets aspsp consent data by consent ID
     *
     * @return String
     */
    public String getAspspConsentDataByConsentId() {
        return consentServiceBaseUrl + "/aspsp-consent-data/consent/{consent-id}";
    }

    /**
     * Returns URL-string to CMS endpoint that gets aspsp consent data by payment ID
     *
     * @return String
     */
    public String getAspspConsentDataByPaymentId() {
        return consentServiceBaseUrl + "/aspsp-consent-data/payment/{payment-id}";
    }

    /**
     * Returns URL-string to CMS endpoint that updates aspsp consent data by consent ID
     *
     * @return String
     */
    public String updateAspspConsentData() {
        return consentServiceBaseUrl + "/aspsp-consent-data/consent/{consent-id}";
    }

    /**
     * Returns URL-string to CMS endpoint that delete aspsp consent data by consent ID
     *
     * @return String
     */
    public String deleteAspspConsentData() {
        return consentServiceBaseUrl + "/aspsp-consent-data/consent/{consent-id}";
    }
}

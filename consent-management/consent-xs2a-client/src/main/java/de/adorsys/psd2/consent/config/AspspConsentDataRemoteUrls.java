/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.consent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AspspConsentDataRemoteUrls {
    private static final String ASPSP_CONSENT_DATA_ENDPOINT = "/aspsp-consent-data/consents/{consent-id}";

    @Value("${xs2a.cms.consent-service.baseurl:http://localhost:38080/api/v1}")
    private String consentServiceBaseUrl;

    /**
     * Returns URL-string to CMS endpoint that gets aspsp consent data by consent ID / payment ID
     *
     * @return String
     */
    public String getAspspConsentData() {
        return consentServiceBaseUrl + ASPSP_CONSENT_DATA_ENDPOINT;
    }

    /**
     * Returns URL-string to CMS endpoint that updates aspsp consent data by consent ID / payment ID
     *
     * @return String
     */
    public String updateAspspConsentData() {
        return consentServiceBaseUrl + ASPSP_CONSENT_DATA_ENDPOINT;
    }

    /**
     * Returns URL-string to CMS endpoint that delete aspsp consent data by consent ID / payment ID
     *
     * @return String
     */
    public String deleteAspspConsentData() {
        return consentServiceBaseUrl + ASPSP_CONSENT_DATA_ENDPOINT;
    }
}

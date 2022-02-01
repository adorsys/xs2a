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
public class ConsentRemoteUrls {
    @Value("${xs2a.cms.consent-service.baseurl:http://localhost:38080/api/v1}")
    private String consentServiceBaseUrl;

    public String createConsent() {
        return consentServiceBaseUrl + "/consent/";
    }

    public String getConsentStatusById() {
        return consentServiceBaseUrl + "/consent/{encrypted-consent-id}/status";
    }

    public String getConsentById() {
        return consentServiceBaseUrl + "/consent/{encrypted-consent-id}";
    }

    public String updateConsentStatusById() {
        return consentServiceBaseUrl + "/consent/{encrypted-consent-id}/status/{status}";
    }

    public String findAndTerminateOldConsentsByNewConsentId() {
        return consentServiceBaseUrl + "/consent/{encrypted-consent-id}/old-consents";
    }

    public String findAndTerminateOldConsents() {
        return consentServiceBaseUrl + "/consent/{encrypted-consent-id}/old-consents";
    }

    public String getPsuDataByConsentId() {
        return consentServiceBaseUrl + "/consent/{encrypted-consent-id}/psu-data";
    }

    public String updateMultilevelScaRequired() {
        return consentServiceBaseUrl + "/consent/{encrypted-consent-id}/multilevel-sca?multilevel-sca={multilevel-sca}";
    }
}

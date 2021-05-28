/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

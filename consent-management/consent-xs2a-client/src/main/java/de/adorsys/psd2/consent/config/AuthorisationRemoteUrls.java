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
public class AuthorisationRemoteUrls {
    @Value("${xs2a.cms.consent-service.baseurl:http://localhost:38080/api/v1}")
    private String consentServiceBaseUrl;

    public String createAuthorisation() {
        return consentServiceBaseUrl + "/{authorisation-type}/{parent-id}/authorisations";
    }

    public String getAuthorisationById() {
        return consentServiceBaseUrl + "/authorisations/{authorisation-id}";
    }

    public String updateAuthorisation() {
        return consentServiceBaseUrl + "/authorisations/{authorisation-id}";
    }

    public String getAuthorisationsByParentId() {
        return consentServiceBaseUrl + "/{authorisation-type}/{parent-id}/authorisations";
    }

    public String getAuthorisationScaStatus() {
        return consentServiceBaseUrl + "/{authorisation-type}/{parent-id}/authorisations/{authorisation-id}/status";
    }

    public String updateAuthorisationStatus() {
        return consentServiceBaseUrl + "/authorisations/{authorisation-id}/status/{status}";
    }

    public String isAuthenticationMethodDecoupled() {
        return consentServiceBaseUrl + "/authorisations/{authorisation-id}/authentication-methods/{authentication-method-id}";
    }

    public String saveAuthenticationMethods() {
        return consentServiceBaseUrl + "/authorisations/{authorisation-id}/authentication-methods";
    }

    public String updateScaApproach() {
        return consentServiceBaseUrl + "/authorisations/{authorisation-id}/sca-approach/{sca-approach}";
    }

    public String getAuthorisationScaApproach() {
        return consentServiceBaseUrl + "/authorisations/{authorisation-id}/sca-approach";
    }
}

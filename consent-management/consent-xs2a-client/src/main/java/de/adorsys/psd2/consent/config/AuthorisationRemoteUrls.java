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

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

package de.adorsys.aspsp.xs2a.domain.consent;

import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.consent.api.CmsScaMethod;
import lombok.Data;

import java.util.List;

@Data
public class UpdateConsentPsuDataResponse {

    private String psuId;
    private String consentId;
    private String authorizationId;

    private Xs2aScaStatus scaStatus;
    private List<CmsScaMethod> availableScaMethods;
    private String chosenScaMethod;
    private String authenticationMethodId;
    private String scaAuthenticationData;
    private String password;
    private Links links;

    private ConsentAuthorizationResponseLinkType responseLinkType;
    private String psuMessage;

    private MessageErrorCode errorCode;

    public boolean hasError() {
        return errorCode != null;
    }
}

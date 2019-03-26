/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.integration.builder.ais;

import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;

public class AisConsentAuthorizationResponseBuilder {
    private static final String ENCRYPT_CONSENT_ID = "DfLtDOgo1tTK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";
    private static final String AUTHORISATION_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";

    public static AisConsentAuthorizationResponse buildAisConsentAuthorizationResponse(ScaApproach scaApproach) {
        AisConsentAuthorizationResponse aisConsentAuthorizationResponse = new AisConsentAuthorizationResponse();
        aisConsentAuthorizationResponse.setAuthorizationId(AUTHORISATION_ID);
        aisConsentAuthorizationResponse.setConsentId(ENCRYPT_CONSENT_ID);
        aisConsentAuthorizationResponse.setScaStatus(ScaStatus.RECEIVED);
        aisConsentAuthorizationResponse.setChosenScaApproach(scaApproach);
        return aisConsentAuthorizationResponse;
    }
}

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

package de.adorsys.psd2.xs2a.domain.consent;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class CreateConsentAuthorizationResponse implements AuthorisationResponse {
    private String consentId;
    private String authorisationId;
    private ScaStatus scaStatus;
    private String psuMessage;
    private Links links;
    private String internalRequestId;
    private PsuIdData psuIdData;
    private ScaApproach scaApproach;
    private final Set<TppMessageInformation> tppMessageInformation = new HashSet<>();
}

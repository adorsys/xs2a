/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

// Class can't be immutable, because it it used in aspect (links setting)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CreateConsentAuthorisationProcessorResponse extends AuthorisationProcessorResponse {
    private final ScaApproach scaApproach;
    private final Set<TppMessageInformation> tppMessages;

    public CreateConsentAuthorisationProcessorResponse(ErrorHolder errorHolder, ScaApproach scaApproach, String consentId, PsuIdData psuIdData) {
        this(ScaStatus.FAILED, scaApproach, null, null, consentId, psuIdData);
        this.errorHolder = errorHolder;
    }


    public CreateConsentAuthorisationProcessorResponse(ScaStatus scaStatus, ScaApproach scaApproach, String psuMessage, Set<TppMessageInformation> tppMessages, String consentId, PsuIdData psuIdData) {
        this.scaStatus = scaStatus;
        this.scaApproach = scaApproach;
        this.psuMessage = psuMessage;
        this.tppMessages = tppMessages;
        this.consentId = consentId;
        this.psuData = psuIdData;
    }
}

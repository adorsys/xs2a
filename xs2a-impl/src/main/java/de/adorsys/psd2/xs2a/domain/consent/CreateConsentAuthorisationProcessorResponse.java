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

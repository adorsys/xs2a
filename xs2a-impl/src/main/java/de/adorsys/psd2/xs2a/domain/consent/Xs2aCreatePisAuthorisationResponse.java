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

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
public class Xs2aCreatePisAuthorisationResponse implements AuthorisationResponse {
    private String authorisationId;
    private ScaStatus scaStatus;
    private PaymentType paymentType;
    private Links links = new Links();
    private String internalRequestId;
    private PsuIdData psuIdData;
    private String psuMessage;
    private ScaApproach scaApproach;
    private final Set<TppMessageInformation> tppMessageInformation = new HashSet<>();

    public Xs2aCreatePisAuthorisationResponse(String authorisationId, ScaStatus scaStatus, PaymentType paymentType,
                                              String internalRequestId, PsuIdData psuIdData) {
        this.authorisationId = authorisationId;
        this.scaStatus = scaStatus;
        this.paymentType = paymentType;
        this.internalRequestId = internalRequestId;
        this.psuIdData = psuIdData;
    }
}

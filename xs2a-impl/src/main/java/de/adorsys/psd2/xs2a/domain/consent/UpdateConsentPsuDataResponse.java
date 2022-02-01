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
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

// Class can't be immutable, because it it used in aspect (links setting)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UpdateConsentPsuDataResponse extends AuthorisationProcessorResponse {

    public UpdateConsentPsuDataResponse(ScaStatus scaStatus, ErrorHolder errorHolder, String consentId, String authorisationId, PsuIdData psuIdData) {
        this(scaStatus, consentId, authorisationId, psuIdData);
        this.errorHolder = errorHolder;
    }

    public UpdateConsentPsuDataResponse(ErrorHolder errorHolder, String consentId, String authorisationId, PsuIdData psuIdData) {
        this(ScaStatus.FAILED, consentId, authorisationId, psuIdData);
        this.errorHolder = errorHolder;
    }

    public UpdateConsentPsuDataResponse(ScaStatus scaStatus, String consentId, String authorisationId, PsuIdData psuIdData) {
        this.scaStatus = scaStatus;
        this.consentId = consentId;
        this.authorisationId = authorisationId;
        this.psuData = psuIdData;
    }
}

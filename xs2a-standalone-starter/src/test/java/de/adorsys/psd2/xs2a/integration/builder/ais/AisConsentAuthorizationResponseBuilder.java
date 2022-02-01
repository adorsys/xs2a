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

package de.adorsys.psd2.xs2a.integration.builder.ais;

import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;

public class AisConsentAuthorizationResponseBuilder {
    private static final String ENCRYPT_CONSENT_ID = "DfLtDOgo1tTK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";
    private static final String AUTHORISATION_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";

    public static Authorisation buildAisConsentAuthorizationResponse(ScaApproach scaApproach) {
        Authorisation authorisationResponse = new Authorisation();
        authorisationResponse.setAuthorisationId(AUTHORISATION_ID);
        authorisationResponse.setParentId(ENCRYPT_CONSENT_ID);
        authorisationResponse.setScaStatus(ScaStatus.RECEIVED);
        authorisationResponse.setChosenScaApproach(scaApproach);
        return authorisationResponse;
    }

    public static Authorisation buildAisConsentAuthorizationResponse(ScaApproach scaApproach, PsuIdData psuIdData) {
        Authorisation authorisationResponse = buildAisConsentAuthorizationResponse(scaApproach);
        authorisationResponse.setPsuIdData(psuIdData);
        return authorisationResponse;
    }
}

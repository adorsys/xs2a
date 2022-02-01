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

package de.adorsys.psd2.consent.psu.api;

import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class CmsPsuConfirmationOfFundsAuthorisation {
    private PsuIdData psuIdData;
    private String piisConsentId;
    private String authorisationId;
    private ScaStatus scaStatus;
    private ScaApproach scaApproach;
    private AuthorisationType type;
    private OffsetDateTime redirectUrlExpirationTimestamp;
    private OffsetDateTime authorisationExpirationTimestamp;
    private String tppOkRedirectUri;
    private String tppNokRedirectUri;
}

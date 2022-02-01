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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.Links;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

@Data
public class Xs2aConfirmationOfFundsResponse {
    private final String consentStatus;
    private final String consentId;
    private final boolean multilevelScaRequired;
    @JsonIgnore
    private final String internalRequestId;
    @JsonIgnore
    private String authorizationId;
    @JsonProperty("_links")
    private Links links = new Links();
    @Nullable
    private String psuMessage;
    @Nullable
    private ScaStatus scaStatus;
    @Nullable
    private ScaApproach scaApproach;
    private final Set<TppMessageInformation> tppMessageInformation = new HashSet<>();
}

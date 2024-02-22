/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.spi.domain.authorisation;

import de.adorsys.psd2.xs2a.spi.domain.sca.SpiChallengeData;
import de.adorsys.psd2.xs2a.spi.domain.sca.SpiScaStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SpiAuthorizationCodeResult extends SpiWithExemptionResponse {
    private SpiChallengeData challengeData;
    private SpiAuthenticationObject selectedScaMethod;
    private SpiScaStatus scaStatus;

    public SpiAuthorizationCodeResult(boolean scaExempted, SpiChallengeData challengeData,
                                      SpiAuthenticationObject selectedScaMethod, SpiScaStatus scaStatus) {
        super(scaExempted);
        this.challengeData = challengeData;
        this.selectedScaMethod = selectedScaMethod;
        this.scaStatus = scaStatus;
    }

    public boolean isEmpty() {
        return (challengeData == null || challengeData.isEmpty())
                   && selectedScaMethod == null;
    }
}

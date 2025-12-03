/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.spi.domain.vop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpiOrganisationIdentification {
    private String anyBIC;
    private String lei;

    @JsonProperty("other")
    private List<SpiGenericOrganisationIdentification> other;

    @JsonProperty("others")
    private List<SpiGenericOrganisationIdentification> others;

    // Custom setter to handle both other and others
    public void setOthers(List<SpiGenericOrganisationIdentification> others) {
        this.others = others;
        // Also set other to maintain compatibility
        if (this.other == null) {
            this.other = others;
        }
    }

    public void setOther(List<SpiGenericOrganisationIdentification> other) {
        this.other = other;
        // Also set others to maintain compatibility
        if (this.others == null) {
            this.others = other;
        }
    }
}

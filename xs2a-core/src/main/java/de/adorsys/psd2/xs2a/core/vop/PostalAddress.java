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

package de.adorsys.psd2.xs2a.core.vop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostalAddress {
    private String country;

    @JsonProperty("addressLine")
    private List<String> addressLine;

    @JsonProperty("addressLines")
    private List<String> addressLines;

    private String department;
    private String subDepartment;
    private String streetName;
    private String buildingNumber;
    private String buildingName;
    private String floor;
    private String postBox;
    private String room;
    private String postCode;
    private String townName;
    private String townLocationName;
    private String districtName;
    private String countrySubDivision;

    // Custom setter to handle both addressLine and addressLines
    public void setAddressLines(List<String> addressLines) {
        this.addressLines = addressLines;
        // Also set addressLine to maintain compatibility
        if (this.addressLine == null) {
            this.addressLine = addressLines;
        }
    }

    public void setAddressLine(List<String> addressLine) {
        this.addressLine = addressLine;
        // Also set addressLines to maintain compatibility
        if (this.addressLines == null) {
            this.addressLines = addressLine;
        }
    }
}

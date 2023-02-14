/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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

package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Is used if and only if the bookingStatus entry equals \&quot;information\&quot;.  Every active standing order related to the dedicated payment account result into one entry.
 */
@Schema(description = "Is used if and only if the bookingStatus entry equals \"information\".  Every active standing order related to the dedicated payment account result into one entry. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class AdditionalInformationStructured   {
  @JsonProperty("standingOrderDetails")
  private StandingOrderDetails standingOrderDetails = null;

  public AdditionalInformationStructured standingOrderDetails(StandingOrderDetails standingOrderDetails) {
    this.standingOrderDetails = standingOrderDetails;
    return this;
  }

    /**
     * Get standingOrderDetails
     *
     * @return standingOrderDetails
     **/
    @Schema(required = true, description = "")
    @JsonProperty("standingOrderDetails")
    @NotNull

    @Valid
    public StandingOrderDetails getStandingOrderDetails() {
        return standingOrderDetails;
    }

  public void setStandingOrderDetails(StandingOrderDetails standingOrderDetails) {
    this.standingOrderDetails = standingOrderDetails;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AdditionalInformationStructured additionalInformationStructured = (AdditionalInformationStructured) o;
    return Objects.equals(this.standingOrderDetails, additionalInformationStructured.standingOrderDetails);
  }

  @Override
  public int hashCode() {
    return Objects.hash(standingOrderDetails);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdditionalInformationStructured {\n");

    sb.append("    standingOrderDetails: ").append(toIndentedString(standingOrderDetails)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

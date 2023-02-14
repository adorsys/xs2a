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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * Address
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class Address   {
  @JsonProperty("streetName")
  private String streetName = null;

  @JsonProperty("buildingNumber")
  private String buildingNumber = null;

  @JsonProperty("townName")
  private String townName = null;

  @JsonProperty("postCode")
  private String postCode = null;

  @JsonProperty("country")
  private String country = null;

  public Address streetName(String streetName) {
    this.streetName = streetName;
    return this;
  }

    /**
     * Get streetName
     *
     * @return streetName
     **/
    @Schema(description = "")
    @JsonProperty("streetName")

    @Size(max = 70)
    public String getStreetName() {
        return streetName;
    }

  public void setStreetName(String streetName) {
    this.streetName = streetName;
  }

  public Address buildingNumber(String buildingNumber) {
    this.buildingNumber = buildingNumber;
    return this;
  }

    /**
     * Get buildingNumber
     *
     * @return buildingNumber
     **/
    @Schema(description = "")
    @JsonProperty("buildingNumber")

    public String getBuildingNumber() {
        return buildingNumber;
  }

  public void setBuildingNumber(String buildingNumber) {
    this.buildingNumber = buildingNumber;
  }

  public Address townName(String townName) {
    this.townName = townName;
      return this;
  }

    /**
     * Get townName
     *
     * @return townName
     **/
    @Schema(description = "")
    @JsonProperty("townName")

    public String getTownName() {
    return townName;
  }

  public void setTownName(String townName) {
    this.townName = townName;
  }

  public Address postCode(String postCode) {
    this.postCode = postCode;
      return this;
  }

    /**
     * Get postCode
     *
     * @return postCode
     **/
    @Schema(description = "")
    @JsonProperty("postCode")

    public String getPostCode() {
    return postCode;
  }

  public void setPostCode(String postCode) {
    this.postCode = postCode;
  }

  public Address country(String country) {
      this.country = country;
      return this;
  }

    /**
     * ISO 3166 ALPHA2 country code.
     *
     * @return country
     **/
    @Schema(example = "SE", required = true, description = "ISO 3166 ALPHA2 country code.")
    @JsonProperty("country")
    @NotNull

    @Pattern(regexp = "[A-Z]{2}")
    public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Address address = (Address) o;
    return Objects.equals(this.streetName, address.streetName) &&
        Objects.equals(this.buildingNumber, address.buildingNumber) &&
        Objects.equals(this.townName, address.townName) &&
        Objects.equals(this.postCode, address.postCode) &&
        Objects.equals(this.country, address.country);
  }

  @Override
  public int hashCode() {
    return Objects.hash(streetName, buildingNumber, townName, postCode, country);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Address {\n");

    sb.append("    streetName: ").append(toIndentedString(streetName)).append("\n");
    sb.append("    buildingNumber: ").append(toIndentedString(buildingNumber)).append("\n");
    sb.append("    townName: ").append(toIndentedString(townName)).append("\n");
    sb.append("    postCode: ").append(toIndentedString(postCode)).append("\n");
    sb.append("    country: ").append(toIndentedString(country)).append("\n");
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

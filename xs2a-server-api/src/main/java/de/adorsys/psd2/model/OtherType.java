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

package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * In cases where the specifically defined criteria (IBAN, BBAN, MSISDN) are not provided to identify an instance of the respective account type (e.g. a savings account), the ASPSP shall include a proprietary ID of the respective account that uniquely identifies the account for this ASPSP.
 */
@Schema(description = "In cases where the specifically defined criteria (IBAN, BBAN, MSISDN) are not provided to identify an instance of the respective account type (e.g. a savings account), the ASPSP shall include a proprietary ID of the respective account that uniquely identifies the account for this ASPSP.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class OtherType   {
  @JsonProperty("identification")
  private String identification = null;

  @JsonProperty("schemeNameCode")
  private String schemeNameCode = null;

  @JsonProperty("schemeNameProprietary")
  private String schemeNameProprietary = null;

  @JsonProperty("issuer")
  private String issuer = null;

  public OtherType identification(String identification) {
    this.identification = identification;
    return this;
  }

    /**
     * Proprietary identification of the account.
     *
     * @return identification
     **/
    @Schema(required = true, description = "Proprietary identification of the account.")
    @JsonProperty("identification")
    @NotNull

    @Size(max = 35)
    public String getIdentification() {
        return identification;
    }

  public void setIdentification(String identification) {
    this.identification = identification;
  }

  public OtherType schemeNameCode(String schemeNameCode) {
    this.schemeNameCode = schemeNameCode;
    return this;
  }

    /**
     * An entry provided by an external ISO code list.
     *
     * @return schemeNameCode
     **/
    @Schema(description = "An entry provided by an external ISO code list.")
    @JsonProperty("schemeNameCode")

    @Size(max = 35)
    public String getSchemeNameCode() {
        return schemeNameCode;
  }

  public void setSchemeNameCode(String schemeNameCode) {
    this.schemeNameCode = schemeNameCode;
  }

  public OtherType schemeNameProprietary(String schemeNameProprietary) {
    this.schemeNameProprietary = schemeNameProprietary;
      return this;
  }

    /**
     * A scheme name defined in a proprietary way.
     *
     * @return schemeNameProprietary
     **/
    @Schema(description = "A scheme name defined in a proprietary way.")
    @JsonProperty("schemeNameProprietary")

    @Size(max = 35)
    public String getSchemeNameProprietary() {
        return schemeNameProprietary;
  }

  public void setSchemeNameProprietary(String schemeNameProprietary) {
    this.schemeNameProprietary = schemeNameProprietary;
  }

  public OtherType issuer(String issuer) {
    this.issuer = issuer;
      return this;
  }

    /**
     * Issuer of the identification.
     *
     * @return issuer
     **/
    @Schema(description = "Issuer of the identification.")
    @JsonProperty("issuer")

    @Size(max = 35)
    public String getIssuer() {
    return issuer;
  }

  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OtherType otherType = (OtherType) o;
    return Objects.equals(this.identification, otherType.identification) &&
        Objects.equals(this.schemeNameCode, otherType.schemeNameCode) &&
        Objects.equals(this.schemeNameProprietary, otherType.schemeNameProprietary) &&
        Objects.equals(this.issuer, otherType.issuer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(identification, schemeNameCode, schemeNameProprietary, issuer);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OtherType {\n");

    sb.append("    identification: ").append(toIndentedString(identification)).append("\n");
    sb.append("    schemeNameCode: ").append(toIndentedString(schemeNameCode)).append("\n");
    sb.append("    schemeNameProprietary: ").append(toIndentedString(schemeNameProprietary)).append("\n");
    sb.append("    issuer: ").append(toIndentedString(issuer)).append("\n");
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

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

package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * Structured remittance information Max
 */
@Schema(description = "Structured remittance information Max ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class RemittanceInformationStructuredMax140   {
  @JsonProperty("reference")
  private String reference = null;

  @JsonProperty("referenceType")
  private String referenceType = null;

  @JsonProperty("referenceIssuer")
  private String referenceIssuer = null;

  public RemittanceInformationStructuredMax140 reference(String reference) {
    this.reference = reference;
    return this;
  }

    /**
     * Get reference
     *
     * @return reference
     **/
    @Schema(required = true, description = "")
    @JsonProperty("reference")
    @NotNull

    @Size(max = 140)
    public String getReference() {
        return reference;
    }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public RemittanceInformationStructuredMax140 referenceType(String referenceType) {
    this.referenceType = referenceType;
    return this;
  }

    /**
     * Get referenceType
     *
     * @return referenceType
     **/
    @Schema(description = "")
    @JsonProperty("referenceType")

    @Size(max = 140)
    public String getReferenceType() {
        return referenceType;
  }

  public void setReferenceType(String referenceType) {
    this.referenceType = referenceType;
  }

  public RemittanceInformationStructuredMax140 referenceIssuer(String referenceIssuer) {
    this.referenceIssuer = referenceIssuer;
      return this;
  }

    /**
     * Get referenceIssuer
     *
     * @return referenceIssuer
     **/
    @Schema(description = "")
    @JsonProperty("referenceIssuer")

    @Size(max = 140)
    public String getReferenceIssuer() {
        return referenceIssuer;
  }

  public void setReferenceIssuer(String referenceIssuer) {
    this.referenceIssuer = referenceIssuer;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RemittanceInformationStructuredMax140 remittanceInformationStructuredMax140 = (RemittanceInformationStructuredMax140) o;
    return Objects.equals(this.reference, remittanceInformationStructuredMax140.reference) &&
        Objects.equals(this.referenceType, remittanceInformationStructuredMax140.referenceType) &&
        Objects.equals(this.referenceIssuer, remittanceInformationStructuredMax140.referenceIssuer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(reference, referenceType, referenceIssuer);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RemittanceInformationStructuredMax140 {\n");

    sb.append("    reference: ").append(toIndentedString(reference)).append("\n");
    sb.append("    referenceType: ").append(toIndentedString(referenceType)).append("\n");
    sb.append("    referenceIssuer: ").append(toIndentedString(referenceIssuer)).append("\n");
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

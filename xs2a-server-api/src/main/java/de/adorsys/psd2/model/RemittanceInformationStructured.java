/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Structured remittance information 
 */
@ApiModel(description = "Structured remittance information ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-01-11T12:48:04.675377+02:00[Europe/Kiev]")

public class RemittanceInformationStructured   {
  @JsonProperty("reference")
  private String reference = null;

  @JsonProperty("referenceType")
  private String referenceType = null;

  @JsonProperty("referenceIssuer")
  private String referenceIssuer = null;

  public RemittanceInformationStructured reference(String reference) {
    this.reference = reference;
    return this;
  }

  /**
   * Get reference
   * @return reference
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

@Size(max=35) 
  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public RemittanceInformationStructured referenceType(String referenceType) {
    this.referenceType = referenceType;
    return this;
  }

  /**
   * Get referenceType
   * @return referenceType
  **/
  @ApiModelProperty(value = "")

@Size(max=35) 
  public String getReferenceType() {
    return referenceType;
  }

  public void setReferenceType(String referenceType) {
    this.referenceType = referenceType;
  }

  public RemittanceInformationStructured referenceIssuer(String referenceIssuer) {
    this.referenceIssuer = referenceIssuer;
    return this;
  }

  /**
   * Get referenceIssuer
   * @return referenceIssuer
  **/
  @ApiModelProperty(value = "")

@Size(max=35) 
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
    RemittanceInformationStructured remittanceInformationStructured = (RemittanceInformationStructured) o;
    return Objects.equals(this.reference, remittanceInformationStructured.reference) &&
        Objects.equals(this.referenceType, remittanceInformationStructured.referenceType) &&
        Objects.equals(this.referenceIssuer, remittanceInformationStructured.referenceIssuer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(reference, referenceType, referenceIssuer);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RemittanceInformationStructured {\n");
    
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


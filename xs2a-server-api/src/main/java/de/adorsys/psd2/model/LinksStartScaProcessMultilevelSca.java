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
import java.util.HashMap;
import java.util.Objects;

/**
 * - &#x27;startAuthorisation&#x27;:   In case, where an explicit start of the transaction authorisation is needed,   but no more data needs to be updated (no authentication method to be selected,   no PSU identification nor PSU authentication data to be uploaded). - &#x27;startAuthorisationWithPsuIdentification&#x27;:   The link to the authorisation end-point, where the authorisation sub-resource   has to be generated while uploading the PSU identification data. - &#x27;startAuthorisationWithPsuAuthentication&#x27;:   The link to the authorisation end-point, where an authorisation sub-resource   has to be generated while uploading the PSU authentication data. - &#x27;startAuthorisationWithEncryptedPsuAuthentication&#x27;:   The link to the authorisation end-point, where an authorisation sub-resource   has to be generated while uploading the encrypted PSU authentication data. - &#x27;self&#x27;:   The link to the consent resource created by this request.   This link can be used to retrieve the resource data. - &#x27;status&#x27;:   The link to retrieve the status of the consent.
 */
@Schema(description = "- 'startAuthorisation':   In case, where an explicit start of the transaction authorisation is needed,   but no more data needs to be updated (no authentication method to be selected,   no PSU identification nor PSU authentication data to be uploaded). - 'startAuthorisationWithPsuIdentification':   The link to the authorisation end-point, where the authorisation sub-resource   has to be generated while uploading the PSU identification data. - 'startAuthorisationWithPsuAuthentication':   The link to the authorisation end-point, where an authorisation sub-resource   has to be generated while uploading the PSU authentication data. - 'startAuthorisationWithEncryptedPsuAuthentication':   The link to the authorisation end-point, where an authorisation sub-resource   has to be generated while uploading the encrypted PSU authentication data. - 'self':   The link to the consent resource created by this request.   This link can be used to retrieve the resource data. - 'status':   The link to retrieve the status of the consent. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T13:00:06.283258+03:00[Europe/Kiev]")


public class LinksStartScaProcessMultilevelSca extends HashMap<String, HrefType>  {
  @JsonProperty("startAuthorisation")
  private HrefType startAuthorisation = null;

  @JsonProperty("startAuthorisationWithPsuIdentification")
  private HrefType startAuthorisationWithPsuIdentification = null;

  @JsonProperty("startAuthorisationWithPsuAuthentication")
  private HrefType startAuthorisationWithPsuAuthentication = null;

  @JsonProperty("startAuthorisationWithEncryptedPsuAuthentication")
  private HrefType startAuthorisationWithEncryptedPsuAuthentication = null;

  @JsonProperty("self")
  private HrefType self = null;

  @JsonProperty("status")
  private HrefType status = null;

  public LinksStartScaProcessMultilevelSca startAuthorisation(HrefType startAuthorisation) {
    this.startAuthorisation = startAuthorisation;
    return this;
  }

  /**
   * Get startAuthorisation
   * @return startAuthorisation
   **/
  @Schema(description = "")

    @Valid
    public HrefType getStartAuthorisation() {
    return startAuthorisation;
  }

  public void setStartAuthorisation(HrefType startAuthorisation) {
    this.startAuthorisation = startAuthorisation;
  }

  public LinksStartScaProcessMultilevelSca startAuthorisationWithPsuIdentification(HrefType startAuthorisationWithPsuIdentification) {
    this.startAuthorisationWithPsuIdentification = startAuthorisationWithPsuIdentification;
    return this;
  }

  /**
   * Get startAuthorisationWithPsuIdentification
   * @return startAuthorisationWithPsuIdentification
   **/
  @Schema(description = "")

    @Valid
    public HrefType getStartAuthorisationWithPsuIdentification() {
    return startAuthorisationWithPsuIdentification;
  }

  public void setStartAuthorisationWithPsuIdentification(HrefType startAuthorisationWithPsuIdentification) {
    this.startAuthorisationWithPsuIdentification = startAuthorisationWithPsuIdentification;
  }

  public LinksStartScaProcessMultilevelSca startAuthorisationWithPsuAuthentication(HrefType startAuthorisationWithPsuAuthentication) {
    this.startAuthorisationWithPsuAuthentication = startAuthorisationWithPsuAuthentication;
    return this;
  }

  /**
   * Get startAuthorisationWithPsuAuthentication
   * @return startAuthorisationWithPsuAuthentication
   **/
  @Schema(description = "")

    @Valid
    public HrefType getStartAuthorisationWithPsuAuthentication() {
    return startAuthorisationWithPsuAuthentication;
  }

  public void setStartAuthorisationWithPsuAuthentication(HrefType startAuthorisationWithPsuAuthentication) {
    this.startAuthorisationWithPsuAuthentication = startAuthorisationWithPsuAuthentication;
  }

  public LinksStartScaProcessMultilevelSca startAuthorisationWithEncryptedPsuAuthentication(HrefType startAuthorisationWithEncryptedPsuAuthentication) {
    this.startAuthorisationWithEncryptedPsuAuthentication = startAuthorisationWithEncryptedPsuAuthentication;
    return this;
  }

  /**
   * Get startAuthorisationWithEncryptedPsuAuthentication
   * @return startAuthorisationWithEncryptedPsuAuthentication
   **/
  @Schema(description = "")

    @Valid
    public HrefType getStartAuthorisationWithEncryptedPsuAuthentication() {
    return startAuthorisationWithEncryptedPsuAuthentication;
  }

  public void setStartAuthorisationWithEncryptedPsuAuthentication(HrefType startAuthorisationWithEncryptedPsuAuthentication) {
    this.startAuthorisationWithEncryptedPsuAuthentication = startAuthorisationWithEncryptedPsuAuthentication;
  }

  public LinksStartScaProcessMultilevelSca self(HrefType self) {
    this.self = self;
    return this;
  }

  /**
   * Get self
   * @return self
   **/
  @Schema(description = "")

    @Valid
    public HrefType getSelf() {
    return self;
  }

  public void setSelf(HrefType self) {
    this.self = self;
  }

  public LinksStartScaProcessMultilevelSca status(HrefType status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
   **/
  @Schema(description = "")

    @Valid
    public HrefType getStatus() {
    return status;
  }

  public void setStatus(HrefType status) {
    this.status = status;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LinksStartScaProcessMultilevelSca _linksStartScaProcessMultilevelSca = (LinksStartScaProcessMultilevelSca) o;
    return Objects.equals(this.startAuthorisation, _linksStartScaProcessMultilevelSca.startAuthorisation) &&
        Objects.equals(this.startAuthorisationWithPsuIdentification, _linksStartScaProcessMultilevelSca.startAuthorisationWithPsuIdentification) &&
        Objects.equals(this.startAuthorisationWithPsuAuthentication, _linksStartScaProcessMultilevelSca.startAuthorisationWithPsuAuthentication) &&
        Objects.equals(this.startAuthorisationWithEncryptedPsuAuthentication, _linksStartScaProcessMultilevelSca.startAuthorisationWithEncryptedPsuAuthentication) &&
        Objects.equals(this.self, _linksStartScaProcessMultilevelSca.self) &&
        Objects.equals(this.status, _linksStartScaProcessMultilevelSca.status) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(startAuthorisation, startAuthorisationWithPsuIdentification, startAuthorisationWithPsuAuthentication, startAuthorisationWithEncryptedPsuAuthentication, self, status, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LinksStartScaProcessMultilevelSca {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    startAuthorisation: ").append(toIndentedString(startAuthorisation)).append("\n");
    sb.append("    startAuthorisationWithPsuIdentification: ").append(toIndentedString(startAuthorisationWithPsuIdentification)).append("\n");
    sb.append("    startAuthorisationWithPsuAuthentication: ").append(toIndentedString(startAuthorisationWithPsuAuthentication)).append("\n");
    sb.append("    startAuthorisationWithEncryptedPsuAuthentication: ").append(toIndentedString(startAuthorisationWithEncryptedPsuAuthentication)).append("\n");
    sb.append("    self: ").append(toIndentedString(self)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
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
